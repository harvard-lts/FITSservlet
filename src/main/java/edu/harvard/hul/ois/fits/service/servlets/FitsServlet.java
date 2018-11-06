//
// Copyright (c) 2016 by The President and Fellows of Harvard College
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the License at:
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permission and limitations under the License.
//

package edu.harvard.hul.ois.fits.service.servlets;

import static edu.harvard.hul.ois.fits.service.common.Constants.ENV_PROJECT_PROPS;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_FILE_PARAM;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_FORM_FIELD_DATAFILE;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_HOME_SYSTEM_PROP_NAME;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_RESOURCE_PATH_VERSION;
import static edu.harvard.hul.ois.fits.service.common.Constants.INCLUDE_STANDARD_OUTPUT_PARAM;
import static edu.harvard.hul.ois.fits.service.common.Constants.PROPERTIES_FILE_NAME;
import static edu.harvard.hul.ois.fits.service.common.Constants.TEXT_PLAIN_MIMETYPE;
import static edu.harvard.hul.ois.fits.service.common.Constants.TEXT_XML_MIMETYPE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.service.common.Constants;
import edu.harvard.hul.ois.fits.service.common.ErrorMessage;
import edu.harvard.hul.ois.fits.service.pool.FitsWrapper;
import edu.harvard.hul.ois.fits.service.pool.FitsWrapperFactory;
import edu.harvard.hul.ois.fits.service.pool.FitsWrapperPool;

/**
 * Handles the upload of a file either locally or remotely for processing by FITS.
 * For a local upload HTTP GET is used by having a request parameter point to the local file's location.
 * For a remote upload HTTP POST is used to pass the in the file as form data.
 */
public class FitsServlet extends HttpServlet {
    private static final long serialVersionUID = 7485524766400256957L;

    private static String fitsHome = "";

    private static final String UPLOAD_DIRECTORY = "upload";
	private static final int MIN_IDLE_OBJECTS_IN_POOL = 3;
	private static final String DEFAULT_MAX_OBJECTS_IN_POOL = "10";
	private static final String DEFAULT_MAX_UPLOAD_SIZE = "40";  // in MB
	private static final String DEFAULT_MAX_REQUEST_SIZE = "50"; // in MB
	private static final String DEFAULT_IN_MEMORY_FILE_SIZE = "3"; // in MB - above which the temporary file is stored to disk
	private static final long MB_MULTIPLIER = 1024 * 1024;
	private static final String FALSE = "false";

    private static final Logger logger = Logger.getLogger(FitsServlet.class);

    private File uploadBaseDir; // base directory into which all uploaded files will be placed
    private FitsWrapperPool fitsWrapperPool;
	private Properties applicationProps = null;
	private int maxInMemoryFileSizeMb; // Uploaded files above this threshold will be placed in a temporary directory (separate from the upload dir) rather than kept in memory.
	private long maxFileUploadSizeMb;
	private long maxRequestSizeMb;

