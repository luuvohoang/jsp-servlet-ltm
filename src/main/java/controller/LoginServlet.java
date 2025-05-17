package controller;

import dao.UserDAO;
import model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO;
    
    public void init() {
        userDAO = new UserDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/auth/LoginPage.jsp")
           .forward(request, response);
}
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // Kiểm tra mặc định username=1 và password=1
        if ("1".equals(username) && "1".equals(password)) {
            // Tạo user mặc định để đăng nhập nhanh
            User defaultUser = new User();
            defaultUser.setId(999);
            defaultUser.setUsername("admin");
            defaultUser.setEmail("admin@example.com");
            
            // Lưu user vào session
            HttpSession session = request.getSession();
            session.setAttribute("user", defaultUser);
            
            // Chuyển hướng đến trang home
            response.sendRedirect(request.getContextPath() + "/upload");
            return;
        }
        
        try {
            User user = userDAO.login(username, password);
            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                response.sendRedirect(request.getContextPath() + "/upload");
            } else {
                request.setAttribute("error", "Invalid username or password");
                request.getRequestDispatcher("/WEB-INF/views/auth/LoginPage.jsp")
                       .forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Login failed: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/auth/LoginPage.jsp")
                   .forward(request, response);
        }
    }
}