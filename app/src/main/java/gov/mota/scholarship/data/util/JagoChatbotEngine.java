package gov.mota.scholarship.data.util;

import android.content.Context;

import gov.mota.scholarship.data.BeneficiaryRepository;
import gov.mota.scholarship.data.SchemeCatalogRepository;
import gov.mota.scholarship.data.model.Beneficiary;
import gov.mota.scholarship.data.model.SchemeApplication;
import gov.mota.scholarship.data.model.SchemeCatalogItem;
import gov.mota.scholarship.data.model.WalletDocument;
import gov.mota.scholarship.util.LocaleHelper;

import java.util.List;
import java.util.Locale;

/**
 * Intelligent Multilingual Conversational AI Engine for MoTA JAGO Assistant.
 * 
 * Provides deep local language understanding (Hindi, Hinglish, Regional, English)
 * and generates precise, data-grounded responses using the student's live scholarship records,
 * DBT banking status, and official Ministry scheme guidelines.
 */
public class JagoChatbotEngine {

    public interface ChatReplyCallback {
        void onReply(String responseText);
    }

    public static void processMessage(Context context, Beneficiary beneficiary, String userMessage, ChatReplyCallback callback) {
        if (beneficiary == null || userMessage == null || userMessage.trim().isEmpty()) {
            callback.onReply("Namaste! I am JAGO, your MoTA Scholarship AI Virtual Assistant. How can I help you today?");
            return;
        }

        // Fetch live beneficiary record
        Beneficiary live = BeneficiaryRepository.getInstance(context).findByOtrId(beneficiary.getOtrId());
        if (live == null) live = beneficiary;

        String studentName = live.getFullName();
        String m = userMessage.trim();
        String lowerMsg = m.toLowerCase(Locale.ROOT);

        // Detect user language
        String persistedLang = LocaleHelper.getPersistedLanguage(context);
        String detectedLang = detectLanguage(lowerMsg, m, persistedLang);

        // Classify intent
        IntentType intent = classifyIntent(lowerMsg);

        String reply;
        switch (intent) {
            case STATUS_AND_PAYMENT:
                reply = buildStatusAndPaymentResponse(studentName, live, detectedLang);
                break;
            case PAYMENT_DBT:
                reply = buildPaymentResponse(studentName, live, detectedLang);
                break;
            case STATUS_TRACK:
                reply = buildStatusResponse(studentName, live, detectedLang);
                break;
            case DOCUMENTS:
                reply = buildDocumentsResponse(studentName, live, detectedLang);
                break;
            case VERIFICATION_STAGE:
                reply = buildVerificationResponse(studentName, live, detectedLang);
                break;
            case DEFICIENCY_FIX:
                reply = buildDeficiencyResponse(studentName, live, detectedLang);
                break;
            case PRE_MATRIC:
                reply = buildPreMatricResponse(studentName, live, detectedLang);
                break;
            case POST_MATRIC:
                reply = buildPostMatricResponse(studentName, live, detectedLang);
                break;
            case TOP_CLASS:
                reply = buildTopClassResponse(studentName, live, detectedLang);
                break;
            case NFST_FELLOWSHIP:
                reply = buildNfstResponse(studentName, live, detectedLang);
                break;
            case NOS_OVERSEAS:
                reply = buildNosResponse(studentName, live, detectedLang);
                break;
            case NPCI_AADHAAR_HELP:
                reply = buildNpciHelpResponse(studentName, live, detectedLang);
                break;
            case GREETING:
                reply = buildGreetingResponse(studentName, live, detectedLang);
                break;
            case ELIGIBILITY_ALL:
            default:
                reply = buildGeneralOrEligibilityResponse(studentName, live, detectedLang, lowerMsg);
                break;
        }

        callback.onReply(reply);
    }

    private enum IntentType {
        STATUS_AND_PAYMENT,
        PAYMENT_DBT,
        STATUS_TRACK,
        DOCUMENTS,
        VERIFICATION_STAGE,
        DEFICIENCY_FIX,
        PRE_MATRIC,
        POST_MATRIC,
        TOP_CLASS,
        NFST_FELLOWSHIP,
        NOS_OVERSEAS,
        NPCI_AADHAAR_HELP,
        GREETING,
        ELIGIBILITY_ALL
    }

    /**
     * Automatic language detector across 8 languages:
     * - "hi" : Hindi / Hinglish
     * - "mr" : Marathi (Devanagari script with Marathi markers)
     * - "bn" : Bengali / Banglish
     * - "or" : Odia
     * - "gu" : Gujarati
     * - "te" : Telugu
     * - "sat": Santali (Ol Chiki script: \u1C50 - \u1C7F)
     * - "en" : English (Default fallback)
     */
    public static String detectLanguage(String lowerMsg, String rawMsg, String persistedLang) {
        int devanagariCount = 0;
        int bengaliCount = 0;
        int odiaCount = 0;
        int gujaratiCount = 0;
        int teluguCount = 0;
        int olChikiCount = 0;

        for (char c : rawMsg.toCharArray()) {
            if (c >= '\u0900' && c <= '\u097F') devanagariCount++;
            else if (c >= '\u0980' && c <= '\u09FF') bengaliCount++;
            else if (c >= '\u0B00' && c <= '\u0B7F') odiaCount++;
            else if (c >= '\u0A80' && c <= '\u0AFF') gujaratiCount++;
            else if (c >= '\u0C00' && c <= '\u0C7F') teluguCount++;
            else if (c >= '\u1C50' && c <= '\u1C7F') olChikiCount++;
        }

        // Script-based high confidence detections
        if (bengaliCount >= 2) return "bn";
        if (odiaCount >= 2) return "or";
        if (gujaratiCount >= 2) return "gu";
        if (teluguCount >= 2) return "te";
        if (olChikiCount >= 2) return "sat";

        // Devanagari can be Marathi or Hindi
        if (devanagariCount >= 2) {
            String[] marathiDevanagariTokens = new String[]{
                    "आहे", "नाही", "कधी", "येतील", "माझे", "माझा", "माझी", "झाले", "झाली", "पाहिजे",
                    "कसे", "मिळेल", "मिळतील", "पैसे", "खात्यात", "अर्जाची", "स्थिती", "कागदपत्रे"
            };
            for (String t : marathiDevanagariTokens) {
                if (lowerMsg.contains(t)) return "mr";
            }
            return "hi";
        }

        // Romanized Bengali (Banglish) indicators
        String[] banglishTokens = new String[]{
                "amar", "amake", "kobe", "taka", "ashbe", "pabo", "kothay", "abedon", "hobe", "shoshthi"
        };
        for (String t : banglishTokens) {
            if (lowerMsg.contains(t)) return "bn";
        }

        // Romanized Marathi indicators
        String[] marathiRomanTokens = new String[]{
                "majhe", "majha", "majhi", "kadhi", "yetil", "paise", "ahe", "nahi", "pahije", "kase", "milel"
        };
        for (String t : marathiRomanTokens) {
            if (lowerMsg.contains(t)) return "mr";
        }

        // Romanized Gujarati indicators
        String[] gujaratiRomanTokens = new String[]{
                "maro", "mari", "maru", "kyare", "aavse", "paisa", "malashe", "kya", "arji", "status che"
        };
        for (String t : gujaratiRomanTokens) {
            if (lowerMsg.contains(t)) return "gu";
        }

        // Romanized Telugu indicators
        String[] teluguRomanTokens = new String[]{
                "naa", "eppudu", "vasthundi", "dabbulu", "vasthai", "vastayi", "darakhasthu", "choodali", "epudu"
        };
        for (String t : teluguRomanTokens) {
            if (lowerMsg.contains(t)) return "te";
        }

        // Romanized Odia indicators
        String[] odiaRomanTokens = new String[]{
                "mora", "mote", "kebe", "asiba", "tankaa", "miliba", "abedana", "stithi"
        };
        for (String t : odiaRomanTokens) {
            if (lowerMsg.contains(t)) return "or";
        }

        // Romanized Hindi (Hinglish) indicators
        String[] hinglishTokens = new String[]{
                "mera", "meri", "mere", "kab", "kahan", "kaise", "kya", "paisa", "paise", "aayega",
                "aayegi", "milega", "milegi", "nahi", "aaya", "karo", "batao", "chahiye", "stithi",
                "kitna", "kitni", "kaun", "kist", "khata", "namaste", "dastavej", "satyapan", "jaanch"
        };
        for (String token : hinglishTokens) {
            if (lowerMsg.contains(token)) return "hi";
        }

        // Fallback to persisted app locale if set and non-English
        if (persistedLang != null && !persistedLang.equalsIgnoreCase("en")) {
            return persistedLang.toLowerCase(Locale.ROOT);
        }

        return "en";
    }