    public void init() throws ServletException {

        // "fits.home" property set differently in Tomcat 7 and JBoss 7.
        // Tomcat: set in catalina.properties
        // JBoss: set as a command line value "-Dfits.home=<path/to/fits/home>
    	fitsHome = System.getProperty(FITS_HOME_SYSTEM_PROP_NAME);
        logger.info(FITS_HOME_SYSTEM_PROP_NAME + ": " + fitsHome);

        if (StringUtils.isEmpty(fitsHome)) {
            logger.fatal(FITS_HOME_SYSTEM_PROP_NAME + " system property HAS NOT BEEN SET!!! This web application will not properly run.");
            throw new ServletException(FITS_HOME_SYSTEM_PROP_NAME + " system property HAS NOT BEEN SET!!! This web application will not properly run.");
        }

        // Set the projects properties.
		// First look for a system property pointing to a project properties file. (e.g. - file:/path/to/file)
		// If this value either does not exist or is not valid, the default
		// file that comes with this application will be used for initialization.
		String environmentProjectPropsFile = System.getProperty(ENV_PROJECT_PROPS);
		logger.info("Value of environment property: [ + ENV_PROJECT_PROPS + ] for finding external properties file in location: [" + environmentProjectPropsFile + "]");
		if (environmentProjectPropsFile != null) {
			logger.info("Will look for properties file from environment in location: [" + environmentProjectPropsFile + "]");
			try {
				File projectProperties = new File(environmentProjectPropsFile);
				if (projectProperties.exists() && projectProperties.isFile() && projectProperties.canRead()) {
					InputStream is = new FileInputStream(projectProperties);
					applicationProps = new Properties();
					applicationProps.load(is);
				}
			} catch (IOException e) {
				// fall back to default file
				logger.error("Unable to load properties file: [" + environmentProjectPropsFile + "] -- reason: " + e.getMessage(), e);
				logger.error("Falling back to default project.properties file: [" + PROPERTIES_FILE_NAME + "]");
				applicationProps = null;
			}
		}

		if (applicationProps == null) { // did not load from environment variable location
			try {
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				InputStream resourceStream = classLoader.getResourceAsStream(PROPERTIES_FILE_NAME);
				if (resourceStream != null) {
					applicationProps = new Properties();
					applicationProps.load(resourceStream);
					logger.info("loaded default applicationProps");
				} else {
					logger.warn("project.properties not found!!!");
				}
			} catch (IOException e) {
				logger.error("Could not load properties file: [" + PROPERTIES_FILE_NAME + "]", e);
				// couldn't load default properties so bail...
				throw new ServletException("Couldn't load an applications properties file.", e);
			}
		}
		int maxPoolSize = Integer.valueOf(applicationProps.getProperty("max.objects.in.pool", DEFAULT_MAX_OBJECTS_IN_POOL));
		maxFileUploadSizeMb = Long.valueOf(applicationProps.getProperty("max.upload.file.size.MB", DEFAULT_MAX_UPLOAD_SIZE));
		maxRequestSizeMb = Long.valueOf(applicationProps.getProperty("max.request.size.MB", DEFAULT_MAX_REQUEST_SIZE));
		maxInMemoryFileSizeMb = Integer.valueOf(applicationProps.getProperty("max.in.memory.file.size.MB", DEFAULT_IN_MEMORY_FILE_SIZE));
		logger.info("Max objects in object pool: " + maxPoolSize +
				" -- Max file upload size: " +
				maxFileUploadSizeMb +"MB -- Max request object size: " +
				maxRequestSizeMb + "MB -- Max in-memory file size: " +
				maxInMemoryFileSizeMb + "MB");

        logger.debug("Initializing FITS pool");
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMinIdle(MIN_IDLE_OBJECTS_IN_POOL);
        poolConfig.setMaxTotal(maxPoolSize);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setBlockWhenExhausted(true);
        fitsWrapperPool = new FitsWrapperPool(new FitsWrapperFactory(), poolConfig);
        logger.debug("FITS pool finished Initializing");

        String uploadBaseDirName = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
        uploadBaseDir = new File(uploadBaseDirName);
        if (!uploadBaseDir.exists()) {
        	uploadBaseDir.mkdir();
        	logger.info("Created upload base directory: " + uploadBaseDir.getAbsolutePath());
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method. There are currently two end point for GET:
     * <ol>
     * <li>/examine -- to have FITS examine a file and return "text/xml" FITS output. Use this when uploading
     * a file locally.</li>
     * <li>/version -- to receive "text/plain" output of the version of FITS being used to process files.</li>
     * </ol>
     * "/examine" requires the path to the file to be analyzed
     * with the request parameter "file" set to location of the file.
     * E.g.: http://<host>[:port]/fits/examine?file=<path/to/file/filename
     * Note: "fits" in the above URL needs to be adjusted to the final name of the WAR file.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String servletPath = request.getServletPath(); // gives servlet mapping
        logger.debug("Entering doGet(): " + servletPath);

        // See if path is just requesting version number. If so, just return it.
        if (FITS_RESOURCE_PATH_VERSION.equals(servletPath)) {
            sendFitsVersionResponse(request, response); // outputs version of FITS, not the version of web application
            return;
        }

        // Send it to the FITS processor...
        String filePath = request.getParameter(FITS_FILE_PARAM);
        
        boolean includeStdOutput = true; // include standard output by default
        String includeStandardMetadata = request.getParameter(INCLUDE_STANDARD_OUTPUT_PARAM);
        if (FALSE.equalsIgnoreCase(includeStandardMetadata)) {
        	includeStdOutput = false;
        }

        try {
            sendFitsExamineResponse(filePath, includeStdOutput, request, response);
        } catch (Exception e){
            logger.error("Unexpected exception: " + e.getMessage(), e);
            ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage(),
                    request.getRequestURL().toString());
            sendErrorMessageResponse(errorMessage, response);
        }
    }

