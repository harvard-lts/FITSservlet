# Script for running the test client from a terminal -- edu.harvard.hul.ois.fits.clients.FormFileUploaderClientApplication
# Be sure to include the input file parameter to a valid file location.

FITS_SERVICE_HOME=.

TEST_CLASSPATH=$FITS_SERVICE_HOME:$FITS_SERVICE_HOME/WebContent/WEB-INF/classes:\
$FITS_SERVICE_HOME/WebContent/WEB-INF/lib/log4j-1.2.17.jar:$FITS_SERVICE_HOME/WebContent/WEB-INF/lib/commons-logging-1.2.jar:\
$FITS_SERVICE_HOME/lib/httpclient-4.5.2.jar:$FITS_SERVICE_HOME/lib/httpclient-cache-4.5.2.jar:$FITS_SERVICE_HOME/lib/httpcore-4.4.4.jar:$FITS_SERVICE_HOME/lib/httpmime-4.5.2.jar

java -cp $TEST_CLASSPATH edu.harvard.hul.ois.fits.clients.FormFileUploaderClientApplication $1 $2
