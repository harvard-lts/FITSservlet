package edu.harvard.hul.ois.fits.tests;

import java.net.*; 
import java.io.*; 

public class TestFitsServlet {
	  
	public static void main (String args[]) { 
		
        String urlLocal = "http://localhost:8080/fits_service/FitsService";
        String urlProd = "http://remark.hul.harvard.edu:10574/fits_service/FitsService";
        
        String localFilePath = "/Users/Freeze.png";
        String serverFilePath = "/home/users/des/brady.jpg";
        
        try {
            
            URL url = new URL(urlLocal);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            
            BufferedWriter out = 
                new BufferedWriter( new OutputStreamWriter( conn.getOutputStream() ) );
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
