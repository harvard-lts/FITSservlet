package edu.harvard.hul.ois.fits.service.pool;

import org.apache.log4j.Logger;

import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.exceptions.FitsConfigurationException;
import edu.harvard.hul.ois.fits.exceptions.FitsException;
import edu.harvard.hul.ois.fits.service.config.ReadPropertiesFile;

/**
 * Wrapper around a fits instance
 *
 */
public class FitsWrapper {
	

    private static final String fitsHome = System.getProperty("fits.home");    
    private static Logger LOG = Logger.getLogger(FitsWrapper.class);
    private Fits fits;

    public FitsWrapper() {
    	
        LOG.debug("Creating new Fits wrapper");
        System.out.println("fitsHome..."+fitsHome);
        
        if (fitsHome == null)
        	System.out.println("**** ERROR **** MISSING CONFIGURATION DATA!");
        
        try {
            this.fits = new Fits(fitsHome);
        } catch (FitsConfigurationException fce){
            LOG.error("Error initializing FITS " + fce.getMessage());
            this.fits = null;
        } catch (@SuppressWarnings("hiding") FitsException e) {
			e.printStackTrace();
		}
        if (fits != null){
            LOG.debug("Wrapper contains new fits instance");
        } else {
            LOG.error("Fits wrapper initialization failed");
        }
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