    private static IntentType classifyIntent(String m) {
        // Only return GREETING if the input is purely a short greeting (never on long multi-word queries)
        if (matchesGreetingOnly(m)) {
            return IntentType.GREETING;
        }

        // Multi-clause scoring across the entire sentence (multilingual: Hindi, Marathi, Bengali, Odia, Gujarati, Telugu, Santali, English)
        int paymentScore = scoreMatches(m,
                "paisa", "paise", "payment", "dbt", "credit", "credited", "money", "rupaye", "amount", "installment", "kist", "account me", "disburs", "kab aayega", "kab milega", "kitna paisa", "kitne rupaye",
                "पैसे", "पैसे कधी", "मिळतील", "रुपये", // Marathi
                "টাকা", "কবে টাকা", "পাব", "অর্থ", "টাকা আসবে", // Bengali
                "ଟଙ୍କା", "କେବେ ଆସିବ", "ଟଙ୍କା ମିଳିବ", "ରାଶି", // Odia
                "પૈસા", "ક્યારે આવશે", "રૂપિયા", "મળશે", // Gujarati
                "డబ్బులు", "ఎప్పుడు వస్తాయి", "చెల్లింపు", "ఖాతాలో", // Telugu
                "ᱴᱟᱠᱟ", "ᱯᱩᱭᱥᱟᱹ", // Santali
                "khata", "bank me", "paisa kab", "sanction amount", "bharpai", "credit kab");

        int statusScore = scoreMatches(m,
                "status", "track", "application", "kahan pahuncha", "kahan tak", "sthiti", "form check", "applied", "check status", "progress", "app id", "kya hua", "aavedan", "kya chal raha", "form ka kya",
                "स्थिति", "अर्जाची स्थिती", "अर्ज कुठे", // Marathi
                "অবস্থা", "আবেদনের অবস্থা", "আবেদন ট্র্যাকিং", // Bengali
                "ସ୍ଥିତି", "ଆବେଦନ ସ୍ଥିତି", "ଟ୍ରାକ", // Odia
                "સ્થિતિ", "અરજીની સ્થિતિ", "ક્યાં પહોંચી", // Gujarati
                "స్థితి", "దరఖాస్తు స్థితి", "ఎక్కడి వరకు", // Telugu
                "ᱟᱨᱡᱤ ᱨᱮᱭᱟᱜ ᱦᱟᱞᱚᱛ", // Santali
                "kahan aya", "aavedan sthiti", "form bhara");

        int docScore = scoreMatches(m,
                "document", "documents", "cert", "certificate", "praman patra", "dastavej", "income cert", "caste cert", "marksheet", "digilocker", "reupload", "missing doc", "upload", "kaagaz", "aay praman", "jati praman",
                "कागदपत्रे", "दाखला", "प्रमाणपत्र", "कागद", // Marathi
                "নথি", "শংসাপত্র", "ডকুমেন্ট", "জাতি শংসাপত্র", "আয়ের শংসাপত্র", // Bengali
                "ଦଲିଲ", "ଦସ୍ତାବିଜ", "ପ୍ରମାଣପତ୍ର", "ଜାତି ପ୍ରମାଣପତ୍ର", // Odia
                "દસ્તાવેજ", "પ્રમાણપત્ર", "જાતિનો દાખલો", "આવકનો દાખલો", // Gujarati
                "పత్రాలు", "సర్టిఫికెట్", "కులం సర్టిఫికెట్", "ఆదాయ ధృవీకరణ", // Telugu
                "ᱫᱚᱞᱤᱞ", "ᱥᱟᱠᱟᱢ", // Santali
                "कागज़", "दस्तावेज़", "praman", "marks", "certificate upload");

        int verifScore = scoreMatches(m,
                "verif", "stage", "lifecycle", "satyapan", "institute verification", "district approval", "sanction", "approval",
                "पडताळणी", "सत्यापन", "महाविद्यालय पडताळणी", // Marathi
                "যাচাইকরণ", "যাচাই", "প্রতিষ্ঠান যাচাইকরণ", // Bengali
                "ଯାଞ୍ଚ", "ଅନୁଷ୍ଠାନ ଯାଞ୍ଚ", "ଅନୁମୋଦନ", // Odia
                "ચકાસણી", "સંસ્થા ચકાસણી", "મંજૂરી", // Gujarati
                "ధృవీకరణ", "సంస్థ ధృవీకరణ", "ఆమోదం", // Telugu
                "ᱯᱚᱨᱠᱷᱟᱹᱣ", // Santali
                "college verify", "school verify", "ino", "dwo", "charan", "verification pending");

        int deficScore = scoreMatches(m,
                "deficien", "rejected", "reject", "correction", "problem", "issue", "galti", "truti", "khata kharab", "khata band", "failed", "aswikar", "action required", "hold", "remedy",
                "त्रुटी", "अडचण", "समस्या", "नाकारले", // Marathi
                "ত্রুটি", "সমস্যা", "বাতিল", "সংশোধন", // Bengali
                "ତ୍ରୁଟି", "ସମସ୍ୟା", "ଅଗ୍ରାହ୍ୟ", // Odia
                "ખામી", "સમસ્યા", "રદ", "સુધારો", // Gujarati
                "లోపం", "సమస్య", "తిరస్కరణ", "సవరణ", // Telugu
                "ᱮᱴᱠᱮᱴᱚᱬᱮ"); // Santali

        int npciScore = scoreMatches(m,
                "npci", "aadhaar seed", "bank link", "mapper inactive", "seeding", "mandate", "branch", "mapper", "bank problem", "bank link nahi", "aadhaar bridge",
                "आधार लिंक", "बँक समस्या", "एनपीसीआय", // Marathi / Hindi
                "আধার লিঙ্ক", "ব্যাঙ্ক সমস্যা", "NPCI", // Bengali
                "ଆଧାର ଲିଙ୍କ", "ବ୍ୟାଙ୍କ ସମସ୍ୟା", // Odia
                "આધાર લિંક", "બેંક સમસ્યા", // Gujarati
                "ఆధార్ లింక్", "బ్యాంక్ సమస్య"); // Telugu

        int preMatricScore = scoreMatches(m,
                "pre-matric", "pre matric", "9th", "10th", "class 9", "class 10", "school scholarship", "navin", "dasvi", "prematric",
                "प्री-मॅट्रिक", "इयत्ता ९", "इयत्ता १०", // Marathi
                "প্রি-ম্যাট্রিক", "নবম", "দশম", // Bengali
                "ପ୍ରି-ମାଟ୍ରିକ", // Odia
                "પ્રી-મેટ્રિક", "ધોરણ ૯", "ધોરણ ૧૦", // Gujarati
                "ప్రీ-మెట్రిక్", "9వ తరగతి", "10వ తరగతి", // Telugu
                "ᱯᱨᱤ-ᱢᱮᱴᱨᱤᱠ"); // Santali

        int postMatricScore = scoreMatches(m,
                "post-matric", "post matric", "11th", "12th", "college scholarship", "diploma", "graduation", "btech", "ba", "bsc", "postmatric", "degree scholarship", "polytechnic",
                "पोस्ट-मॅट्रिक", "पदवी", "महाविद्यालयीन", // Marathi
                "পোস্ট-ম্যাট্রিক", "স্নাতক", "কলেজ", // Bengali
                "ପୋଷ୍ଟ-ମାଟ୍ରିକ", "କଲେଜ", // Odia
                "પોસ્ટ-મેટ્રિક", "કોલેજ", "ડિગ્રી", // Gujarati
                "పోస్ట్-మెట్రిక్", "డిగ్రీ", "కళాశాల", // Telugu
                "ᱯᱳᱥᱴ-ᱢᱮᱴᱨᱤᱠ"); // Santali

        int topClassScore = scoreMatches(m, "top class", "topclass", "iit", "iim", "nit", "aiims", "nlu", "premier institute", "tuition claim", "premier", "ટોપ ક્લાસ", "শীর্ষ শ্রেণি", "శ్రేష్ట ଶ୍ରେଣୀ", "టాప్ క్లాస్", "टॉप क्लास");

        int nfstScore = scoreMatches(m, "nfst", "fellowship", "phd", "ph.d", "mphil", "ugc net", "jrf", "research grant", "canara sfmp", "fellow", "फेलोशिप", "ফেলোশিপ", "ଫେଲୋସିପ", "ફેલોશિપ", "ఫెలోషిప్", "ᱯᱷᱮᱞᱳᱥᱤᱯ");

        int nosScore = scoreMatches(m, "nos", "overseas", "foreign", "abroad", "foreign study", "uk", "usa", "overseas scholarship", "videsh", "bahar padhai", "विदेश", "বিদেশ", "ବିଦେଶ", "વિદેશ", "విదేశీ");

        // Compound long sentence queries: Status + Payment
        if (statusScore >= 2 && paymentScore >= 2) {
            return IntentType.STATUS_AND_PAYMENT;
        }

        // Scheme priority
        int maxSchemeScore = Math.max(preMatricScore, Math.max(postMatricScore, Math.max(topClassScore, Math.max(nfstScore, nosScore))));
        if (maxSchemeScore >= 2) {
            if (maxSchemeScore == preMatricScore) return IntentType.PRE_MATRIC;
            if (maxSchemeScore == postMatricScore) return IntentType.POST_MATRIC;
            if (maxSchemeScore == topClassScore) return IntentType.TOP_CLASS;
            if (maxSchemeScore == nfstScore) return IntentType.NFST_FELLOWSHIP;
            if (maxSchemeScore == nosScore) return IntentType.NOS_OVERSEAS;
        }

        // Specific category priority
        if (npciScore >= 2 && npciScore >= paymentScore) return IntentType.NPCI_AADHAAR_HELP;
        if (deficScore >= 2 && deficScore >= statusScore) return IntentType.DEFICIENCY_FIX;
        if (verifScore >= 4 && verifScore >= statusScore) return IntentType.VERIFICATION_STAGE;
        if (paymentScore >= 2 && paymentScore >= statusScore) return IntentType.PAYMENT_DBT;
        if (statusScore >= 2) return IntentType.STATUS_TRACK;
        if (docScore >= 2) return IntentType.DOCUMENTS;
        if (verifScore >= 2) return IntentType.VERIFICATION_STAGE;
        if (paymentScore >= 1) return IntentType.PAYMENT_DBT;

        return IntentType.ELIGIBILITY_ALL;
    }

    private static boolean matchesGreetingOnly(String m) {
        String cleaned = m.replaceAll("[^a-zA-Z\\u0900-\\u097F\\s]", " ").trim();
        String[] words = cleaned.split("\\s+");
        if (words.length > 3) {
            return false;
        }
        for (String w : words) {
            String lw = w.toLowerCase(Locale.ROOT);
            if (lw.equals("namaste") || lw.equals("hello") || lw.equals("hi") ||
                    lw.equals("hey") || lw.equals("johar") || lw.equals("pranam") ||
                    lw.equals("नमस्ते") || lw.equals("जोहार") || lw.equals("प्रणाम")) {
                return true;
            }
        }
        return false;
    }

    private static int scoreMatches(String text, String... patterns) {
        int score = 0;
        for (String p : patterns) {
            if (text.contains(p)) {
                score += 2;
                if (p.contains(" ")) {
                    score += 2;
                }
            }
        }
        return score;
    }

    private static String getLanguageBadge(String lang) {
        if ("bn".equalsIgnoreCase(lang)) return "🌐 [বাংলা - Bengali AI Response]\n\n";
        if ("or".equalsIgnoreCase(lang)) return "🌐 [ଓଡ଼ିଆ - Odia AI Response]\n\n";
        if ("gu".equalsIgnoreCase(lang)) return "🌐 [ગુજરાતી - Gujarati AI Response]\n\n";
        if ("mr".equalsIgnoreCase(lang)) return "🌐 [मराठी - Marathi AI Response]\n\n";
        if ("te".equalsIgnoreCase(lang)) return "🌐 [తెలుగు - Telugu AI Response]\n\n";
        if ("sat".equalsIgnoreCase(lang)) return "🌐 [ᱥᱟᱱᱛᱟᱲᱤ - Santali AI Response]\n\n";
        if ("hi".equalsIgnoreCase(lang)) return "🌐 [हिन्दी - Hindi AI Response]\n\n";
        return "";
    }

    private static String buildStatusAndPaymentResponse(String name, Beneficiary b, String lang) {
        String status = buildStatusResponse(name, b, lang);
        String payment = buildPaymentResponse(name, b, lang);
        return status + "\n\n────────────────────\n\n" + payment;
    }

    // ==========================================
    // RESPONSE GENERATORS (Multilingual)
    // ==========================================

