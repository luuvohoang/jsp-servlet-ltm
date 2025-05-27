<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Image Processing Results</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .card-image {
            position: relative;
            overflow: hidden;
            height: 200px;
            background-color: #f8f9fa;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .card-image img {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
        }
        
        .card {
            transition: transform 0.3s, box-shadow 0.2s;
        }
        
        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 20px rgba(0,0,0,0.1);
        }
        
        .badge-status {
            position: absolute;
            top: 10px;
            right: 10px;
        }

        
        .img-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #6c757d;
        }
        
        .img-placeholder i {
            font-size: 48px;
        }
        
        .btn-outline-light:hover {
            color: #fff;
        }
        
        /* Filter controls */
        .filter-controls {
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/results">Scam App</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/upload">
                            <i class="fas fa-upload me-1"></i>Upload
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link active" href="${pageContext.request.contextPath}/results">
                            <i class="fas fa-images me-1"></i>My Images
                        </a>
                    </li>
                </ul>
                <div class="d-flex">
                    <span class="navbar-text me-3 text-white">
                        <i class="fas fa-user me-1"></i>${user.username}
                    </span>
                    <a class="btn btn-outline-light" href="${pageContext.request.contextPath}/login">
                        <i class="fas fa-sign-out-alt me-1"></i>Logout
                    </a>
                </div>
            </div>
        </div>
    </nav>
    
    <div class="container mt-5">
        <div class="row justify-content-between align-items-center mb-4">
            <div class="col-md-6">
                <h2><i class="fas fa-images me-2"></i>My Processed Images</h2>
                <p class="text-muted">Showing ${imageTasks.size()} of ${totalTasks} image processing tasks</p>
            </div>
            <div class="col-md-6 text-md-end">
                <a href="${pageContext.request.contextPath}/upload" class="btn btn-primary">
                    <i class="fas fa-plus-circle me-2"></i>Upload New Image
                </a>
            </div>
        </div>
        
        <!-- Filter and Sort Controls -->
        <div class="filter-controls shadow-sm">
            <form action="${pageContext.request.contextPath}/results" method="get" id="filterForm">
                <div class="row align-items-center">
                    <div class="col-md-3 mb-2 mb-md-0">
                        <label class="form-label mb-1">Filter by Status</label>
                        <select class="form-select" name="filter" id="statusFilter">
                            <option value="all" ${filter == 'all' ? 'selected' : ''}>All Images</option>
                            <option value="completed" ${filter == 'completed' ? 'selected' : ''}>Completed</option>
                            <option value="processing" ${filter == 'processing' ? 'selected' : ''}>Processing</option>
                            <option value="failed" ${filter == 'failed' ? 'selected' : ''}>Failed</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-2 mb-md-0">
                        <label class="form-label mb-1">Sort By</label>
                        <select class="form-select" name="sortBy" id="sortBy">
                            <option value="submitTime" ${sortBy == 'submitTime' ? 'selected' : ''}>Date Uploaded</option>
                            <option value="status" ${sortBy == 'status' ? 'selected' : ''}>Status</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-2 mb-md-0">
                        <label class="form-label mb-1">Order</label>
                        <select class="form-select" name="sortOrder" id="sortOrder">
                            <option value="desc" ${sortOrder == 'desc' ? 'selected' : ''}>Newest First</option>
                            <option value="asc" ${sortOrder == 'asc' ? 'selected' : ''}>Oldest First</option>
                        </select>
                    </div>
                    <div class="col-md-3 text-md-end mt-md-4">
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-filter me-1"></i>Apply Filters
                        </button>
                        <a href="${pageContext.request.contextPath}/results" class="btn btn-outline-secondary">
                            <i class="fas fa-redo me-1"></i>Reset
                        </a>
                    </div>
                </div>
            </form>
        </div>
        
        <!-- Results Display -->
        <c:choose>
            <c:when test="${empty imageTasks}">
                <div class="text-center py-5 my-5">
                    <div class="display-1 text-muted">
                        <i class="far fa-images"></i>
                    </div>
                    <h3 class="mt-4">No Images Found</h3>
                    <p class="text-muted">You haven't uploaded any images yet or no images match your filters.</p>
                    <a href="${pageContext.request.contextPath}/upload" class="btn btn-primary mt-3">
                        <i class="fas fa-upload me-2"></i>Upload Your First Image
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4 mb-4">
                    <c:forEach var="task" items="${imageTasks}">
                        <div class="col">
                            <div class="card h-100 shadow-sm">
                                <div class="card-image">
                                    <c:choose>
                                        <c:when test="${not empty task.resultPath}">
                                            <img src="${pageContext.request.contextPath}/uploads/${task.originalFilename}"
                                                 alt="Image result" class="img-fluid">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="img-placeholder">
<%--                                                <i class="fas fa-image"></i>--%>
                                                <img src="${pageContext.request.contextPath}/uploads/${task.originalFilename}"
                                                     alt="Image result" class="img-fluid">
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                    <c:choose>
                                        <c:when test="${task.status eq 'COMPLETED'}">
                                            <span class="badge bg-success badge-status">Completed</span>
                                        </c:when>
                                        <c:when test="${task.status eq 'PROCESSING'}">
                                            <span class="badge bg-warning badge-status">Processing</span>
                                        </c:when>
                                        <c:when test="${task.status eq 'FAILED'}">
                                            <span class="badge bg-danger badge-status">Failed</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary badge-status">Pending</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="card-body">
                                    <h5 class="card-title text-truncate">
                                        ${not empty task.originalFilename ? task.originalFilename : 'Untitled Image'}
                                    </h5>
                                    <p class="card-text">
                                        <small class="text-muted">
                                            <fmt:formatDate value="${task.submitTime}" pattern="dd/MM/yyyy HH:mm" />
                                        </small>
                                    </p>
                                    <c:if test="${not empty task.description}">
                                        <p class="card-text small text-truncate">${task.description}</p>
                                    </c:if>
                                </div>
                                <div class="card-footer bg-transparent">
                                    <div class="d-flex justify-content-between align-items-center">
<%--                                        <div class="btn-group">--%>
<%--                                            <c:if test="${task.status eq 'COMPLETED'}">--%>
<%--                                                <small class="text-muted">#${task.id}</small>--%>
<%--                                            </c:if>--%>
<%--                                        </div>--%>
<%--                                        <small class="text-muted">#${task.id}</small>--%>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
                
                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <nav aria-label="Page navigation" class="mt-4">
                        <ul class="pagination justify-content-center">
                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/results?page=${currentPage - 1}&filter=${filter}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                    <i class="fas fa-chevron-left"></i> Previous
                                </a>
                            </li>
                            
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <c:choose>
                                    <c:when test="${currentPage == i}">
                                        <li class="page-item active">
                                            <span class="page-link">${i}</span>
                                        </li>
                                    </c:when>
                                    <c:otherwise>
                                        <li class="page-item">
                                            <a class="page-link" href="${pageContext.request.contextPath}/results?page=${i}&filter=${filter}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                                ${i}
                                            </a>
                                        </li>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            
                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/results?page=${currentPage + 1}&filter=${filter}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                    Next <i class="fas fa-chevron-right"></i>
                                </a>
                            </li>
                        </ul>
                    </nav>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
    
    <footer class="bg-light py-4 mt-5">
        <div class="container text-center">
            <p class="text-muted mb-0">© 2025 Scam </p>
        </div>
    </footer>
    
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        $(document).ready(function() {
            $('#statusFilter, #sortBy, #sortOrder').change(function() {
                $('#filterForm').submit();
            });
        });
    </script>
</body>
</html>
