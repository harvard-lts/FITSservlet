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

import static javax.servlet.http.HttpServletResponse.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name="error")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"statusCode",  "message", "request", "support"})

public class ErrorMessage {
	@XmlElement
	private int statusCode;
	@XmlElement
	private String message;
	@XmlElement
	private String request;
	@XmlElement
	private String support;

	public ErrorMessage() {
		super();
	}

	/**
	 * Full constructor
	 *
	 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html for status code and reason phrase
	 * @param statusCode - the http status code as a String
	 * @param message - the detailed error message
	 * @param request - the request URL
	 * @param support - a placeholder for any further useful information
	 */
	public ErrorMessage(int statusCode, String message, String request, String support){
		this.statusCode = statusCode  != 0 ? statusCode : SC_INTERNAL_SERVER_ERROR;
		this.message = message;
		this.request = request;
		this.support = support;
	}

	/**
	 * minimal constructor
	 * This constructor sets the status to an internal error
	 *
	 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html for status code and reason phrase
	 * @param statusCode - the http status code as a String
	 * @param reasonPhrase - the http status line reasonPhrase
	 * @param message - the detailed error message
	 * @param request - the request URL
	 */
	public ErrorMessage(String message, String request){
		this.statusCode = SC_INTERNAL_SERVER_ERROR;
		this.message = message;
		this.request = request;
	}

	/**
	 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html for status code and reason phrase
	 * @param statusCode - the http status code as a String
	 * @param message - the detailed error message
	 * @param request - the request URL
	 */
	public ErrorMessage(int statusCode, String message, String request){
		this.statusCode = statusCode  != 0 ? statusCode : SC_INTERNAL_SERVER_ERROR;
		this.message = message;
		this.request = request;
	}

	/**
	 * This constructor sets the status to an internal error
	 *
	 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html for status code and reason phrase
	 * @param message - the detailed error message
	 * @param request - the request URL
	 * @param support - a placeholder for any further useful information
	 */
	public ErrorMessage(String message, String request, String support){
		this.statusCode =  SC_INTERNAL_SERVER_ERROR;
		this.message = message;
		this.request = request;
		this.support = support;
	}


	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getReasonPhrase() {
		return getReasonPhrase(this.statusCode);
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = support;
	}


	/**
	 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html
	 * @param statusCode
	 * @return
	 */
	public String getReasonPhrase(int statusCode){
		switch (statusCode) {
			case SC_CONTINUE            : return "Continue";
			case SC_SWITCHING_PROTOCOLS : return "Switching Protocols";
			case SC_OK                  : return "OK";
			case SC_CREATED             : return "Created";
			case SC_ACCEPTED            : return "Accepted";
			case SC_NON_AUTHORITATIVE_INFORMATION : return  "Non-Authoritative Information";
			case SC_NO_CONTENT          : return "No Content";
			case SC_RESET_CONTENT       : return "Reset Content";
			case SC_PARTIAL_CONTENT     : return "Partial Content";
			case SC_MULTIPLE_CHOICES    : return "Multiple Choices";
			case SC_MOVED_PERMANENTLY   : return "Moved Permanently";
			case SC_FOUND               : return "Found";
			case SC_SEE_OTHER           : return "See Other";
			case SC_NOT_MODIFIED        : return "Not Modified";
			case SC_USE_PROXY           : return "Use Proxy";
			case SC_TEMPORARY_REDIRECT  : return "Temporary Redirect";
			case SC_BAD_REQUEST         : return "Bad Request";
			case SC_UNAUTHORIZED        : return "Unauthorized";
			case SC_PAYMENT_REQUIRED    : return "Payment Required";
			case SC_FORBIDDEN           : return "Forbidden";
			case SC_NOT_FOUND           : return "Not Found";
			case SC_METHOD_NOT_ALLOWED  : return "Method Not Allowed";
			case SC_NOT_ACCEPTABLE      : return "Not Acceptable";
			case SC_PROXY_AUTHENTICATION_REQUIRED : return "Proxy Authentication Required";
			case SC_REQUEST_TIMEOUT     : return "Request Time-out";
			case SC_CONFLICT            : return "Conflict";
			case SC_GONE                : return "Gone";
			case SC_LENGTH_REQUIRED     : return "Length Required";
			case SC_PRECONDITION_FAILED : return "Precondition Failed";
			case SC_REQUEST_ENTITY_TOO_LARGE : return "Request Entity Too Large";
			case SC_REQUEST_URI_TOO_LONG : return "Request-URI Too Large";
			case SC_UNSUPPORTED_MEDIA_TYPE : return "Unsupported Media Type";
			case SC_REQUESTED_RANGE_NOT_SATISFIABLE : return "Requested range not satisfiable";
			case SC_EXPECTATION_FAILED   : return "Expectation Failed";
			case SC_INTERNAL_SERVER_ERROR : return "Internal Server Error";
			case SC_NOT_IMPLEMENTED    : return "Not Implemented";
			case SC_BAD_GATEWAY        : return "Bad Gateway";
			case SC_SERVICE_UNAVAILABLE: return "Service Unavailable";
			case SC_GATEWAY_TIMEOUT    : return "Gateway Time-out";
			case SC_HTTP_VERSION_NOT_SUPPORTED : return "HTTP Version not supported";
			default: return "Internal Server Error";
		}
	}



	/**
	 * A convenience in case jaxb marshalling is not available
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		sb.append("<error>")
			.append("<statusCode>")
				.append(statusCode)
			.append("</statusCode>")
			.append("<message>")
				.append(message != null? message : "")
			.append("</message>")
			.append("<request>")
				.append(request != null? request : "")
			.append("</request>")
			.append("<support>")
				.append("[toString version]" + (support != null? support : ""))
			.append("</support>")
		.append("</error>");
		return sb.toString();
	}

}
