//
// Copyright (c) 2016 by The President and Fellows of Harvard College
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the License at:
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permission and limitations under the License.
//

package edu.harvard.hul.ois.fits.service.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;


//import edu.harvard.hul.ois.eas.util.ExternalProgram;
//import edu.harvard.hul.ois.eas.util.StringUtil;
//import edu.harvard.hul.ois.eas.util.TimeUtil;

public class FileUtil {

    /**
     * Used to substitute account names into configuration settings.
     */
    public static final String PHRASE_ACCT_NAME = "!ACCOUNT!";

    /**
     * Used to substitute instance names into configuration settings.
     */
    public static final String PHRASE_INSTANCE_NAME = "!INSTANCE!";

    static Logger logger = Logger.getLogger(FileUtil.class);

    /**
     * Append to a file.
     *
     * @param part
     * @param toFile
     * @throws IOException
     */
    public void appendToFile(String part, File toFile) throws IOException{
        Writer pencil = new BufferedWriter(new FileWriter(toFile, true));
        pencil.write(part);
        pencil.close();
    }

    /**
     * Performs a sanity check to make sure that all configuration
     * values needed by an object are set. This method relies on
     * required property keys being coded as String variables in the
     * <code>o</code> using the naming convention of "PROP_*". Reflection is
     * used on <code>o</code> to find all object fields representing
     * property keys.
     *
     * @param o an Object whose configuration needs to be checked
     * @param props a Hashtable object representing the current configuration state of <code>o</code> in key/value mappings
     */
    public static void checkConfiguration(final Object o, final Hashtable<String, String> props) throws IOException {
    	// use reflection to get property keys
    	Field[] fields = o.getClass().getDeclaredFields();
    	ArrayList<String> propsToCheck = new ArrayList<String>();
    	for (Field f : fields) {
    		if (f.getName().startsWith("PROP_")) {
	    		try {
					propsToCheck.add((String)f.get(o));
				} catch (IllegalArgumentException e) {
					// should never happen since argument is "this"
				} catch (IllegalAccessException e) {
					// do nothing, if required property cannot be accessed, checkConfiguartion()
					// will handle it
				} catch (ClassCastException e) {
					// also should not happen, since all property keys are strings
				}
    		}
    	}

        checkConfiguration(propsToCheck.toArray(new String[propsToCheck.size()]), props);
    }

    /**
     * Performs a sanity check to make sure that all configuration
     * values needed by an object are set. This method relies on
     * required property keys being coded as String variables in the
     * <code>o</code> using the naming convention of "PROP_*". Reflection is
     * used on <code>o</code> to find all object fields representing
     * property keys.
     *
     * @param o an Object whose configuration needs to be checked
     * @param props a Properties object representing the current configuration state of <code>o</code> in key/value mappings
     */
    public static void checkConfiguration(final Object o, final Properties props) throws IOException {
    	// use reflection to get property keys
    	Field[] fields = o.getClass().getDeclaredFields();
    	ArrayList<String> propsToCheck = new ArrayList<String>();
    	for (Field f : fields) {
    		if (f.getName().startsWith("PROP_")) {
	    		try {
					propsToCheck.add((String)f.get(o));
				} catch (IllegalArgumentException e) {
					// should never happen since argument is "this"
				} catch (IllegalAccessException e) {
					// do nothing, if required property cannot be accessed, checkConfiguartion()
					// will handle it
				} catch (ClassCastException e) {
					// also should not happen, since all property keys are strings
				}
    		}
    	}

        checkConfiguration(propsToCheck.toArray(new String[propsToCheck.size()]), props);
    }

    /**
     * Iterate through configuration settings that must be set and make sure
     * they are not null. Using this method after reading in the configuration
     * settings ensures that everything needed for processing will be non-null.
     *
     * @param requiredProps an array of strings representing required property keys
     * @param props a Hashtable object representing the current configuration state
     * @throws IOException
     */
    public static void checkConfiguration(final String[] requiredProps, final Hashtable<String, String> props) throws IOException {
        for (String prop : requiredProps) {
            if (props.get(prop)== null) {
                throw new IOException("Missing config value: " + prop);
            }
        }
    }

    /**
     * Iterate through configuration settings that must be set and make sure
     * they are not null. Using this method after reading in the configuration
     * settings ensures that everything needed for processing will be non-null.
     *
     * @param requiredProps an array of strings representing required property keys
     * @param props a Properties object representing the current configuration state
     * @throws IOException
     */
    public static void checkConfiguration(final String[] requiredProps, final Properties props) throws IOException {
        for (String prop : requiredProps) {
            if (props.getProperty(prop)== null) {
                throw new IOException("Missing config value: " + prop);
            }
        }
    }

}
