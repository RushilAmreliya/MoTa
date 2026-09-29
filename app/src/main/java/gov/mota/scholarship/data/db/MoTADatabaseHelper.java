package gov.mota.scholarship.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import gov.mota.scholarship.data.model.Beneficiary;
import gov.mota.scholarship.data.model.SchemeApplication;
import gov.mota.scholarship.data.model.WalletDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoTADatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mota_scholarship_central.db";
    private static final int DATABASE_VERSION = 4;

    // Table: Beneficiaries
    public static final String TABLE_BENEFICIARIES = "beneficiaries";
    public static final String COL_BEN_OTR_ID = "otr_id";
    public static final String COL_BEN_FULL_NAME = "full_name";
    public static final String COL_BEN_AADHAAR = "aadhaar_number";
    public static final String COL_BEN_DOB = "dob";
    public static final String COL_BEN_GENDER = "gender";
    public static final String COL_BEN_CATEGORY = "category";
    public static final String COL_BEN_IS_PVTG = "is_pvtg";
    public static final String COL_BEN_PVTG_NAME = "pvtg_community_name";
    public static final String COL_BEN_HOUSEHOLD_ID = "household_id";
    public static final String COL_BEN_APAAR_ID = "apaar_id";
    public static final String COL_BEN_MOBILE = "mobile_number";
    public static final String COL_BEN_STATE = "state";
    public static final String COL_BEN_DISTRICT = "district";
    public static final String COL_BEN_INST_NAME = "institution_name";
    public static final String COL_BEN_AISHE_CODE = "aishe_udise_code";
    public static final String COL_BEN_BANK_NAME = "bank_name";
    public static final String COL_BEN_ACCOUNT_MASKED = "account_masked";
    public static final String COL_BEN_NPCI_MAPPED = "npci_mapped";
    public static final String COL_BEN_NPCI_REASON = "npci_failure_reason";
    public static final String COL_BEN_NPCI_REMEDY = "npci_remediation";

    // Table: Applications
    public static final String TABLE_APPLICATIONS = "scheme_applications";
    public static final String COL_APP_ID = "application_id";
    public static final String COL_APP_OTR_ID = "beneficiary_otr_id";
    public static final String COL_APP_SCHEME_CODE = "scheme_code";
    public static final String COL_APP_SCHEME_TITLE = "scheme_title";
    public static final String COL_APP_ACADEMIC_YEAR = "academic_year";
    public static final String COL_APP_SOURCE_SYSTEM = "source_system";
    public static final String COL_APP_STAGE = "stage";
    public static final String COL_APP_STATUS = "status";
    public static final String COL_APP_BADGES = "verification_badges";
    public static final String COL_APP_DBT_STATUS = "dbt_status";
    public static final String COL_APP_AMOUNT = "amount_inr";
    public static final String COL_APP_PFMS_TXN = "pfms_transaction_id";
    public static final String COL_APP_REMARKS = "remarks";

    // Table: Uploaded Documents (Zero-Upload & Manual Verification Registry)
    public static final String TABLE_DOCUMENTS = "wallet_documents";
    public static final String COL_DOC_ID = "doc_id";
    public static final String COL_DOC_OTR_ID = "beneficiary_otr_id";
    public static final String COL_DOC_TITLE = "title";
    public static final String COL_DOC_AUTHORITY = "authority";
    public static final String COL_DOC_NUMBER = "doc_number";
    public static final String COL_DOC_STATUS = "status";
    public static final String COL_DOC_ISSUE_DATE = "issue_date";
    public static final String COL_DOC_IS_REUSED = "is_reused";
    public static final String COL_DOC_FILE_ATTACHED = "file_attached"; // 1 = real file uploaded, 0 = cert number only
    public static final String COL_DOC_LOCAL_PATH = "local_file_path";  // Private in-app storage path
    public static final String COL_DOC_MIME_TYPE = "mime_type";        // e.g. application/pdf, image/jpeg

    public MoTADatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createBeneficiaries = "CREATE TABLE " + TABLE_BENEFICIARIES + " (" +
                COL_BEN_OTR_ID + " TEXT PRIMARY KEY, " +
                COL_BEN_FULL_NAME + " TEXT, " +
                COL_BEN_AADHAAR + " TEXT UNIQUE, " +
                COL_BEN_DOB + " TEXT, " +
                COL_BEN_GENDER + " TEXT, " +
                COL_BEN_CATEGORY + " TEXT, " +
                COL_BEN_IS_PVTG + " INTEGER, " +
                COL_BEN_PVTG_NAME + " TEXT, " +
                COL_BEN_HOUSEHOLD_ID + " TEXT, " +
                COL_BEN_APAAR_ID + " TEXT, " +
                COL_BEN_MOBILE + " TEXT, " +
                COL_BEN_STATE + " TEXT, " +
                COL_BEN_DISTRICT + " TEXT, " +
                COL_BEN_INST_NAME + " TEXT, " +
                COL_BEN_AISHE_CODE + " TEXT, " +
                COL_BEN_BANK_NAME + " TEXT, " +
                COL_BEN_ACCOUNT_MASKED + " TEXT, " +
                COL_BEN_NPCI_MAPPED + " INTEGER, " +
                COL_BEN_NPCI_REASON + " TEXT, " +
                COL_BEN_NPCI_REMEDY + " TEXT);";

        String createApplications = "CREATE TABLE " + TABLE_APPLICATIONS + " (" +
                COL_APP_ID + " TEXT PRIMARY KEY, " +
                COL_APP_OTR_ID + " TEXT, " +
                COL_APP_SCHEME_CODE + " TEXT, " +
                COL_APP_SCHEME_TITLE + " TEXT, " +
                COL_APP_ACADEMIC_YEAR + " TEXT, " +
                COL_APP_SOURCE_SYSTEM + " TEXT, " +
                COL_APP_STAGE + " TEXT, " +
                COL_APP_STATUS + " TEXT, " +
                COL_APP_BADGES + " TEXT, " +
                COL_APP_DBT_STATUS + " TEXT, " +
                COL_APP_AMOUNT + " TEXT, " +
                COL_APP_PFMS_TXN + " TEXT, " +
                COL_APP_REMARKS + " TEXT);";

        String createDocuments = "CREATE TABLE " + TABLE_DOCUMENTS + " (" +
                COL_DOC_ID + " TEXT PRIMARY KEY, " +
                COL_DOC_OTR_ID + " TEXT, " +
                COL_DOC_TITLE + " TEXT, " +
                COL_DOC_AUTHORITY + " TEXT, " +
                COL_DOC_NUMBER + " TEXT, " +
                COL_DOC_STATUS + " TEXT, " +
                COL_DOC_ISSUE_DATE + " TEXT, " +
                COL_DOC_IS_REUSED + " INTEGER, " +
                COL_DOC_FILE_ATTACHED + " INTEGER DEFAULT 1, " +
                COL_DOC_LOCAL_PATH + " TEXT, " +
                COL_DOC_MIME_TYPE + " TEXT);";

        db.execSQL(createBeneficiaries);
        db.execSQL(createApplications);
        db.execSQL(createDocuments);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPLICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BENEFICIARIES);
        onCreate(db);
    }

    public boolean isDatabasePopulated() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BENEFICIARIES, null);
        boolean hasData = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                hasData = cursor.getInt(0) > 0;
            }
            cursor.close();
        }
        return hasData;
    }

    public void insertBeneficiary(Beneficiary b) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BEN_OTR_ID, b.getOtrId());
        cv.put(COL_BEN_FULL_NAME, b.getFullName());
        cv.put(COL_BEN_AADHAAR, b.getAadhaarNumber());
        cv.put(COL_BEN_DOB, b.getDob());
        cv.put(COL_BEN_GENDER, b.getGender());
        cv.put(COL_BEN_CATEGORY, b.getCategory());
        cv.put(COL_BEN_IS_PVTG, b.isPvtg() ? 1 : 0);
        cv.put(COL_BEN_PVTG_NAME, b.getPvtgCommunityName());
        cv.put(COL_BEN_HOUSEHOLD_ID, b.getHouseholdId());
        cv.put(COL_BEN_APAAR_ID, b.getApaarId());
        cv.put(COL_BEN_MOBILE, b.getMobileNumber());
        cv.put(COL_BEN_STATE, b.getState());
        cv.put(COL_BEN_DISTRICT, b.getDistrict());
        cv.put(COL_BEN_INST_NAME, b.getInstitutionName());
        cv.put(COL_BEN_AISHE_CODE, b.getAisheUdiseCode());
        cv.put(COL_BEN_BANK_NAME, b.getBankName());
        cv.put(COL_BEN_ACCOUNT_MASKED, b.getAccountMasked());
        cv.put(COL_BEN_NPCI_MAPPED, b.isNpciMapped() ? 1 : 0);
        cv.put(COL_BEN_NPCI_REASON, b.getNpciFailureReason());
        cv.put(COL_BEN_NPCI_REMEDY, b.getNpciRemediation());

        db.insertWithOnConflict(TABLE_BENEFICIARIES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        if (b.getApplications() != null) {
            for (SchemeApplication app : b.getApplications()) {
                insertApplication(b.getOtrId(), app);
            }
        }

        if (b.getDocuments() != null) {
            for (WalletDocument doc : b.getDocuments()) {
                insertDocument(b.getOtrId(), doc);
            }
        }
    }

    public void insertApplication(String otrId, SchemeApplication app) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_APP_ID, app.getApplicationId());
        cv.put(COL_APP_OTR_ID, otrId);
        cv.put(COL_APP_SCHEME_CODE, app.getSchemeCode());
        cv.put(COL_APP_SCHEME_TITLE, app.getSchemeTitle());
        cv.put(COL_APP_ACADEMIC_YEAR, app.getAcademicYear());
        cv.put(COL_APP_SOURCE_SYSTEM, app.getSourceSystem());
        cv.put(COL_APP_STAGE, app.getStage());
        cv.put(COL_APP_STATUS, app.getStatus());
        
        String badgesStr = "";
        if (app.getVerificationBadges() != null && !app.getVerificationBadges().isEmpty()) {
            badgesStr = String.join("||", app.getVerificationBadges());
        }
        cv.put(COL_APP_BADGES, badgesStr);
        cv.put(COL_APP_DBT_STATUS, app.getDbtStatus());
        cv.put(COL_APP_AMOUNT, app.getAmountInr());
        cv.put(COL_APP_PFMS_TXN, app.getPfmsTransactionId());
        cv.put(COL_APP_REMARKS, app.getRemarks());

        db.insertWithOnConflict(TABLE_APPLICATIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void insertDocument(String otrId, WalletDocument doc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DOC_ID, doc.getDocId());
        cv.put(COL_DOC_OTR_ID, otrId);
        cv.put(COL_DOC_TITLE, doc.getTitle());
        cv.put(COL_DOC_AUTHORITY, doc.getAuthority());
        cv.put(COL_DOC_NUMBER, doc.getDocNumber());
        cv.put(COL_DOC_STATUS, doc.getStatus());
        cv.put(COL_DOC_ISSUE_DATE, doc.getIssueDate());
        cv.put(COL_DOC_IS_REUSED, doc.isReused() ? 1 : 0);
        // file_attached = 1 means a real file was uploaded (not just a cert number)
        cv.put(COL_DOC_FILE_ATTACHED, doc.isFileAttached() ? 1 : 0);
        cv.put(COL_DOC_LOCAL_PATH, doc.getLocalFilePath() != null ? doc.getLocalFilePath() : "");
        cv.put(COL_DOC_MIME_TYPE, doc.getMimeType() != null ? doc.getMimeType() : "application/pdf");

        db.insertWithOnConflict(TABLE_DOCUMENTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<SchemeApplication> getApplicationsForBeneficiary(String otrId) {
        List<SchemeApplication> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_APPLICATIONS, null, COL_APP_OTR_ID + "=?", new String[]{otrId}, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                String badgesRaw = c.getString(c.getColumnIndexOrThrow(COL_APP_BADGES));
                List<String> badges = new ArrayList<>();
                if (badgesRaw != null && !badgesRaw.isEmpty()) {
                    badges = new ArrayList<>(Arrays.asList(badgesRaw.split("\\|\\|")));
                }

                SchemeApplication app = new SchemeApplication(
                        c.getString(c.getColumnIndexOrThrow(COL_APP_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_APP_SCHEME_CODE)),
                        c.getString(c.getColumnIndexOrThrow(COL_APP_SCHEME_TITLE)),
                        c.getString(c.getColumnIndexOrThrow(COL_APP_ACADEMIC_YEAR)),
                        c.getString(c.getColumnIndexOrThrow(COL_APP_SOURCE_SYSTEM)),
                        c.getString(c.getColumnIndexOrThrow(COL_APP_STAGE)),
                        c.getString(c.getColumnIndexOrThrow(COL_APP_STATUS)),
                        badges,
                        c.getString(c.getColumnIndexOrThrow(COL_APP_DBT_STATUS)),
                        c.getString(c.getColumnIndexOrThrow(COL_APP_AMOUNT)),
                        c.getString(c.getColumnIndexOrThrow(COL_APP_PFMS_TXN)),
                        c.getString(c.getColumnIndexOrThrow(COL_APP_REMARKS))
                );
                list.add(app);
            }
            c.close();
        }
        return list;
    }

    public List<WalletDocument> getDocumentsForBeneficiary(String otrId) {
        List<WalletDocument> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Only return documents where a real file was actually uploaded (file_attached = 1)
        Cursor c = db.query(TABLE_DOCUMENTS, null,
                COL_DOC_OTR_ID + "=? AND " + COL_DOC_FILE_ATTACHED + "=1",
                new String[]{otrId}, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                String localPath = c.getString(c.getColumnIndexOrThrow(COL_DOC_LOCAL_PATH));
                String mimeType = c.getString(c.getColumnIndexOrThrow(COL_DOC_MIME_TYPE));
                WalletDocument doc = new WalletDocument(
                        c.getString(c.getColumnIndexOrThrow(COL_DOC_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_DOC_TITLE)),
                        c.getString(c.getColumnIndexOrThrow(COL_DOC_AUTHORITY)),
                        c.getString(c.getColumnIndexOrThrow(COL_DOC_NUMBER)),
                        c.getString(c.getColumnIndexOrThrow(COL_DOC_STATUS)),
                        c.getString(c.getColumnIndexOrThrow(COL_DOC_ISSUE_DATE)),
                        c.getInt(c.getColumnIndexOrThrow(COL_DOC_IS_REUSED)) == 1,
                        true, // file_attached = 1 means real file
                        localPath,
                        mimeType
                );
                list.add(doc);
            }
            c.close();
        }
        return list;
    }

    public Beneficiary getBeneficiaryByOtr(String otrId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_BENEFICIARIES, null, COL_BEN_OTR_ID + "=?", new String[]{otrId}, null, null, null);
        if (c != null && c.moveToFirst()) {
            Beneficiary b = cursorToBeneficiary(c);
            c.close();
            return b;
        }
        if (c != null) c.close();
        return null;
    }

    public Beneficiary authenticateBeneficiary(String name, String cleanAadhaar) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_BENEFICIARIES, null, COL_BEN_AADHAAR + "=?", new String[]{cleanAadhaar}, null, null, null);
        if (c != null && c.moveToFirst()) {
            String dbName = c.getString(c.getColumnIndexOrThrow(COL_BEN_FULL_NAME));
            boolean nameMatches = false;
            if (dbName != null && name != null) {
                String trimmedInput = name.trim();
                String trimmedDb = dbName.trim();
                if (trimmedDb.equalsIgnoreCase(trimmedInput)) {
                    nameMatches = true;
                } else {
                    // Check against any known regional language translation for this beneficiary
                    String[] supportedLangs = {"hi", "or", "bn", "te", "gu", "mr", "sat"};
                    for (String l : supportedLangs) {
                        String translated = gov.mota.scholarship.util.LocalizationUtils.getLocalizedBeneficiaryName(
                                new android.view.ContextThemeWrapper(
                                        gov.mota.scholarship.util.LocaleHelper.setLocale(db.getPath() != null ? this.getReadableDatabase().getPath().length() > 0 ? null : null : null, l) != null ? null : null, 0
                                ), trimmedDb);
                    }
                    // Direct multi-script matching for all seeded demo users
                    if (isLocalizedMatch(trimmedDb, trimmedInput)) {
                        nameMatches = true;
                    }
                }
            }
            if (nameMatches) {
                Beneficiary b = cursorToBeneficiary(c);
                c.close();
                return b;
            }
        }
        if (c != null) c.close();
        return null;
    }

    private boolean isLocalizedMatch(String dbName, String input) {
        if (dbName == null || input == null) return false;
        String d = dbName.trim().toLowerCase();
        String inp = input.trim();

        if (d.contains("kavita")) {
            return inp.equals("Kavita Marandi") || inp.equals("कविता मरांडी") || inp.equals("କବିତା ମାରାଣ୍ଡି") ||
                   inp.equals("কবিতা মারান্ডি") || inp.equals("కవిత మరాండి") || inp.equals("કવિતા મરાંડી") ||
                   inp.equals("ᱠᱚᱵᱤᱛᱟ ᱢᱟᱨᱟᱱᱰᱤ");
        } else if (d.contains("birsa")) {
            return inp.equals("Birsa Munda") || inp.equals("बिरसा मुंडा") || inp.equals("ବିର୍ସା ମୁଣ୍ଡା") ||
                   inp.equals("বিরসা মুন্ডা") || inp.equals("బిర్సా ముండా") || inp.equals("બિરસા મુંડા") ||
                   inp.equals("ᱵᱤᱨᱥᱟ ᱢᱩᱱᱰᱟ");
        } else if (d.contains("salomi")) {
            return inp.equals("Salomi Munda") || inp.equals("सलोमी मुंडा") || inp.equals("ସାଲୋମି ମୁଣ୍ଡା") ||
                   inp.equals("সালোমি মুন্ডা") || inp.equals("సలోమి ముండా") || inp.equals("સલોમી મુંડા") ||
                   inp.equals("ᱥᱟᱞᱳᱢᱤ ᱢᱩᱱᱰᱟ");
        } else if (d.contains("sunita")) {
            return inp.equals("Sunita Marandi") || inp.equals("सुनीता मरांडी") || inp.equals("ସୁନୀତା ମାରାଣ୍ଡି") ||
                   inp.equals("সুনীতা মারান্ডি") || inp.equals("సునీత మరాండి") || inp.equals("સુનીતા મરાંડી") ||
                   inp.equals("ᱥᱩᱱᱤᱛᱟ ᱢᱟᱨᱟᱱᱰᱤ");
        } else if (d.contains("kiran")) {
            return inp.equals("Kiran Kumar Jamatia") || inp.equals("किरण कुमार जमातिया") || inp.equals("କିରଣ କୁମାର ଜମାତିଆ") ||
                   inp.equals("কিরণ কুমার জমাতিয়া") || inp.equals("కిరణ్ కుమార్ జమాతియా") || inp.equals("કિરણ કુમાર જમાતિયા") ||
                   inp.equals("ᱠᱤᱨᱚᱱ ᱠᱩᱢᱟᱨ ᱡᱟᱢᱟᱛᱤᱭᱟ");
        } else if (d.contains("anjali")) {
            return inp.equals("Anjali Gond") || inp.equals("अंजलि गोंड") || inp.equals("ଅଞ୍ଜଳି ଗୋଣ୍ଡ") ||
                   inp.equals("অঞ্জলি গোন্ড") || inp.equals("అంజలి గోండ్") || inp.equals("અંજલિ ગોંડ") ||
                   inp.equals("ᱚᱧᱡᱚᱞᱤ ᱜᱳᱱᱰ") || inp.equals("अंजली गोंड");
        } else if (d.contains("rameshwar")) {
            return inp.equals("Rameshwar Uraon") || inp.equals("रामेश्वर उरांव") || inp.equals("ରାମେଶ୍ୱର ଉରାଓଁ") ||
                   inp.equals("রামেশ্বর উরাওঁ") || inp.equals("రామేశ్వర్ ఉరాన్") || inp.equals("રામેશ્વર ઉરાંવ") ||
                   inp.equals("ᱨᱟᱢᱮᱥᱣᱚᱨ ᱩᱨᱟᱶ");
        }
        return false;
    }

    public List<Beneficiary> getAllBeneficiaries() {
        List<Beneficiary> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_BENEFICIARIES, null, null, null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToBeneficiary(c));
            }
            c.close();
        }
        return list;
    }

    public List<Beneficiary> getHouseholdMembers(String householdId) {
        List<Beneficiary> list = new ArrayList<>();
        if (householdId == null || householdId.isEmpty()) return list;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_BENEFICIARIES, null, COL_BEN_HOUSEHOLD_ID + "=?", new String[]{householdId}, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToBeneficiary(c));
            }
            c.close();
        }
        return list;
    }

    private Beneficiary cursorToBeneficiary(Cursor c) {
        String otrId = c.getString(c.getColumnIndexOrThrow(COL_BEN_OTR_ID));
        List<SchemeApplication> apps = getApplicationsForBeneficiary(otrId);
        List<WalletDocument> docs = getDocumentsForBeneficiary(otrId);

        return new Beneficiary(
                c.getString(c.getColumnIndexOrThrow(COL_BEN_FULL_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_AADHAAR)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_DOB)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_GENDER)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_CATEGORY)),
                c.getInt(c.getColumnIndexOrThrow(COL_BEN_IS_PVTG)) == 1,
                c.getString(c.getColumnIndexOrThrow(COL_BEN_PVTG_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_HOUSEHOLD_ID)),
                otrId,
                c.getString(c.getColumnIndexOrThrow(COL_BEN_APAAR_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_MOBILE)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_STATE)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_DISTRICT)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_INST_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_AISHE_CODE)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_BANK_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_ACCOUNT_MASKED)),
                c.getInt(c.getColumnIndexOrThrow(COL_BEN_NPCI_MAPPED)) == 1,
                c.getString(c.getColumnIndexOrThrow(COL_BEN_NPCI_REASON)),
                c.getString(c.getColumnIndexOrThrow(COL_BEN_NPCI_REMEDY)),
                apps,
                docs
        );
    }
}
