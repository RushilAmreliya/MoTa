package gov.mota.scholarship.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import gov.mota.scholarship.R;
import gov.mota.scholarship.data.BeneficiaryRepository;
import gov.mota.scholarship.data.model.Beneficiary;
import gov.mota.scholarship.data.model.WalletDocument;
import gov.mota.scholarship.data.util.DocumentStorageManager;
import gov.mota.scholarship.data.util.PdfDocumentScanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private TextView tvStudentName;
    private TextView tvCategoryTag;
    private TextView tvOtrApaar;
    private TextView tvInstitution;
    private TextView tvLocation;
    private TextView tvNpciStatus;
    private TextView tvNpciRemediation;
    private LinearLayout llNpciStatus;
    private MaterialButton btnApplyNewScheme;
    private RecyclerView rvApplications;
    private RecyclerView rvDocuments;
    private TextView tvEmptyApplications;
    private TextView tvEmptyDocuments;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabJagoChatbot;
    private androidx.core.widget.NestedScrollView scrollHome;
    private androidx.core.widget.NestedScrollView scrollDocuments;
    private androidx.core.widget.NestedScrollView scrollProfile;
    private TextView tvHomeUserName;
    private MaterialButton btnProfileSignOut;

    private com.google.android.material.card.MaterialCardView cardFamilyHousehold;
    private TextView tvHouseholdIdBadge;
    private TextView tvHouseholdSubtitle;
    private LinearLayout llFamilyMembers;

    private Beneficiary currentBeneficiary;
    private BeneficiaryRepository repository;

    private Uri pendingUploadUri = null;
    private TextView tvDialogSelectedFile = null;
    private AutoCompleteTextView dialogActvDocType = null;

    private final ActivityResultLauncher<String> independentFilePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    String fileName = resolveFileName(uri);
                    if (!isPdfFile(uri, fileName)) {
                        pendingUploadUri = null;
                        if (tvDialogSelectedFile != null) {
                            tvDialogSelectedFile.setText("⚠ Only PDF files allowed (.pdf)");
                            tvDialogSelectedFile.setTextColor(Color.parseColor("#F87171"));
                        }
                        Toast.makeText(this, "Only PDF files (.pdf) are allowed. Please select a valid PDF file.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Determine expected DocumentType from dialog dropdown
                    String selectedType = dialogActvDocType != null && dialogActvDocType.getText() != null ?
                            dialogActvDocType.getText().toString().trim() : "";
                    String lowerType = selectedType.toLowerCase(Locale.ROOT);
                    PdfDocumentScanner.DocumentType expected;
                    if (lowerType.contains("income") || lowerType.contains("aay")) {
                        expected = PdfDocumentScanner.DocumentType.INCOME;
                    } else if (lowerType.contains("caste") || lowerType.contains("tribe") || lowerType.contains("jati")) {
                        expected = PdfDocumentScanner.DocumentType.CASTE;
                    } else if (lowerType.contains("mark") || lowerType.contains("secondary") || lowerType.contains("bonafide") || lowerType.contains("admission")) {
                        expected = PdfDocumentScanner.DocumentType.ACADEMIC;
                    } else {
                        expected = PdfDocumentScanner.DocumentType.GENERAL;
                    }

                    if (tvDialogSelectedFile != null) {
                        tvDialogSelectedFile.setText("🔍 Scanning & authenticating document...");
                        tvDialogSelectedFile.setTextColor(Color.parseColor("#FCD34D"));
                    }

                    PdfDocumentScanner.scanDocumentAsync(this, uri, fileName, expected, result -> {
                        if (result.isValid()) {
                            pendingUploadUri = uri;
                            if (tvDialogSelectedFile != null) {
                                tvDialogSelectedFile.setText("✓ " + fileName + " (Verified: " + result.getDetectedType() + ")");
                                tvDialogSelectedFile.setTextColor(Color.parseColor("#4ADE80"));
                            }
                            Toast.makeText(this, "✓ Document Authenticated: " + result.getDetectedType(), Toast.LENGTH_SHORT).show();
                        } else {
                            pendingUploadUri = null;
                            if (tvDialogSelectedFile != null) {
                                tvDialogSelectedFile.setText("❌ Rejected: " + result.getDetectedType());
                                tvDialogSelectedFile.setTextColor(Color.parseColor("#F87171"));
                            }
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle("❌ Document Rejected by Scanner")
                                    .setMessage(result.getRejectionReason() + "\n\n• Detected Type: " + result.getDetectedType() +
                                            (selectedType.isEmpty() ? "" : "\n• Selected Category: " + selectedType) +
                                            "\n\nPre-Upload Rejection: Incorrect or random documents cannot be saved to your government scholarship vault.")
                                    .setPositiveButton("Select Another PDF", (d, w) -> {
                                        d.dismiss();
                                        launchIndependentFilePicker();
                                    })
                                    .setNegativeButton("Dismiss", null)
                                    .show();
                        }
                    });
                }
            });

    private void launchIndependentFilePicker() {
        if (independentFilePicker != null) {
            independentFilePicker.launch("application/pdf");
        }
    }

    private final ActivityResultLauncher<Intent> applySchemeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Beneficiary refreshed = repository.findByOtrId(currentBeneficiary.getOtrId());
                    if (refreshed != null) {
                        populateData(refreshed);
                        Toast.makeText(this, getString(R.string.data_refreshed_toast), Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        repository = BeneficiaryRepository.getInstance(this);
        currentBeneficiary = (Beneficiary) getIntent().getSerializableExtra("beneficiary");

        initViews();
        populateData(currentBeneficiary);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvHomeUserName = findViewById(R.id.tvHomeUserName);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvCategoryTag = findViewById(R.id.tvCategoryTag);
        tvOtrApaar = findViewById(R.id.tvOtrApaar);
        tvInstitution = findViewById(R.id.tvInstitution);
        tvLocation = findViewById(R.id.tvLocation);
        tvNpciStatus = findViewById(R.id.tvNpciStatus);
        tvNpciRemediation = findViewById(R.id.tvNpciRemediation);
        llNpciStatus = findViewById(R.id.llNpciStatus);
        btnApplyNewScheme = findViewById(R.id.btnApplyNewScheme);
        rvApplications = findViewById(R.id.rvApplications);
        rvDocuments = findViewById(R.id.rvDocuments);
        tvEmptyApplications = findViewById(R.id.tvEmptyApplications);
        tvEmptyDocuments = findViewById(R.id.tvEmptyDocuments);
        fabJagoChatbot = findViewById(R.id.fabJagoChatbot);
        scrollHome = findViewById(R.id.scrollHome);
        scrollDocuments = findViewById(R.id.scrollDocuments);
        scrollProfile = findViewById(R.id.scrollProfile);
        btnProfileSignOut = findViewById(R.id.btnProfileSignOut);
        cardFamilyHousehold = findViewById(R.id.cardFamilyHousehold);
        tvHouseholdIdBadge = findViewById(R.id.tvHouseholdIdBadge);
        tvHouseholdSubtitle = findViewById(R.id.tvHouseholdSubtitle);
        llFamilyMembers = findViewById(R.id.llFamilyMembers);

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        com.google.android.material.button.MaterialButton btnUploadDoc = findViewById(R.id.btnUploadDocumentWallet);
        android.widget.EditText etSearch = findViewById(R.id.etDocSearch);

        btnProfileSignOut.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnApplyNewScheme.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ApplyScholarshipActivity.class);
            intent.putExtra("beneficiary", currentBeneficiary);
            applySchemeLauncher.launch(intent);
        });

        if (btnUploadDoc != null) {
            btnUploadDoc.setOnClickListener(v -> showUploadDocumentDialog());
        }

        fabJagoChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, JagoChatbotActivity.class);
            intent.putExtra(JagoChatbotActivity.EXTRA_BENEFICIARY, currentBeneficiary);
            startActivity(intent);
        });

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    scrollHome.setVisibility(View.VISIBLE);
                    scrollDocuments.setVisibility(View.GONE);
                    scrollProfile.setVisibility(View.GONE);
                    fabJagoChatbot.show();
                    scrollHome.smoothScrollTo(0, 0);
                    toolbar.setTitle(R.string.mota_dashboard_title);
                    toolbar.setSubtitle(R.string.mota_dashboard_subtitle);
                    return true;
                } else if (id == R.id.nav_documents) {
                    scrollHome.setVisibility(View.GONE);
                    scrollDocuments.setVisibility(View.VISIBLE);
                    scrollProfile.setVisibility(View.GONE);
                    fabJagoChatbot.show();
                    scrollDocuments.smoothScrollTo(0, 0);
                    toolbar.setTitle(R.string.my_documents_title);
                    toolbar.setSubtitle(R.string.digital_wallet_subtitle);
                    return true;
                } else if (id == R.id.nav_chat) {
                    Intent intent = new Intent(DashboardActivity.this, JagoChatbotActivity.class);
                    intent.putExtra(JagoChatbotActivity.EXTRA_BENEFICIARY, currentBeneficiary);
                    startActivity(intent);
                    return false;
                } else if (id == R.id.nav_profile) {
                    // Profile Tab: show full profile details and sign out
                    scrollHome.setVisibility(View.GONE);
                    scrollDocuments.setVisibility(View.GONE);
                    scrollProfile.setVisibility(View.VISIBLE);
                    fabJagoChatbot.hide();
                    toolbar.setTitle(R.string.nav_profile);
                    if (currentBeneficiary != null) {
                        toolbar.setSubtitle(gov.mota.scholarship.util.LocalizationUtils.getLocalizedBeneficiaryName(this, currentBeneficiary.getFullName()));
                    }
                    scrollProfile.smoothScrollTo(0, 0);
                    return true;
                }
                return false;
            });
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterDocuments(s != null ? s.toString() : "");
                }
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void filterDocuments(String query) {
        if (currentBeneficiary == null || currentBeneficiary.getDocuments() == null) return;
        List<gov.mota.scholarship.data.model.WalletDocument> all = currentBeneficiary.getDocuments();
        if (query.trim().isEmpty()) {
            rvDocuments.setAdapter(new WalletDocumentAdapter(this, all));
            tvEmptyDocuments.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }

        List<gov.mota.scholarship.data.model.WalletDocument> filtered = new java.util.ArrayList<>();
        String q = query.toLowerCase().trim();
        for (gov.mota.scholarship.data.model.WalletDocument doc : all) {
            if ((doc.getTitle() != null && doc.getTitle().toLowerCase().contains(q)) ||
                (doc.getDocNumber() != null && doc.getDocNumber().toLowerCase().contains(q)) ||
                (doc.getAuthority() != null && doc.getAuthority().toLowerCase().contains(q)) ||
                (doc.getStatus() != null && doc.getStatus().toLowerCase().contains(q))) {
                filtered.add(doc);
            }
        }
        rvDocuments.setAdapter(new WalletDocumentAdapter(this, filtered));
        tvEmptyDocuments.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void populateData(Beneficiary beneficiary) {
        if (beneficiary == null) return;
        this.currentBeneficiary = beneficiary;

        String localizedName = gov.mota.scholarship.util.LocalizationUtils.getLocalizedBeneficiaryName(this, beneficiary.getFullName());
        String localizedState = gov.mota.scholarship.util.LocalizationUtils.getLocalizedState(this, beneficiary.getState());
        String localizedInst = gov.mota.scholarship.util.LocalizationUtils.getLocalizedInstitution(this, beneficiary.getInstitutionName());

        toolbar.setTitle(R.string.mota_dashboard_title);
        toolbar.setSubtitle(R.string.mota_dashboard_subtitle);
        tvHomeUserName.setText(localizedName);
        tvStudentName.setText(localizedName);

        String cat = getString(R.string.category_prefix, beneficiary.getCategory(), beneficiary.getGender(), beneficiary.getDob());
        if (beneficiary.isPvtg() && beneficiary.getPvtgCommunityName() != null && !beneficiary.getPvtgCommunityName().isEmpty()) {
            cat += getString(R.string.pvtg_prefix, beneficiary.getPvtgCommunityName());
        }
        tvCategoryTag.setText(cat);

        String idText = "OTR ID: " + beneficiary.getOtrId() + 
                        "\nAPAAR ID: " + beneficiary.getApaarId() + 
                        "\nAadhaar: •••• •••• " + (beneficiary.getAadhaarNumber().length() >= 4 ? 
                        beneficiary.getAadhaarNumber().substring(beneficiary.getAadhaarNumber().length() - 4) : "****");
        tvOtrApaar.setText(idText);

        tvInstitution.setText(getString(R.string.institution_prefix, localizedInst, beneficiary.getAisheUdiseCode()));
        tvLocation.setText(getString(R.string.location_prefix, beneficiary.getDistrict(), localizedState, beneficiary.getMobileNumber()));

        // DBT / NPCI Subsystem (PRD Module 1 - Section 5.1.4)
        if (beneficiary.isNpciMapped()) {
            llNpciStatus.setBackgroundColor(Color.parseColor("#DCFCE7"));
            tvNpciStatus.setTextColor(Color.parseColor("#166534"));
            tvNpciStatus.setText(getString(R.string.dbt_enabled, beneficiary.getBankName(), beneficiary.getAccountMasked()));
            tvNpciRemediation.setVisibility(View.GONE);
        } else {
            llNpciStatus.setBackgroundColor(Color.parseColor("#FEE2E2"));
            tvNpciStatus.setTextColor(Color.parseColor("#991B1B"));
            String reason = beneficiary.getNpciFailureReason() != null ? beneficiary.getNpciFailureReason() : "NPCI Mapper Inactive";
            tvNpciStatus.setText(getString(R.string.dbt_failed, reason));
            if (beneficiary.getNpciRemediation() != null && !beneficiary.getNpciRemediation().isEmpty()) {
                tvNpciRemediation.setVisibility(View.VISIBLE);
                tvNpciRemediation.setText(getString(R.string.remediation_step, beneficiary.getNpciRemediation()));
            } else {
                tvNpciRemediation.setVisibility(View.GONE);
            }
        }

        // Applications list
        boolean hasApps = beneficiary.getApplications() != null && !beneficiary.getApplications().isEmpty();
        if (hasApps) {
            tvEmptyApplications.setVisibility(View.GONE);
            rvApplications.setVisibility(View.VISIBLE);
            rvApplications.setLayoutManager(new LinearLayoutManager(this));
            SchemeApplicationAdapter appAdapter = new SchemeApplicationAdapter(this, beneficiary.getApplications());
            rvApplications.setAdapter(appAdapter);
        } else {
            tvEmptyApplications.setVisibility(View.VISIBLE);
            rvApplications.setVisibility(View.GONE);
        }

        // Digital Document Wallet
        boolean hasDocs = beneficiary.getDocuments() != null && !beneficiary.getDocuments().isEmpty();
        if (hasDocs) {
            tvEmptyDocuments.setVisibility(View.GONE);
            rvDocuments.setVisibility(View.VISIBLE);
            rvDocuments.setLayoutManager(new LinearLayoutManager(this));
            WalletDocumentAdapter docAdapter = new WalletDocumentAdapter(this, beneficiary.getDocuments());
            rvDocuments.setAdapter(docAdapter);
        } else {
            tvEmptyDocuments.setVisibility(View.VISIBLE);
            rvDocuments.setVisibility(View.GONE);
        }

        // Family / Sibling Profiles (Household Switcher)
        populateFamilyHousehold(beneficiary);
    }

    private void populateFamilyHousehold(Beneficiary beneficiary) {
        if (cardFamilyHousehold == null || llFamilyMembers == null) return;

        String householdId = beneficiary.getHouseholdId();
        if (householdId == null || householdId.trim().isEmpty()) {
            cardFamilyHousehold.setVisibility(View.GONE);
            return;
        }

        List<Beneficiary> familyMembers = repository.getHouseholdMembers(householdId);
        if (familyMembers == null || familyMembers.isEmpty()) {
            cardFamilyHousehold.setVisibility(View.GONE);
            return;
        }

        cardFamilyHousehold.setVisibility(View.VISIBLE);
        if (tvHouseholdIdBadge != null) {
            tvHouseholdIdBadge.setText(householdId);
        }

        llFamilyMembers.removeAllViews();

        for (Beneficiary member : familyMembers) {
            boolean isCurrent = member.getOtrId().equalsIgnoreCase(beneficiary.getOtrId());
            String localizedMemberName = gov.mota.scholarship.util.LocalizationUtils.getLocalizedBeneficiaryName(this, member.getFullName());
            String localizedInst = gov.mota.scholarship.util.LocalizationUtils.getLocalizedInstitution(this, member.getInstitutionName());

            View memberView = LayoutInflater.from(this).inflate(R.layout.item_wallet_document, llFamilyMembers, false);

            TextView tvDocTitle = memberView.findViewById(R.id.tvDocTitle);
            TextView tvDocStatusBadge = memberView.findViewById(R.id.tvDocStatusBadge);
            TextView tvDocAuthority = memberView.findViewById(R.id.tvDocAuthority);
            TextView tvDocNumber = memberView.findViewById(R.id.tvDocNumber);
            TextView tvReusedBadge = memberView.findViewById(R.id.tvReusedBadge);
            TextView tvInAppStorageBadge = memberView.findViewById(R.id.tvInAppStorageBadge);
            MaterialButton btnAction = memberView.findViewById(R.id.btnViewDoc);

            tvDocTitle.setText(localizedMemberName + " (" + member.getGender() + ", DOB: " + member.getDob() + ")");
            tvDocAuthority.setText(localizedInst + " • " + member.getAisheUdiseCode());
            tvDocNumber.setText("OTR ID: " + member.getOtrId() + " • Aadhaar: •••• " +
                    (member.getAadhaarNumber().length() >= 4 ? member.getAadhaarNumber().substring(member.getAadhaarNumber().length() - 4) : "****"));

            if (tvReusedBadge != null) tvReusedBadge.setVisibility(View.GONE);
            if (tvInAppStorageBadge != null) tvInAppStorageBadge.setVisibility(View.GONE);

            if (isCurrent) {
                tvDocStatusBadge.setText("Active (You)");
                tvDocStatusBadge.setTextColor(Color.parseColor("#166534"));
                tvDocStatusBadge.setBackgroundColor(Color.parseColor("#DCFCE7"));
                btnAction.setVisibility(View.GONE);
            } else {
                tvDocStatusBadge.setText("Sibling / Ward");
                tvDocStatusBadge.setTextColor(Color.parseColor("#1E40AF"));
                tvDocStatusBadge.setBackgroundColor(Color.parseColor("#DBEAFE"));
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText(getString(R.string.btn_view_sibling, localizedMemberName.split(" ")[0]));
                btnAction.setIconResource(R.drawable.ic_nav_profile);

                View.OnClickListener switchProfileListener = v -> {
                    Toast.makeText(this, getString(R.string.switch_profile_toast, localizedMemberName), Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(DashboardActivity.this, LoginActivity.class);
                    loginIntent.putExtra("switch_sibling_name", localizedMemberName);
                    loginIntent.putExtra("switch_sibling_prompt", getString(R.string.sibling_auth_prompt, localizedMemberName));
                    startActivity(loginIntent);
                    finish();
                };

                btnAction.setOnClickListener(switchProfileListener);
                memberView.setOnClickListener(switchProfileListener);
            }

            llFamilyMembers.addView(memberView);
        }
    }

    private void showUploadDocumentDialog() {
        if (currentBeneficiary == null) return;
        pendingUploadUri = null;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_upload_document, null);
        AutoCompleteTextView actvDocType = dialogView.findViewById(R.id.actvDocType);
        TextInputEditText etDocNumber = dialogView.findViewById(R.id.etDocNumber);
        TextInputEditText etDocAuthority = dialogView.findViewById(R.id.etDocAuthority);
        MaterialButton btnChooseFile = dialogView.findViewById(R.id.btnChooseFile);
        tvDialogSelectedFile = dialogView.findViewById(R.id.tvSelectedFileName);

        String[] docSuggestions = new String[]{
                "ST Caste Certificate",
                "Annual Household Income Certificate",
                "Class X / Secondary Marksheet",
                "Class XII / Senior Secondary Marksheet",
                "Bonafide Student Certificate",
                "Institute Admission Letter",
                "Domicile / Resident Certificate",
                "Bank Account Passbook / Statement",
                "Aadhaar Card Copy",
                "Other Supporting Document"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.item_dropdown_text, docSuggestions
        );
        actvDocType.setAdapter(adapter);
        actvDocType.setText(docSuggestions[0], false);
        dialogActvDocType = actvDocType;

        btnChooseFile.setOnClickListener(v -> launchIndependentFilePicker());

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Upload & Save", (dialog, which) -> {
                    String title = actvDocType.getText() != null ? actvDocType.getText().toString().trim() : "";
                    String docNumber = etDocNumber.getText() != null ? etDocNumber.getText().toString().trim() : "";
                    String authority = etDocAuthority.getText() != null ? etDocAuthority.getText().toString().trim() : "";

                    if (title.isEmpty()) {
                        title = "General Document";
                    }
                    if (pendingUploadUri == null) {
                        Toast.makeText(this, "Please choose and verify an authentic PDF file to upload", Toast.LENGTH_LONG).show();
                        return;
                    }

                    saveIndependentDocument(title, docNumber, authority, pendingUploadUri);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    pendingUploadUri = null;
                    tvDialogSelectedFile = null;
                    dialogActvDocType = null;
                })
                .setOnDismissListener(dialog -> {
                    tvDialogSelectedFile = null;
                    dialogActvDocType = null;
                })
                .show();
    }

    private void saveIndependentDocument(String title, String docNumber, String authority, Uri fileUri) {
        String fileName = resolveFileName(fileUri);
        if (!isPdfFile(fileUri, fileName)) {
            Toast.makeText(this, "Only PDF files (.pdf) are allowed.", Toast.LENGTH_LONG).show();
            return;
        }
        String savedFilePath = DocumentStorageManager.saveFileLocally(
                this, currentBeneficiary.getOtrId(), fileUri, "wallet"
        );
        String mimeType = DocumentStorageManager.getMimeType(this, fileUri);

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String currentDateStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        String finalDocNum = (docNumber.isEmpty() ? "DOC-" + timestamp : docNumber) + " [" + fileName + "]";
        String finalAuthority = authority.isEmpty() ? "DigiLocker / Self-Uploaded" : authority;

        WalletDocument newDoc = new WalletDocument(
                "DOC-MANUAL-" + timestamp,
                title,
                finalAuthority,
                finalDocNum,
                "Uploaded & Verified",
                currentDateStr,
                false,
                true,
                savedFilePath != null ? savedFilePath : "",
                mimeType
        );

        repository.addDocument(currentBeneficiary.getOtrId(), newDoc);

        // Sync uploaded document to cloud registry
        try {
            gov.mota.scholarship.data.remote.SupabaseClient.getInstance().upsertDocument(newDoc, new gov.mota.scholarship.data.remote.SupabaseClient.Callback<Boolean>() {
                @Override public void onSuccess(Boolean result) {}
                @Override public void onError(Exception e) {}
            });
        } catch (Exception ignored) {}

        currentBeneficiary = repository.findByOtrId(currentBeneficiary.getOtrId());
        if (currentBeneficiary != null && currentBeneficiary.getDocuments() != null) {
            rvDocuments.setAdapter(new WalletDocumentAdapter(this, currentBeneficiary.getDocuments()));
            tvEmptyDocuments.setVisibility(currentBeneficiary.getDocuments().isEmpty() ? View.VISIBLE : View.GONE);
        }

        Toast.makeText(this, "✓ " + title + " uploaded and saved to your Digital Wallet!", Toast.LENGTH_LONG).show();
        pendingUploadUri = null;
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

    private boolean isPdfFile(Uri uri, String fileName) {
        if (fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
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
}
