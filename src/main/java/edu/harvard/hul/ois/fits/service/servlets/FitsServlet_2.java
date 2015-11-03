package edu.harvard.hul.ois.fits.service.servlets;

import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_FILE_PARAM;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_HOME_SYSTEM_PROP_NAME;
import static edu.harvard.hul.ois.fits.service.common.Constants.TEXT_HTML_MIMETYPE;
import static edu.harvard.hul.ois.fits.service.common.Constants.TEXT_PLAIN_MIMETYPE;
import static edu.harvard.hul.ois.fits.service.common.Constants.TEXT_XML_MIMETYPE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
 * Servlet implementation class FitsServlet_2
 */
public class FitsServlet_2 extends HttpServlet {
 
	private static final long serialVersionUID = 7485524766400256957L;
    private FitsWrapperPool fitsWrapperPool;
    private static Logger logger = Logger.getLogger(FitsServlet.class);
    private static String fitsHome = "";
    private static String responseContentMimeType = TEXT_XML_MIMETYPE;
    private static boolean remFile = false;

    private static final String UPLOAD_DIRECTORY = "upload";
    private static final int THRESHOLD_SIZE     = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB
 
    
    public void init() throws ServletException {
        
  	  // "fits.home" property set differently in Tomcat 7 and JBoss 7.
  	  // Tomcat: set in catalina.properties
  	  // JBoss: set as a command line value "-Dfits.home=<path/to/fits/home>
  	  logger.info(FITS_HOME_SYSTEM_PROP_NAME + ": " + System.getProperty(FITS_HOME_SYSTEM_PROP_NAME));
  	  fitsHome = System.getProperty(FITS_HOME_SYSTEM_PROP_NAME);
  	  
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
    }
    
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	  
	  // Just pass it along
    	this.doPost(req, resp);
    }
    
    /**
     * Handles the HTTP, and file upload for FITS processing
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	boolean isLocal = false;
    	boolean isStream = false;

        if (ServletFileUpload.isMultipartContent(request)) {
            isStream = true;
            remFile = true;
        } else if (request.getParameter(FITS_FILE_PARAM) != null) {
        	isLocal = true;
        	remFile = false;
        }
        
        ErrorMessage errorMessage = null;
        
        if (isStream) {
        	
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
	                if (!item.isFormField() && item.getFieldName().equals("datafile")) {
	                    //String fileName = new File(item.getName()).getName();
	                    String fileName = new File("fits-" + item.getName()).getName();
	                    
	                    String filePath = uploadPath + File.separator + fileName;
	                    File storeFile = new File(filePath);
	                     
	                    // saves the file on disk
	                    item.write(storeFile);
	                    	                    
	                    // In case the file didn't make it
	                    if (!storeFile.exists()) {
	                        errorMessage = new ErrorMessage("Error in upload file.", request.getRequestURL().toString(), " Unspecified.");
	                        sendErrorMessageResponse(errorMessage, response);
	                    }
	                        // Send it to the FITS processor...
		                    try {
		                  	  
		                        sendFitsExamineResponse(storeFile.getAbsolutePath(), request, response);
	
		                    } catch (Exception e){
		                        errorMessage = new ErrorMessage(e.getMessage(), request.getRequestURL().toString(), e.getMessage());
		                        sendErrorMessageResponse(errorMessage, response);
		                    }
	                } else {
                        errorMessage = new ErrorMessage("The request did not have the correct name attribute of \"datafile\" in the form processing. ", request.getRequestURL().toString(), " Processing halted.");
                        sendErrorMessageResponse(errorMessage, response);
	                }
	                
	            }
	            
	            request.setAttribute("message", "Upload has been done successfully!");
	            
	        } catch (Exception ex) {
	            request.setAttribute("message", "There was an error: " + ex.getMessage());
	        }
	        
	        // Uncomment to test with a .jsp message page
	        //getServletContext().getRequestDispatcher("/message.jsp").forward(request, response);
	        
        } else if (isLocal) {

      	  // Send it to the FITS processor...
  	      String filePath = request.getParameter(FITS_FILE_PARAM);
  	      remFile = false;
  	    
            try {
          	  
                sendFitsExamineResponse(filePath, request, response);

            } catch (Exception e){
                errorMessage = new ErrorMessage(e.getMessage(), request.getRequestURL().toString(), e.getMessage());
                sendErrorMessageResponse(errorMessage, response);
            }
        }
    }
    
    /*
     * Send the local "file=xxx" to get examined by FITS
     */
	private void sendFitsExamineResponse(String filePath, HttpServletRequest req, HttpServletResponse resp) throws IOException {
	      
	      if (filePath == null) {
	          ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter " + FITS_FILE_PARAM, req.getRequestURL().toString());
	          sendErrorMessageResponse(errorMessage, resp);
	          return;
	      }
	      
	      File file = new File(filePath);
	      
	      if (!file.exists()) {
	          ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,  "Bad parameter value for " + FITS_FILE_PARAM + ": " + file.getCanonicalPath(), " " + req.getRequestURL().toString());
	          sendErrorMessageResponse(errorMessage, resp);
	          return;
	      }
	      
	      // Required log4j.properties in .tomcat/bin/tools -- fits keeps tools in separate locale
		  FitsWrapper fitsWrapper = null;
		  
		  try {
		      logger.debug("Borrowing fits from pool");
		      fitsWrapper = fitsWrapperPool.borrowObject();
		      
		      // To validate the object
		      //System.out.println("T/F. Is this object valid:  "+fitsWrapper.isValid());
		      
		      logger.debug("Running FITS on " + file.getPath());
		
		      // Start the output process
		      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		      FitsOutput fitsOutput = fitsWrapper.getFits().examine(file);
		      fitsOutput.addStandardCombinedFormat();
		      fitsOutput.output(outStream);
		      String outputString = outStream.toString();
			  resp.setContentType(responseContentMimeType);
		      PrintWriter out = resp.getWriter();
		      out.println(outputString);
		      
		  } catch (Exception e){
			  e.printStackTrace(); // remove later
		      ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,  "Fits examine failed", req.getRequestURL().toString(), e.getMessage());
		      sendErrorMessageResponse(errorMessage, resp);
		  } finally {
		      if (fitsWrapper != null){
		          logger.debug("Returning FITS to pool");
		          fitsWrapperPool.returnObject(fitsWrapper);
		      }
		  }
	      
		  // Remove the temp file
		  if (remFile) {
			  
			  try{
				  
		    		if(file.delete()){
		    			logger.debug(file.getName() + " is deleted!");
		    		} else {
		    			logger.debug(file.getName() + " could not be deleted!");
		    		}
		 
		    	} catch(Exception e) {
		  		      ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,  "Fits examine failed", req.getRequestURL().toString(), e.getMessage());
				      sendErrorMessageResponse(errorMessage, resp);
		    	}
		  }
	  }  
	  
	  private void sendErrorMessageResponse(ErrorMessage errorMessage,  HttpServletResponse resp) throws IOException {
	      String errorMessageStr = errorMessageToString(errorMessage);      
	      PrintWriter out = resp.getWriter();
	      resp.setContentType(TEXT_HTML_MIMETYPE);
	      out.println(errorMessageStr);
	  }
	  
	  private void sendFitsVersionResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	      resp.setContentType(TEXT_PLAIN_MIMETYPE);
	      PrintWriter out = resp.getWriter();
	      out.println(FitsOutput.VERSION);
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
