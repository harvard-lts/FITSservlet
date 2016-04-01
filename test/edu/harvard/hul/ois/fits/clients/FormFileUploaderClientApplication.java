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
import org.apache.log4j.Logger;

/**
 * This program is a test client to upload files to a web server using HTTP POST.
 * Modify the endpoint URL as well as the path to the file for uploading.
 * 
 * @author dan179
 */
public class FormFileUploaderClientApplication {
	private static String serverUrl = "http://localhost:8080/fits-1.1.0/examine"; // default value - override with args[1]
	private static Logger logger = null;

	private static final String LOG4J_PROPERTIES_FILE = "tests.log4j.properties";
	
	static {
        File log4jProperties = new File(LOG4J_PROPERTIES_FILE); // looks to load test log4j properties file first.
        System.out.println(LOG4J_PROPERTIES_FILE + " -- File exists: " + log4jProperties.exists());
        if (log4jProperties.exists()) {
        	URI log4jUri = log4jProperties.toURI();
        	System.setProperty("log4j.configuration", log4jUri.toString());
        }
        String log4jProp = System.getProperty("log4j.configuration");
        System.out.println("log4j.configuration: " + log4jProp);
        // else should set log4j properties file from environment variable either in Eclipse of command line with -Dlog4j.configuration=<some location>
        logger = Logger.getLogger(FormFileUploaderClientApplication.class);
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
			HttpPost httppost = new HttpPost(serverUrl);
			FileBody bin = new FileBody(uploadFile);
			HttpEntity reqEntity = MultipartEntityBuilder.create().addPart(FITS_FORM_FIELD_DATAFILE, bin).build();
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
					StringBuilder sb = new StringBuilder("Response data received:");
					while ( (output = in.readLine()) != null ) {
						sb.append(System.getProperty("line.separator"));
						sb.append( output );
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