    private static String buildPaymentResponse(String name, Beneficiary b, String lang) {
        List<SchemeApplication> apps = b.getApplications();
        SchemeApplication latestApp = (apps != null && !apps.isEmpty()) ? apps.get(0) : null;
        String amount = latestApp != null ? latestApp.getAmountInr() : "₹ 13,500/year";
        String scheme = latestApp != null ? latestApp.getSchemeTitle() : "MoTA Scholarship";
        String txn = latestApp != null ? latestApp.getPfmsTransactionId() : "PFMS-ACK-2026";
        String dbtStatus = latestApp != null ? latestApp.getDbtStatus() : "PENDING_VERIFICATION";

        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if ("bn".equalsIgnoreCase(lang)) {
            sb.append("নমস্কার ").append(name).append("! 🙏\n\n");
            sb.append("💰 **DBT স্কলারশিপ পেমেন্ট বিবরণ:**\n");
            if (b.isNpciMapped()) {
                sb.append("• ব্যাঙ্ক স্ট্যাটাস: ✅ সক্রিয় ও আধার লিঙ্কযুক্ত (Active)\n")
                  .append("• ব্যাঙ্ক নাম: ").append(b.getBankName()).append(" (অ্যাকাউন্ট: ").append(b.getAccountMasked()).append(")\n")
                  .append("• স্কিম: ").append(scheme).append("\n")
                  .append("• অনুমোদিত অনুদান: ").append(amount).append("\n")
                  .append("• PFMS রেফারেন্স: ").append(txn).append("\n")
                  .append("• DBT স্থিতি: ").append(dbtStatus).append("\n\n")
                  .append("📌 **টাকা কবে অ্যাকাউন্টে ঢুকবে?**\n")
                  .append("প্রতিষ্ঠান ও জেলা যাচাই সম্পূর্ণ হলে মন্ত্রকের DBT প্রক্রিয়ার মাধ্যমে ৩ থেকে ৫ কার্যদিবসের মধ্যে টাকা সরাসরি আপনার ব্যাঙ্ক অ্যাকাউন্টে জমা হবে।");
            } else {
                sb.append("• ব্যাঙ্ক স্ট্যাটাস: ⚠ সমস্যা (NPCI আধার লিঙ্ক নিষ্ক্রিয়)\n")
                  .append("• ব্যাঙ্ক: ").append(b.getBankName()).append(" (অ্যাকাউন্ট: ").append(b.getAccountMasked()).append(")\n")
                  .append("• কারণ: ").append(b.getNpciFailureReason() != null ? b.getNpciFailureReason() : "ব্যাঙ্ক অ্যাকাউন্ট আধারের সাথে লিঙ্ক নেই").append("\n\n")
                  .append("🚨 **করণীয় পদক্ষেপ:**\n")
                  .append("অনতিবিলম্বে আপনার ব্যাঙ্ক শাখায় গিয়ে আধার DBT ম্যান্ডেট ফর্ম জমা দিন, নতুবা স্কলারশিপের টাকা পাঠানো সম্ভব হবে না।");
            }
            return sb.toString();
        } else if ("mr".equalsIgnoreCase(lang)) {
            sb.append("नमस्कार ").append(name).append("! 🙏\n\n");
            sb.append("💰 **DBT शिष्यवृत्ती जमा तपशील:**\n");
            if (b.isNpciMapped()) {
                sb.append("• बँक स्थिती: ✅ सक्रिय व आधार लिंक (Active)\n")
                  .append("• बँक नाव: ").append(b.getBankName()).append(" (खाते: ").append(b.getAccountMasked()).append(")\n")
                  .append("• योजना: ").append(scheme).append("\n")
                  .append("• मंजूर रक्कम: ").append(amount).append("\n")
                  .append("• PFMS संदर्भ: ").append(txn).append("\n")
                  .append("• DBT प्रगती: ").append(dbtStatus).append("\n\n")
                  .append("📌 **पैसे कधी जमा होणार?**\n")
                  .append("महाविद्यालय व जिल्हा स्तरावरील पडताळणी पूर्ण होताच मंत्रालयाच्या DBT चक्रानुसार ३ ते ५ कामकाजाच्या दिवसांत पैसे थेट बँक खात्यात जमा केले जातील.");
            } else {
                sb.append("• बँक स्थिती: ⚠ त्रुटी (NPCI मॅपर निष्क्रिय)\n")
                  .append("• बँक: ").append(b.getBankName()).append(" (खाते: ").append(b.getAccountMasked()).append(")\n")
                  .append("• कारण: ").append(b.getNpciFailureReason() != null ? b.getNpciFailureReason() : "बँक खाते आधारशी जोडलेले नाही").append("\n\n")
                  .append("🚨 **तातडीची कृती:**\n")
                  .append("त्वरित आपल्या बँक शाखेत जाऊन आधार DBT मॅन्डेट फॉर्म भरा, अन्यथा शिष्यवृत्ती जमा होणार नाही.");
            }
            return sb.toString();
        } else if ("gu".equalsIgnoreCase(lang)) {
            sb.append("નમસ્તે ").append(name).append("! 🙏\n\n");
            sb.append("💰 **DBT શિષ્યવૃત્તિ ચુકવણી સ્થિતિ:**\n");
            if (b.isNpciMapped()) {
                sb.append("• બેંક સ્થિતિ: ✅ સક્રિય અને આધાર લિંક (Active)\n")
                  .append("• બેંક: ").append(b.getBankName()).append(" (ખાતું: ").append(b.getAccountMasked()).append(")\n")
                  .append("• યોજના: ").append(scheme).append("\n")
                  .append("• મંજૂર રકમ: ").append(amount).append("\n")
                  .append("• PFMS સંદર્ભ: ").append(txn).append("\n")
                  .append("• DBT સ્થિતિ: ").append(dbtStatus).append("\n\n")
                  .append("📌 **પૈસા ક્યારે ખાતામાં આવશે?**\n")
                  .append("શાળા/કોલેજ ચકાસણી પૂર્ણ થતાં જ મંત્રાલય દ્વારા ૩ થી ૫ કામકાજના દિવસોમાં સીધા તમારા આધાર લિંક બેંક ખાતામાં જમા કરવામાં આવશે.");
            } else {
                sb.append("• બેંક સ્થિતિ: ⚠ જરૂરી પગલું (NPCI લિંક નથી)\n")
                  .append("• બેંક: ").append(b.getBankName()).append(" (ખાતું: ").append(b.getAccountMasked()).append(")\n")
                  .append("• કારણ: ").append(b.getNpciFailureReason() != null ? b.getNpciFailureReason() : "બેંક ખાતું આધાર સાથે જોડાયેલ નથી").append("\n\n")
                  .append("🚨 **સુધારણા માટે:**\n")
                  .append("તાત્કાલિક તમારી બેંક શાખાની મુલાકાત લો અને આધાર DBT મેન્ડેટ ફોર્મ જમા કરો.");
            }
            return sb.toString();
        } else if ("te".equalsIgnoreCase(lang)) {
            sb.append("నమస్కారం ").append(name).append("! 🙏\n\n");
            sb.append("💰 **DBT స్కాలర్‌షిప్ చెల్లింపు వివరాలు:**\n");
            if (b.isNpciMapped()) {
                sb.append("• బ్యాంక్ స్థితి: ✅ యాక్టివ్ & ఆధార్ లింక్ (Active)\n")
                  .append("• బ్యాంక్: ").append(b.getBankName()).append(" (ఖాతా: ").append(b.getAccountMasked()).append(")\n")
                  .append("• పథకం: ").append(scheme).append("\n")
                  .append("• మంజూరైన మొత్తం: ").append(amount).append("\n")
                  .append("• PFMS రిఫరెన్స్: ").append(txn).append("\n")
                  .append("• DBT స్థితి: ").append(dbtStatus).append("\n\n")
                  .append("📌 **డబ్బులు ఎప్పుడు వస్తాయి?**\n")
                  .append("కాలేజీ మరియు జిల్లా స్థాయి ధృవీకరణ పూర్తయిన తర్వాత 3 నుండి 5 పని దినాలలో నేరుగా మీ బ్యాంక్ ఖాతాలో జమ చేయబడుతుంది.");
            } else {
                sb.append("• బ్యాంక్ స్థితి: ⚠ చర్య అవసరం (NPCI ఇన్‌యాక్టివ్)\n")
                  .append("• బ్యాంక్: ").append(b.getBankName()).append(" (ఖాతా: ").append(b.getAccountMasked()).append(")\n")
                  .append("• కారణం: ").append(b.getNpciFailureReason() != null ? b.getNpciFailureReason() : "బ్యాంక్ ఖాతా ఆధార్‌తో సీడ్ కాలేదు").append("\n\n")
                  .append("🚨 **తక్షణ చర్య:**\n")
                  .append("దయచేసి వెంటనే మీ బ్యాంక్ బ్రాంచ్‌ను సంప్రదించి ఆధార్ DBT మ్యాండేట్ ఫారమ్ సమర్పించండి.");
            }
            return sb.toString();
        } else if ("or".equalsIgnoreCase(lang)) {
            sb.append("ନମସ୍କାର ").append(name).append("! 🙏\n\n");
            sb.append("💰 **DBT ଛାତ୍ରବୃତ୍ତି ପ୍ରଦାନ ବିବରଣୀ:**\n");
            if (b.isNpciMapped()) {
                sb.append("• ବ୍ୟାଙ୍କ ସ୍ଥିତି: ✅ ସକ୍ରିୟ ଓ ଆଧାର ସଂଯୋଗ (Active)\n")
                  .append("• ବ୍ୟାଙ୍କ: ").append(b.getBankName()).append(" (ଖାତା: ").append(b.getAccountMasked()).append(")\n")
                  .append("• ଯୋଜନା: ").append(scheme).append("\n")
                  .append("• ମଞ୍ଜୁର ରାଶି: ").append(amount).append("\n")
                  .append("• PFMS ରେଫରେନ୍ସ: ").append(txn).append("\n")
                  .append("• DBT ସ୍ଥିତି: ").append(dbtStatus).append("\n\n")
                  .append("📌 **ଟଙ୍କା କେବେ ମିଳିବ?**\n")
                  .append("ଅନୁଷ୍ଠାନ ଏବଂ ଜିଲ୍ଲା ଯାଞ୍ଚ ଶେଷ ହେବା ପରେ ମନ୍ତ୍ରଣାଳୟ ତରଫରୁ ୩ ରୁ ୫ ଦିନ ମଧ୍ୟରେ ସିଧାସଳଖ ଆପଣଙ୍କ ବ୍ୟାଙ୍କ ଖାତାରେ ଜମା ହେବ।");
            } else {
                sb.append("• ବ୍ୟାଙ୍କ ସ୍ଥିତି: ⚠ ଅସୁବିଧା (NPCI ସଂଯୋଗ ନାହିଁ)\n")
                  .append("• ବ୍ୟାଙ୍କ: ").append(b.getBankName()).append(" (ଖାତା: ").append(b.getAccountMasked()).append(")\n")
                  .append("• କାରଣ: ").append(b.getNpciFailureReason() != null ? b.getNpciFailureReason() : "ବ୍ୟାଙ୍କ ଖାତା ଆଧାର ସହ ଯୋଡ଼ା ହୋଇନାହିଁ").append("\n\n")
                  .append("🚨 **ପଦକ୍ଷେପ:**\n")
                  .append("ତୁରନ୍ତ ନିଜ ବ୍ୟାଙ୍କ ଶାଖାକୁ ଯାଇ ଆଧାର DBT ମ୍ୟାଣ୍ଡେଟ୍ ଫର୍ମ ଦାଖଲ କରନ୍ତୁ।");
            }
            return sb.toString();
        } else if ("sat".equalsIgnoreCase(lang)) {
            sb.append("ᱡᱚᱦᱟᱨ ").append(name).append("! 🙏\n\n");
            sb.append("💰 **DBT ᱥᱠᱚᱞᱟᱨᱥᱤᱯ ᱴᱟᱠᱟ ᱵᱤᱵᱚᱨᱚᱬ:**\n");
            if (b.isNpciMapped()) {
                sb.append("• ᱵᱮᱸᱠ ᱦᱟᱞᱚᱛ: ✅ ᱪᱟᱹᱞᱩ ᱢᱮᱱᱟᱜᱼᱟ (Active)\n")
                  .append("• ᱵᱮᱸᱠ: ").append(b.getBankName()).append(" (ᱮᱠᱟᱣᱩᱱᱴ: ").append(b.getAccountMasked()).append(")\n")
                  .append("• ᱡᱚᱡᱚᱱᱟ: ").append(scheme).append("\n")
                  .append("• ᱢᱚᱧᱡᱩᱨ ᱴᱟᱠᱟ: ").append(amount).append("\n")
                  .append("• DBT ᱦᱟᱞᱚᱛ: ").append(dbtStatus).append("\n\n")
                  .append("📌 ᱵᱤᱨᱫᱟᱹᱜᱟᱲ ᱯᱚᱨᱠᱷᱟᱹᱣ ᱥᱟᱹᱛ ᱮᱱ ᱠᱷᱟᱱ ᱓-᱕ ᱢᱟᱦᱟᱸ ᱨᱮ ᱴᱟᱠᱟ ᱵᱮᱸᱠ ᱮᱠᱟᱣᱩᱱᱴ ᱨᱮ ᱡᱚᱢᱟᱜᱼᱟ᱾");
            } else {
                sb.append("• ᱵᱮᱸᱠ ᱦᱟᱞᱚᱛ: ⚠ ᱮᱴᱠᱮᱴᱚᱬᱮ (Aadhaar Seed ᱵᱟᱹᱱᱩᱜᱼᱟ)\n")
                  .append("• ᱵᱮᱸᱠ ᱪᱟᱞᱟᱣ ᱠᱟᱛᱮ Aadhaar Seeding DBT ᱯᱮᱨᱮᱡ ᱢᱮ᱾");
            }
            return sb.toString();
        } else if ("hi".equalsIgnoreCase(lang)) {
            sb.append("नमस्ते ").append(name).append("! 🙏\n\n");
            sb.append("💰 **डीबीटी भुगतान विवरण (DBT Payment Status):**\n");
            if (b.isNpciMapped()) {
                sb.append("• बैंक स्थिति: ✅ सक्रिय (ACTIVE & HEALTHY)\n")
                  .append("• बैंक नाम: ").append(b.getBankName()).append(" (खाता: ").append(b.getAccountMasked()).append(")\n")
                  .append("• एनपीसीआई आधार सीडिंग: सफलतापूर्वक लिंक है।\n")
                  .append("• संबंधित योजना: ").append(scheme).append("\n")
                  .append("• स्वीकृत राशि: ").append(amount).append("\n")
                  .append("• पीएफएमएस संदर्भ (PFMS Ref): ").append(txn).append("\n")
                  .append("• वर्तमान डीबीटी स्थिति: ").append(dbtStatus).append("\n\n")
                  .append("📌 **पैसा कब आएगा?**\n")
                  .append("संस्थान और जिला सत्यापन पूरा होते ही मंत्रालय के डीबीटी चक्र के अनुसार राशि सीधे 3 से 5 कार्य दिवसों में आपके बैंक खाते में क्रेडिट हो जाएगी।");
            } else {
                sb.append("• बैंक स्थिति: ⚠ समस्या (NPCI Inactive)\n")
                  .append("• बैंक नाम: ").append(b.getBankName()).append(" (खाता: ").append(b.getAccountMasked()).append(")\n")
                  .append("• कारण: ").append(b.getNpciFailureReason() != null ? b.getNpciFailureReason() : "बैंक खाता आधार से सीडेड नहीं है").append("\n\n")
                  .append("🚨 **आवश्यक कार्रवाई:**\n")
                  .append("तुरंत अपनी बैंक शाखा जाएं और आधार डीबीटी मैंडेट फॉर्म जमा करें, अन्यथा छात्रवृत्ति का भुगतान बैंक द्वारा अस्वीकार हो जाएगा।");
            }
            return sb.toString();
        } else {
            sb.append("Hello ").append(name).append("! 👋\n\n");
            sb.append("💰 **Direct Benefit Transfer (DBT) Payment Status:**\n");
            if (b.isNpciMapped()) {
                sb.append("• Bank Status: ✅ Active & NPCI Mapped\n")
                  .append("• Bank Name: ").append(b.getBankName()).append(" (Acc: ").append(b.getAccountMasked()).append(")\n")
                  .append("• Scheme: ").append(scheme).append("\n")
                  .append("• Sanction Amount: ").append(amount).append("\n")
                  .append("• PFMS Ref ID: ").append(txn).append("\n")
                  .append("• DBT State: ").append(dbtStatus).append("\n\n")
                  .append("📌 **Disbursement Timeline:**\n")
                  .append("Upon completion of institute verification, funds are released within 3-5 business days directly to your Aadhaar-seeded bank account.");
            } else {
                sb.append("• Bank Status: ⚠ Action Required (NPCI Inactive)\n")
                  .append("• Reason: ").append(b.getNpciFailureReason()).append("\n")
                  .append("• Remediation: ").append(b.getNpciRemediation()).append("\n")
                  .append("Visit your bank branch with your Aadhaar card to enable DBT mandates.");
            }
            return sb.toString();
        }
    }

