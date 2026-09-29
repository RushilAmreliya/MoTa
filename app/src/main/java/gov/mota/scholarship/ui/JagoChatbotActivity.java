package gov.mota.scholarship.ui;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import gov.mota.scholarship.R;
import gov.mota.scholarship.data.model.Beneficiary;
import gov.mota.scholarship.data.model.ChatMessage;
import gov.mota.scholarship.data.util.JagoChatbotEngine;
import gov.mota.scholarship.util.LocaleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interactive JAGO AI Chatbot Activity.
 * Connected directly to the live beneficiary profile, scholarship database,
 * Speech-to-Text (Voice In), and Text-to-Speech (Voice Out).
 */
public class JagoChatbotActivity extends BaseActivity {

    public static final String EXTRA_BENEFICIARY = "extra_beneficiary";

    private MaterialToolbar toolbarChatbot;
    private RecyclerView rvChatMessages;
    private EditText etChatInput;
    private MaterialButton btnVoiceInput;
    private MaterialButton btnSendMessage;
    private MaterialButton chipStatus;
    private MaterialButton chipDbt;
    private MaterialButton chipDocs;
    private MaterialButton chipVerification;
    private MaterialButton chipEligibility;

    private Beneficiary beneficiary;
    private final List<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;

    private TextToSpeech textToSpeech;
    private boolean isTtsEnabled = true;

