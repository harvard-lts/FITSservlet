package edu.harvard.hul.ois.fits.tests;

import java.net.*; 
import java.io.*; 

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

public class TestFitsServlet {
	  
	private final String USER_AGENT = "Mozilla/5.0";
	
	public static void main (String args[]) throws Exception {
		
		/*
		 * You had to add a lot of .jar files to the Tomcat lib dir in order to get DROID to run.
		 * You really need to correct the class path in the manifest to handle this issue.
		 * Replace the new fits.jar (fitsDS.jar) with the original fits.jar in the serlvet deployment.
		 * 
		 * All of these had to be in ../tomcat/lib since the classpath is not set correctly in
		 * the .war
		 * 
		 * annotations-api.jar					commons-digester-2.1.jar			el-api.jar
			antlr-2.7.7.jar						commons-httpclient-3.1.jar			jasper-el.jar
			antlr-3.2.jar						commons-io-2.4.jar					jasper.jar
			antlr-runtime-3.2.jar				commons-lang-2.6.jar				jsp-api.jar
			byteseek-1.1.1.jar					commons-logging-1.1.1.jar			log4j-1.2.16.jar
			catalina-ant.jar					commons-pool-1.5.4.jar				servlet-api.jar
			catalina-ha.jar						droid-command-line-6.1.3.jar		solr-velocity-4.4.0.jar
			catalina-tribes.jar					droid-container-6.1.3.jar			tomcat-api.jar
			catalina.jar						droid-core-6.1.3.jar				tomcat-coyote.jar
			commons-beanutils-1.8.3.jar			droid-core-interfaces-6.1.3.jar		tomcat-dbcp.jar
			commons-beanutils-core-1.8.3.jar	droid-export-6.1.3.jar				tomcat-i18n-es.jar
			commons-cli-1.2.jar					droid-export-interfaces-6.1.3.jar	tomcat-i18n-fr.jar
			commons-codec-1.4.jar				droid-help-6.1.3.jar				tomcat-i18n-ja.jar
			commons-collections-3.2.1.jar		droid-report-6.1.3.jar				tomcat-jdbc.jar
			commons-compress-1.4.1.jar			droid-report-interfaces-6.1.3.jar	tomcat-util.jar
			commons-configuration-1.8.jar		droid-results-6.1.3.jar
			commons-dbcp-1.4.jar				ecj-3.7.2.jar
			*
			* NOTE: There are still some log4j issues in stdout
			* 
		 */
		
		TestFitsServlet http = new TestFitsServlet();
		 
		http.sendGet();
		//http.sendPost();
		
	}
	
	
	// HTTP POST request
	private void sendPost() throws Exception {
 
		String url = "http://localhost:8080/FITSservlet/FitsServlet_2";
        String urlLocal = url; //"http://localhost:8080/FITSservlet/FitsServlet_2"; //"http://localhost:8080/fits_service/FitsService";
        String urlProd = "http://remark.hul.harvard.edu:10574/fits_service/FitsService";

        String localFilePath = "/Users/Dave/Pictures/temp1.jpg"; //"/Users/Freeze.png";
        String serverFilePath = "/Users/Dave/Pictures/temp1.jpg"; //"/home/users/des/brady.jpg";
 
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
 
		// add header
		post.setHeader("User-Agent", USER_AGENT);

    	File file = new File(localFilePath);
        FileBody bin = new FileBody(file);            
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("datafile", bin);
        post.setEntity(reqEntity);            


        //-String responseBody = invokeResponseAsString(httpResponse);                
        //-return responseBody;            
		HttpResponse response = client.execute(post);
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + post.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
 
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
 
		System.out.println(result.toString());
 
	}
		
	// HTTP GET request
	private void sendGet() throws Exception {
 
        String urlLocal = "http://localhost:8080/FITSservlet/FitsServlet_2"; //"http://localhost:8080/fits_service/FitsService";
        String urlProd = "http://remark.hul.harvard.edu:10574/fits_service/FitsService";

        String localFilePath = "/Users/Freeze.png";
        String serverFilePath = "/Users/Dave/Pictures/temp1.jpg"; //"/home/users/des/brady.jpg";

        try {
        	
			// add request header if you want to
			//req.addHeader("User-Agent", USER_AGENT);

        	// This sends to doGet - GR's code.
            URL url = new URL(urlLocal+"?file="+serverFilePath);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            
            BufferedWriter out = new BufferedWriter( new OutputStreamWriter( conn.getOutputStream() ) );
            out.write("file="+localFilePath);
            out.flush();
            out.close();
            
            BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
            
            String response;
            while ( (response = in.readLine()) != null ) {
                System.out.println( response );
            }
            in.close();
        } catch ( MalformedURLException ex ) {
            // a real program would need to handle this exception
        } catch ( IOException ex ) {
            // a real program would need to handle this exception
        }
	}
 
}
