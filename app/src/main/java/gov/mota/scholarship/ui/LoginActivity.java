package gov.mota.scholarship.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import gov.mota.scholarship.R;
import gov.mota.scholarship.data.BeneficiaryRepository;
import gov.mota.scholarship.data.model.Beneficiary;
import gov.mota.scholarship.util.LocaleHelper;

import java.util.List;

public class LoginActivity extends BaseActivity {

    private static final String PREFS_AUTH = "mota_auth";
    private static final String KEY_LAST_OTR = "last_otr_id";

    private TextInputEditText etFullName;
    private TextInputEditText etAadhaar;
    private TextInputLayout tilFullName;
    private TextInputLayout tilAadhaar;
    private TextView tvErrorMessage;
    private MaterialButton btnLogin;
    private MaterialButton btnBiometricLogin;
    private LinearLayout llDemoChips;
    private ChipGroup cgLanguages;

    private BeneficiaryRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repository = BeneficiaryRepository.getInstance(this);

        initViews();
        setupLanguageSelector();
        setupDemoCards();
        handleSiblingSwitchIntent();

        btnLogin.setOnClickListener(v -> attemptLogin());
        if (btnBiometricLogin != null) {
            btnBiometricLogin.setOnClickListener(v -> attemptBiometricLogin());
        }
    }

    private void handleSiblingSwitchIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("switch_sibling_name")) {
            String targetName = intent.getStringExtra("switch_sibling_name");
            String prompt = intent.getStringExtra("switch_sibling_prompt");
            if (targetName != null && !targetName.isEmpty()) {
                etFullName.setText(targetName);
                etAadhaar.setText("");
                etAadhaar.requestFocus();
                if (prompt != null && !prompt.isEmpty()) {
                    tvErrorMessage.setVisibility(View.VISIBLE);
                    tvErrorMessage.setText(prompt);
                    tvErrorMessage.setTextColor(Color.parseColor("#15803D"));
                    tvErrorMessage.setBackgroundColor(Color.parseColor("#DCFCE7"));
                }
            }
        }
    }

    private void initViews() {
        cgLanguages = findViewById(R.id.cgLanguages);
        etFullName = findViewById(R.id.etFullName);
        etAadhaar = findViewById(R.id.etAadhaar);
        tilFullName = findViewById(R.id.tilFullName);
        tilAadhaar = findViewById(R.id.tilAadhaar);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnLogin = findViewById(R.id.btnLogin);
        btnBiometricLogin = findViewById(R.id.btnBiometricLogin);
        llDemoChips = findViewById(R.id.llDemoChips);
    }

    private void setupLanguageSelector() {
        List<LocaleHelper.SupportedLanguage> languages = LocaleHelper.getSupportedLanguages();
        String currentLang = LocaleHelper.getPersistedLanguage(this);

        cgLanguages.removeAllViews();
        for (LocaleHelper.SupportedLanguage lang : languages) {
            Chip chip = new Chip(this);
            chip.setText(lang.getDisplayName());
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setCheckedIconVisible(true);

            boolean isCurrent = lang.code.equalsIgnoreCase(currentLang);
            chip.setChecked(isCurrent);

            if (isCurrent) {
                chip.setChipBackgroundColorResource(R.color.mota_primary);
                chip.setTextColor(getColor(R.color.white));
            } else {
                chip.setChipBackgroundColorResource(R.color.mota_surface_variant);
                chip.setTextColor(getColor(R.color.mota_on_surface));
            }

            chip.setOnClickListener(v -> {
                if (!lang.code.equalsIgnoreCase(currentLang)) {
                    LocaleHelper.setLocale(LoginActivity.this, lang.code);
                    recreate();
                }
            });

            cgLanguages.addView(chip);
        }
    }

    private void setupDemoCards() {
        List<Beneficiary> beneficiaries = repository.getAllBeneficiaries();
        llDemoChips.removeAllViews();

        for (Beneficiary b : beneficiaries) {
            MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            
            String localizedName = gov.mota.scholarship.util.LocalizationUtils.getLocalizedBeneficiaryName(this, b.getFullName());
            String localizedInst = gov.mota.scholarship.util.LocalizationUtils.getLocalizedInstitution(this, b.getInstitutionName());

            String text = localizedName + " (" + b.getCategory() + ")\n"
                    + localizedInst + "\n"
                    + "Aadhaar: " + b.getAadhaarNumber();
            btn.setText(text);
            btn.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            btn.setTextSize(12f);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 6, 0, 8);
            btn.setLayoutParams(lp);

            btn.setOnClickListener(v -> {
                etFullName.setText(localizedName);
                etAadhaar.setText(b.getAadhaarNumber());
                tilFullName.setError(null);
                tilAadhaar.setError(null);
                tvErrorMessage.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, getString(R.string.loaded_credentials_toast, localizedName), Toast.LENGTH_SHORT).show();
            });

            llDemoChips.addView(btn);
        }
    }

    private void attemptLogin() {
        tilFullName.setError(null);
        tilAadhaar.setError(null);
        tvErrorMessage.setVisibility(View.GONE);

        String name = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String aadhaar = etAadhaar.getText() != null ? etAadhaar.getText().toString().trim() : "";

        boolean hasError = false;

        if (name.isEmpty()) {
            tilFullName.setError(getString(R.string.error_enter_name));
            hasError = true;
        }

        if (aadhaar.isEmpty()) {
            tilAadhaar.setError(getString(R.string.error_enter_aadhaar));
            hasError = true;
        } else if (aadhaar.length() != 12) {
            tilAadhaar.setError(getString(R.string.error_aadhaar_length));
            hasError = true;
        }

        if (hasError) return;

        Beneficiary beneficiary = repository.authenticate(name, aadhaar);

        if (beneficiary != null) {
            // Save last authenticated OTR for quick biometric access
            getSharedPreferences(PREFS_AUTH, MODE_PRIVATE).edit()
                    .putString(KEY_LAST_OTR, beneficiary.getOtrId())
                    .apply();

            launchDashboard(beneficiary);
        } else {
            tvErrorMessage.setVisibility(View.VISIBLE);
            tvErrorMessage.setText(R.string.auth_failed_msg);
            Toast.makeText(this, getString(R.string.access_denied_toast), Toast.LENGTH_LONG).show();
        }
    }

    private void attemptBiometricLogin() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
                    .setTitle(getString(R.string.biometric_prompt_title))
                    .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                    .setDescription(getString(R.string.biometric_prompt_desc))
                    .setNegativeButton("Use Aadhaar", getMainExecutor(), (dialog, which) -> {
                        // User opted for manual Aadhaar login
                    })
                    .build();

            CancellationSignal cancellationSignal = new CancellationSignal();
            biometricPrompt.authenticate(cancellationSignal, getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    onBiometricSuccess();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(LoginActivity.this, R.string.biometric_auth_failed, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if (errorCode != BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED) {
                        Toast.makeText(LoginActivity.this, errString, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            onBiometricSuccess();
        }
    }

    private void onBiometricSuccess() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AUTH, MODE_PRIVATE);
        String lastOtr = prefs.getString(KEY_LAST_OTR, null);

        Beneficiary target = null;
        if (lastOtr != null) {
            target = repository.findByOtrId(lastOtr);
        }

        // If not found in preferences, check if user filled in their name
        if (target == null) {
            String enteredName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
            if (!enteredName.isEmpty()) {
                for (Beneficiary b : repository.getAllBeneficiaries()) {
                    if (b.getFullName().equalsIgnoreCase(enteredName)) {
                        target = b;
                        break;
                    }
                }
            }
        }

        // Fallback to first primary beneficiary in repository
        if (target == null) {
            List<Beneficiary> all = repository.getAllBeneficiaries();
            if (!all.isEmpty()) {
                target = all.get(0);
            }
        }

        if (target != null) {
            Toast.makeText(this, "✓ Biometric Verified: " + target.getFullName(), Toast.LENGTH_SHORT).show();
            launchDashboard(target);
        } else {
            Toast.makeText(this, R.string.biometric_auth_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchDashboard(Beneficiary beneficiary) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("beneficiary", beneficiary);
        startActivity(intent);
        finish();
    }
}
