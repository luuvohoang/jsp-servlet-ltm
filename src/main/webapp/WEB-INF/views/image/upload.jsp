<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Upload Image for Processing</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .preview-container {
            width: 100%;
            min-height: 200px;
            border: 2px dashed #ccc;
            border-radius: 5px;
            position: relative;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s ease;
        }
        
        .preview-container:hover {
            border-color: #0d6efd;
        }
        
        .preview-container .upload-text {
            color: #6c757d;
            text-align: center;
        }
        
        .preview-container .upload-text i {
            font-size: 3rem;
            margin-bottom: 1rem;
        }
        
        .preview-container img {
            max-width: 100%;
            max-height: 300px;
            display: none;
        }
        
        .progress-upload {
            height: 10px;
            margin-top: 10px;
            display: none;
        }
        
        .file-info {
            margin-top: 10px;
            font-size: 0.9rem;
            color: #6c757d;
            display: none;
        }
        
        .btn-primary {
            padding: 10px 30px;
        }
    </style>
</head>
<body>
<%--    <jsp:include page="/WEB-INF/layouts/header.jsp" />--%>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/results">Scam App</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link active" href="${pageContext.request.contextPath}/upload">
                            <i class="fas fa-upload me-1"></i>Upload
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/results">
                            <i class="fas fa-images me-1"></i>My Images
                        </a>
                    </li>
                </ul>
                <div class="d-flex">
                    <a class="btn btn-outline-light" href="${pageContext.request.contextPath}/login">
                        <i class="fas fa-sign-in-alt me-1"></i>Logout (use 1/1)
                    </a>
                </div>
            </div>
        </div>
    </nav>
    
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow">
                    <div class="card-header bg-primary text-white">
                        <h3 class="mb-0">Upload Image for Processing</h3>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger">
                                ${error}
                            </div>
                        </c:if>
                        
                        <form action="upload" method="post" enctype="multipart/form-data" id="uploadForm">
                            <!-- Image Upload Section -->
                            <div class="mb-4">
                                <label class="form-label">Image Upload</label>
                                <div class="preview-container" id="dropArea">
                                    <div class="upload-text">
                                        <i class="fas fa-cloud-upload-alt"></i>
                                        <p>Drag & drop your image here or click to browse</p>
                                    </div>
                                    <img id="imagePreview" alt="Preview">
                                </div>
                                <input type="file" class="form-control d-none" id="imageInput" name="image" accept="image/*" required>
                                <div class="progress progress-upload" id="uploadProgress">
                                    <div class="progress-bar" role="progressbar" style="width: 0%"></div>
                                </div>
                                <div class="file-info" id="fileInfo">
                                    <span id="fileName"></span> (<span id="fileSize"></span>)
                                    <button type="button" class="btn btn-sm btn-link text-danger" id="removeFile">Remove</button>
                                </div>
                            </div>

                                <div class="mb-3">
                                    <label for="description" class="form-label">Description (Optional)</label>
                                    <textarea class="form-control" id="description" name="description" rows="2" 
                                              placeholder="Enter a description of this processing task"></textarea>
                                </div>
                            </div>
                            
                            <div class="d-grid gap-2 d-md-flex justify-content-md-end mt-4">
                                <a href="results" class="btn btn-outline-secondary me-md-2">Cancel</a>
                                <button type="submit" class="btn btn-primary" id="submitBtn">
                                    <i class="fas fa-upload me-2"></i>Upload and Process
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        $(document).ready(function() {
            // Process type change
            $('#processingType').change(function() {
                if ($(this).val() === 'custom') {
                    $('#customParamsContainer').show();
                } else {
                    $('#customParamsContainer').hide();
                }
            });
            
            // Preview image functionality
            const dropArea = document.getElementById('dropArea');
            const imageInput = document.getElementById('imageInput');
            const imagePreview = document.getElementById('imagePreview');
            const fileInfo = document.getElementById('fileInfo');
            const fileName = document.getElementById('fileName');
            const fileSize = document.getElementById('fileSize');
            const removeFile = document.getElementById('removeFile');
            const uploadProgress = document.getElementById('uploadProgress');
            const uploadText = document.querySelector('.upload-text');
            
            // Click to browse
            dropArea.addEventListener('click', function() {
                imageInput.click();
            });
            
            // Handle drag and drop
            ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
                dropArea.addEventListener(eventName, preventDefaults, false);
            });
            
            function preventDefaults(e) {
                e.preventDefault();
                e.stopPropagation();
            }
            
            ['dragenter', 'dragover'].forEach(eventName => {
                dropArea.addEventListener(eventName, highlight, false);
            });
            
            ['dragleave', 'drop'].forEach(eventName => {
                dropArea.addEventListener(eventName, unhighlight, false);
            });
            
            function highlight() {
                dropArea.classList.add('dragover');
            }
            
            function unhighlight() {
                dropArea.classList.remove('dragover');
            }
            
            // Handle file drop
            dropArea.addEventListener('drop', handleDrop, false);
            
            function handleDrop(e) {
                const dt = e.dataTransfer;
                const files = dt.files;
                
                if (files.length > 0) {
                    handleFiles(files);
                }
            }
            
            // Handle file selection
            imageInput.addEventListener('change', function() {
                handleFiles(this.files);
            });
            
            function handleFiles(files) {
                if (files.length > 0) {
                    const file = files[0];
                    
                    // Check if file is an image
                    if (!file.type.match('image.*')) {
                        alert('Please select an image file (JPEG, PNG, GIF, etc.)');
                        return;
                    }
                    
                    // Display file info
                    fileName.textContent = file.name;
                    fileSize.textContent = formatFileSize(file.size);
                    fileInfo.style.display = 'block';
                    
                    // Show preview
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        imagePreview.src = e.target.result;
                        imagePreview.style.display = 'block';
                        uploadText.style.display = 'none';
                    };
                    reader.readAsDataURL(file);
                    
                    // Show progress (simulated)
                    uploadProgress.style.display = 'flex';
                    const progressBar = uploadProgress.querySelector('.progress-bar');
                    progressBar.style.width = '0%';
                    
                    // Simulate progress
                    let width = 0;
                    const interval = setInterval(() => {
                        width += 5;
                        progressBar.style.width = width + '%';
                        if (width >= 100) {
                            clearInterval(interval);
                            setTimeout(() => {
                                uploadProgress.style.display = 'none';
                            }, 500);
                        }
                    }, 50);
                }
            }
            
            // Remove file
            removeFile.addEventListener('click', function() {
                imageInput.value = '';
                imagePreview.src = '';
                imagePreview.style.display = 'none';
                fileInfo.style.display = 'none';
                uploadText.style.display = 'block';
                uploadProgress.style.display = 'none';
            });
            
            // Format file size
            function formatFileSize(bytes) {
                if (bytes === 0) return '0 Bytes';
                
                const k = 1024;
                const sizes = ['Bytes', 'KB', 'MB', 'GB'];
                const i = Math.floor(Math.log(bytes) / Math.log(k));
                
                return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
            }
            
            // Form submission
            $('#uploadForm').submit(function(e) {
                if (!imageInput.files.length) {
                    e.preventDefault();
                    alert('Please select an image to upload');
                    return false;
                }
                
                // Show processing message
                $('#submitBtn').prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Processing...');
                
                return true;
            });
        });
    </script>
</body>
</html>