    private static String buildStatusResponse(String name, Beneficiary b, String lang) {
        List<SchemeApplication> apps = b.getApplications();
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if (apps == null || apps.isEmpty()) {
            if ("bn".equalsIgnoreCase(lang)) return sb.append(name).append(", আপনার OTR ID (").append(b.getOtrId()).append(") তে কোনো সক্রিয় আবেদন নেই। ড্যাশবোর্ডে '+ স্কলারশিপ আবেদন' বোতামে ট্যাপ করে আবেদন করুন।").toString();
            if ("mr".equalsIgnoreCase(lang)) return sb.append(name).append(", आपल्या OTR ID (").append(b.getOtrId()).append(") वर कोणताही सक्रिय अर्ज आढळला नाही. नवीन अर्जासाठी '+ शिष्यवृत्ती अर्ज' वर टॅप करा.").toString();
            if ("gu".equalsIgnoreCase(lang)) return sb.append(name).append(", તમારા OTR ID (").append(b.getOtrId()).append(") પર કોઈ સક્રિય અરજી મળી નથી. ડેશબોર્ડ પર '+ શિષ્યવૃત્તિ અરજી' દબાવીને અરજી કરો.").toString();
            if ("te".equalsIgnoreCase(lang)) return sb.append(name).append(", మీ OTR ID (").append(b.getOtrId()).append(") పై ఎలాంటి యాక్టివ్ దరఖాస్తు లేదు. '+ స్కాలర్‌షిప్ దరఖాస్తు' బటన్ నొక్కండి.").toString();
            if ("or".equalsIgnoreCase(lang)) return sb.append(name).append(", ଆପଣଙ୍କ OTR ID (").append(b.getOtrId()).append(") ରେ କୌଣସି ସକ୍ରିୟ ଆବେଦନ ନାହିଁ। ନୂଆ ଆବେଦନ ପାଇଁ '+ ଛାତ୍ରବୃତ୍ତି ଆବେଦନ' ଚୟନ କରନ୍ତୁ।").toString();
            if ("sat".equalsIgnoreCase(lang)) return sb.append(name).append(", OTR ID (").append(b.getOtrId()).append(") ᱨᱮ ᱡᱟᱦᱟᱸᱱ ᱟᱨᱡᱤ ᱵᱟᱹᱱᱩᱜᱼᱟ᱾ '+ ᱥᱠᱚᱞᱟᱨᱥᱤᱯ ᱟᱨᱡᱤ' ᱞᱤᱸᱵᱟᱹᱭ ᱢᱮ᱾").toString();
            if ("hi".equalsIgnoreCase(lang)) return sb.append(name).append(" जी, आपके OTR ID (").append(b.getOtrId()).append(") पर कोई सक्रिय आवेदन नहीं मिला। आप डैशबोर्ड पर '+ Apply Scholarship' बटन दबाकर आवेदन कर सकते हैं।").toString();
            return sb.append(name).append(", no active scholarship applications found for your OTR ID (").append(b.getOtrId()).append("). You can apply using the '+ Apply Scholarship' button.").toString();
        }

        if ("bn".equalsIgnoreCase(lang)) {
            sb.append("নমস্কার ").append(name).append("! 🙏\n\n📋 **আপনার স্কলারশিপ আবেদনের স্থিতি (Application Tracking):**\n\n");
            for (SchemeApplication app : apps) {
                sb.append("• **স্কিম:** ").append(app.getSchemeTitle()).append("\n")
                  .append("  - আবেদন ID: ").append(app.getApplicationId()).append("\n")
                  .append("  - শিক্ষাবর্ষ: ").append(app.getAcademicYear()).append("\n")
                  .append("  - বর্তমান পর্যায়: ").append(app.getStage()).append(" (৬-পর্যায়ের প্রক্রিয়া)\n")
                  .append("  - স্থিতি: ").append(app.getStatus()).append("\n")
                  .append("  - DBT স্থিতি: ").append(app.getDbtStatus()).append("\n\n");
            }
            sb.append("ℹ আপনার আবেদনটি বর্তমানে প্রতিষ্ঠান যাচাইকরণ (Institute Verification) স্তরে রয়েছে।");
            return sb.toString();
        } else if ("mr".equalsIgnoreCase(lang)) {
            sb.append("नमस्कार ").append(name).append("! 🙏\n\n📋 **आपल्या अर्जाची स्थिती (Application Tracking):**\n\n");
            for (SchemeApplication app : apps) {
                sb.append("• **योजना:** ").append(app.getSchemeTitle()).append("\n")
                  .append("  - अर्ज ID: ").append(app.getApplicationId()).append("\n")
                  .append("  - शैक्षणिक वर्ष: ").append(app.getAcademicYear()).append("\n")
                  .append("  - सध्याचा टप्पा: ").append(app.getStage()).append(" (६-टप्पे)\n")
                  .append("  - स्थिती: ").append(app.getStatus()).append("\n")
                  .append("  - DBT स्थिती: ").append(app.getDbtStatus()).append("\n\n");
            }
            sb.append("ℹ आपला अर्ज सध्या महाविद्यालयीन/शालेय पडताळणी (Institute Verification) टप्प्यावर प्रलंबित आहे.");
            return sb.toString();
        } else if ("gu".equalsIgnoreCase(lang)) {
            sb.append("નમસ્તે ").append(name).append("! 🙏\n\n📋 **તમારી શિષ્યવૃત્તિ અરજીની સ્થિતિ:**\n\n");
            for (SchemeApplication app : apps) {
                sb.append("• **યોજના:** ").append(app.getSchemeTitle()).append("\n")
                  .append("  - અરજી ID: ").append(app.getApplicationId()).append("\n")
                  .append("  - શૈક્ષણિક વર્ષ: ").append(app.getAcademicYear()).append("\n")
                  .append("  - તબક્કો: ").append(app.getStage()).append(" (૬-તબક્કા)\n")
                  .append("  - સ્થિતિ: ").append(app.getStatus()).append("\n")
                  .append("  - DBT સ્થિતિ: ").append(app.getDbtStatus()).append("\n\n");
            }
            sb.append("ℹ તમારી અરજી હાલમાં શાળા/કોલેજ ચકાસણી (Institute Verification) સ્તરે છે.");
            return sb.toString();
        } else if ("te".equalsIgnoreCase(lang)) {
            sb.append("నమస్కారం ").append(name).append("! 🙏\n\n📋 **మీ స్కాలర్‌షిప్ దరఖాస్తు స్థితి:**\n\n");
            for (SchemeApplication app : apps) {
                sb.append("• **పథకం:** ").append(app.getSchemeTitle()).append("\n")
                  .append("  - దరఖాస్తు ID: ").append(app.getApplicationId()).append("\n")
                  .append("  - విద్యా సంవత్సరం: ").append(app.getAcademicYear()).append("\n")
                  .append("  - ప్రస్తుత దశ: ").append(app.getStage()).append(" (6-దశల ప్రక్రియ)\n")
                  .append("  - స్థితి: ").append(app.getStatus()).append("\n")
                  .append("  - DBT స్థితి: ").append(app.getDbtStatus()).append("\n\n");
            }
            sb.append("ℹ మీ దరఖాస్తు ప్రస్తుతం విద్యాసంస్థ ధృవీకరణ (Institute Verification) దశలో పరిశీలనలో ఉంది.");
            return sb.toString();
        } else if ("or".equalsIgnoreCase(lang)) {
            sb.append("ନମସ୍କାର ").append(name).append("! 🙏\n\n📋 **ଆପଣଙ୍କ ଆବେଦନର ସ୍ଥିତି:**\n\n");
            for (SchemeApplication app : apps) {
                sb.append("• **ଯୋଜନା:** ").append(app.getSchemeTitle()).append("\n")
                  .append("  - ଆବେଦନ ID: ").append(app.getApplicationId()).append("\n")
                  .append("  - ଶିକ୍ଷା ବର୍ଷ: ").append(app.getAcademicYear()).append("\n")
                  .append("  - ପର୍ଯ୍ୟାୟ: ").append(app.getStage()).append(" (୬-ପର୍ଯ୍ୟାୟ)\n")
                  .append("  - ସ୍ଥିତି: ").append(app.getStatus()).append("\n")
                  .append("  - DBT ସ୍ଥିତି: ").append(app.getDbtStatus()).append("\n\n");
            }
            sb.append("ℹ ଆପଣଙ୍କ ଆବେଦନ ବର୍ତ୍ତମାନ ଅନୁଷ୍ଠାନ ଯାଞ୍ଚ (Institute Verification) ଅଧୀନରେ ଅଛି।");
            return sb.toString();
        } else if ("hi".equalsIgnoreCase(lang)) {
            sb.append("नमस्ते ").append(name).append("! 🙏\n\n📋 **आपके आवेदन की स्थिति (Application Tracking):**\n\n");
            for (SchemeApplication app : apps) {
                sb.append("• **योजना:** ").append(app.getSchemeTitle()).append("\n")
                  .append("  - आवेदन संख्या: ").append(app.getApplicationId()).append("\n")
                  .append("  - शैक्षणिक सत्र: ").append(app.getAcademicYear()).append("\n")
                  .append("  - वर्तमान चरण: ").append(app.getStage()).append(" (6-चरणीय प्रक्रिया)\n")
                  .append("  - स्थिति: ").append(app.getStatus()).append("\n")
                  .append("  - डीबीटी स्थिति: ").append(app.getDbtStatus()).append("\n")
                  .append("  - संदर्भ: ").append(app.getPfmsTransactionId()).append("\n\n");
            }
            sb.append("ℹ आपका आवेदन वर्तमान में संस्थान स्तर (Institute Verification) पर समीक्षा में है।");
            return sb.toString();
        } else {
            sb.append("Hello ").append(name).append("! 👋\n\n📋 **Your Scholarship Application Status:**\n\n");
            for (SchemeApplication app : apps) {
                sb.append("• **").append(app.getSchemeTitle()).append("**\n")
                  .append("  - Application ID: ").append(app.getApplicationId()).append("\n")
                  .append("  - Academic Year: ").append(app.getAcademicYear()).append("\n")
                  .append("  - Current Stage: ").append(app.getStage()).append(" (6-Stage Pipeline)\n")
                  .append("  - Status: ").append(app.getStatus()).append("\n")
                  .append("  - DBT Status: ").append(app.getDbtStatus()).append("\n\n");
            }
            return sb.toString().trim();
        }
    }

