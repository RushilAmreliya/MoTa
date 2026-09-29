package gov.mota.scholarship.data.remote;

/**
 * Configuration for Google AI Studio / Gemini API.
 * Uses the API key provided for the Ministry of Tribal Affairs (MoTA) AI Assistant (JAGO).
 */
public final class GeminiConfig {

    private GeminiConfig() {}

    // In production, provide via BuildConfig or server proxy to avoid client-side API key exposure
    public static final String GEMINI_API_KEY = System.getProperty("GEMINI_API_KEY", "YOUR_GEMINI_API_KEY_HERE");

    public static final String GEMINI_MODEL = "gemini-1.5-flash";

    public static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
}
