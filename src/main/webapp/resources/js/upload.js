/**
 * Image Upload Processing - Main JS File
 * Handles image upload preview, drag & drop, and form validation
 */

document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const dropArea = document.getElementById('dropArea');
    const imageInput = document.getElementById('imageInput');
    const imagePreview = document.getElementById('imagePreview');
    const fileInfo = document.getElementById('fileInfo');
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');
    const removeFile = document.getElementById('removeFile');
    const uploadProgress = document.getElementById('uploadProgress');
    const uploadText = document.querySelector('.upload-text');
    const processingType = document.getElementById('processingType');
    const customParamsContainer = document.getElementById('customParamsContainer');
    const uploadForm = document.getElementById('uploadForm');
    const submitBtn = document.getElementById('submitBtn');
    
    // Show/hide custom parameters based on processing type
    if (processingType) {
        processingType.addEventListener('change', function() {
            if (this.value === 'custom') {
                customParamsContainer.style.display = 'block';
            } else {
                customParamsContainer.style.display = 'none';
            }
        });
    }
    
    // Click to browse functionality
    if (dropArea && imageInput) {
        dropArea.addEventListener('click', function() {
            imageInput.click();
        });
    }
    
    // Handle drag and drop
    if (dropArea) {
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
    }
    
    // Handle file selection
    if (imageInput) {
        imageInput.addEventListener('change', function() {
            handleFiles(this.files);
        });
    }
    
    // Process selected files
    function handleFiles(files) {
        if (!files || files.length === 0) return;
        
        const file = files[0];
        
        // Check if file is an image
        if (!file.type.match('image.*')) {
            alert('Please select an image file (JPEG, PNG, GIF, etc.)');
            return;
        }
        
        // Check file size (limit to 10MB)
        if (file.size > 10 * 1024 * 1024) {
            alert('File size too large. Please select an image less than 10MB.');
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
    
    // Remove file
    if (removeFile) {
        removeFile.addEventListener('click', function() {
            imageInput.value = '';
            imagePreview.src = '';
            imagePreview.style.display = 'none';
            fileInfo.style.display = 'none';
            uploadText.style.display = 'block';
            uploadProgress.style.display = 'none';
        });
    }
    
    // Format file size
    function formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
    
    // Form validation on submit
    if (uploadForm) {
        uploadForm.addEventListener('submit', function(e) {
            if (!imageInput.files.length) {
                e.preventDefault();
                alert('Please select an image to upload');
                return false;
            }
            
            if (!processingType.value) {
                e.preventDefault();
                alert('Please select a processing type');
                return false;
            }
            
            // Show processing message
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Processing...';
            
            return true;
        });
    }
});
