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
 * 
 * @author www.codejava.net
 *
 */
public class FormFileUploaderClientApplication {
	static final String UPLOAD_URL = "http://localhost:8080/fits-1.1.0/examine";
	static final int BUFFER_SIZE = 4096;
	static String localFilePath = "/Users/dan179/Documents/FITS-WP-test/input-holding/My_Word_Doc.doc";
	
	private static Logger logger = null;
	
	static {
        File log4jProperties = new File("tests.log4j.properties");
        System.out.println("File exists: " + log4jProperties.exists());
        URI log4jUri = log4jProperties.toURI();
        System.setProperty( "log4j.configuration", log4jUri.toString());
        logger = Logger.getLogger(FormFileUploaderClientApplication.class);
    }

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

		logger.info("File to upload: " + filePath);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpPost httppost = new HttpPost(UPLOAD_URL);
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
