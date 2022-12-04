# FITS Service Release Notes

## Version 1.3.0 (TBD)
- Update to FITS 1.6.0
- Java 11 now required
- Update Docker image

## Version 1.2.0 (11/7/2018)
- Converted the project to a Maven build.
- Add a request parameter allowing "standard output" metadata to be suppressed. That is, the default result returned is as though running FITS with the -xc flag. Appending the request parameter `includeStandardOutput=false` to the FITS Service web application URL is the equivalent to not using the -xc flag.

## Version 1.1.3 (9/13/16)
- Error responses (HTTP 400, 500, etc.) are now returned as XML instead of plain text.
- Allow a 0-length file as valid input; valid XML will be returned rather than a HTTP 400 code.
- Exception stack traces now sent to log file. (Previously only the exception message was logged.)
- Externalize servlet configuration values to a properties file. (See the [README](https://github.com/harvard-lts/FITSservlet#add-entries-to-catalinaproperties) section on pointing to this file from catalina.properties for more information.)
- Fix mistake in 'curl' example in README file.

## Version 1.1.2 (6/10/16)
- Update licensing at top of source files for consistency across FITS project.
- Update build for better access to fits.jar via either environment variable setting or fallback setting.
- Removed duplication of JAR files that were put into the WAR's WEB-INF/lib directory by build.xml.

## Version 1.1.1 (4/18/16)
- Updated documentation.
- Added 'description' attributes to a few Ant targets in build.xml.

## Version 1.1.0 (4/15/16)
- Implement POST handling of streaming file upload from any location.
- Add Java test client - FormFileUploaderClientApplication.java - for testing POST functionality of Servlet.
- Add script to exercise Java test client from terminal.
- Built and tested with Java 8.

## Version 1.0.0 (1/5/16)
- Major refactoring.
- This release is compatible only with FITS v.0.9.0 and later.
- Removed all FITS project JAR files.
- Removed WAR build artifact from source control.
- FITS files runtime resolution via Tomcat or JBOSS classpath addition.
- Created an Ant build file to create WAR artifact.
- Initially only Servlet end point is GET HTTP request for local file upload.
