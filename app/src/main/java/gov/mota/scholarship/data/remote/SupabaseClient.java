package gov.mota.scholarship.data.remote;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gov.mota.scholarship.data.model.Beneficiary;
import gov.mota.scholarship.data.model.SchemeApplication;
import gov.mota.scholarship.data.model.WalletDocument;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * REST Client for Supabase database operations.
 * Communicates with Supabase PostgREST API using standard HttpURLConnection.
 * Ensures all remote operations strictly isolate records per user/beneficiary OTR ID.
 */
public class SupabaseClient {

    private static SupabaseClient instance;
    private final String baseUrl;
    private final String apiKey;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final Gson gson;

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private SupabaseClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.executor = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient(SupabaseConfig.DEFAULT_SUPABASE_URL, SupabaseConfig.SUPABASE_PUBLISHABLE_KEY);
        }
        return instance;
    }

    public static synchronized SupabaseClient getInstance(String customUrl) {
        if (instance == null || !instance.baseUrl.equals(customUrl)) {
            instance = new SupabaseClient(customUrl, SupabaseConfig.SUPABASE_PUBLISHABLE_KEY);
        }
        return instance;
    }

    /**
     * Fetch beneficiary by OTR ID from Supabase (Strictly isolated by OTR ID).
     */
    public void fetchBeneficiary(String otrId, Callback<Beneficiary> callback) {
        executor.execute(() -> {
            try {
                String endpoint = baseUrl + SupabaseConfig.REST_V1 + "/" + SupabaseConfig.TABLE_BENEFICIARIES
                        + "?otr_id=eq." + otrId + "&select=*";
                String json = executeGet(endpoint);
                List<Beneficiary> list = gson.fromJson(json, new TypeToken<List<Beneficiary>>() {}.getType());
                Beneficiary result = (list != null && !list.isEmpty()) ? list.get(0) : null;
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Fetch applications strictly for the authenticated beneficiary OTR ID.
     */
    public void fetchApplications(String otrId, Callback<List<SchemeApplication>> callback) {
        executor.execute(() -> {
            try {
                String endpoint = baseUrl + SupabaseConfig.REST_V1 + "/" + SupabaseConfig.TABLE_APPLICATIONS
                        + "?beneficiary_otr_id=eq." + otrId + "&select=*";
                String json = executeGet(endpoint);
                List<SchemeApplication> list = gson.fromJson(json, new TypeToken<List<SchemeApplication>>() {}.getType());
                mainHandler.post(() -> callback.onSuccess(list != null ? list : new ArrayList<>()));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Fetch verified documents strictly for the authenticated beneficiary OTR ID.
     */
    public void fetchDocuments(String otrId, Callback<List<WalletDocument>> callback) {
        executor.execute(() -> {
            try {
                String endpoint = baseUrl + SupabaseConfig.REST_V1 + "/" + SupabaseConfig.TABLE_DOCUMENTS
                        + "?beneficiary_otr_id=eq." + otrId + "&select=*";
                String json = executeGet(endpoint);
                List<WalletDocument> list = gson.fromJson(json, new TypeToken<List<WalletDocument>>() {}.getType());
                mainHandler.post(() -> callback.onSuccess(list != null ? list : new ArrayList<>()));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Upsert a new application to Supabase.
     */
    public void upsertApplication(SchemeApplication app, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                String endpoint = baseUrl + SupabaseConfig.REST_V1 + "/" + SupabaseConfig.TABLE_APPLICATIONS;
                String body = gson.toJson(app);
                executePost(endpoint, body);
                mainHandler.post(() -> callback.onSuccess(true));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Upsert a document record to Supabase.
     */
    public void upsertDocument(WalletDocument doc, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                String endpoint = baseUrl + SupabaseConfig.REST_V1 + "/" + SupabaseConfig.TABLE_DOCUMENTS;
                String body = gson.toJson(doc);
                executePost(endpoint, body);
                mainHandler.post(() -> callback.onSuccess(true));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private String executeGet(String endpointUrl) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("apikey", apiKey);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        return readStream(is);
    }

    private String executePost(String endpointUrl, String jsonBody) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", apiKey);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Prefer", "resolution=merge-duplicates");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        return readStream(is);
    }

    private String readStream(InputStream is) throws Exception {
        if (is == null) return "";
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
