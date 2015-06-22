
FITS servlet

Setup:

There are 2 files to modify
web.xml-- Found in WEB-INF/
project.properties -- Found in WEB-INF/classes

web.xml controls the deployment aspects of the servlet. Specifically, the servlet name as seen by the servlet container.

FITS itself still lives on the local filesystem, but only the TOOLS and XML directories are needed.
The local fits.xml controls the tools a user wants to use

Calling the servlet:

	public static void main (String args[]) { 
		
        String urlLocal = "http://localhost:8080/fits_service/FitsService";
        String urlProd = "http://remark.hul.harvard.edu:10574/fits_service/FitsService";
        
        String localFilePath = "/Users/Dave/Desktop/PERSONAL/CreditFreeze.png";
        String serverFilePath = "/home/users/dsiegel/brady.jpg";
        
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

