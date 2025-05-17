package util;

import model.ImageTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;

/**
 * Utility class for managing the image processing queue
 */
public class QueueUtil {
    
    // Queue for normal processing tasks
    private static final BlockingQueue<ImageTask> taskQueue = new LinkedBlockingQueue<>();
    
    // Priority queue for prioritized tasks (high, normal, low)
    private static final BlockingQueue<ImageTask> priorityTaskQueue = new PriorityBlockingQueue<>(
            100, Comparator.comparing(task -> {

                        return 1;

            }));
    
    // Flag to indicate which queue to use
    private static final boolean USE_PRIORITY_QUEUE = true;
    
    /**
     * Add a task to the processing queue
     * @param task The task to add
     */
    public void enqueue(ImageTask task) {
        if (USE_PRIORITY_QUEUE) {
            priorityTaskQueue.offer(task);
        } else {
            taskQueue.offer(task);
        }
        System.out.println("Task added to queue: " + task.getId());
    }
    
    /**
     * Take the next task from the queue (blocking operation)
     * @return The next task to process
     * @throws InterruptedException if interrupted while waiting
     */
    public ImageTask dequeue() throws InterruptedException {
        if (USE_PRIORITY_QUEUE) {
            return priorityTaskQueue.take();
        } else {
            return taskQueue.take();
        }
    }
    
    /**
     * Get the current size of the queue
     * @return The number of tasks in the queue
     */
    public int getQueueSize() {
        if (USE_PRIORITY_QUEUE) {
            return priorityTaskQueue.size();
        } else {
            return taskQueue.size();
        }
    }
    
    /**
     * Check if the queue is empty
     * @return true if the queue is empty, false otherwise
     */
    public boolean isQueueEmpty() {
        if (USE_PRIORITY_QUEUE) {
            return priorityTaskQueue.isEmpty();
        } else {
            return taskQueue.isEmpty();
        }
    }
    
    /**
     * Clear all tasks from the queue
     */
    public void clearQueue() {
        if (USE_PRIORITY_QUEUE) {
            priorityTaskQueue.clear();
        } else {
            taskQueue.clear();
        }
    }
}
