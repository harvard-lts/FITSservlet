
FITS Servlet Usage Notes

The FITS application itself is accessed via this servlet application. The servlet application is deployed as a web archive (.war). In Tomcat, a war 
is dropped into the /webapps directory. Tomcat uncompresses the .war and deploys the application under a directory as the same name as the .war. The
actual name of the service that's called is defined in web.xml. The port that the servlet is accessed is controlled independantly of this application.
To run the FITS servlet you must have two directories from the FITS application deployed on the same machine as the FITS servlet. However, only the
XML and TOOLS directories are needed. The FITS servlet .war is deployed with the latest FITS version, and associated .jar files.


This application was developed and tested using Tomcat 7.


Setup:

There are 2 files to modify

web.xml-- Found in WEB-INF/
project.properties -- Found in WEB-INF/classes

web.xml controls the deployment aspects of the servlet. Specifically, the servlet name as seen by the servlet container.

In the following configuration:

  <servlet>
    <servlet-name>FitsService</servlet-name>
    <servlet-class>edu.harvard.hul.ois.fits.service.servlets.FitsServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>FitsService</servlet-name>
    <url-pattern>/FitsService</url-pattern>
  </servlet-mapping>

The name of the Java class is edu.harvard.hul.ois.fits.service.servlets.FitsServlet and is referenced in the servlet container as:

		http://yourserver.yourdomain.com:<port>/<project name>/FitsService

The project.properties file holds the path to the local installation FITS path.

In the following configuration:

  <servlet>
    <servlet-name>FitsService</servlet-name>
    <servlet-class>edu.harvard.hul.ois.fits.service.servlets.FitsServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>FitsService</servlet-name>
    <url-pattern>/FitsService</url-pattern>
  </servlet-mapping>

The name of the Java class is edu.harvard.hul.ois.fits.service.servlets.FitsServlet and is referenced in the servlet container as:

http://yourserver.yourdomain.com:0000/FitsService

The project.properties file holds the path to the local installation FITS path.

FITS itself still lives on the local filesystem, but only the TOOLS and XML directories are needed.
The local fits.xml controls the tools a user wants to use and should be modified according to your preferences.

Basic example of calling the servlet in Java:



	public static void main (String args[]) { 
		
        String urlLocal = "http://localhost:8080/fits_service/FitsService";
        String localFilePath = "/Users/fits/Desktop/PERSONAL/someimage.png";
        
        try {
            
            URL url = new URL(urlLocal);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            
            BufferedWriter out = 
                new BufferedWriter( new OutputStreamWriter( conn.getOutputStream() ) );
            out.write("file="+localFilePath);
            out.flush();
            
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

To create a project in Eclipse with this code, and save it as a .war for deployment, this link is helpful:

       http://www.mkyong.com/java/how-to-convert-java-project-to-web-project-in-eclipse/
       
 

