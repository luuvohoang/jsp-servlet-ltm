package service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.json.JSONObject;

public class AIModeratorService {
    private static final String API_URL = "http://127.0.0.1:5000/predict";

    public AIModeratorResponse moderateImage(String imagePath) throws IOException {
        try {
            byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("image", base64Image);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                return parseResponse(jsonResponse);
            }

        } catch (Exception e) {
            throw new IOException("Error calling AI service: " + e.getMessage(), e);
        }
    }

    private AIModeratorResponse parseResponse(JSONObject jsonResponse) {
        System.out.println("AI Response: " + jsonResponse.toString());
        
        try {
            AIModeratorResponse response = new AIModeratorResponse();
            response.setApproved(jsonResponse.getBoolean("is_approved"));
            response.setConfidence(jsonResponse.getDouble("confidence"));

            if (!response.isApproved()) {
                response.setReason(jsonResponse.optString("reason", "Content violated guidelines"));
            }

            return response;
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + e.getMessage());
            throw e;
        }
    }
}