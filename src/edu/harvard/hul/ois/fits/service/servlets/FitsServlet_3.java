package edu.harvard.hul.ois.fits.service.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.output.*;

public class FitsServlet_3 extends HttpServlet {
	   
	 
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            // first check if the upload request coming in is a multipart request
            boolean isMultipart = FileUpload.isMultipartContent(request);
            log("content-length: " + request.getContentLength());
            log("method: " + request.getMethod());
            log("character encoding: " + request.getCharacterEncoding());
 
            if (isMultipart) {
                @SuppressWarnings("deprecation")
				DiskFileUpload upload = new DiskFileUpload();
                List items = null;
 
                try {
                    // parse this request by the handler
                    // this gives us a list of items from the request
                    items = upload.parseRequest(request);
                    log("items: " + items.toString());
                } catch (FileUploadException ex) {
                    log("Failed to parse request", ex);
                }
                Iterator itr = items.iterator();
 
                while (itr.hasNext()) {
                    FileItem item = (FileItem) itr.next();
 
                    // check if the current item is a form field or an uploaded file
                    if (item.isFormField()) {
 
                        // get the name of the field
                        String fieldName = item.getFieldName();
 
                        // if it is name, we can set it in request to thank the user
                        if (fieldName.equals("name")) {
                            out.print("Thank You: " + item.getString());
                        }
 
                    } else {
 
                        // the item must be an uploaded file save it to disk. Note that there
                        // seems to be a bug in item.getName() as it returns the full path on
                        // the client's machine for the uploaded file name, instead of the file
                        // name only. To overcome that, I have used a workaround using
                        // fullFile.getName().
                        File fullFile = new File(item.getName());
                        File savedFile = new File(getServletContext().getRealPath("/"), fullFile.getName());
                        try {
                            item.write(savedFile);
                        } catch (Exception ex) {
                            log("Failed to save file", ex);
                        }
                    }
                }
 
            }
        } finally {
            out.close();
        }
    }
 
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
 
    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
 
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}