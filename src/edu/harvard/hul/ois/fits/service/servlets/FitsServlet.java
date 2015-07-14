package edu.harvard.hul.ois.fits.service.servlets;

import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_FILE_PARAM;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_PRODUCES_MIMETYPE;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_RESOURCE_PATH_EXAMINE;
import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_RESOURCE_PATH_VERSION;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import edu.harvard.hul.ois.fits.service.common.ErrorMessage;
import edu.harvard.hul.ois.fits.service.pool.FitsWrapper;
import edu.harvard.hul.ois.fits.service.pool.FitsWrapperFactory;
import edu.harvard.hul.ois.fits.service.pool.FitsWrapperPool;
import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.ots.schemas.XmlContent.XmlContent;

// Extend HttpServlet class
public class FitsServlet extends HttpServlet {
 
  private FitsWrapperPool fitsWrapperPool;
  private static final long serialVersionUID = 7410633585875958440L;
  private static Logger LOG = Logger.getLogger(FitsServlet.class);
  private static String fitsHome = "";
  private static String mimeType = "";
  
  public void init() throws ServletException {
      
      // Do required initialization
      LOG.debug("Initializing FITS pool");
      
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
	  
	  try {

	    InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("project.properties");
	    Properties props = new Properties();
	    props.load(inStream);
	    fitsHome = props.getProperty("fits.home");
	    mimeType = props.getProperty("out.mimetype");
	    // Set for the Wrapper Pool
	    System.setProperty("fits.home", fitsHome);

	  } catch (IOException e) {
		e.printStackTrace();
	  }

	  if (fitsHome == null) {
          ErrorMessage fitsErrorMessage = new ErrorMessage(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter " + FITS_FILE_PARAM, req.getRequestURL().toString());
          sendErrorMessageResponse(fitsErrorMessage, resp);
          return;
	  }

	  // Send it to the processor...
      try {
    	  
          sendFitsExamineResponse(req, resp);

      } catch (Exception e){
          errorMessage = new ErrorMessage(e.getMessage(), req.getRequestURL().toString(), e.getMessage());
          sendErrorMessageResponse(errorMessage, resp);
      }
      
  }
  

  private void sendFitsExamineResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {

      String filePath = req.getParameter("file");
      
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
	      LOG.debug("Borrowing fits from pool");
	      fitsWrapper = fitsWrapperPool.borrowObject();
	      
	      // To validate the object
	      //System.out.println("T/F. Is this object valid:  "+fitsWrapper.isValid());
	      
	      LOG.debug("Running FITS on " + file.getPath());
	
	      // Start the output process
	      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	      FitsOutput fitsOutput = fitsWrapper.getFits().examine(file);
	      fitsOutput.addStandardCombinedFormat();
	      fitsOutput.output(outStream);
	      String outputString = outStream.toString();
	      
		      if (mimeType == null)
		    	  mimeType = "text/html";
		      
		  resp.setContentType(mimeType);
	      PrintWriter out = resp.getWriter();
	      out.println(outputString);
	      
	  } catch (Exception e){
		  e.printStackTrace(); // remove later
	      ErrorMessage errorMessage = new ErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,  "Fits examine failed", req.getRequestURL().toString(), e.getMessage());
	      sendErrorMessageResponse(errorMessage, resp);
	  } finally {
	      if (fitsWrapper != null){
	          LOG.debug("Returning FITS to pool");
	          fitsWrapperPool.returnObject(fitsWrapper);
	      }
	  }
      
  }  
  
  private void sendErrorMessageResponse(ErrorMessage errorMessage,  HttpServletResponse resp) throws IOException {
      String errorMessageStr = errorMessageToString(errorMessage);      
      PrintWriter out = resp.getWriter();
      resp.setContentType("text/html");
      out.println(errorMessageStr);
  }
  
  private void sendFitsVersionResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      resp.setContentType(FITS_PRODUCES_MIMETYPE);
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
  
  
  public void destroy() {
      // do nothing.
  }
}