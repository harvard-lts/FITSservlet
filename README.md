* [FITS Servlet Usage Notes](#servlet-usage)


Eclipse (and other IDE's) it will be necessary to resolve reference to classes in the FITS project.
At runtime these references will be resolved via Tomcat classpath and JBoss module additions.

FITS Servlet Usage Notes

The FITS application itself is accessed via this servlet application. The servlet application is deployed as a web archive (.war). In Tomcat 7, a WAR file 
is dropped into the /webapps directory. Tomcat uncompresses the .war and deploys the application under a directory as the same name as the .war. The
actual name of the service that's called is defined in web.xml. The port that the servlet is accessed is controlled independently of this application.
To run the FITS servlet you must have unpacked the FITS application zip file to the file system. In Tomcat the conf/catalina.properties file must be modified
to indicate this location. The following both sets the FITS location as well as adding it lib/*.jar files to the Tomcat shared class loader. NOTE: The JAR files
in the FITS lib/ subdirectories are added programmatically. Do not add them in the catalina.properties file.

This application was developed and tested using Tomcat 7.

Setup:
Modify <Tomcat Home>/conf/catalina.properties by adding the following to the end of the file:
# FITS properties
fits.home=/path/to/FITS/installation
# (copied 'shared.loader' from above to add to the shared classpath)
shared.loader=${fits.home}/lib/*.jar

<a name="servlet-usage"></a>FITS Servlet Usage Notes

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

