package edu.harvard.hul.ois.fits.service.pool;

import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_HOME_SYSTEM_PROP_NAME;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.exceptions.FitsException;

/**
 * Wrapper around a fits instance
 *
 */
public class FitsWrapper {
	

    private static final String fitsHome = System.getProperty(FITS_HOME_SYSTEM_PROP_NAME);    
    private static Logger logger = Logger.getLogger(FitsWrapper.class);
    private Fits fits;

    public FitsWrapper() throws ServletException {
    	
        logger.debug("Creating new Fits wrapper");
        logger.info("FITS HOME: "+fitsHome);
        
        // This really should have been checked earlier.
        if (fitsHome == null) {
        	logger.fatal(FITS_HOME_SYSTEM_PROP_NAME + " system property HAS NOT BEEN SET!!! This web application will not properly run.");
        	throw new ServletException(FITS_HOME_SYSTEM_PROP_NAME + " system property HAS NOT BEEN SET!!! This web application will not properly run.");
        }
        
        try {
            this.fits = new Fits(fitsHome);
        } catch (FitsException fce){
            logger.error("Error initializing FITS " + fce.getMessage());
        	throw new ServletException("Error initializing FITS ", fce);
		} catch (Throwable t) {
			logger.error("Unexpected Throwable:", t);
        	throw new ServletException("Unexpected Throwable:", t);
		}
        if (fits == null){
            logger.error("Fits is null. Something unexpected happened.");
            throw new ServletException("Fits is null. Something unexpected initialization happened.");
        }

        logger.debug("Wrapper contains new Fits instance");
    }

    public Fits getFits(){
        return fits;
    }

    public boolean isValid() {
        if (fits != null){
            return true;
        } else {
            return false;
        }
    }

}
