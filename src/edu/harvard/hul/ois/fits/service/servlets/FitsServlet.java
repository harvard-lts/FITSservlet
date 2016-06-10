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

import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_FILE_PARAM;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_FORM_FIELD_DATAFILE;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_HOME_SYSTEM_PROP_NAME;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_RESOURCE_PATH_VERSION;
import static edu.harvard.hul.ois.fits.service.common.Constants.TEXT_HTML_MIMETYPE;
import static edu.harvard.hul.ois.fits.service.common.Constants.TEXT_PLAIN_MIMETYPE;
import static edu.harvard.hul.ois.fits.service.common.Constants.TEXT_XML_MIMETYPE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
    private static final int THRESHOLD_SIZE     = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB
    private static final Logger logger = Logger.getLogger(FitsServlet.class);

    private FitsWrapperPool fitsWrapperPool;

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

        logger.debug("Initializing FITS pool");
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        int numObjectsInPool = 5;
        poolConfig.setMinIdle(numObjectsInPool);
        poolConfig.setMaxTotal(numObjectsInPool);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setBlockWhenExhausted(true);
        fitsWrapperPool = new FitsWrapperPool(new FitsWrapperFactory(), poolConfig);
        logger.debug("FITS pool finished Initializing");
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
        logger.info("Entering doGet(): " + servletPath);

        // See if path is just requesting version number. If so, just return it.
        if (FITS_RESOURCE_PATH_VERSION.equals(servletPath)) {
            sendFitsVersionResponse(request, response); // outputs version of FITS, not the version of web application
            return;
        }

        // Send it to the FITS processor...
        String filePath = request.getParameter(FITS_FILE_PARAM);

        try {
            sendFitsExamineResponse(filePath, request, response);
        } catch (Exception e){
            ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage(),
                    request.getRequestURL().toString(),
                    e.getMessage());
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

    	logger.info("Entering doPost()");
        if (!ServletFileUpload.isMultipartContent(request)) {
            ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                    " Missing Multipart Form Data. ",
                    request.getRequestURL().toString());
            sendErrorMessageResponse(errorMessage, response);
            return;
        }

        // configures upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(THRESHOLD_SIZE);
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);
        upload.setSizeMax(MAX_REQUEST_SIZE);

        // constructs the directory path to store upload file & creates the directory if it does not exist
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        try {
            List<FileItem> formItems = upload.parseRequest(request);
            Iterator<FileItem> iter = formItems.iterator();

            // iterates over form's fields
            while (iter.hasNext()) {
                FileItem item = iter.next();

                // processes only fields that are not form fields
                //if (!item.isFormField()) {
                if (!item.isFormField() && item.getFieldName().equals(FITS_FORM_FIELD_DATAFILE)) {

                    long fileSize = item.getSize();
                    if (fileSize < 1) {
                        ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                                " Missing File Data. ",
                                request.getRequestURL().toString());
                        sendErrorMessageResponse(errorMessage, response);
                        return;
                    }
                    // ensure a unique local fine name
                    String fileSuffix = String.valueOf((new Date()).getTime());
                    String fileNameAndPath = uploadPath + File.separator + "fits-" + fileSuffix + "-" + item.getName();
                    File storeFile = new File(fileNameAndPath);
                    item.write(storeFile); // saves the file on disk

                    if (!storeFile.exists()) {
                    	ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                " Error in upload file.",
                                request.getRequestURL().toString(),
                                " Unspecified.");
                        sendErrorMessageResponse(errorMessage, response);
                        return;
                    }
                    // Send it to the FITS processor...
                    try {

                        sendFitsExamineResponse(storeFile.getAbsolutePath(), request, response);

                    } catch (Exception e){
                    	ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                e.getMessage(),
                                request.getRequestURL().toString(),
                                e.getMessage());
                        sendErrorMessageResponse(errorMessage, response);
                        return;
                    } finally {
                        // delete the uploaded file
                        if(storeFile.delete()){
                            logger.debug(storeFile.getName() + " is deleted!");
                        } else {
                            logger.debug(storeFile.getName() + " could not be deleted!");
                        }
                    }
                } else {
                	ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                            " The request did not have the correct name attribute of \"datafile\" in the form processing. ",
                            request.getRequestURL().toString(),
                            " Processing halted.");
                    sendErrorMessageResponse(errorMessage, response);
                    return;
                }

            }

        } catch (Exception ex) {
        	ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    " There was an unexpected server error. ",
                    request.getRequestURL().toString(),
                    " Processing halted.");
            sendErrorMessageResponse(errorMessage, response);
            return;
        }
    }

    private void sendFitsExamineResponse(String filePath, HttpServletRequest req, HttpServletResponse resp) throws IOException {

          if (filePath == null) {
              ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                      " Missing parameter: [" + FITS_FILE_PARAM + "] ",
                      req.getRequestURL().toString());
              sendErrorMessageResponse(errorMessage, resp);
              return;
          }

          File file = new File(filePath);
          if (!file.exists()) {
              ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                      " File not sent with request: " + file.getCanonicalPath(),
                      " " + req.getRequestURL().toString());
              sendErrorMessageResponse(errorMessage, resp);
              return;
          }

          FitsWrapper fitsWrapper = null;
          try {
              logger.debug("Borrowing fits from pool");
              fitsWrapper = fitsWrapperPool.borrowObject();

              logger.debug("Running FITS on " + file.getPath());

              // Start the output process
              ByteArrayOutputStream outStream = new ByteArrayOutputStream();
              FitsOutput fitsOutput = fitsWrapper.getFits().examine(file);
              fitsOutput.addStandardCombinedFormat();
              fitsOutput.output(outStream);
              String outputString = outStream.toString();
              resp.setContentType(TEXT_XML_MIMETYPE);
              PrintWriter out = resp.getWriter();
              out.println(outputString);

          } catch (Exception e){
              logger.error("Unexpected exception: " + e.getLocalizedMessage(), e);
              ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                      " Fits examine failed",
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
              logger.error("Problem executing call...", e);
              ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                      " Getting FITS version failed",
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
          logger.error("Error -- Status:" + errorMessage.getStatusCode() + " - " +
        		  errorMessage.getReasonPhrase() + ", " + errorMessage.getMessage());
          PrintWriter out = resp.getWriter();
          resp.setContentType(TEXT_HTML_MIMETYPE);
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
