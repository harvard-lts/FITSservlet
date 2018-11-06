//
// Copyright (c) 2016 by The President and Fellows of Harvard College
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the License at:
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permission and limitations under the License.
//

package edu.harvard.hul.ois.fits.service.common;

public class Constants {

	/** Environment variable for setting path to external application properties file */
	public final static String ENV_PROJECT_PROPS = "FITS_SERVICE_PROPS";

	/** Name of application properties file */
	public final static String PROPERTIES_FILE_NAME = "fits-service.properties";

	/** Resource path for processing an input file */
	public final static String FITS_RESOURCE_PATH_EXAMINE = "/examine";

	/** Resource path for obtaining the FITS version (GET only) */
    public final static String FITS_RESOURCE_PATH_VERSION = "/version";

	/** Form variable name for access to input file (POST) */
    public final static String FITS_FORM_FIELD_DATAFILE = "datafile";
    
    /** Request parameter name for pointing to input file (GET) */
    public final static String FITS_FILE_PARAM = "file";
    
    /**
     * Request parameter for including standard (content model) metadata output
     * along with FITS metadata. Default if 'true'.
     * Value <code>false</code> do not include standard output; otherwise 
     * output is included (default).
     */
    public final static String INCLUDE_STANDARD_OUTPUT_PARAM = "includeStandardOutput";
    
    /** Location of the FITS application home directory */
    public final static String FITS_HOME_SYSTEM_PROP_NAME = "fits.home";

    public final static String TEXT_HTML_MIMETYPE = "text/html";
    public final static String TEXT_PLAIN_MIMETYPE = "text/plain";
    public final static String TEXT_XML_MIMETYPE = "text/xml";
}
