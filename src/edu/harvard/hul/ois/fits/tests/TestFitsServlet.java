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
		
		TestFitsServlet http = new TestFitsServlet();
		 
		System.out.println("Testing Http GET request");
		http.sendGet();
 
		//System.out.println("\nTesting Http POST request");
		//http.sendPost();
		
	}
	
	
	// HTTP POST request
	private void sendPost() throws Exception {
 
		String url = "http://localhost:8080/FITSservlet/FitsServlet_2";
        String urlLocal = url; //"http://localhost:8080/FITSservlet/FitsServlet_2"; //"http://localhost:8080/fits_service/FitsService";
        String urlProd = "http://remark.hul.harvard.edu:10574/fits_service/FitsService";

        String localFilePath = "/Users/Dave/Desktop/unnamed.png"; //"/Users/Freeze.png";
        String serverFilePath = "/Users/Dave/Desktop/unnamed.png"; //"/home/users/des/brady.jpg";
 
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
        String serverFilePath = "/Users/Dave/Desktop/unnamed.png"; //"/home/users/des/brady.jpg";

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
