package edu.harvard.hul.ois.fits.tests;

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
public class NonFormFileUploader {
    static final String UPLOAD_URL = "http://localhost:8080/CodeWeb/ReceiveFileServlet";
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
        httpConn.setRequestProperty("fileName", uploadFile.getName());
 
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
