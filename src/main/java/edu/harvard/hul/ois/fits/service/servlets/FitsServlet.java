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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.service.common.ErrorMessage;
import edu.harvard.hul.ois.fits.service.pool.FitsWrapper;
import edu.harvard.hul.ois.fits.service.pool.FitsWrapperFactory;
import edu.harvard.hul.ois.fits.service.pool.FitsWrapperPool;

public class FitsServlet extends HttpServlet {
 
  private FitsWrapperPool fitsWrapperPool;
  private static final long serialVersionUID = -8091319477831924496L;
  private static Logger logger = Logger.getLogger(FitsServlet.class);
  private static String fitsHome = "";
  private static String responseContentMimeType = TEXT_XML_MIMETYPE;
  
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
      
      // Do required initialization
      logger.debug("Initializing FITS pool");
      
      GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
	  int numObjectsInPool = 5;
	  poolConfig.setMinIdle(numObjectsInPool);
	  poolConfig.setMaxTotal(numObjectsInPool);
	  poolConfig.setTestOnBorrow(true);
	  poolConfig.setBlockWhenExhausted(true);
	  fitsWrapperPool = new FitsWrapperPool(new FitsWrapperFactory(), poolConfig);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	  
	  // Just pass to doGet
	  doGet(req, resp);
  }
  
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	  	  
	  // The FITS jar and it's dependencies would live in the servlet, and only the 'xml' and 'tools' 
	  // directories would live externally in the FITS_HOME dir. When you initialize the Fits object you tell 
	  // it where FITS_HOME is, and FITS reads the configuration and uses the tools at that path

  	  ErrorMessage errorMessage = null;

	  // Send it to the processor...
      try {
    	  
          sendFitsExamineResponse(req, resp);

      } catch (Exception e){
          errorMessage = new ErrorMessage(e.getMessage(), req.getRequestURL().toString(), e.getMessage());
          sendErrorMessageResponse(errorMessage, resp);
      }
  }
  

  private void sendFitsExamineResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {

      String filePath = req.getParameter(FITS_FILE_PARAM);
      
      if (filePath == null) {
          ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter " + FITS_FILE_PARAM, req.getRequestURL().toString());
          sendErrorMessageResponse(errorMessage, resp);
          return;
      }
      
      File file = new File(filePath);
      
      if (!file.exists()) {
          ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST,  "Bad parameter value for " + FITS_FILE_PARAM + ": " + file.getCanonicalPath(), req.getRequestURL().toString());
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
		  logger.error("Problem executing call...", e);
	      ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,  "Fits examine failed", req.getRequestURL().toString(), e.getMessage());
	      sendErrorMessageResponse(errorMessage, resp);
	  } finally {
	      if (fitsWrapper != null){
	          logger.debug("Returning FITS to pool");
	          fitsWrapperPool.returnObject(fitsWrapper);
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