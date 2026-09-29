package gov.mota.scholarship.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import gov.mota.scholarship.R;
import gov.mota.scholarship.data.BeneficiaryRepository;
import gov.mota.scholarship.data.SchemeCatalogRepository;
import gov.mota.scholarship.data.model.Beneficiary;
import gov.mota.scholarship.data.model.SchemeApplication;
import gov.mota.scholarship.data.model.SchemeCatalogItem;
import gov.mota.scholarship.data.model.WalletDocument;
import gov.mota.scholarship.data.util.DocumentStorageManager;

import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import gov.mota.scholarship.data.util.PdfDocumentScanner;

public class ApplyScholarshipActivity extends BaseActivity {

    private MaterialToolbar toolbarApply;
    private TextView tvApplicantSummary;
    private TextView tvApplicantInst;

    // Step 1: Scheme Selection
    private RadioGroup rgSchemes;
    private RadioButton rbPreMatric;
    private RadioButton rbPostMatric;
    private RadioButton rbTopClass;
    private RadioButton rbNfst;
    private RadioButton rbNos;
    private TextView tvSchemeBenefit;
    private TextView tvSchemeEligibility;
    private TextView tvSchemeRegistry;

    // Step 2: Form Particulars
    private TextInputEditText etAcademicYear;
    private TextInputEditText etIncome;

    private LinearLayout llPreMatricFields;
    private TextInputEditText etUdiseCode;
    private TextInputEditText etClassLevel;
    private TextInputEditText etHostelType;

    private LinearLayout llPostMatricFields;
    private TextInputEditText etPostMatricAishe;
    private TextInputEditText etCourseYear;
    private TextInputEditText etApaarIdInput;

    private LinearLayout llTopClassFields;
    private TextInputEditText etTopInstituteName;
    private TextInputEditText etEntranceRank;
    private TextInputEditText etTuitionClaim;

    private LinearLayout llNfstFields;
    private TextInputEditText etNetRollNo;
    private TextInputEditText etResearchTopic;
    private TextInputEditText etPhdRegDate;

    private LinearLayout llNosFields;
    private TextInputEditText etForeignUni;
    private TextInputEditText etForeignCourse;
    private TextInputEditText etPassportNum;

    // Step 3: Documents
    private TextView tvDoc1Label;
    private TextInputEditText etCasteCertNum;
    private TextView tvDoc2Label;
    private TextInputEditText etIncomeCertNum;
    private TextView tvDoc3Label;
    private TextInputEditText etMarksheetNum;
    private MaterialButton btnUploadCaste;
    private MaterialButton btnUploadIncome;
    private MaterialButton btnUploadMarksheet;
    private TextView tvCasteFileStatus;
    private TextView tvIncomeFileStatus;
    private TextView tvMarksheetFileStatus;

    // Track which upload slot the user is currently filling
    private static final int SLOT_CASTE = 1;
    private static final int SLOT_INCOME = 2;
    private static final int SLOT_MARKSHEET = 3;
    private int pendingUploadSlot = 0;

    // Track whether a real file was picked (Uri is non-null)
    private Uri casteFileUri = null;
    private Uri incomeFileUri = null;
    private Uri marksheetFileUri = null;

    private CheckBox cbDigiLockerConsent;
    private TextView tvDeDupWarning;
    private MaterialButton btnSubmitApplication;

    private Beneficiary currentBeneficiary;
    private BeneficiaryRepository repository;
    private SchemeCatalogItem selectedScheme;