    private final ActivityResultLauncher<Intent> speechRecognizerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String bestSpokenText = matches.get(0);
                        for (String candidate : matches) {
                            if (candidate != null && candidate.length() > bestSpokenText.length()) {
                                bestSpokenText = candidate;
                            }
                        }
                        etChatInput.setText(bestSpokenText);
                        handleSend();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jago_chatbot);

        beneficiary = (Beneficiary) getIntent().getSerializableExtra(EXTRA_BENEFICIARY);
        if (beneficiary == null) {
            Toast.makeText(this, "Beneficiary profile missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupTextToSpeech();
        setupRecyclerView();
        setupQuickChips();
        postWelcomeMessage();
    }

    private void initViews() {
        toolbarChatbot = findViewById(R.id.toolbarChatbot);
        rvChatMessages = findViewById(R.id.rvChatMessages);
        etChatInput = findViewById(R.id.etChatInput);
        btnVoiceInput = findViewById(R.id.btnVoiceInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        chipStatus = findViewById(R.id.chipStatus);
        chipDbt = findViewById(R.id.chipDbt);
        chipDocs = findViewById(R.id.chipDocs);
        chipVerification = findViewById(R.id.chipVerification);
        chipEligibility = findViewById(R.id.chipEligibility);

        toolbarChatbot.setNavigationOnClickListener(v -> finish());
        toolbarChatbot.inflateMenu(R.menu.menu_jago_chat);
        toolbarChatbot.setOnMenuItemClickListener(this::onToolbarMenuItemClick);

        btnSendMessage.setOnClickListener(v -> handleSend());
        if (btnVoiceInput != null) {
            btnVoiceInput.setOnClickListener(v -> startVoiceRecognition());
        }

        etChatInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handleSend();
                return true;
            }
            return false;
        });
    }

    private void setupTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                String lang = LocaleHelper.getPersistedLanguage(this);
                Locale locale = new Locale(lang, "IN");
                int res = textToSpeech.setLanguage(locale);
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra("android.speech.extra.DICTATION_MODE", true);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 6000L);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3500L);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        String lang = LocaleHelper.getPersistedLanguage(this);
        String langTag = getLanguageTag(lang);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false);
        intent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                new String[]{"hi-IN", "en-IN", "bn-IN", "or-IN", "mr-IN", "gu-IN", "te-IN"});
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.jago_voice_prompt));

        try {
            speechRecognizerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getLanguageTag(String langCode) {
        if ("hi".equalsIgnoreCase(langCode)) return "hi-IN";
        if ("bn".equalsIgnoreCase(langCode)) return "bn-IN";
        if ("mr".equalsIgnoreCase(langCode)) return "mr-IN";
        if ("gu".equalsIgnoreCase(langCode)) return "gu-IN";
        if ("te".equalsIgnoreCase(langCode)) return "te-IN";
        if ("or".equalsIgnoreCase(langCode)) return "or-IN";
        if ("sat".equalsIgnoreCase(langCode)) return "hi-IN";
        return "en-IN";
    }

    private boolean onToolbarMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_tts_toggle) {
            isTtsEnabled = !isTtsEnabled;
            if (isTtsEnabled) {
                item.setIcon(android.R.drawable.ic_lock_silent_mode_off);
                Toast.makeText(this, R.string.voice_readout_on, Toast.LENGTH_SHORT).show();
            } else {
                item.setIcon(android.R.drawable.ic_lock_silent_mode);
                if (textToSpeech != null) {
                    textToSpeech.stop();
                }
                Toast.makeText(this, R.string.voice_readout_off, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(this, messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);
        rvChatMessages.setAdapter(chatAdapter);
    }

    private void setupQuickChips() {
        chipStatus.setOnClickListener(v -> sendPredefinedMessage("Track my scholarship application status"));
        chipDbt.setOnClickListener(v -> sendPredefinedMessage("What is my DBT payment and bank status?"));
        chipDocs.setOnClickListener(v -> sendPredefinedMessage("Which documents are pending or verified?"));
        chipVerification.setOnClickListener(v -> sendPredefinedMessage("Show my verification lifecycle progress"));
        chipEligibility.setOnClickListener(v -> sendPredefinedMessage("Which MoTA scholarship schemes am I eligible for?"));
    }

    private void postWelcomeMessage() {
        String welcome = "Namaste " + beneficiary.getFullName() + "! 🙏\n"
                + "I am JAGO, your AI Virtual Assistant for the Ministry of Tribal Affairs (MoTA) Unified Scholarship Portal.\n\n"
                + "I can help you with:\n"
                + "• Real-time application tracking across NSP/SFMP\n"
                + "• Direct Benefit Transfer (DBT) & NPCI Aadhaar bank status\n"
                + "• Verified Digital Wallet documents\n"
                + "• Multi-scheme eligibility guidelines\n\n"
                + "Tap a quick option above, type, or tap the microphone to speak your question!";
        addMessage(new ChatMessage(ChatMessage.TYPE_BOT, welcome));
    }

    private void sendPredefinedMessage(String text) {
        etChatInput.setText(text);
        handleSend();
    }

    private void handleSend() {
        String input = etChatInput.getText().toString().trim();
        if (input.isEmpty()) return;

        // Add user message
        addMessage(new ChatMessage(ChatMessage.TYPE_USER, input));
        etChatInput.setText("");

        // Process via JAGO Chatbot Engine
        JagoChatbotEngine.processMessage(this, beneficiary, input, replyText -> {
            addMessage(new ChatMessage(ChatMessage.TYPE_BOT, replyText));
            if (isTtsEnabled) {
                speakText(replyText);
            }
        });
    }

    private void speakText(String text) {
        if (textToSpeech != null && text != null && !text.isEmpty()) {
            int devanagariCount = 0;
            int bengaliCount = 0;
            int odiaCount = 0;
            int gujaratiCount = 0;
            int teluguCount = 0;

            for (char c : text.toCharArray()) {
                if (c >= '\u0900' && c <= '\u097F') devanagariCount++;
                else if (c >= '\u0980' && c <= '\u09FF') bengaliCount++;
                else if (c >= '\u0B00' && c <= '\u0B7F') odiaCount++;
                else if (c >= '\u0A80' && c <= '\u0AFF') gujaratiCount++;
                else if (c >= '\u0C00' && c <= '\u0C7F') teluguCount++;
            }

            Locale targetLocale;
            if (bengaliCount > 5) {
                targetLocale = new Locale("bn", "IN");
            } else if (gujaratiCount > 5) {
                targetLocale = new Locale("gu", "IN");
            } else if (teluguCount > 5) {
                targetLocale = new Locale("te", "IN");
            } else if (odiaCount > 5) {
                targetLocale = new Locale("or", "IN");
            } else if (devanagariCount > 5) {
                if (text.contains("आहे") || text.contains("पाहिजे") || text.contains("मिळेल") || text.contains("मराठी")) {
                    targetLocale = new Locale("mr", "IN");
                } else {
                    targetLocale = new Locale("hi", "IN");
                }
            } else {
                String lang = LocaleHelper.getPersistedLanguage(this);
                targetLocale = new Locale(lang, "IN");
            }

            int result = textToSpeech.setLanguage(targetLocale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech.setLanguage(new Locale("hi", "IN"));
            }

            String cleanText = text.replace("*", "").replace("•", "").replace("#", "").replace("🌐", "").trim();
            textToSpeech.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "JAGO_TTS_ID");
        }
    }

    private void addMessage(ChatMessage msg) {
        messageList.add(msg);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
