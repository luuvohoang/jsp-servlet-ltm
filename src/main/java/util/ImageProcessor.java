package util;

import dao.ImageTaskDAO;
import model.ImageTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Utility class for processing images
 */
public class ImageProcessor {
    
    private ImageTaskDAO imageTaskDAO;
    
    public ImageProcessor() {
        this.imageTaskDAO = new ImageTaskDAO();
    }
    
    /**
     * Process an image task
     * @param task The task to process
     * @return true if processing was successful, false otherwise
     */
    public boolean processTask(ImageTask task) {
        System.out.println("Processing task: " + task.getId());
        
        try {
            // Update task status to PROCESSING
            task.setStatus("PROCESSING");
            imageTaskDAO.update(task);
            
            // Get the input image
            File inputFile = new File(task.getImagePath());
            if (!inputFile.exists()) {
                throw new IOException("Input file not found: " + task.getImagePath());
            }
            
            BufferedImage inputImage = ImageIO.read(inputFile);
            if (inputImage == null) {
                throw new IOException("Failed to read image: " + task.getImagePath());
            }
            
            // Process the image based on the processing type
            BufferedImage outputImage = null;

            if (outputImage == null) {
                throw new IOException("Failed to process image");
            }
            
            // Generate output filename
            String inputPath = task.getImagePath();
            String outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) 

                              + inputPath.substring(inputPath.lastIndexOf('.'));
            
            // Save the processed image
            File outputFile = new File(outputPath);
            String format = outputPath.substring(outputPath.lastIndexOf('.') + 1);
            ImageIO.write(outputImage, format, outputFile);
            
            // Update task status to COMPLETED
            task.setStatus("COMPLETED");

            imageTaskDAO.update(task);
            
            return true;
        } catch (Exception e) {
            // Update task status to FAILED
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            imageTaskDAO.update(task);
            
            e.printStackTrace();
            return false;
        }
    }
    
    // Image processing methods
    
    private BufferedImage applyBlur(BufferedImage image) {
        BufferedImage output = new BufferedImage(
                image.getWidth(), image.getHeight(), image.getType());
        
        // Simple box blur implementation
        int[][] kernel = {
                {1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        };
        
        return applyConvolution(image, kernel, 9);
    }
    
    private BufferedImage applySharpen(BufferedImage image) {
        BufferedImage output = new BufferedImage(
                image.getWidth(), image.getHeight(), image.getType());
        
        // Sharpen kernel
        int[][] kernel = {
                {0, -1, 0},
                {-1, 5, -1},
                {0, -1, 0}
        };
        
        return applyConvolution(image, kernel, 1);
    }
    
    private BufferedImage applyGrayscale(BufferedImage image) {
        BufferedImage output = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        
        Graphics g = output.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return output;
    }
    
    private BufferedImage applyResize(BufferedImage image, String params) {
        // Parse width and height from params
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (params != null && !params.isEmpty()) {
            String[] parts = params.split(",");
            if (parts.length >= 2) {
                try {
                    width = Integer.parseInt(parts[0].trim());
                    height = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    // Use original dimensions if parsing fails
                }
            }
        }
        
        BufferedImage output = new BufferedImage(width, height, image.getType());
        Graphics2D g = output.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        
        return output;
    }
    
    private BufferedImage applyWatermark(BufferedImage image, String watermarkText) {
        if (watermarkText == null || watermarkText.isEmpty()) {
            watermarkText = "Watermark";
        }
        
        BufferedImage output = new BufferedImage(
                image.getWidth(), image.getHeight(), image.getType());
        
        // Copy the original image
        Graphics2D g = output.createGraphics();
        g.drawImage(image, 0, 0, null);
        
        // Add watermark
        g.setColor(new Color(255, 255, 255, 128));
        g.setFont(new Font("Arial", Font.BOLD, 36));
        
        FontMetrics metrics = g.getFontMetrics();
        int x = (image.getWidth() - metrics.stringWidth(watermarkText)) / 2;
        int y = (image.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        
        // Draw watermark text
        g.drawString(watermarkText, x, y);
        g.dispose();
        
        return output;
    }
    
    private BufferedImage applyCustomProcessing(BufferedImage image, String params) {
        // For demonstration, just apply a sepia tone effect
        BufferedImage output = new BufferedImage(
                image.getWidth(), image.getHeight(), image.getType());
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                
                // Apply sepia formula
                int newRed = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int newGreen = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int newBlue = (int) (0.272 * r + 0.534 * g + 0.131 * b);
                
                // Clamp values
                r = Math.min(255, newRed);
                g = Math.min(255, newGreen);
                b = Math.min(255, newBlue);
                
                // Set the new RGB value
                output.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        
        return output;
    }
    
    private BufferedImage applyConvolution(BufferedImage image, int[][] kernel, int divisor) {
        BufferedImage output = new BufferedImage(
                image.getWidth(), image.getHeight(), image.getType());
        
        int kernelSize = kernel.length;
        int kernelRadius = kernelSize / 2;
        
        for (int y = kernelRadius; y < image.getHeight() - kernelRadius; y++) {
            for (int x = kernelRadius; x < image.getWidth() - kernelRadius; x++) {
                int a = 0, r = 0, g = 0, b = 0;
                
                // Apply convolution
                for (int ky = 0; ky < kernelSize; ky++) {
                    for (int kx = 0; kx < kernelSize; kx++) {
                        int pixel = image.getRGB(x + kx - kernelRadius, y + ky - kernelRadius);
                        int kernelValue = kernel[ky][kx];
                        
                        a += ((pixel >> 24) & 0xff) * kernelValue;
                        r += ((pixel >> 16) & 0xff) * kernelValue;
                        g += ((pixel >> 8) & 0xff) * kernelValue;
                        b += (pixel & 0xff) * kernelValue;
                    }
                }
                
                // Normalize and clamp values
                a = Math.min(255, Math.max(0, a / divisor));
                r = Math.min(255, Math.max(0, r / divisor));
                g = Math.min(255, Math.max(0, g / divisor));
                b = Math.min(255, Math.max(0, b / divisor));
                
                // Set the new RGB value
                output.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        
        return output;
    }
}
