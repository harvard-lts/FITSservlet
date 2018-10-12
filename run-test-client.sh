#
# Copyright (c) 2016 by The President and Fellows of Harvard College
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License. You may obtain a copy of the License at:
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software distributed under the License is
# distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permission and limitations under the License.
#

# Script for running the test client from a terminal -- edu.harvard.hul.ois.fits.clients.FormFileUploaderClientApplication
# Be sure to include the input file parameter to a valid file location.

FITS_SERVICE_HOME=.

TEST_CLASSPATH=$FITS_SERVICE_HOME:$FITS_SERVICE_HOME/WebContent/WEB-INF/classes:\
$FITS_SERVICE_HOME/WebContent/WEB-INF/lib/log4j-1.2.17.jar:$FITS_SERVICE_HOME/WebContent/WEB-INF/lib/commons-logging-1.2.jar:\
$FITS_SERVICE_HOME/lib/httpclient-4.5.2.jar:$FITS_SERVICE_HOME/lib/httpclient-cache-4.5.2.jar:$FITS_SERVICE_HOME/lib/httpcore-4.4.4.jar:$FITS_SERVICE_HOME/lib/httpmime-4.5.2.jar

java -cp $TEST_CLASSPATH edu.harvard.hul.ois.fits.clients.FormFileUploaderClientApplication $1 $2