    /**
     * Handles the file upload for FITS processing via streaming of the file using the
     * <code>POST</code> method.
     * Example: curl -X POST -F datafile=@<path/to/file> <host>:[<port>]/fits/examine
     * Note: "fits" in the above URL needs to be adjusted to the final name of the WAR file.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	logger.debug("Entering doPost()");
        if (!ServletFileUpload.isMultipartContent(request)) {
            ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing multipart POST form data.",
                    request.getRequestURL().toString());
            sendErrorMessageResponse(errorMessage, response);
            return;
        }

        // configures upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold((maxInMemoryFileSizeMb * (int)MB_MULTIPLIER));
        String tempDir = System.getProperty("java.io.tmpdir");
        logger.debug("Creating temp directory for storing uploaded files: " + tempDir);
        factory.setRepository(new File(tempDir));

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(maxFileUploadSizeMb * MB_MULTIPLIER);
        upload.setSizeMax(maxRequestSizeMb * MB_MULTIPLIER);

        try {
            List<FileItem> formItems = upload.parseRequest(request);
            Iterator<FileItem> iter = formItems.iterator();
            Map<String, String[]> paramMap = request.getParameterMap();
            
            boolean includeStdMetadata = true;
            String[] vals = paramMap.get(Constants.INCLUDE_STANDARD_OUTPUT_PARAM);
            if (vals != null && vals.length > 0) {
            	if (FALSE.equalsIgnoreCase(vals[0])) {
            		includeStdMetadata = false;
            		logger.debug("flag includeStdMetadata set to : " + includeStdMetadata);
            	}
            }
            
            // file-specific directory path to store uploaded file
            // ensures unique sub-directory to handle rare case of duplicate file name
            String subDir = String.valueOf((new Date()).getTime());
            String uploadPath = uploadBaseDir + File.separator + subDir;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
            	uploadDir.mkdir();
            }

            // iterates over form's fields -- should only be one for uploaded file
            while (iter.hasNext()) {
                FileItem item = iter.next();
                if (!item.isFormField() && item.getFieldName().equals(FITS_FORM_FIELD_DATAFILE)) {

                	String fileName = item.getName();
                    if (StringUtils.isEmpty(fileName)) {
                        ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                                "Missing File Data.",
                                request.getRequestURL().toString());
                        sendErrorMessageResponse(errorMessage, response);
                        return;
                    }
                    // ensure a unique local fine name
                    String fileNameAndPath = uploadPath + File.separator + item.getName();
                    File storeFile = new File(fileNameAndPath);
                    item.write(storeFile); // saves the file on disk

                    if (!storeFile.exists()) {
                    	ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                "Uploaded file does not exist.",
                                request.getRequestURL().toString());
                        sendErrorMessageResponse(errorMessage, response);
                        return;
                    }
                    // Send it to the FITS processor...
                    try {

                        sendFitsExamineResponse(storeFile.getAbsolutePath(), includeStdMetadata, request, response);

                    } catch (Exception e){
                        logger.error("Unexpected exception: " + e.getMessage(), e);
                    	ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                e.getMessage(),
                                request.getRequestURL().toString());
                        sendErrorMessageResponse(errorMessage, response);
                        return;
                    } finally {
                        // delete the uploaded file
                        if (storeFile.delete()){
                            logger.debug(storeFile.getName() + " is deleted!");
                        } else {
                            logger.warn(storeFile.getName() + " could not be deleted!");
                        }
                        if (uploadDir.delete()) {
                            logger.debug(uploadDir.getName() + " is deleted!");
                        } else {
                            logger.warn(uploadDir.getName() + " could not be deleted!");
                        }
                    }
                } else {
                	ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                            "The request did not have the correct name attribute of \"datafile\" in the form processing. ",
                            request.getRequestURL().toString(),
                            "Processing halted.");
                    sendErrorMessageResponse(errorMessage, response);
                    return;
                }

            }

        } catch (Exception ex) {
            logger.error("Unexpected exception: " + ex.getMessage(), ex);
        	ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        			ex.getMessage(),
                    request.getRequestURL().toString(),
                    "Processing halted.");
            sendErrorMessageResponse(errorMessage, response);
            return;
        }
    }

    private void sendFitsExamineResponse(String filePath, boolean includeStdOutput, HttpServletRequest req, HttpServletResponse resp) throws IOException {

          if (filePath == null) {
              ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                      "Missing file parameter: [" + FITS_FILE_PARAM + "]",
                      req.getRequestURL().toString());
              sendErrorMessageResponse(errorMessage, resp);
              return;
          }

          File file = new File(filePath);
          if (!file.exists()) {
              ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                      " File not sent with request: " + file.getName(),
                      " " + req.getRequestURL().toString());
              sendErrorMessageResponse(errorMessage, resp);
              return;
          }

          FitsWrapper fitsWrapper = null;
          try {
              logger.debug("About to borrow FITS object from pool");
              fitsWrapper = fitsWrapperPool.borrowObject();

              logger.debug("Running FITS on " + file.getPath());

              // Start the output process
              ByteArrayOutputStream outStream = new ByteArrayOutputStream();
              FitsOutput fitsOutput = fitsWrapper.getFits().examine(file);
              if (includeStdOutput) {
            	  fitsOutput.addStandardCombinedFormat();
              }
              fitsOutput.output(outStream);
              String outputString = outStream.toString();
              resp.setContentType(TEXT_XML_MIMETYPE);
              resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
              PrintWriter out = resp.getWriter();
              out.println(outputString);

          } catch (Exception e){
              logger.error("Unexpected exception: " + e.getMessage(), e);
              ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                      "Fits examine failed",
                      req.getRequestURL().toString(),
                      e.getMessage());
              sendErrorMessageResponse(errorMessage, resp);
          } finally {
              if (fitsWrapper != null){
                  logger.debug("Returning FITS to pool");
                  fitsWrapperPool.returnObject(fitsWrapper);
              }
          }
      }

      private void sendFitsVersionResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {

          FitsWrapper fitsWrapper = null;
          String fitsVersion = null;
          try {
              logger.debug("Borrowing fits from pool");
              fitsWrapper = fitsWrapperPool.borrowObject();
              fitsVersion = fitsWrapper.getFits().VERSION;
          } catch (Exception e){
              logger.error("Problem executing call: " + e.getMessage(), e);
              ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                      "Getting FITS version failed",
                      req.getRequestURL().toString(),
                      e.getMessage());
              sendErrorMessageResponse(errorMessage, resp);
          } finally {
              if (fitsWrapper != null){
                  logger.debug("Returning FITS to pool");
                  fitsWrapperPool.returnObject(fitsWrapper);
              }
          }
          resp.setContentType(TEXT_PLAIN_MIMETYPE);
          PrintWriter out = resp.getWriter();
          out.println(fitsVersion);
      }

      private void sendErrorMessageResponse(ErrorMessage errorMessage,  HttpServletResponse resp) throws IOException {
          String errorMessageStr = errorMessageToString(errorMessage);
          PrintWriter out = resp.getWriter();
          resp.setContentType(TEXT_XML_MIMETYPE);
          resp.setStatus(errorMessage.getStatusCode());
          out.println(errorMessageStr);
      }

      private String errorMessageToString(ErrorMessage errorMessage){
          String errorMessageStr = null;
          try {
              ByteArrayOutputStream outStream = new ByteArrayOutputStream();
              JAXBContext jaxbContext = JAXBContext.newInstance(ErrorMessage.class);
              Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
              jaxbMarshaller.marshal(errorMessage, outStream);
              errorMessageStr =  outStream.toString();
          } catch (JAXBException jbe){
              errorMessageStr =  errorMessage.toString();
          }
          return errorMessageStr;
      }
}
