package controller;

import dao.ImageTaskDAO;
import model.ImageTask;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/results")
public class ResultsServlet extends HttpServlet {

    private ImageTaskDAO imageTaskDAO;

    public void init() {
        imageTaskDAO = new ImageTaskDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int page = 1;
        int pageSize = 10;
        String sortBy = "submitTime";
        String sortOrder = "desc";
        String filter = "all"; // all, completed, processing, failed

        try {
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            }

            String sortByParam = request.getParameter("sortBy");
            if (sortByParam != null && !sortByParam.isEmpty()) {
                sortBy = sortByParam;
            }

            String sortOrderParam = request.getParameter("sortOrder");
            if (sortOrderParam != null && !sortOrderParam.isEmpty()) {
                sortOrder = sortOrderParam;
            }

            String filterParam = request.getParameter("filter");
            if (filterParam != null && !filterParam.isEmpty()) {
                filter = filterParam;
            }
        } catch (NumberFormatException e) {
        }

        List<ImageTask> imageTasks = imageTaskDAO.getImageTasksByUserId(user.getId(), filter, sortBy, sortOrder, page, pageSize);
        int totalTasks = imageTaskDAO.countImageTasksByUserId(user.getId(), filter);
        int totalPages = (int) Math.ceil((double) totalTasks / pageSize);

        request.setAttribute("imageTasks", imageTasks);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("sortBy", sortBy);
        request.setAttribute("sortOrder", sortOrder);
        request.setAttribute("filter", filter);
        request.setAttribute("totalTasks", totalTasks);

        request.getRequestDispatcher("/WEB-INF/views/image/results.jsp").forward(request, response);
    }
}
