package model;

import java.util.Date;

/**
 * Represents an image processing task in the system.
 * This class stores all information about an image that needs processing,
 * including its metadata and current status.
 */
public class ImageTask {
    private Long id;
    private Long userId;
    private String imagePath;
    private String originalFilename;
    private String resultPath;
    private String description;
    private String customParams;
    private boolean notifyWhenComplete;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private Date submitTime;
    private Date processingTime;
    private String errorMessage;

    // Constructors
    public ImageTask() {
    }

    public ImageTask(Long userId, String imagePath) {
        this.userId = userId;
        this.imagePath = imagePath;
        this.status = "PENDING";
        this.submitTime = new Date();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCustomParams() {
        return customParams;
    }
    
    public void setCustomParams(String customParams) {
        this.customParams = customParams;
    }
    
    public boolean isNotifyWhenComplete() {
        return notifyWhenComplete;
    }
    
    public void setNotifyWhenComplete(boolean notifyWhenComplete) {
        this.notifyWhenComplete = notifyWhenComplete;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getSubmitTime() {
        return submitTime;
    }
    
    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }
    
    public Date getProcessingTime() {
        return processingTime;
    }
    
    public void setProcessingTime(Date processingTime) {
        this.processingTime = processingTime;
    }
    
    public String getResultPath() {
        return resultPath;
    }
    
    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ImageTask{" +
                "id=" + id +
                ", userId=" + userId +
                ", imagePath='" + imagePath + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
