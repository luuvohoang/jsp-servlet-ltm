package util;

import model.ImageTask;
import dao.ImageTaskDAO;
import service.AIModeratorResponse;
import service.AIModeratorService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class QueueUtil {
    private static final BlockingQueue<ImageTask> moderationQueue = new LinkedBlockingQueue<>();
    private static final int THREAD_POOL_SIZE = 5;
    private final ExecutorService executorService;
    private final ImageTaskDAO imageTaskDAO;
    private final AIModeratorService aiService;
    private final String approvedPath;

    public QueueUtil(String approvedPath) {
        this.imageTaskDAO = new ImageTaskDAO();
        this.aiService = new AIModeratorService(); // Giả sử có service này
        this.approvedPath = approvedPath;

        // Tạo thư mục approved nếu chưa tồn tại
        new File(approvedPath).mkdirs();

        // Khởi động worker thread để xử lý queue
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        startQueueProcessor();
    }

    private void startQueueProcessor() {
        // Xử lý queue với multiple threads
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            Thread worker = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        ImageTask task = moderationQueue.poll(1, TimeUnit.SECONDS);
                        if (task != null) {
                            CompletableFuture.runAsync(() -> {
                                processImageWithAI(task);
                            }, executorService);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            worker.setDaemon(true);
            worker.start();
        }
    }

    public void enqueue(ImageTask task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        moderationQueue.offer(task);
    }

    private void processImageWithAI(ImageTask task) {
        try {
            task.setStatus("PROCESSING");
            imageTaskDAO.update(task);

            // Gọi AI service
            AIModeratorResponse result = aiService.moderateImage(task.getImagePath());

            // Xử lý kết quả
            if (!result.isApproved()) {
                System.out.println("oke");
                // Sửa: Lấy đường dẫn đầy đủ từ imagePath thay vì chỉ lấy filename
                Path originalPath = Paths.get(task.getImagePath());
                handleApprovedImage(task, originalPath);
            } else {
                System.out.println("not oke");
                Path imagePath = Paths.get(task.getImagePath());
                handleRejectedImage(task, imagePath, result.getReason());
            }

            // Cập nhật confidence score
            task.setCustomParams("confidence=" + result.getConfidence());
            imageTaskDAO.update(task);

        } catch (Exception e) {
            handleProcessingError(task, e);
        }
    }

    // Signature đã được sửa để khớp với cách gọi
    private void handleApprovedImage(ImageTask task, Path originalPath) throws IOException {
        if (!Files.exists(originalPath)) {
            throw new IOException("Original file not found: " + Paths.get(task.getOriginalFilename()));
        }

        String filename = originalPath.getFileName().toString();
        Path approvedImagePath = Paths.get(approvedPath, filename);

        Files.createDirectories(Paths.get(approvedPath));

        Files.move(originalPath, approvedImagePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File moved successfully to: " + approvedImagePath);

        task.setStatus("COMPLETED");
        task.setResultPath(approvedImagePath.toString());
        imageTaskDAO.update(task);
    }

    // Signature đã được sửa để khớp với cách gọi
    private void handleRejectedImage(ImageTask task, Path imagePath, String reason) {
        try {
            // Xóa file vi phạm
//            Files.deleteIfExists(imagePath);

            // Cập nhật task
            task.setStatus("FAILED");
            task.setErrorMessage(reason);
            imageTaskDAO.update(task);
        } catch (Exception e) {
            handleProcessingError(task, e);
        }
    }

    private void handleProcessingError(ImageTask task, Exception e) {
        System.out.println("Error processing task: " + task.getId());
        System.out.println("Error message: " + e.getMessage());
        System.out.println("Error details:");
        e.printStackTrace();

        task.setStatus("FAILED");
        task.setErrorMessage(e.getMessage());
        imageTaskDAO.update(task);
    }

    public int getQueueSize() {
        return moderationQueue.size();
    }

    public boolean isQueueEmpty() {
        return moderationQueue.isEmpty();
    }
}