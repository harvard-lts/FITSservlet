package edu.harvard.hul.ois.fits.service.listeners;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Set up Log4j configuration with more robust error checking than would normally occur.
 * If there is a System property that references a Log4j properties file and either that
 * file does not exist or results in some error, there is normally no fall-back to use
 * the default file supplied with the WAR file. This class remedies this shortcoming.
 * 
 * @author dan179
 */
public class LoggingConfigurator implements ServletContextListener {
	
	/*
	 * This is the System property that the Log4j framework will use to configure its properties.
	 * It should reference a log4j.properties file.
	 */
	private static final String LOG4J_SYSTEM_PROPERTY = "log4j.configuration";

	/**
	 * Configure the Log4j logging framework on application initialization.
	 * 
	 * @param ctx - The ServletContextEvent
	 */
    @Override
    public void contextInitialized(ServletContextEvent ctx) {

        // Set up logging.
    	// Attempt to make it more robust.
    	// Log4j seems to want a valid URL for proper initialization.
    	// If the property is just a path then convert it to a URL and set it back into the system property.
        String log4jSystemProp = System.getProperty(LOG4J_SYSTEM_PROPERTY);
        URI log4jUri = null;
        if (log4jSystemProp != null) {
            try {
                log4jUri = new URI(log4jSystemProp);
                // log4j system needs a scheme in the URI so convert to file if necessary.
                if (null == log4jUri.getScheme()) {
                    File log4jProperties = new File(log4jSystemProp);
                    if (log4jProperties.exists() && log4jProperties.isFile()) {
                        log4jUri = log4jProperties.toURI();
                    } else {
                        // No scheme and not a file - yikes!!! Let's bail and use fall-back file.
                        log4jUri = null;
                        throw new URISyntaxException(log4jSystemProp, "Not a valid file");
                    }
                    
                    // Even if set, reset logging System property to ensure it's in a URI format
                    // with scheme so the log4j framework can initialize.
                    System.setProperty( LOG4J_SYSTEM_PROPERTY, log4jUri.toString());
                }
            } catch (URISyntaxException e) {
                // fall back to default file from WAR -- must first clear System property for this to work
                System.err.println("Unable to load log4j.properties file: " + log4jSystemProp + " -- reason: " + e.getReason());
                System.err.println("Falling back to default log4j.properties file from within WAR file.");
                System.clearProperty(LOG4J_SYSTEM_PROPERTY);
            }
        }
        // Else no external log4j.properties file then the default file from WAR will be used.
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent ctx) {
        // nothing to do here
    }

}
