package gov.mota.scholarship.data.remote;

/**
 * Configuration holds credentials for the Supabase backend database and storage,
 * as provided for the Ministry of Tribal Affairs (MoTA) Unified Scholarship Platform.
 */
public final class SupabaseConfig {

    private SupabaseConfig() {}

    // Public API Key (Anon / Publishable)
    public static final String SUPABASE_PUBLISHABLE_KEY = System.getProperty("SUPABASE_ANON_KEY", "YOUR_SUPABASE_ANON_KEY");

    // Secret API Key (Service Role Key)
    public static final String SUPABASE_SECRET_KEY = System.getProperty("SUPABASE_SERVICE_ROLE_KEY", "YOUR_SUPABASE_SERVICE_KEY");

    // Supabase project base URL
    // Can be customized if a custom project domain or self-hosted instance is used
    public static final String DEFAULT_SUPABASE_URL = "https://mota-scholarships.supabase.co";

    // Standard REST API endpoints
    public static final String REST_V1 = "/rest/v1";
    public static final String STORAGE_V1 = "/storage/v1";

    // Tables
    public static final String TABLE_BENEFICIARIES = "beneficiaries";
    public static final String TABLE_APPLICATIONS = "scheme_applications";
    public static final String TABLE_DOCUMENTS = "wallet_documents";
}
