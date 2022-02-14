//
// Copyright (c) 2016 by The President and Fellows of Harvard College
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the License at:
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permission and limitations under the License.
//

package edu.harvard.hul.ois.fits.service.listeners;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
	 * This is the System property that the Log4j2 framework will use to configure its properties.
	 * It should reference a log4j2.xml file.
	 */
	private static final String LOG4J_SYSTEM_PROPERTY = "log4j2.configurationFile";

	/**
	 * Configure the Log4j logging framework on application initialization.
	 *
	 * @param ctx - The ServletContextEvent
	 */
    @Override
    public void contextInitialized(ServletContextEvent ctx) {

    	System.err.println("Attempting to set up log4j logging...");
    	boolean useFallback = false;
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
                    System.err.print("Will look for log4j2.xml properties file here: " + log4jUri.toString());
                }
            } catch (URISyntaxException e) {
                // fall back to default file from WAR -- must first clear System property for this to work
                System.err.println("Unable to load log4j2.xml file: " + log4jSystemProp + " -- reason: " + e.getReason());
                System.err.println("Falling back to default log4j2.xml file from within WAR file.");
                System.clearProperty(LOG4J_SYSTEM_PROPERTY);
                useFallback = true;
            }
        } else {
        	System.err.println("No system property for [" + LOG4J_SYSTEM_PROPERTY + "] -- using default log4j2.xml within WAR file");
        	useFallback = true;
        }
        
        if (useFallback) {
        	try {
				URL propFileURL = ctx.getServletContext().getResource("/WEB-INF/classes/log4j2.xml");
				log4jUri = propFileURL.toURI();
                System.setProperty( LOG4J_SYSTEM_PROPERTY, log4jUri.toString());
                System.err.println("Look for log4j2.xml properties file in WAR here: " + log4jUri.toString());
			} catch (MalformedURLException | URISyntaxException e) {
				System.err.println("Could not access log4j2.xml in /WEB-INF/classes/");
				e.printStackTrace();
			}
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent ctx) {
        // nothing to do here
    }

}