    // Real file picker — opens system file picker, handles result and triggers intelligent scanning
    private final ActivityResultLauncher<String> filePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    Toast.makeText(this, getString(R.string.no_file_attached), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Resolve display name from URI
                String fileName = resolveFileName(uri);

                if (!isPdfFile(uri, fileName)) {
                    Toast.makeText(this, "Only PDF files (.pdf) are allowed. Please select a valid PDF file.", Toast.LENGTH_LONG).show();
                    pendingUploadSlot = 0;
                    return;
                }

                int slot = pendingUploadSlot;
                pendingUploadSlot = 0;
                scanAndValidateDocument(uri, fileName, slot);
            });

    private void scanAndValidateDocument(Uri uri, String fileName, int slot) {
        PdfDocumentScanner.DocumentType expectedType;
        TextView targetStatusView;
        String docTypeName;

        switch (slot) {
            case SLOT_CASTE:
                expectedType = PdfDocumentScanner.DocumentType.CASTE;
                targetStatusView = tvCasteFileStatus;
                docTypeName = "ST Caste Certificate";
                break;
            case SLOT_INCOME:
                expectedType = PdfDocumentScanner.DocumentType.INCOME;
                targetStatusView = tvIncomeFileStatus;
                docTypeName = "Annual Household Income Certificate";
                break;
            case SLOT_MARKSHEET:
                expectedType = PdfDocumentScanner.DocumentType.ACADEMIC;
                targetStatusView = tvMarksheetFileStatus;
                docTypeName = "Academic Marksheet / Degree Certificate";
                break;
            default:
                return;
        }

        targetStatusView.setText("🔍 Scanning & authenticating " + docTypeName + "...");
        targetStatusView.setTextColor(Color.parseColor("#B45309"));

        PdfDocumentScanner.scanDocumentAsync(this, uri, fileName, expectedType, result -> {
            if (result.isValid()) {
                targetStatusView.setText("✓ " + fileName + " (Verified " + result.getDetectedType() + ")");
                targetStatusView.setTextColor(Color.parseColor("#15803D"));
                Toast.makeText(this, "✓ Document Authenticated: " + result.getDetectedType(), Toast.LENGTH_SHORT).show();
                switch (slot) {
                    case SLOT_CASTE:
                        casteFileUri = uri;
                        break;
                    case SLOT_INCOME:
                        incomeFileUri = uri;
                        break;
                    case SLOT_MARKSHEET:
                        marksheetFileUri = uri;
                        break;
                }
            } else {
                switch (slot) {
                    case SLOT_CASTE:
                        casteFileUri = null;
                        break;
                    case SLOT_INCOME:
                        incomeFileUri = null;
                        break;
                    case SLOT_MARKSHEET:
                        marksheetFileUri = null;
                        break;
                }
                targetStatusView.setText("❌ Rejected: Invalid / Mismatched Document");
                targetStatusView.setTextColor(Color.parseColor("#DC2626"));

                new MaterialAlertDialogBuilder(this)
                        .setTitle("❌ Document Rejected by Security Scanner")
                        .setMessage(result.getRejectionReason() + "\n\n• Detected Type: " + result.getDetectedType() +
                                "\n• Expected Type: " + docTypeName +
                                "\n\nAutomatic Pre-Submission Rejection: To protect your application from being disqualified by the Ministry or state nodal authorities, only authentic documents matching the required category can be submitted.")
                        .setPositiveButton("Select Another PDF", (dialog, which) -> {
                            dialog.dismiss();
                            pendingUploadSlot = slot;
                            filePicker.launch("application/pdf");
                        })
                        .setNegativeButton("Dismiss", null)
                        .setCancelable(false)
                        .show();
            }
        });
    }

    private boolean isPdfFile(Uri uri, String fileName) {
        if (fileName != null && fileName.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            return true;
        }
        try {
            String mime = getContentResolver().getType(uri);
            if (mime != null && mime.equalsIgnoreCase("application/pdf")) {
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private String resolveFileName(Uri uri) {
        String name = null;
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        if (name == null || name.isEmpty()) {
            String path = uri.getPath();
            if (path != null && path.contains("/")) {
                name = path.substring(path.lastIndexOf('/') + 1);
            } else {
                name = "document";
            }
        }
        return name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_scholarship);

        repository = BeneficiaryRepository.getInstance(this);
        currentBeneficiary = (Beneficiary) getIntent().getSerializableExtra("beneficiary");

        initViews();
        populateApplicantDetails();
        setupSchemeRadioGroup();
    }

    private void initViews() {
        toolbarApply = findViewById(R.id.toolbarApply);
        tvApplicantSummary = findViewById(R.id.tvApplicantSummary);
        tvApplicantInst = findViewById(R.id.tvApplicantInst);

        rgSchemes = findViewById(R.id.rgSchemes);
        rbPreMatric = findViewById(R.id.rbPreMatric);
        rbPostMatric = findViewById(R.id.rbPostMatric);
        rbTopClass = findViewById(R.id.rbTopClass);
        rbNfst = findViewById(R.id.rbNfst);
        rbNos = findViewById(R.id.rbNos);

        tvSchemeBenefit = findViewById(R.id.tvSchemeBenefit);
        tvSchemeEligibility = findViewById(R.id.tvSchemeEligibility);
        tvSchemeRegistry = findViewById(R.id.tvSchemeRegistry);

        etAcademicYear = findViewById(R.id.etAcademicYear);
        etIncome = findViewById(R.id.etIncome);

        llPreMatricFields = findViewById(R.id.llPreMatricFields);
        etUdiseCode = findViewById(R.id.etUdiseCode);
        etClassLevel = findViewById(R.id.etClassLevel);
        etHostelType = findViewById(R.id.etHostelType);

        llPostMatricFields = findViewById(R.id.llPostMatricFields);
        etPostMatricAishe = findViewById(R.id.etPostMatricAishe);
        etCourseYear = findViewById(R.id.etCourseYear);
        etApaarIdInput = findViewById(R.id.etApaarIdInput);

        llTopClassFields = findViewById(R.id.llTopClassFields);
        etTopInstituteName = findViewById(R.id.etTopInstituteName);
        etEntranceRank = findViewById(R.id.etEntranceRank);
        etTuitionClaim = findViewById(R.id.etTuitionClaim);

        llNfstFields = findViewById(R.id.llNfstFields);
        etNetRollNo = findViewById(R.id.etNetRollNo);
        etResearchTopic = findViewById(R.id.etResearchTopic);
        etPhdRegDate = findViewById(R.id.etPhdRegDate);

        llNosFields = findViewById(R.id.llNosFields);
        etForeignUni = findViewById(R.id.etForeignUni);
        etForeignCourse = findViewById(R.id.etForeignCourse);
        etPassportNum = findViewById(R.id.etPassportNum);

        tvDoc1Label = findViewById(R.id.tvDoc1Label);
        etCasteCertNum = findViewById(R.id.etCasteCertNum);
        tvDoc2Label = findViewById(R.id.tvDoc2Label);
        etIncomeCertNum = findViewById(R.id.etIncomeCertNum);
        tvDoc3Label = findViewById(R.id.tvDoc3Label);
        etMarksheetNum = findViewById(R.id.etMarksheetNum);
        btnUploadCaste = findViewById(R.id.btnUploadCaste);
        btnUploadIncome = findViewById(R.id.btnUploadIncome);
        btnUploadMarksheet = findViewById(R.id.btnUploadMarksheet);
        tvCasteFileStatus = findViewById(R.id.tvCasteFileStatus);
        tvIncomeFileStatus = findViewById(R.id.tvIncomeFileStatus);
        tvMarksheetFileStatus = findViewById(R.id.tvMarksheetFileStatus);

        cbDigiLockerConsent = findViewById(R.id.cbDigiLockerConsent);
        tvDeDupWarning = findViewById(R.id.tvDeDupWarning);
        btnSubmitApplication = findViewById(R.id.btnSubmitApplication);

        toolbarApply.setNavigationOnClickListener(v -> finish());
        btnSubmitApplication.setOnClickListener(v -> attemptSubmission());

        btnUploadCaste.setOnClickListener(v -> {
            pendingUploadSlot = SLOT_CASTE;
            filePicker.launch("application/pdf");
        });

        btnUploadIncome.setOnClickListener(v -> {
            pendingUploadSlot = SLOT_INCOME;
            filePicker.launch("application/pdf");
        });

        btnUploadMarksheet.setOnClickListener(v -> {
            pendingUploadSlot = SLOT_MARKSHEET;
            filePicker.launch("application/pdf");
        });
    }

    private void populateApplicantDetails() {
        if (currentBeneficiary == null) return;

        String localizedName = gov.mota.scholarship.util.LocalizationUtils.getLocalizedBeneficiaryName(this, currentBeneficiary.getFullName());
        String localizedInst = gov.mota.scholarship.util.LocalizationUtils.getLocalizedInstitution(this, currentBeneficiary.getInstitutionName());

        String summary = localizedName + " • " +
                getString(R.string.category_prefix, currentBeneficiary.getCategory(), currentBeneficiary.getGender(), currentBeneficiary.getDob()) +
                (currentBeneficiary.isPvtg() ? " (" + currentBeneficiary.getPvtgCommunityName() + ")" : "") +
                "\nOTR: " + currentBeneficiary.getOtrId() + " | APAAR: " + currentBeneficiary.getApaarId() +
                "\nAadhaar: •••• •••• " + (currentBeneficiary.getAadhaarNumber().length() >= 4 ?
                currentBeneficiary.getAadhaarNumber().substring(currentBeneficiary.getAadhaarNumber().length() - 4) : "****");

        tvApplicantSummary.setText(summary);
        tvApplicantInst.setText(getString(R.string.institution_prefix, localizedInst, currentBeneficiary.getAisheUdiseCode()));

        // All form fields blank
        etAcademicYear.setText("");
        etIncome.setText("");
        etUdiseCode.setText("");
        etClassLevel.setText("");
        etHostelType.setText("");
        etPostMatricAishe.setText("");
        etCourseYear.setText("");
        etApaarIdInput.setText("");
        etTopInstituteName.setText("");
        etEntranceRank.setText("");
        etTuitionClaim.setText("");
        etNetRollNo.setText("");
        etResearchTopic.setText("");
        etPhdRegDate.setText("");
        etForeignUni.setText("");
        etForeignCourse.setText("");
        etPassportNum.setText("");
        etCasteCertNum.setText("");
        etIncomeCertNum.setText("");
        etMarksheetNum.setText("");

        // Reset all file URIs and status labels
        casteFileUri = null;
        incomeFileUri = null;
        marksheetFileUri = null;
        int grey = getColor(R.color.mota_on_surface_variant);
        tvCasteFileStatus.setText(R.string.no_file_attached);
        tvCasteFileStatus.setTextColor(grey);
        tvIncomeFileStatus.setText(R.string.no_file_attached);
        tvIncomeFileStatus.setTextColor(grey);
        tvMarksheetFileStatus.setText(R.string.no_file_attached);
        tvMarksheetFileStatus.setTextColor(grey);
    }

    private void setupSchemeRadioGroup() {
        rgSchemes.setOnCheckedChangeListener((group, checkedId) -> {
            String schemeCode = "POST_MATRIC";
            if (checkedId == R.id.rbPreMatric) schemeCode = "PRE_MATRIC";
            else if (checkedId == R.id.rbPostMatric) schemeCode = "POST_MATRIC";
            else if (checkedId == R.id.rbTopClass) schemeCode = "TOP_CLASS";
            else if (checkedId == R.id.rbNfst) schemeCode = "NFST";
            else if (checkedId == R.id.rbNos) schemeCode = "NOS";

            selectedScheme = SchemeCatalogRepository.getSchemeByCode(schemeCode);
            if (selectedScheme != null) updateUIForScheme(selectedScheme);
        });
        rbPostMatric.setChecked(true);
    }

    private void updateUIForScheme(SchemeCatalogItem scheme) {
        tvSchemeBenefit.setText(getString(R.string.benefit_prefix, scheme.getFinancialBenefit()));
        tvSchemeEligibility.setText(getString(R.string.eligibility_prefix, scheme.getEligibilitySummary(), scheme.getTargetAudience()));
        tvSchemeRegistry.setText(getString(R.string.registry_prefix, scheme.getRequiredRegistry(), scheme.getSourcePortal()));

        llPreMatricFields.setVisibility(View.GONE);
        llPostMatricFields.setVisibility(View.GONE);
        llTopClassFields.setVisibility(View.GONE);
        llNfstFields.setVisibility(View.GONE);
        llNosFields.setVisibility(View.GONE);

        switch (scheme.getSchemeCode()) {
            case "PRE_MATRIC":
                llPreMatricFields.setVisibility(View.VISIBLE);
                tvDoc2Label.setText(R.string.doc_income_pre_matric);
                tvDoc3Label.setText(R.string.doc_acad_pre_matric);
                break;
            case "POST_MATRIC":
                llPostMatricFields.setVisibility(View.VISIBLE);
                tvDoc2Label.setText(R.string.doc_income_post_matric);
                tvDoc3Label.setText(R.string.doc_acad_post_matric);
                break;
            case "TOP_CLASS":
                llTopClassFields.setVisibility(View.VISIBLE);
                tvDoc2Label.setText(R.string.doc_income_top_class);
                tvDoc3Label.setText(R.string.doc_acad_top_class);
                break;
            case "NFST":
                llNfstFields.setVisibility(View.VISIBLE);
                tvDoc2Label.setText(R.string.doc_income_nfst);
                tvDoc3Label.setText(R.string.doc_acad_nfst);
                break;
            case "NOS":
                llNosFields.setVisibility(View.VISIBLE);
                tvDoc2Label.setText(R.string.doc_income_nos);
                tvDoc3Label.setText(R.string.doc_acad_nos);
                break;
        }
        validateDeDuplication(scheme);
    }

    private void validateDeDuplication(SchemeCatalogItem scheme) {
        tvDeDupWarning.setVisibility(View.GONE);
        if (currentBeneficiary.getApplications() == null) return;

        boolean alreadyApplied = false;
        boolean hasActiveFellowship = false;

        for (SchemeApplication app : currentBeneficiary.getApplications()) {
            if (app.getSchemeCode().equalsIgnoreCase(scheme.getSchemeCode()) &&
                    ("APPROVED".equalsIgnoreCase(app.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(app.getStatus()))) {
                alreadyApplied = true;
            }
            if (("NFST".equalsIgnoreCase(app.getSchemeCode()) || "NOS".equalsIgnoreCase(app.getSchemeCode())) &&
                    ("APPROVED".equalsIgnoreCase(app.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(app.getStatus()))) {
                hasActiveFellowship = true;
            }
        }

        if (alreadyApplied) {
            tvDeDupWarning.setVisibility(View.VISIBLE);
            tvDeDupWarning.setText(R.string.dedup_existing_app);
        } else if (hasActiveFellowship &&
                ("POST_MATRIC".equalsIgnoreCase(scheme.getSchemeCode()) ||
                 "PRE_MATRIC".equalsIgnoreCase(scheme.getSchemeCode()))) {
            tvDeDupWarning.setVisibility(View.VISIBLE);
            tvDeDupWarning.setText(R.string.dedup_conflict_fellowship);
        }
    }

    private void attemptSubmission() {
        if (selectedScheme == null) {
            Toast.makeText(this, getString(R.string.warn_select_scheme), Toast.LENGTH_SHORT).show();
            return;
        }

        String academicYear = text(etAcademicYear);
        String income = text(etIncome);
        String casteNum = text(etCasteCertNum);
        String incomeNum = text(etIncomeCertNum);
        String marksheetNum = text(etMarksheetNum);

        if (academicYear.isEmpty()) {
            Toast.makeText(this, getString(R.string.warn_enter_ay), Toast.LENGTH_SHORT).show();
            return;
        }
        if (income.isEmpty()) {
            Toast.makeText(this, getString(R.string.warn_enter_income), Toast.LENGTH_SHORT).show();
            return;
        }
        if (casteNum.isEmpty()) {
            Toast.makeText(this, getString(R.string.warn_enter_caste_no), Toast.LENGTH_SHORT).show();
            return;
        }
        if (casteFileUri == null) {
            Toast.makeText(this, "Please attach and verify an authentic ST Caste Certificate PDF before submitting.", Toast.LENGTH_LONG).show();
            return;
        }
        if (incomeNum.isEmpty()) {
            Toast.makeText(this, getString(R.string.warn_enter_income_no), Toast.LENGTH_SHORT).show();
            return;
        }
        if (incomeFileUri == null) {
            Toast.makeText(this, "Please attach and verify an authentic Income Certificate PDF before submitting.", Toast.LENGTH_LONG).show();
            return;
        }
        if (marksheetNum.isEmpty()) {
            Toast.makeText(this, getString(R.string.warn_enter_doc3_no), Toast.LENGTH_SHORT).show();
            return;
        }
        if (marksheetFileUri == null) {
            Toast.makeText(this, "Please attach and verify an authentic Academic Document PDF before submitting.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!cbDigiLockerConsent.isChecked()) {
            Toast.makeText(this, getString(R.string.warn_check_consent), Toast.LENGTH_SHORT).show();
            return;
        }

        // Build scheme-specific remarks
        String schemeRemarks = buildRemarks();
        String currentDateStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String generatedAppId = "APP-2026-" + selectedScheme.getSchemeCode() + "-" + (int)(1000 + Math.random() * 9000);
        String generatedTxn = "DVIG-ACK-" + timestamp;

        // Securely store the uploaded files into the app's private files directory, isolated by OTR ID
        String casteFilePath = DocumentStorageManager.saveFileLocally(this, currentBeneficiary.getOtrId(), casteFileUri, "caste");
        String incomeFilePath = DocumentStorageManager.saveFileLocally(this, currentBeneficiary.getOtrId(), incomeFileUri, "income");
        String marksheetFilePath = DocumentStorageManager.saveFileLocally(this, currentBeneficiary.getOtrId(), marksheetFileUri, "academic");

        String casteMime = DocumentStorageManager.getMimeType(this, casteFileUri);
        String incomeMime = DocumentStorageManager.getMimeType(this, incomeFileUri);
        String marksheetMime = DocumentStorageManager.getMimeType(this, marksheetFileUri);

        String casteFileName = resolveFileName(casteFileUri);
        String incomeFileName = resolveFileName(incomeFileUri);
        String marksheetFileName = resolveFileName(marksheetFileUri);

        WalletDocument casteDoc = new WalletDocument(
                "DOC-ST-" + timestamp,
                "ST Caste Certificate (" + selectedScheme.getSchemeCode() + ")",
                "State e-District / DigiLocker Central",
                casteNum + " [" + casteFileName + "]",
                "Digitally Verified",
                currentDateStr,
                true,
                true,
                casteFilePath != null ? casteFilePath : "",
                casteMime
        );
        WalletDocument incomeDoc = new WalletDocument(
                "DOC-INC-" + timestamp,
                tvDoc2Label.getText().toString().replace(" *", ""),
                "Revenue Authority / Central DB",
                incomeNum + " [" + incomeFileName + "]",
                "Verified (< Limits)",
                currentDateStr,
                false,
                true,
                incomeFilePath != null ? incomeFilePath : "",
                incomeMime
        );
        WalletDocument proofDoc = new WalletDocument(
                "DOC-ACAD-" + timestamp,
                tvDoc3Label.getText().toString().replace(" *", ""),
                selectedScheme.getRequiredRegistry(),
                marksheetNum + " [" + marksheetFileName + "]",
                "Auto-Verified via Registry",
                currentDateStr,
                true,
                true,
                marksheetFilePath != null ? marksheetFilePath : "",
                marksheetMime
        );

        repository.addDocument(currentBeneficiary.getOtrId(), casteDoc);
        repository.addDocument(currentBeneficiary.getOtrId(), incomeDoc);
        repository.addDocument(currentBeneficiary.getOtrId(), proofDoc);

        // Save Application
        List<String> badges = new ArrayList<>();
        badges.add("Central DB Stored");
        badges.add("Documents Uploaded");
        badges.add("DVIG Green Badge");

        SchemeApplication newApp = new SchemeApplication(
                generatedAppId,
                selectedScheme.getSchemeCode(),
                selectedScheme.getSchemeTitle(),
                academicYear,
                selectedScheme.getSourcePortal(),
                "Institute Verification",
                "IN_PROGRESS",
                badges,
                "PENDING_VERIFICATION",
                selectedScheme.getFinancialBenefit().split("\\(")[0].trim(),
                generatedTxn,
                schemeRemarks
        );
        repository.addApplication(currentBeneficiary.getOtrId(), newApp);

        // Sync application and documents to remote central server
        try {
            gov.mota.scholarship.data.remote.SupabaseClient supabase = gov.mota.scholarship.data.remote.SupabaseClient.getInstance();
            supabase.upsertApplication(newApp, new gov.mota.scholarship.data.remote.SupabaseClient.Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {}
                @Override
                public void onError(Exception e) {}
            });
            supabase.upsertDocument(casteDoc, new gov.mota.scholarship.data.remote.SupabaseClient.Callback<Boolean>() {
                @Override public void onSuccess(Boolean r) {}
                @Override public void onError(Exception e) {}
            });
            supabase.upsertDocument(incomeDoc, new gov.mota.scholarship.data.remote.SupabaseClient.Callback<Boolean>() {
                @Override public void onSuccess(Boolean r) {}
                @Override public void onError(Exception e) {}
            });
            supabase.upsertDocument(proofDoc, new gov.mota.scholarship.data.remote.SupabaseClient.Callback<Boolean>() {
                @Override public void onSuccess(Boolean r) {}
                @Override public void onError(Exception e) {}
            });
        } catch (Exception ignored) {}

        String officialPortalUrl = getOfficialPortalUrl(selectedScheme.getSchemeCode());

        new AlertDialog.Builder(this)
                .setTitle(R.string.submission_success_title)
                .setMessage(getString(R.string.submission_success_msg, selectedScheme.getSchemeTitle(), generatedAppId, officialPortalUrl))
                .setPositiveButton(R.string.btn_open_official_portal, (d, w) -> {
                    d.dismiss();
                    Toast.makeText(this, getString(R.string.launching_portal_toast), Toast.LENGTH_SHORT).show();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(officialPortalUrl));
                    startActivity(browserIntent);
                    setResult(RESULT_OK);
                    finish();
                })
                .setNeutralButton(R.string.btn_go_to_dashboard, (d, w) -> {
                    d.dismiss();
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private String getOfficialPortalUrl(String schemeCode) {
        if ("NOS".equalsIgnoreCase(schemeCode)) {
            return "https://overseas.tribal.gov.in/";
        } else if ("NFST".equalsIgnoreCase(schemeCode)) {
            return "https://fellowship.tribal.gov.in/";
        } else {
            // PRE_MATRIC, POST_MATRIC, TOP_CLASS
            return "https://scholarships.gov.in/";
        }
    }

    private String buildRemarks() {
        String instCode = currentBeneficiary.getAisheUdiseCode();
        switch (selectedScheme.getSchemeCode()) {
            case "PRE_MATRIC":
                return "Pre-Matric " + text(etClassLevel) + " (" + text(etHostelType) + ") at UDISE " +
                        (text(etUdiseCode).isEmpty() ? instCode : text(etUdiseCode));
            case "POST_MATRIC":
                return "Post-Matric " + text(etCourseYear) + " at AISHE " +
                        (text(etPostMatricAishe).isEmpty() ? instCode : text(etPostMatricAishe)) +
                        " (APAAR: " + text(etApaarIdInput) + ")";
            case "TOP_CLASS":
                return "Top Class at " + text(etTopInstituteName) + " (Rank: " + text(etEntranceRank) + "). Fee: ₹ " + text(etTuitionClaim);
            case "NFST":
                return "NFST Ph.D Fellowship (NET Roll: " + text(etNetRollNo) + ", Date: " + text(etPhdRegDate) + "). Topic: " + text(etResearchTopic);
            case "NOS":
                return "NOS Overseas — " + text(etForeignUni) + " (" + text(etForeignCourse) + "), Passport: " + text(etPassportNum);
            default:
                return "";
        }
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