    private static String buildDocumentsResponse(String name, Beneficiary b, String lang) {
        List<WalletDocument> docs = b.getDocuments();
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if (docs == null || docs.isEmpty()) {
            if ("bn".equalsIgnoreCase(lang)) return sb.append(name).append(", আপনার ডিজিট্যাল ওয়ালেটে সমস্ত নথি স্বয়ং-যাচাইকৃত।").toString();
            if ("mr".equalsIgnoreCase(lang)) return sb.append(name).append(", आपल्या डिजिटल वॉलेटमधील सर्व कागदपत्रे पडताळलेली आहेत.").toString();
            if ("gu".equalsIgnoreCase(lang)) return sb.append(name).append(", તમારા ડિજિટલ વૉલેટમાં તમામ દસ્તાવેજો ચકાસાયેલા છે.").toString();
            if ("te".equalsIgnoreCase(lang)) return sb.append(name).append(", మీ డిజిటల్ వాలెట్‌లోని పత్రాలన్నీ ధృవీకరించబడ్డాయి.").toString();
            if ("or".equalsIgnoreCase(lang)) return sb.append(name).append(", ଆପଣଙ୍କ ଡିଜିଟାଲ୍ ୱାଲେଟରେ ଥିବା ସମସ୍ତ ଦଲିଲ ଯାଞ୍ଚ ହୋଇସାରିଛି।").toString();
            if ("sat".equalsIgnoreCase(lang)) return sb.append(name).append(", ᱣᱟᱞᱮᱴ ᱨᱮᱭᱟᱜ ᱡᱚᱛᱚ ᱥᱟᱠᱟᱢ ᱯᱚᱨᱠᱷᱟᱹᱣ ᱥᱟᱹᱛ ᱮᱱᱟ᱾").toString();
            if ("hi".equalsIgnoreCase(lang)) return sb.append(name).append(" जी, आपके डिजिटल वॉलेट में सभी दस्तावेज़ ऑटो-वेरिफाइड हैं या कोई नया दस्तावेज़ लंबित नहीं है।").toString();
            return sb.append(name).append(", all required documents in your digital wallet are verified via DigiLocker.").toString();
        }

        if ("bn".equalsIgnoreCase(lang) || "mr".equalsIgnoreCase(lang) || "gu".equalsIgnoreCase(lang) || "te".equalsIgnoreCase(lang) || "or".equalsIgnoreCase(lang) || "hi".equalsIgnoreCase(lang)) {
            sb.append("📁 **ডিজিটাল ওয়ালেট / डिजिटल दस्तावेज़ स्थिति:**\n\n");
            for (WalletDocument doc : docs) {
                sb.append("• ").append(doc.getTitle()).append("\n")
                  .append("  - ID: ").append(doc.getDocNumber()).append("\n")
                  .append("  - Status: ").append(doc.getStatus()).append("\n")
                  .append("  - Authority: ").append(doc.getAuthority()).append("\n")
                  .append("  - Storage: ").append(doc.isReused() ? "♻ Zero Re-upload Safe" : "DigiLocker").append("\n\n");
            }
            sb.append("✅ আপনার সমস্ত নথি সুরক্ষিত এবং পুনঃব্যবহারযোগ্য।");
            return sb.toString();
        } else {
            sb.append("Hello ").append(name).append("! 👋\n\n📁 **Your Verified Digital Wallet Documents:**\n\n");
            for (WalletDocument doc : docs) {
                sb.append("• ").append(doc.getTitle()).append("\n")
                  .append("  - Document No: ").append(doc.getDocNumber()).append("\n")
                  .append("  - Status: ").append(doc.getStatus()).append("\n")
                  .append("  - Authority: ").append(doc.getAuthority()).append("\n\n");
            }
            return sb.toString().trim();
        }
    }

    private static String buildVerificationResponse(String name, Beneficiary b, String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if ("bn".equalsIgnoreCase(lang)) {
            return sb.append("নমস্কার ").append(name).append("! 🙏\n\n🔍 **৬-পর্যায়ের যাচাইকরণ অগ্রগতি (6-Stage Pipeline):**\n\n")
                    .append("1. **আবেদন জমা (Submission):** ✅ সম্পন্ন\n")
                    .append("2. **প্রতিষ্ঠান যাচাইকরণ (Institute Verification):** ⏳ প্রক্রিয়াধীন (বিদ্যালয় / কলেজ ইনো দ্বারা)\n")
                    .append("3. **জেলা / রাজ্য অনুমোদন (District/State Approval):** ⏸ অপেক্ষমাণ\n")
                    .append("4. **MoTA মঞ্জুরি (Ministry Sanction):** ⏸ অপেক্ষমাণ\n")
                    .append("5. **PFMS DBT তৈরি (DBT Generation):** ⏸ অপেক্ষমাণ\n")
                    .append("6. **ব্যাঙ্ক অ্যাকাউন্টে টাকা জমা (Bank Credit):** ⏸ অপেক্ষমাণ\n\n")
                    .append("💡 **পরামর্শ:** আপনার স্কুল/কলেজের স্কলারশিপ নোডাল অফিসারের সাথে যোগাযোগ করে দ্রুত প্রতিষ্ঠান যাচাই সম্পন্ন করান।").toString();
        } else if ("mr".equalsIgnoreCase(lang)) {
            return sb.append("नमस्कार ").append(name).append("! 🙏\n\n🔍 **६-टप्प्यांची पडताळणी प्रगती (6-Stage Pipeline):**\n\n")
                    .append("1. **अर्ज सादर (Submission):** ✅ पूर्ण\n")
                    .append("2. **महाविद्यालय/शाळा पडताळणी (Institute Verification):** ⏳ चालू आहे\n")
                    .append("3. **जिल्हा/राज्य मंजुरी (District/State Approval):** ⏸ प्रलंबित\n")
                    .append("4. **मंत्रालय मंजुरी (Ministry Sanction):** ⏸ प्रलंबित\n")
                    .append("5. **PFMS DBT निर्मिती (DBT Generation):** ⏸ प्रलंबित\n")
                    .append("6. **बँक खात्यात जमा (Bank Credit):** ⏸ प्रलंबित\n\n")
                    .append("💡 **सल्ला:** महाविद्यालयाच्या नोडल अधिकाऱ्यांशी संपर्क साधून पडताळणी लवकर पूर्ण करून घ्यावी.").toString();
        } else if ("gu".equalsIgnoreCase(lang)) {
            return sb.append("નમસ્તે ").append(name).append("! 🙏\n\n🔍 **૬-તબક્કાની ચકાસણી પ્રગતિ:**\n\n")
                    .append("1. **અરજી સબમિશન (Submission):** ✅ પૂર્ણ\n")
                    .append("2. **સંસ્થા ચકાસણી (Institute Verification):** ⏳ પ્રગતિમાં છે\n")
                    .append("3. **જિલ્લા/રાજ્ય મંજૂરી (District Approval):** ⏸ બાકી\n")
                    .append("4. **મંત્રાલય મંજૂરી (MoTA Sanction):** ⏸ બાકી\n")
                    .append("5. **PFMS DBT નિર્માણ:** ⏸ બાકી\n")
                    .append("6. **બેંક ખાતામાં જમા:** ⏸ બાકી\n\n")
                    .append("💡 **સલાહ:** શાળા/કોલેજના નોડલ અધિકારીનો સંપર્ક કરીને વહેલી ચકાસણી કરાવો.").toString();
        } else if ("te".equalsIgnoreCase(lang)) {
            return sb.append("నమస్కారం ").append(name).append("! 🙏\n\n🔍 **6-దశల ధృవీకరణ పురోగతి:**\n\n")
                    .append("1. **దరఖాస్తు సమర్పణ:** ✅ పూర్తయింది\n")
                    .append("2. **విద్యాసంస్థ ధృవీకరణ (Institute Verification):** ⏳ పురోగతిలో ఉంది\n")
                    .append("3. **జిల్లా/రాష్ట్ర ఆమోదం:** ⏸ పెండింగ్‌లో ఉంది\n")
                    .append("4. **మంత్రిత్వ శాఖ మంజూరు:** ⏸ పెండింగ్‌లో ఉంది\n")
                    .append("5. **PFMS DBT జనరేషన్:** ⏸ పెండింగ్‌లో ఉంది\n")
                    .append("6. **బ్యాంక్ జమ (Bank Credit):** ⏸ పెండింగ్‌లో ఉంది\n\n")
                    .append("💡 **సలహా:** మీ కాలేజీ నోడల్ ఆఫీసర్‌ను సంప్రదించి సంస్థ ధృవీకరణను త్వరగా పూర్తి చేయించండి.").toString();
        } else if ("or".equalsIgnoreCase(lang)) {
            return sb.append("ନମସ୍କାର ").append(name).append("! 🙏\n\n🔍 **୬-ପର୍ଯ୍ୟାୟ ଯାଞ୍ଚ ପ୍ରଗତି:**\n\n")
                    .append("1. **ଆବେଦନ ଦାଖଲ:** ✅ ସମ୍ପୂର୍ଣ୍ଣ\n")
                    .append("2. **ଅନୁଷ୍ଠାନ ଯାଞ୍ଚ (Institute Verification):** ⏳ ଚାଲୁଅଛି\n")
                    .append("3. **ଜିଲ୍ଲା/ରାଜ୍ୟ ଅନୁମୋଦନ:** ⏸ ବାକି\n")
                    .append("4. **ମନ୍ତ୍ରଣାଳୟ ମଞ୍ଜୁରୀ:** ⏸ ବାକି\n")
                    .append("5. **PFMS DBT ପ୍ରସ୍ତୁତି:** ⏸ ବାକି\n")
                    .append("6. **ବ୍ୟାଙ୍କ ଖାତାରେ ଜମା:** ⏸ ବାକି\n\n")
                    .append("💡 **ସୂଚନା:** ଶୀଘ୍ର ଯାଞ୍ଚ ପାଇଁ ଆପଣଙ୍କ କଲେଜ/ସ୍କୁଲର ନୋଡାଲ ଅଧିକାରୀଙ୍କ ସହିତ ଯୋଗାଯୋଗ କରନ୍ତୁ।").toString();
        } else if ("hi".equalsIgnoreCase(lang)) {
            return sb.append("नमस्ते ").append(name).append("! 🙏\n\n🔍 **6-चरणीय सत्यापन प्रगति (6-Stage Lifecycle):**\n\n")
                    .append("1. **आवेदन प्रस्तुतीकरण (Submission):** ✅ पूर्ण\n")
                    .append("2. **संस्थान सत्यापन (Institute Verification):** ⏳ प्रगति पर (विद्यालय / कॉलेज नोडल अधिकारी द्वारा)\n")
                    .append("3. **जिला / राज्य अनुमोदन (District/State Approval):** ⏸ लंबित\n")
                    .append("4. **MoTA मंजूरी (Ministry Sanction):** ⏸ लंबित\n")
                    .append("5. **पीएफएमएस डीबीटी निर्माण (PFMS DBT Generation):** ⏸ लंबित\n")
                    .append("6. **बैंक खाता क्रेडिट (Bank Credit):** ⏸ लंबित\n\n")
                    .append("💡 **सुझाव:** अपने संस्थान (विद्यालय/कॉलेज) के स्कॉलरशिप नोडल अधिकारी से संपर्क करके संस्थान सत्यापन शीघ्र पूरा करवाएं।").toString();
        } else {
            return sb.append("Hello ").append(name).append("! 👋\n\n🔍 **6-Stage Lifecycle Progress:**\n\n")
                    .append("1. Submission: ✅ Completed\n")
                    .append("2. Institute Verification: ⏳ In Progress (School/College INO Review)\n")
                    .append("3. District/State Approval: ⏸ Queued\n")
                    .append("4. MoTA Sanction: ⏸ Queued\n")
                    .append("5. PFMS/SFMP DBT Generation: ⏸ Queued\n")
                    .append("6. Bank Account Credit: ⏸ Queued\n\n")
                    .append("Tip: You can request your Institute Nodal Officer (INO) to expedite stage 2 verification.").toString();
        }
    }

