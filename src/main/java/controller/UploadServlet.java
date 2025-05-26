package controller;

import dao.ImageTaskDAO;
import model.User;
import model.ImageTask;
import util.QueueUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 10 * 1024 * 1024,  // 10 MB
    maxRequestSize = 15 * 1024 * 1024 // 15 MB
)
public class UploadServlet extends HttpServlet {
    
    private ImageTaskDAO imageTaskDAO;
    private QueueUtil queueUtil;
    private String uploadPath;
    
    public void init() {
        String approvedPath = getServletContext().getRealPath("") + File.separator + "approved";
        imageTaskDAO = new ImageTaskDAO();
        queueUtil = new QueueUtil(approvedPath);
        
        // Get the upload directory path
        uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check if user is logged in
        HttpSession session = request.getSession();
        if (session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Forward to upload page
        request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check if user is logged in
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        try {
            // Get the uploaded file part
            Part filePart = request.getPart("image");
            if (filePart == null || filePart.getSize() == 0) {
                request.setAttribute("error", "No file was uploaded");
                request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
                return;
            }
            
            // Check file type
            String contentType = filePart.getContentType();
            if (!contentType.startsWith("image/")) {
                request.setAttribute("error", "Only image files are allowed");
                request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
                return;
            }
            
            // Get form parameters
            String processingType = request.getParameter("processingType");
            String priority = request.getParameter("priority");
            String description = request.getParameter("description");
            String customParams = request.getParameter("customParams");
            boolean notifyWhenComplete = "on".equals(request.getParameter("notify"));

            // Generate unique filename
            // Use original filename
            String originalFilename = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String filePath = uploadPath + File.separator + originalFilename;

            // Save the file
            filePart.write(filePath);

            // Create ImageTask
            ImageTask task = new ImageTask();
            task.setUserId((long) user.getId());
            task.setImagePath(filePath);
            task.setOriginalFilename(originalFilename);
            task.setDescription(description);
            task.setCustomParams(customParams);
            task.setNotifyWhenComplete(notifyWhenComplete);
            task.setStatus("PENDING");
            task.setSubmitTime(new Date());
            
            // Save task to database
            imageTaskDAO.save(task);
            
            // Add task to processing queue
            queueUtil.enqueue(task);
            
            // Set success message and redirect
            request.setAttribute("success", "Image uploaded successfully and queued for processing");
            
            // Forward back to the upload page with success message
            request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error uploading image: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
        }
    }
}