package gov.mota.scholarship.data.remote;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * REST Client for Google AI Studio (Gemini 1.5 Flash).
 * Used for automated verification assistance, scheme guidance, and multilingual student queries (JAGO).
 */
public class GeminiClient {

    private static GeminiClient instance;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface GeminiCallback {
        void onSuccess(String responseText);
        void onError(Exception e);
    }

    private GeminiClient() {
        this.executor = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized GeminiClient getInstance() {
        if (instance == null) {
            instance = new GeminiClient();
        }
        return instance;
    }

    /**
     * Send a prompt to Google AI Studio Gemini API.
     *
     * @param systemInstruction Context or system instructions
     * @param userPrompt The user's query or text to verify
     * @param callback Result callback on Main/UI Thread
     */
    public void generateResponse(String systemInstruction, String userPrompt, GeminiCallback callback) {
        executor.execute(() -> {
            try {
                String endpoint = GeminiConfig.GEMINI_BASE_URL + GeminiConfig.GEMINI_MODEL
                        + ":generateContent?key=" + GeminiConfig.GEMINI_API_KEY;

                JSONObject root = new JSONObject();
                
                // Add system instructions if present
                if (systemInstruction != null && !systemInstruction.trim().isEmpty()) {
                    JSONObject systemPart = new JSONObject().put("text", systemInstruction);
                    JSONObject systemContent = new JSONObject().put("parts", new JSONArray().put(systemPart));
                    root.put("system_instruction", systemContent);
                }

                // Add user prompt contents
                JSONObject userPart = new JSONObject().put("text", userPrompt);
                JSONObject userContent = new JSONObject()
                        .put("role", "user")
                        .put("parts", new JSONArray().put(userPart));

                root.put("contents", new JSONArray().put(userContent));

                String responseJson = postRequest(endpoint, root.toString());

                // Parse response
                JSONObject respObj = new JSONObject(responseJson);
                JSONArray candidates = respObj.optJSONArray("candidates");
                String reply = "";
                if (candidates != null && candidates.length() > 0) {
                    JSONObject firstCand = candidates.getJSONObject(0);
                    JSONObject content = firstCand.optJSONObject("content");
                    if (content != null) {
                        JSONArray parts = content.optJSONArray("parts");
                        if (parts != null && parts.length() > 0) {
                            reply = parts.getJSONObject(0).optString("text", "");
                        }
                    }
                }

                String finalReply = reply;
                mainHandler.post(() -> callback.onSuccess(finalReply));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private String postRequest(String endpointUrl, String jsonBody) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