    private static String buildDeficiencyResponse(String name, Beneficiary b, String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        boolean hasIssue = !b.isNpciMapped();
        if (!hasIssue) {
            if ("bn".equalsIgnoreCase(lang)) return sb.append("নমস্কার ").append(name).append("! 🎉 চমৎকার খবর! আপনার আবেদনে কোনো ত্রুটি বা সমস্যা নেই।").toString();
            if ("mr".equalsIgnoreCase(lang)) return sb.append("नमस्कार ").append(name).append("! 🎉 अभिनंदन! आपल्या अर्जात कोणतीही त्रुटी किंवा अडचण नाही.").toString();
            if ("gu".equalsIgnoreCase(lang)) return sb.append("નમસ્તે ").append(name).append("! 🎉 અભિનંદન! તમારી અરજીમાં કોઈ ખામી કે સમસ્યા નથી.").toString();
            if ("te".equalsIgnoreCase(lang)) return sb.append("నమస్కారం ").append(name).append("! 🎉 అభినందనలు! మీ దరఖాస్తులో ఎలాంటి లోపాలు లేవు.").toString();
            if ("or".equalsIgnoreCase(lang)) return sb.append("ନମସ୍କାର ").append(name).append("! 🎉 ଶୁଭ ସୂଚନା! ଆପଣଙ୍କ ଆବେଦନରେ କୌଣସି ତ୍ରୁଟି ନାହିଁ।").toString();
            if ("hi".equalsIgnoreCase(lang)) return sb.append("नमस्ते ").append(name).append("! 🎉 बहुत बढ़िया! आपके आवेदन या बैंक खाते में कोई भी त्रुटि (Deficiency) या समस्या नहीं है। सब कुछ सही है।").toString();
            return sb.append("Hello ").append(name).append("! 🎉 Great news! There are no deficiency flags or pending corrections on your profile or applications.").toString();
        }

        if ("bn".equalsIgnoreCase(lang)) {
            return sb.append("নমস্কার ").append(name).append("! ⚠ **সংশোধন বিজ্ঞপ্তি (Deficiency Alert):**\n\n")
                    .append("• সমস্যা: ").append(b.getNpciFailureReason()).append("\n")
                    .append("• সমাধান পদক্ষেপ: ").append(b.getNpciRemediation()).append("\n\n")
                    .append("অনুগ্রহ করে অবিলম্বে আপনার ব্যাঙ্ক শাখায় গিয়ে আধার লিঙ্ক করান।").toString();
        } else if ("mr".equalsIgnoreCase(lang)) {
            return sb.append("नमस्कार ").append(name).append("! ⚠ **त्रुटी निवारण सूचना (Deficiency Alert):**\n\n")
                    .append("• समस्या: ").append(b.getNpciFailureReason()).append("\n")
                    .append("• आवश्यक कृती: ").append(b.getNpciRemediation()).append("\n\n")
                    .append("कृपया आपल्या बँकेत जाऊन आधार सीडिंग त्वरित करून घ्या.").toString();
        } else if ("gu".equalsIgnoreCase(lang)) {
            return sb.append("નમસ્તે ").append(name).append("! ⚠ **ખામી સુધારણા ચેતવણી:**\n\n")
                    .append("• સમસ્યા: ").append(b.getNpciFailureReason()).append("\n")
                    .append("• પગલું: ").append(b.getNpciRemediation()).append("\n\n")
                    .append("કૃપા કરીને તરત જ બેંક શાખાની મુલાકાત લઈ આધાર મેન્ડેટ ફોર્મ ભરો.").toString();
        } else if ("te".equalsIgnoreCase(lang)) {
            return sb.append("నమస్కారం ").append(name).append("! ⚠ **లోప సరిదిద్దే హెచ్చరిక:**\n\n")
                    .append("• సమస్య: ").append(b.getNpciFailureReason()).append("\n")
                    .append("• పరిష్కారం: ").append(b.getNpciRemediation()).append("\n\n")
                    .append("దయచేసి మీ బ్యాంక్ శాఖను సంప్రదించి సమస్యను పరిష్కరించుకోండి.").toString();
        } else if ("or".equalsIgnoreCase(lang)) {
            return sb.append("ନମସ୍କାର ").append(name).append("! ⚠ **ତ୍ରୁଟି ସଂଶୋଧନ ସୂଚନା:**\n\n")
                    .append("• ସମସ୍ୟା: ").append(b.getNpciFailureReason()).append("\n")
                    .append("• ସମାଧାନ: ").append(b.getNpciRemediation()).append("\n\n")
                    .append("ଦୟାକରି ଶୀଘ୍ର ନିଜ ବ୍ୟାଙ୍କ ଶାଖାରେ ଯୋଗାଯୋଗ କରନ୍ତୁ।").toString();
        } else if ("hi".equalsIgnoreCase(lang)) {
            return sb.append("नमस्ते ").append(name).append("! ⚠ **त्रुटि सुधार सूचना (Deficiency Alert):**\n\n")
                    .append("• समस्या: ").append(b.getNpciFailureReason()).append("\n")
                    .append("• आवश्यक कदम: ").append(b.getNpciRemediation()).append("\n\n")
                    .append("कृपया तुरंत अपनी बैंक शाखा जाकर इस समस्या का समाधान कराएं ताकि स्कॉलरशिप का भुगतान न रुके।").toString();
        } else {
            return sb.append("Hello ").append(name).append("! ⚠ **Deficiency Alert:**\n\n")
                    .append("• Issue: ").append(b.getNpciFailureReason()).append("\n")
                    .append("• Action: ").append(b.getNpciRemediation()).append("\n\n")
                    .append("Please visit your bank branch to resolve Aadhaar seeding to ensure successful DBT payments.").toString();
        }
    }

    private static String buildPreMatricResponse(String name, Beneficiary b, String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if (!"en".equalsIgnoreCase(lang)) {
            return sb.append("📘 **प्री-मैट्रिक एसटी छात्रवृत्ति योजना / Pre-Matric Scholarship (Class 9 & 10):**\n\n")
                    .append("• **पात्रता / Eligibility:** कक्षा 9वीं और 10वीं में अध्ययनरत अनुसूचित जनजाति (ST) के छात्र।\n")
                    .append("• **आय सीमा / Income Limit:** परिवार की कुल वार्षिक आय ₹2.50 लाख तक (Up to ₹2.50 Lakh/year)।\n")
                    .append("• **वित्तीय लाभ / Benefits:**\n")
                    .append("  - डे स्कॉलर (Day Scholar): ₹3,500 प्रति वर्ष।\n")
                    .append("  - हॉस्टलर (Hosteller): ₹7,000 प्रति वर्ष।\n")
                    .append("• **आवश्यक दस्तावेज़ / Documents:** एसटी जाति प्रमाण पत्र, आय प्रमाण पत्र, अंकतालिका।\n")
                    .append("• **पोर्टल:** नेशनल स्कॉलरशिप पोर्टल (NSP - scholarships.gov.in)।").toString();
        } else {
            return sb.append("📘 **Pre-Matric Scholarship for ST Students (Class IX & X):**\n\n")
                    .append("• Eligibility: ST students studying in Class 9 or 10 in recognized schools.\n")
                    .append("• Annual Family Income: Up to ₹2.50 Lakh/year.\n")
                    .append("• Financial Benefit: ₹3,500/year (Day Scholars) | ₹7,000/year (Hostellers).\n")
                    .append("• Registry: UDISE+ and National Scholarship Portal (NSP).").toString();
        }
    }

    private static String buildPostMatricResponse(String name, Beneficiary b, String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if (!"en".equalsIgnoreCase(lang)) {
            return sb.append("🎓 **पोस्ट-मैट्रिक एसटी छात्रवृत्ति योजना / Post-Matric Scholarship:**\n\n")
                    .append("• **पात्रता / Eligibility:** 10वीं पास के बाद 11वीं, 12वीं, आईटीआई, डिप्लोमा, कॉलेज डिग्री (UG/PG) ST छात्र।\n")
                    .append("• **आय सीमा / Income Limit:** पारिवारिक वार्षिक आय ₹2.50 लाख तक।\n")
                    .append("• **वित्तीय लाभ / Benefits:** अनिवार्य नॉन-रिफंडेबल ट्यूशन फीस की 100% प्रतिपूर्ति + ₹13,500 से ₹23,000/वर्ष अनुरक्षण भत्ता।\n")
                    .append("• **पोर्टल:** नेशनल स्कॉलरशिप पोर्टल (NSP) एवं राज्य पोर्टल।").toString();
        } else {
            return sb.append("🎓 **Post-Matric Scholarship for ST Students:**\n\n")
                    .append("• Eligibility: ST students in Class 11, 12, ITI, Diploma, UG, or PG courses.\n")
                    .append("• Annual Family Income: Up to ₹2.50 Lakh/year.\n")
                    .append("• Financial Benefit: Full non-refundable tuition reimbursement + ₹13,500 to ₹23,000/year maintenance allowance.\n")
                    .append("• Portal: NSP (scholarships.gov.in) & State e-District.").toString();
        }
    }

    private static String buildTopClassResponse(String name, Beneficiary b, String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if (!"en".equalsIgnoreCase(lang)) {
            return sb.append("🏛 **टॉप क्लास एजुकेशन छात्रवृत्ति योजना / Top Class Scheme:**\n\n")
                    .append("• **पात्रता / Eligibility:** 266+ शीर्ष संस्थानों (IIT, IIM, NIT, AIIMS, NLU) में अध्ययनरत ST छात्र।\n")
                    .append("• **आय सीमा / Income Limit:** कुल पारिवारिक वार्षिक आय ₹6.00 लाख तक।\n")
                    .append("• **वित्तीय लाभ / Benefits:** 100% पूरी ट्यूशन फीस + ₹3,000/माह निर्वाह भत्ता + ₹5,000/वर्ष पुस्तकें + ₹45,000 एकमुश्त कंप्यूटर सहायता।").toString();
        } else {
            return sb.append("🏛 **Top Class Education Scheme for ST Students:**\n\n")
                    .append("• Eligibility: ST students admitted to 266+ premier institutes (IITs, IIMs, NITs, AIIMS, NLUs).\n")
                    .append("• Annual Family Income: Up to ₹6.00 Lakh/year.\n")
                    .append("• Financial Benefit: 100% full tuition fees + ₹3,000/month living expense + ₹5,000/year stationery + ₹45,000 one-time computer grant.").toString();
        }
    }

    private static String buildNfstResponse(String name, Beneficiary b, String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if (!"en".equalsIgnoreCase(lang)) {
            return sb.append("🔬 **राष्ट्रीय उच्च शिक्षा फैलोशिप (NFST - National Fellowship):**\n\n")
                    .append("• **पात्रता:** विश्वविद्यालयों में नियमित एम.फिल (M.Phil) या पी.एचडी (Ph.D) ST शोधार्थी।\n")
                    .append("• **आय सीमा:** कोई सीमा नहीं (मेरिट आधारित)।\n")
                    .append("• **वित्तीय लाभ:** जेआरएफ ₹37,000/माह | एसआरएफ ₹42,000/माह + आकस्मिक अनुदान + HRA।\n")
                    .append("• **भुगतान:** Canara Bank SFMP पोर्टल द्वारा सीधा डीबीटी।").toString();
        } else {
            return sb.append("🔬 **National Fellowship for Higher Education of ST Students (NFST):**\n\n")
                    .append("• Eligibility: ST scholars pursuing full-time regular M.Phil or Ph.D.\n")
                    .append("• Income Criteria: No income ceiling.\n")
                    .append("• Benefit: ₹37,000/month (JRF) | ₹42,000/month (SRF) + ₹20,500 to ₹25,000/year contingency + HRA.\n")
                    .append("• Disbursing Portal: Canara Bank SFMP Portal (monthly direct DBT).").toString();
        }
    }

    private static String buildNosResponse(String name, Beneficiary b, String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if (!"en".equalsIgnoreCase(lang)) {
            return sb.append("✈ **राष्ट्रीय प्रवासी छात्रवृत्ति योजना (National Overseas Scholarship - NOS):**\n\n")
                    .append("• **पात्रता:** विश्व के शीर्ष 500 क्यूएस-रैंक विदेशी विश्वविद्यालयों में मास्टर्स या पी.एचडी हेतु चयनित ST छात्र।\n")
                    .append("• **आय सीमा:** वार्षिक पारिवारिक आय ₹8.00 लाख तक।\n")
                    .append("• **वित्तीय लाभ:** 100% पूरी ट्यूशन फीस + यूके (£9,900) / यूएस ($15,400) निर्वाह भत्ता + हवाई टिकट।\n")
                    .append("• **पोर्टल:** overseas.tribal.gov.in").toString();
        } else {
            return sb.append("✈ **National Overseas Scholarship (NOS) for ST Candidates:**\n\n")
                    .append("• Eligibility: ST students admitted to top 500 QS-ranked overseas universities for Masters/Ph.D.\n")
                    .append("• Annual Family Income: Up to ₹8.00 Lakh/year.\n")
                    .append("• Financial Benefit: 100% tuition fees + living allowance (£9,900 in UK / $15,400 in US) + return airfare + health insurance.\n")
                    .append("• Portal: overseas.tribal.gov.in.").toString();
        }
    }

