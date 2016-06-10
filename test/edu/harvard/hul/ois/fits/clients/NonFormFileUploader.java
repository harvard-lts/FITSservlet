//
// Copyright (c) 2016 by The President and Fellows of Harvard College
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the License at:
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permission and limitations under the License.
//

package edu.harvard.hul.ois.fits.clients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This program demonstrates how to upload files to a web server
 * using HTTP POST request without any HTML form.
 * @author www.codejava.net
 *
 */
@Deprecated // The POST access of the Servlet will only handle form data.
public class NonFormFileUploader {
    static final String UPLOAD_URL = "http://localhost:8080/fits-1.1.0/examine";
    static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) throws IOException {
        // takes file path from first program's argument
        String filePath = args[0];
        File uploadFile = new File(filePath);

        System.out.println("File to upload: " + filePath);

        // creates a HTTP connection
        URL url = new URL(UPLOAD_URL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setRequestMethod("POST");
        // sets file name as a HTTP header
        httpConn.setRequestProperty("datafile", uploadFile.getName());

        // opens output stream of the HTTP connection for writing data
        OutputStream outputStream = httpConn.getOutputStream();

        // Opens input stream of the file for reading data
        FileInputStream inputStream = new FileInputStream(uploadFile);

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;

        System.out.println("Start writing data...");

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        System.out.println("Data was written.");
        outputStream.close();
        inputStream.close();

        // always check HTTP response code from server
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // reads server's response
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String response = reader.readLine();
            System.out.println("Server's response: " + response);
        } else {
            System.out.println("Server returned non-OK code: " + responseCode);
        }
    }
}
