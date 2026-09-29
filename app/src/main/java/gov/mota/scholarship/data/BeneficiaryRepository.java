package gov.mota.scholarship.data;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gov.mota.scholarship.data.db.MoTADatabaseHelper;
import gov.mota.scholarship.data.model.Beneficiary;
import gov.mota.scholarship.data.model.SchemeApplication;
import gov.mota.scholarship.data.model.WalletDocument;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BeneficiaryRepository {
    private static BeneficiaryRepository instance;
    private final MoTADatabaseHelper dbHelper;

    private BeneficiaryRepository(Context context) {
        dbHelper = new MoTADatabaseHelper(context.getApplicationContext());
        seedDatabaseIfNeeded(context);
    }

    public static synchronized BeneficiaryRepository getInstance(Context context) {
        if (instance == null) {
            instance = new BeneficiaryRepository(context.getApplicationContext());
        }
        return instance;
    }

    private void seedDatabaseIfNeeded(Context context) {
        if (!dbHelper.isDatabasePopulated()) {
            try {
                InputStream is = context.getAssets().open("beneficiaries.json");
                InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                Type listType = new TypeToken<List<Beneficiary>>() {}.getType();
                List<Beneficiary> seedList = new Gson().fromJson(reader, listType);
                if (seedList != null) {
                    for (Beneficiary b : seedList) {
                        dbHelper.insertBeneficiary(b);
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Beneficiary> getAllBeneficiaries() {
        return dbHelper.getAllBeneficiaries();
    }

    public Beneficiary authenticate(String fullNameInput, String aadhaarInput) {
        if (fullNameInput == null || aadhaarInput == null) return null;
        
        String cleanName = fullNameInput.trim();
        String cleanAadhaar = aadhaarInput.trim().replaceAll("\\s+", "").replaceAll("-", "");

        return dbHelper.authenticateBeneficiary(cleanName, cleanAadhaar);
    }

    public List<Beneficiary> getHouseholdMembers(String householdId) {
        return dbHelper.getHouseholdMembers(householdId);
    }

    public Beneficiary findByOtrId(String otrId) {
        return dbHelper.getBeneficiaryByOtr(otrId);
    }

    public void addApplication(String otrId, SchemeApplication newApp) {
        dbHelper.insertApplication(otrId, newApp);
    }

    public void addDocument(String otrId, WalletDocument newDoc) {
        dbHelper.insertDocument(otrId, newDoc);
    }
}
