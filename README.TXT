
FITS Servlet Usage Notes

The FITS application itself is accessed via this servlet application. The servlet application is deployed as a web archive (.war). In Tomcat, a war 
is dropped into the /webapps directory. Tomcat uncompresses the .war and deploys the application under a directory as the same name as the .war. The
actual name of the service that's called is defined in web.xml. The port that the servlet is accessed is controlled independantly of this application.
To run the FITS servlet you must have two directories from the FITS application deployed on the same machine as the FITS servlet. However, only the
XML and TOOLS directories are needed. The FITS servlet .war is deployed with the latest FITS version, and associated .jar files.


This application was developed and tested using Tomcat 7.


Setup:

There are 3 files to modify

catalina.properties -- in tomcat/conf

All of the following .jar files should be placed in ./tomcat/shared/lib
annotations-api.jar					commons-compress-1.4.1.jar		droid-container-6.1.3.jar
antlr-2.7.7.jar						commons-configuration-1.8.jar	droid-core-6.1.3.jar
antlr-3.2.jar						commons-dbcp-1.4.jar			droid-core-interfaces-6.1.3.jar
antlr-runtime-3.2.jar				commons-digester-2.1.jar		droid-export-6.1.3.jar
byteseek-1.1.1.jar					commons-httpclient-3.1.jar		droid-export-interfaces-6.1.3.jar
commons-beanutils-1.8.3.jar			commons-io-2.4.jar				droid-help-6.1.3.jar
commons-beanutils-core-1.8.3.jar	commons-lang-2.6.jar			droid-report-6.1.3.jar
commons-cli-1.2.jar					commons-logging-1.1.1.jar		droid-report-interfaces-6.1.3.jar
commons-codec-1.4.jar				commons-pool-1.5.4.jar			droid-results-6.1.3.jar
commons-collections-3.2.1.jar		droid-command-line-6.1.3.jar

Then, the catalina.properties file should have it's shared.loader property set to this location, either by using CATALINA_OPT or by full path:
	
	shared.loader=/usr/local/tomcat/shared/lib/*.jar


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
       
 

