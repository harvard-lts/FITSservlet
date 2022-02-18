//
// Copyright (c) 2016 by The President and Fellows of Harvard College
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the License at:
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permission and limitations under the License.
//

package edu.harvard.hul.ois.fits.clients;

import static edu.harvard.hul.ois.fits.service.common.Constants.FITS_FORM_FIELD_DATAFILE;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This program is a test client to upload files to a web server using HTTP POST.
 * Modify the endpoint URL as well as the path to the file for uploading.
 *
 * @author dan179
 */
public class FormFileUploaderClientApplication {
	private static String serverUrl = "http://localhost:8080/fits/examine?includeStandardOutput="; // default value - override with args[1]
	private static Logger logger = null;

    /*
     * This is the System property that the Log4j2 framework will use to configure its properties.
     * It should reference a log4j2.xml file.
     */
    private static final String LOG4J_SYSTEM_PROPERTY = "log4j2.configurationFile";

    private static final String LOG4J_PROPERTIES_FILE = "log4j2.xml";

    static {
        File log4jProperties = new File(LOG4J_PROPERTIES_FILE); // looks to load test log4j properties file first.
        System.out.println(LOG4J_PROPERTIES_FILE + " -- File exists: " + log4jProperties.exists());
        if (log4jProperties.exists()) {
            URI log4jUri = log4jProperties.toURI();
        	System.setProperty(LOG4J_SYSTEM_PROPERTY, log4jUri.toString());
        }
        String log4jProp = System.getProperty(LOG4J_SYSTEM_PROPERTY);
        System.out.println("System property -- " + LOG4J_SYSTEM_PROPERTY + ": " + log4jProp);
        // else should set log4j properties file from environment variable either in Eclipse of command line with -Dlog4j2.configurationFile=<some location>
        logger = LogManager.getLogger(FormFileUploaderClientApplication.class);
    }

	/**
	 * Run the program.
	 *
	 * @param args First argument is path to the file to analyze; second (optional) is path to server for overriding default value.
	 */
	public static void main(String[] args) {
		// takes file path from first program's argument
		if (args.length < 1) {
			logger.error("****** Path to input file must be first argument to program! *******");
			logger.error("===== Exiting Program =====");
			System.exit(1);
		}

		String filePath = args[0];
		File uploadFile = new File(filePath);
		if (!uploadFile.exists()) {
			logger.error("****** File does not exist at expected locations! *******");
			logger.error("===== Exiting Program =====");
			System.exit(1);
		}

		if (args.length > 1) {
			serverUrl = args[1];
		}

		logger.info("File to upload: " + filePath);

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpPost httppost = new HttpPost(serverUrl+"false");
			FileBody bin = new FileBody(uploadFile);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addPart(FITS_FORM_FIELD_DATAFILE, bin);
			HttpEntity reqEntity = builder.build();
			httppost.setEntity(reqEntity);

			logger.info("executing request " + httppost.getRequestLine());
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				logger.info("HTTP Response Status Line: " + response.getStatusLine());
				// Expecting a 200 Status Code
				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					String reason = response.getStatusLine().getReasonPhrase();
					logger.warn("Unexpected HTTP response status code:[" + response.getStatusLine().getStatusCode() +
							"] -- Reason (if available): " + reason);
				} else {
					HttpEntity resEntity = response.getEntity();
					InputStream is = resEntity.getContent();
					BufferedReader in = new BufferedReader( new InputStreamReader( is ) );

					String output;
					StringBuilder sb = new StringBuilder();
					while ( (output = in.readLine()) != null ) {
						sb.append( output );
						sb.append(System.getProperty("line.separator"));
					}
					logger.info(sb.toString());
					in.close();
					EntityUtils.consume(resEntity);
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			logger.error("Caught exception: " + e.getMessage(), e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.warn("Exception closing HTTP client: " + e.getMessage(), e);
			}
			logger.info("DONE");
		}
	}

}