    private static String buildNpciHelpResponse(String name, Beneficiary b, String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if ("bn".equalsIgnoreCase(lang)) {
            return sb.append("🏦 **NPCI আধার সিডিং সহায়তা (NPCI Aadhaar Mapping Guide):**\n\n")
                    .append("DBT স্কলারশিপ পাওয়ার জন্য আপনার ব্যাঙ্ক অ্যাকাউন্ট NPCI ম্যাপিংয়ে সক্রিয় থাকা বাধ্যতামূলক।\n\n")
                    .append("📌 **সহজ ৩টি পদক্ষেপ:**\n")
                    .append("1. আপনার ব্যাঙ্ক শাখায় (").append(b.getBankName()).append(") যান।\n")
                    .append("2. আধার কার্ডের ফটোকপি দিয়ে **'Aadhaar Seeding / DBT Mandate'** ফর্ম জমা দিন।\n")
                    .append("3. ব্যাঙ্ক থেকে অ্যাকনলেজমেন্ট স্লিপ গ্রহণ করুন। ২৪-৪৮ ঘণ্টার মধ্যে স্থિતિ সক্রিয় হবে।\n\n")
                    .append("বর্তমান ব্যাঙ্ক: ").append(b.getBankName()).append(" (অ্যাকাউন্ট: ").append(b.getAccountMasked()).append(")").toString();
        } else if ("mr".equalsIgnoreCase(lang)) {
            return sb.append("🏦 **NPCI आधार सीडिंग मार्गदर्शन (NPCI Aadhaar Mapping Guide):**\n\n")
                    .append("DBT शिष्यवृत्ती मिळवण्यासाठी बँक खाते NPCI मॅपरवर सक्रिय असणे आवश्यक आहे.\n\n")
                    .append("📌 **३ सोप्या पायऱ्या:**\n")
                    .append("1. आपल्या बँक शाखेत (").append(b.getBankName()).append(") जा.\n")
                    .append("2. आधार कार्ड जोडून **'Aadhaar Seeding / DBT Mandate'** अर्ज सादर करा.\n")
                    .append("3. पोचपावती घ्या. २४ ते ४८ तासांत स्थिती अपडेट होईल.\n\n")
                    .append("नोंदणीकृत बँक: ").append(b.getBankName()).append(" (खाते: ").append(b.getAccountMasked()).append(")").toString();
        } else if ("gu".equalsIgnoreCase(lang)) {
            return sb.append("🏦 **NPCI આધાર સીડિંગ સહાય માર્ગદર્શિકા:**\n\n")
                    .append("DBT શિષ્યવૃત્તિ માટે તમારું બેંક ખાતું NPCI મેપર પર લિંક હોવું જરૂરી છે.\n\n")
                    .append("📌 **૩ સરળ પગલાં:**\n")
                    .append("1. તમારી બેંક શાખા (").append(b.getBankName()).append(") પર જાઓ.\n")
                    .append("2. આધાર કાર્ડ સાથે **'Aadhaar Seeding / DBT Mandate'** ફોર્મ જમા કરો.\n")
                    .append("3. રસીદ મેળવો. ૨૪-૪૮ કલાકમાં ખાતું સક્રિય થશે.\n\n")
                    .append("હાલની બેંક: ").append(b.getBankName()).append(" (ખાતું: ").append(b.getAccountMasked()).append(")").toString();
        } else if ("te".equalsIgnoreCase(lang)) {
            return sb.append("🏦 **NPCI ఆధార్ సీడింగ్ మార్గదర్శి:**\n\n")
                    .append("DBT స్కాలర్‌షిప్ పొందడానికి మీ బ్యాంక్ ఖాతా NPCI మ్యాపర్‌లో లింక్ చేయబడి ఉండాలి.\n\n")
                    .append("📌 **3 సులభమైన దశలు:**\n")
                    .append("1. మీ బ్యాంక్ బ్రాంచ్ (").append(b.getBankName()).append(")ను సందర్శించండి.\n")
                    .append("2. ఆధార్ జిరాక్స్ కాపీతో **'Aadhaar Seeding / DBT Mandate'** ఫారమ్‌ను సమర్పించండి.\n")
                    .append("3. రసీదు తీసుకోండి. 24-48 గంటల్లో మీ స్థితి యాక్టివ్ అవుతుంది.\n\n")
                    .append("ప్రస్తుత బ్యాంక్: ").append(b.getBankName()).append(" (ఖాతా: ").append(b.getAccountMasked()).append(")").toString();
        } else if ("or".equalsIgnoreCase(lang)) {
            return sb.append("🏦 **NPCI ଆଧାର ସିଡିଂ ସହାୟତା ମାର୍ଗଦର୍ଶିକା:**\n\n")
                    .append("DBT ଛାତ୍ରବୃତ୍ତି ପାଇଁ ବ୍ୟାଙ୍କ ଖାତା NPCI ମ୍ୟାପରରେ ସକ୍ରିୟ ରହିବା ଜରୁରୀ।\n\n")
                    .append("📌 **୩ଟି ସହଜ ପଦକ୍ଷେପ:**\n")
                    .append("1. ନିଜ ବ୍ୟାଙ୍କ ଶାଖା (").append(b.getBankName()).append(")କୁ ଯାଆନ୍ତୁ।\n")
                    .append("2. ଆଧାର କାର୍ଡ ସହ **'Aadhaar Seeding / DBT Mandate'** ଫର୍ମ ଦାଖଲ କରନ୍ତୁ।\n")
                    .append("3. ରସିଦ ସଂଗ୍ରହ କରନ୍ତୁ। ୨୪-୪୮ ଘଣ୍ଟା ମଧ୍ୟରେ ଅପଡେଟ୍ ହୋଇଯିବ।\n\n")
                    .append("ବର୍ତ୍ତମାନର ବ୍ୟାଙ୍କ: ").append(b.getBankName()).append(" (ଖାତା: ").append(b.getAccountMasked()).append(")").toString();
        } else if ("hi".equalsIgnoreCase(lang)) {
            return sb.append("🏦 **एनपीसीआई आधार सीडिंग सहायता (NPCI Aadhaar Mapping Guide):**\n\n")
                    .append("डीबीटी (DBT) छात्रवृत्ति प्राप्त करने के लिए आपका बैंक खाता एनपीसीआई मैपर में लिंक होना अनिवार्य है।\n\n")
                    .append("📌 **इसे ठीक करने के 3 आसान कदम:**\n")
                    .append("1. अपनी बैंक शाखा में जाएं जहां आपका बचत खाता है।\n")
                    .append("2. बैंक प्रबंधक को अपनी आधार कार्ड की फोटोकॉपी दें और **'Aadhaar Seeding / NPCI DBT Mandate'** फॉर्म भरें।\n")
                    .append("3. बैंक अधिकारी से पुष्टि पर्ची (Acknowledgment Slip) लें। 24-48 घंटों में आपकी स्थिति सक्रिय हो जाएगी।\n\n")
                    .append("आपका वर्तमान बैंक: ").append(b.getBankName()).append(" (खाता: ").append(b.getAccountMasked()).append(")").toString();
        } else {
            return sb.append("🏦 **NPCI Aadhaar Seeding Step-by-Step Guide:**\n\n")
                    .append("To receive DBT scholarship payments, your bank account must be mapped on the NPCI Aadhaar Bridge.\n\n")
                    .append("📌 **Steps to Resolve:**\n")
                    .append("1. Visit your home branch (").append(b.getBankName()).append(").\n")
                    .append("2. Submit your Aadhaar copy with an **Aadhaar Seeding / DBT Mandate Form**.\n")
                    .append("3. Collect the acknowledgment slip. Your NPCI status will update within 24-48 hours.\n\n")
                    .append("Current Registered Bank: ").append(b.getBankName()).append(" (").append(b.getAccountMasked()).append(")").toString();
        }
    }

    private static String buildGreetingResponse(String name, Beneficiary b, String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if ("bn".equalsIgnoreCase(lang)) {
            return sb.append("নমস্কার ").append(name).append("! 🙏 জোহর!\n\n")
                    .append("আমি **JAGO (জাগো)** — উপজাতি বিষয়ক মন্ত্রকের (MoTA) অফিসিয়াল এআই সহকারী।\n\n")
                    .append("আপনি আমাকে আপনার স্কলারশিপ স্থিতি, DBT পেমেন্ট, নথি যাচাইকরণ বা যেকোনো স্কিম সম্পর্কে জিজ্ঞাসা করতে পারেন। আপনি বাংলায় বলতেও পারেন!").toString();
        } else if ("mr".equalsIgnoreCase(lang)) {
            return sb.append("नमस्कार ").append(name).append("! 🙏 जोहार!\n\n")
                    .append("मी **जागो (JAGO)** — आदिवासी कार्य मंत्रालय (MoTA) चा अधिकृत एआय सहाय्यक आहे.\n\n")
                    .append("तुम्ही मला तुमची शिष्यवृत्ती स्थिती, DBT रक्कम, कागदपत्र पडताळणी किंवा योजनांविषयी विचारू शकता. आपण मराठीत बोलूनही प्रश्न विचारू शकता!").toString();
        } else if ("gu".equalsIgnoreCase(lang)) {
            return sb.append("નમસ્તે ").append(name).append("! 🙏 જોહાર!\n\n")
                    .append("હું **જાગો (JAGO)** છું — જનજાતીય બાબતોના મંત્રાલય (MoTA) નો સત્તાવાર AI સહાયક.\n\n")
                    .append("તમે મને તમારી શિષ્યવૃત્તિ સ્થિતિ, DBT ચુકવણી, દસ્તાવેજ ચકાસણી અથવા યોજનાઓ વિશે પૂછી શકો છો. તમે ગુજરાતીમાં બોલીને પણ પ્રશ્ન પૂછી શકો છો!").toString();
        } else if ("te".equalsIgnoreCase(lang)) {
            return sb.append("నమస్కారం ").append(name).append("! 🙏 జోహార్!\n\n")
                    .append("నేను **జాగో (JAGO)** — గిరిజన వ్యవహారాల మంత్రిత్వ శాఖ (MoTA) అధికారిక AI వర్చువల్ అసిస్టెంట్.\n\n")
                    .append("మీరు మీ స్కాలర్‌షిప్ స్థితి, DBT చెల్లింపులు, ధృవీకరణ వివరాలు లేదా పథకాల గురించి నన్ను అడగవచ్చు. మీరు తెలుగులో మాట్లాడి కూడా ప్రశ్నించవచ్చు!").toString();
        } else if ("or".equalsIgnoreCase(lang)) {
            return sb.append("ନମସ୍କାର ").append(name).append("! 🙏 ଜୋହାର!\n\n")
                    .append("ମୁଁ **JAGO (ଜାଗୋ)** — ଜନଜାତି ବ୍ୟାପାର ମନ୍ତ୍ରଣାଳୟର (MoTA) ସରକାରୀ AI ସହାୟକ।\n\n")
                    .append("ଆପଣ ମୋତେ ନିଜର ଛାତ୍ରବୃତ୍ତି ସ୍ଥିତି, DBT ଟଙ୍କା, ପ୍ରମାଣପତ୍ର ଯାଞ୍ଚ କିମ୍ବା ଯୋଜନା ବିଷୟରେ ପଚାରିପାରିବେ। ଆପଣ ଓଡ଼ିଆରେ କହି ମଧ୍ୟ ପ୍ରଶ୍ନ କରିପାରିବେ!").toString();
        } else if ("sat".equalsIgnoreCase(lang)) {
            return sb.append("ᱡᱚᱦᱟᱨ ").append(name).append("! 🙏\n\n")
                    .append("ᱤᱧ ᱫᱚ **JAGO** — MoTA ᱨᱤᱱᱤᱡ AI ᱜᱚᱲᱚᱭᱤᱡ᱾\n\n")
                    .append("ᱟᱢ ᱥᱠᱚᱞᱟᱨᱥᱤᱯ ᱦᱟᱞᱚᱛ, DBT ᱴᱟᱠᱟ, ᱥᱟᱠᱟᱢ ᱯᱚᱨᱠᱷᱟᱹᱣ ᱵᱟᱵᱚᱛ ᱠᱩᱞᱤ ᱫᱟᱲᱮᱭᱟᱜᱼᱟᱢ᱾").toString();
        } else if ("hi".equalsIgnoreCase(lang)) {
            return sb.append("नमस्ते ").append(name).append("! 🙏 जोहार!\n\n")
                    .append("मैं **जागो (JAGO)** हूँ — जनजातीय कार्य मंत्रालय (MoTA) का आधिकारिक एआई सहायक।\n\n")
                    .append("आप मुझसे अपनी छात्रवृत्ति स्थिति, डीबीटी भुगतान, दस्तावेज़ सत्यापन, या किसी भी MoTA योजना के बारे में पूछ सकते हैं। आप हिंदी में बोलकर भी प्रश्न पूछ सकते हैं!").toString();
        } else {
            return sb.append("Hello ").append(name).append("! 🙏 Johar!\n\n")
                    .append("I am **JAGO**, the official AI Virtual Assistant for the Ministry of Tribal Affairs (MoTA).\n\n")
                    .append("You can ask me about your scholarship progress, DBT payment dates, document verification, or any MoTA scheme. Feel free to type or tap the microphone to speak!").toString();
        }
    }

