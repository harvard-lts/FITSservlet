The FITS Servlet is a project that allows FITS to be deployed as a service on either Tomcat or JBoss.
(It has been tested on Tomcat 7 and minimally tested on JBoss 7.1.)

* [FITS Servlet Usage Notes](#servlet-usage)
* [Deploying to Tomcat](#tomcat)
* [Deploying to JBoss](#jboss)
* [IDE Notes](#ide-notes)
* [Test Client](#test-client)

# <a name="servlet-usage"></a>FITS Servlet Usage Notes
This project requires the installation of [FITS](http://fitstool.org).
Download FITS from the [download](http://projects.iq.harvard.edu/fits/downloads) page and unpack the zip file to a directory on the server.

In order to run the FITS Servlet on a server it’s necessary to modify the server’s classpath configuration to add FITS JAR files. Essentially, this mean adding the FITS home directory to the server’s classpath since FITS can (and should) be deployed to a location outside the server.

The name of the Java class is edu.harvard.hul.ois.fits.service.servlets.FitsServlet and is referenced in the servlet container as:
    `http://yourserver.yourdomain.com:<port>/<project name>/FitsService`
See [below](#test-client) for a usage example.

# <a name="tomcat"></a>Deploying to Tomcat 7
## Add Entries to catalina.properties
It’s necessary to add the location of the FITS directory to the file `$CATALINA_BASE/conf/catalina.properties` then add the FITS lib folder JAR files. (See example below.) 
1. Add the “fits.home” environment variable.
2. Add all “fits.home”/lib/ JAR files to the shared class loader classpath with a wildcard ‘*’ and the `${fits.home}` property substitution.
**Note: Do NOT add any JAR files that are contained in any lib/ subdirectories. They are added programmatically at runtime by the application.**

### catalina.properties example
Add the following to the bottom of the file:
    `fits.home=path/to/fits/home`  (note: no final slash in path)
    `shared.loader=${fits.home}/lib/*.jar`

### Additional Information:
Within the WAR file’s META-INF directory is a Tomcat-specific file, context.xml. This file indicates to the Tomcat server to modify the Tomcat default class loader scheme for this application. The result is that, rather than load the WAR’s classes and JAR files first, classes on Tomcat’s shared classpath will be loaded first. This is critical given the nature of the custom class loaders used in FITS. (This file will be ignored if deploying to JBoss.)

# <a name="jboss"></a>Deploying to JBoss 7.1
Setting up the FITS Servlet within JBoss is somewhat more involved due to the more complex nature of JBoss class isolation.
## Setting Environment Variable ‘fits.home’
This can be one in one of two ways:
1. Set environment variable on command line.
In script that calls `<JBOSS-HOME>/bin/standalone.sh` add `fits_home_dir='/path/to/fits/home'`.
Then start the server with `./standalone.sh -Dfits.home=$fits_home_dir`
2. Set an environment variable in `<JBOSS-HOME>/standalone/configuration/standalone.xml`
Note: This MUST be after the closing element `</extensions>`
```
<system-properties>
  <property name="fits.home" value="/path/to/fits/home"/>
</system-properties>
```
## Adding FITS to classpath
1. `$ mkdir <JBOSS-HOME>/modules/fits/main`
This is where the module.xml file and symlink to “fits.home” will reside.
2. Create module and place in `<JBOSS-HOME>/modules/fits/main`

It’s necessary to add the full list of JAR files in <fits-home>/lib. Adding “*.jar” will **not** work for JBoss nor will ${fits.home} as substitution value from the environment variable (unfortunate for both of these).
As a result of this, if the FITS lib/ directory has changed then this file needs to be changed accordingly. **Do NOT add any JAR files that are contained in any lib/ subdirectories. They are added programmatically at runtime by the application.**

    <?xml version="1.0" encoding="UTF-8"?>
    <module xmlns="urn:jboss:module:1.1" name="fits">
        <resources>
            <resource-root path="fits-0.9.0/lib/fits.jar"/>
            <resource-root path="fits-0.9.0/lib/AES31.jar"/>
            ...
            (you MUST list every JAR files in FITS/lib)
        </resources>
        <dependencies>
            <module name="javax.api"/> <!-- for xerces -->
        </dependencies>
    </module>

Create the symlink to fits.home directory (adjusting paths accordingly):
`$ ln -s <fits.home dir> <JBOSS-HOME>/modules/fits/main`
Add a reference to the module in standalone.xml
```
<profile>
...
    <subsystem xmlns="urn:jboss:domain:ee:1.0">
        <global-modules>
            <module name="fits"/>
        </global-modules>
    </subsystem>
</profile>
```

### Additional Information:
Within the WAR file’s WEB-INF directory is a JBoss-specific file, jboss-deployment-structure.xml. This file indicates to the JBoss server the location of the JBoss module for FITS. (This file will be ignored if deploying to Tomcat.)

Additional JBoss information can be found here: https://developer.jboss.org/thread/219956?tstart=0

# <a name="ide-notes"></a>IDE Notes
For Eclipse and other IDE's it will be necessary to resolve reference to classes in the FITS project. When adding this project to an IDE for development the FITS project should also be added and referenced.
At runtime in a server environment these references will be resolved via the Tomcat shared classpath or the JBoss module additions.

# <a name="test-client"></a>Test Client
Here is a basic example of calling the service in Java from a sample client application named FitsServletClient.java.
(Modify URL and path to file that will be processed.)

    import java.io.BufferedReader;
    import java.io.BufferedWriter;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.io.OutputStreamWriter;
    import java.net.MalformedURLException;
    import java.net.URL;
    import java.net.URLConnection;
    
    public class FitsServletClient {
    
        public static void main (String args[]) { 
    
            String urlLocal = "http://localhost:8080/FITSservlet/FitsService";
            String localFilePath = "/Users/some-user/Documents/some-file.gif";
    
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
                System.err.println("Uh oh..." + ex);
                ex.printStackTrace();
            } catch ( IOException ex ) {
                System.err.println("Uh oh..." + ex);
                ex.printStackTrace();
            } catch ( Exception ex ) {
                System.err.println("Uh oh..." + ex);
                ex.printStackTrace();
            }
        }
    }