    private static String buildGeneralOrEligibilityResponse(String name, Beneficiary b, String lang, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLanguageBadge(lang));

        if ("bn".equalsIgnoreCase(lang)) {
            return sb.append("নমস্কার ").append(name).append("! 🙏\n\n")
                    .append("আপনার প্রোফাইল (").append(b.getCategory()).append(b.isPvtg() ? " • PVTG: " + b.getPvtgCommunityName() : "").append(", ").append(b.getState()).append(") অনুযায়ী আপনি নিম্নলিখিত প্রধান MoTA স্কলারশিপের জন্য যোগ্য:\n\n")
                    .append("1. **প্রি-ম্যাট্রিক (Pre-Matric):** ক্লাস ৯-১০ (₹৭,০০০/বছর পর্যন্ত)\n")
                    .append("2. **পোস্ট-ম্যাট্রিক (Post-Matric):** ক্লাস ১১-১২, কলেজ/ডিগ্রি (সম্পূর্ণ ফি + ভাতা)\n")
                    .append("3. **টপ ক্লাস এডুকেশন (Top Class):** IIT, IIM, NIT ইত্যাদিতে সম্পূর্ণ টিউশন ফি রিইম্বার্সমেন্ট\n")
                    .append("4. **জাতীয় ফেলোশিপ (NFST):** Ph.D গবেষকদের জন্য ₹৩৭,০০০/মাস\n")
                    .append("5. **জাতীয় ওভারসিজ (NOS):** বিদেশে উচ্চশিক্ষার জন্য সম্পূর্ণ সরকারি খরচ\n\n")
                    .append("💡 আপনি কোন স্কিম, পেমেন্ট বা স্ট্যাটাস সম্পর্কে জানতে চান? টাইপ করুন বা মাইক চেপে বলুন।").toString();
        } else if ("mr".equalsIgnoreCase(lang)) {
            return sb.append("नमस्कार ").append(name).append("! 🙏\n\n")
                    .append("आपल्या प्रोफाइलनुसार (").append(b.getCategory()).append(b.isPvtg() ? " • PVTG: " + b.getPvtgCommunityName() : "").append(", ").append(b.getState()).append(") आपण पुढील MoTA योजनांसाठी पात्र आहात:\n\n")
                    .append("1. **प्री-मॅट्रिक (Pre-Matric):** इयत्ता ९वी-१०वी साठी (₹७,०००/वर्ष पर्यंत)\n")
                    .append("2. **पोस्ट-मॅट्रिक (Post-Matric):** ११वी, १२वी, पदवी/डिप्लोमा (पूर्ण शुल्क + भत्ता)\n")
                    .append("3. **टॉप क्लास शिक्षण (Top Class):** IIT, IIM, NIT आदींसाठी १००% ट्यूशन फी माफी\n")
                    .append("4. **राष्ट्रीय फेलोशिप (NFST):** Ph.D संशोधकांसाठी ₹३७,०००/महिना\n")
                    .append("5. **परदेशी शिष्यवृत्ती (NOS):** परदेशात शिक्षणासाठी पूर्ण आर्थिक साहाय्य\n\n")
                    .append("💡 आपल्याला कोणत्या योजनेबद्दल माहिती हवी आहे? लिहून किंवा माईक दाबून बोला.").toString();
        } else if ("gu".equalsIgnoreCase(lang)) {
            return sb.append("નમસ્તે ").append(name).append("! 🙏\n\n")
                    .append("તમારી પ્રોફાઇલ (").append(b.getCategory()).append(b.isPvtg() ? " • PVTG: " + b.getPvtgCommunityName() : "").append(", ").append(b.getState()).append(") મુજબ તમે નીચેની MoTA યોજનાઓ માટે પાત્ર છો:\n\n")
                    .append("1. **પ્રી-મેટ્રિક (Pre-Matric):** ધોરણ ૯-૧૦ (₹૭,૦૦૦/વર્ષ સુધી)\n")
                    .append("2. **પોસ્ટ-મેટ્રિક (Post-Matric):** ધોરણ ૧૧-૧૨, કોલેજ (સંપૂર્ણ ફી + ભથ્થું)\n")
                    .append("3. **ટોપ ક્લાસ એજ્યુકેશન (Top Class):** IIT, IIM, NIT વગેરેમાં સંપૂર્ણ ટ્યુશન ફી સહાય\n")
                    .append("4. **રાષ્ટ્રીય ફેલોશિપ (NFST):** Ph.D સંશોધકો માટે ₹૩૭,૦૦૦/માસ\n")
                    .append("5. **રાષ્ટ્રીય વિદેશી યોજના (NOS):** વિદેશ અભ્યાસ માટે સંપૂર્ણ ખર્ચ\n\n")
                    .append("💡 તમે કઈ યોજના કે ચુકવણી વિશે જાણવા માગો છો? લખો અથવા માઇક દબાવીને બોલો.").toString();
        } else if ("te".equalsIgnoreCase(lang)) {
            return sb.append("నమస్కారం ").append(name).append("! 🙏\n\n")
                    .append("మీ ప్రొఫైల్ (").append(b.getCategory()).append(b.isPvtg() ? " • PVTG: " + b.getPvtgCommunityName() : "").append(", ").append(b.getState()).append(") ప్రకారం మీరు ఈ క్రింది MoTA స్కాలర్‌షిప్‌లకు అర్హులు:\n\n")
                    .append("1. **ప్రీ-మెట్రిక్:** 9-10 తరగతుల విద్యార్థులకు (సంవత్సరానికి ₹7,000 వరకు)\n")
                    .append("2. **పోస్ట్-మెట్రిక్:** 11వ తరగతి నుండి డిగ్రీ/పీజీ వరకు (పూర్తి ఫీజు + మెయింటెనెన్స్ అలవెన్స్)\n")
                    .append("3. **టాప్ క్లాస్ విద్య:** IIT, IIM, NIT లలో 100% ట్యూషన్ ఫీజు రీయింబర్స్‌మెంట్\n")
                    .append("4. **జాతీయ ఫెలోషిప్ (NFST):** Ph.D పరిశోధకులకు నెలకు ₹37,000\n")
                    .append("5. **విదేశీ స్కాలర్‌షిప్ (NOS):** విదేశాలలో ఉన్నత చదువులకు పూర్తి నిధులు\n\n")
                    .append("💡 మీకు ఏ పథకం లేదా చెల్లింపు వివరాలు కావాలో టైప్ చేయండి లేదా మైక్ నొక్కి మాట్లాడండి.").toString();
        } else if ("or".equalsIgnoreCase(lang)) {
            return sb.append("ନମସ୍କାର ").append(name).append("! 🙏\n\n")
                    .append("ଆପଣଙ୍କ ପ୍ରୋଫାଇଲ୍ ଅନୁଯାୟୀ ଆପଣ ନିମ୍ନଲିଖିତ MoTA ଛାତ୍ରବୃତ୍ତି ପାଇଁ ଯୋଗ୍ୟ:\n\n")
                    .append("1. **ପ୍ରି-ମାଟ୍ରିକ (Pre-Matric):** କ୍ଲାସ ୯-୧୦ (ବାର୍ଷିକ ₹୭,୦୦୦ ପର୍ଯ୍ୟନ୍ତ)\n")
                    .append("2. **ପୋଷ୍ଟ-ମାଟ୍ରିକ (Post-Matric):** କ୍ଲାସ ୧୧-୧୨, କଲେଜ (ସମ୍ପୂର୍ଣ୍ଣ ଫି + ଭତ୍ତା)\n")
                    .append("3. **ଟପ କ୍ଲାସ ଶିକ୍ଷା (Top Class):** IIT, IIM, NIT ଆଦିରେ ୧୦୦% ଫି ଛାଡ଼\n")
                    .append("4. **ଜାତୀୟ ଫେଲୋସିପ (NFST):** Ph.D ଗବେଷକଙ୍କ ପାଇଁ ମାସିକ ₹୩୭,୦୦୦\n")
                    .append("5. **ଜାତୀୟ ବିଦେଶୀ ବୃତ୍ତି (NOS):** ବିଦେଶରେ ପଢ଼ିବା ପାଇଁ ସମ୍ପୂର୍ଣ୍ଣ ଖର୍ଚ୍ଚ\n\n")
                    .append("💡 ଆପଣ କେଉଁ ଯୋଜନା ବାବଦରେ ଜାଣିବାକୁ ଚାହାନ୍ତି? ଟାଇପ କରନ୍ତୁ କିମ୍ବା ମାଇକ୍ ଦବାଇ କୁହନ୍ତୁ।").toString();
        } else if ("hi".equalsIgnoreCase(lang)) {
            return sb.append("नमस्ते ").append(name).append("! 🙏\n\n")
                    .append("आपके प्रोफाइल (").append(b.getCategory()).append(b.isPvtg() ? " • PVTG: " + b.getPvtgCommunityName() : "").append(", ").append(b.getState()).append(") के अनुसार आप निम्नलिखित प्रमुख MoTA छात्रवृत्तियों के लिए पात्र हैं:\n\n")
                    .append("1. **प्री-मैट्रिक (Pre-Matric):** 9वीं-10वीं कक्षा के लिए (₹7,000/वर्ष तक)\n")
                    .append("2. **पोस्ट-मैट्रिक (Post-Matric):** 11वीं, 12वीं, कॉलेज/डिग्री के लिए (पूर्ण शुल्क + अनुरक्षण भत्ता)\n")
                    .append("3. **टॉप क्लास एजुकेशन (Top Class):** IIT, IIM, NIT आदि में पूर्ण ट्यूशन फीस प्रतिपूर्ति\n")
                    .append("4. **राष्ट्रीय फैलोशिप (NFST):** पी.एचडी शोधार्थियों के लिए ₹37,000/माह\n")
                    .append("5. **राष्ट्रीय प्रवासी (NOS):** विदेश में उच्च शिक्षा के लिए पूर्ण वित्तीय सहायता\n\n")
                    .append("💡 आप किस योजना, भुगतान या आवेदन स्थिति के बारे में अधिक जानना चाहते हैं? कृपया लिखकर या माइक दबाकर बोलें।").toString();
        } else {
            return sb.append("Hello ").append(name).append("! 👋\n\n")
                    .append("Based on your verified profile (").append(b.getCategory()).append(b.isPvtg() ? " • PVTG: " + b.getPvtgCommunityName() : "").append(", ").append(b.getState()).append("), here are the schemes available for you:\n\n")
                    .append("1. **Pre-Matric:** For Class IX & X students (up to ₹7,000/year)\n")
                    .append("2. **Post-Matric:** For Class XI-XII, ITI, Diploma, UG, PG (full fee + allowance)\n")
                    .append("3. **Top Class Education:** 100% tuition waiver in 266+ premier institutes (IIT/IIM/NIT/AIIMS)\n")
                    .append("4. **National Fellowship (NFST):** ₹37,000/month for M.Phil / Ph.D. scholars\n")
                    .append("5. **National Overseas (NOS):** Full sponsorship for top 500 QS global universities\n\n")
                    .append("Ask me about your application status, DBT payment, or any specific scheme to learn more!").toString();
        }
    }
}
