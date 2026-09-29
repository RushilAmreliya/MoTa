package gov.mota.scholarship.util;

import android.content.Context;
import gov.mota.scholarship.R;

public class LocalizationUtils {

    /**
     * Translates beneficiary full names according to current locale.
     */
    public static String getLocalizedBeneficiaryName(Context context, String englishName) {
        if (englishName == null) return "";
        String lang = LocaleHelper.getPersistedLanguage(context);
        if ("en".equalsIgnoreCase(lang)) {
            return englishName;
        }

        switch (englishName.trim()) {
            case "Kavita Marandi":
                switch (lang) {
                    case "hi": return "कविता मरांडी";
                    case "or": return "କବିତା ମାରାଣ୍ଡି";
                    case "bn": return "কবিতা মারান্ডি";
                    case "te": return "కవిత మరాండి";
                    case "gu": return "કવિતા મરાંડી";
                    case "mr": return "कविता मरांडी";
                    case "sat": return "ᱠᱚᱵᱤᱛᱟ ᱢᱟᱨᱟᱱᱰᱤ";
                }
                break;
            case "Birsa Munda":
                switch (lang) {
                    case "hi": return "बिरसा मुंडा";
                    case "or": return "ବିର୍ସା ମୁଣ୍ଡା";
                    case "bn": return "বিরসা মুন্ডা";
                    case "te": return "బిర్సా ముండా";
                    case "gu": return "બિરસા મુંડા";
                    case "mr": return "बिरसा मुंडा";
                    case "sat": return "ᱵᱤᱨᱥᱟ ᱢᱩᱱᱰᱟ";
                }
                break;
            case "Salomi Munda":
                switch (lang) {
                    case "hi": return "सलोमी मुंडा";
                    case "or": return "ସାଲୋମି ମୁଣ୍ଡା";
                    case "bn": return "সালোমি মুন্ডা";
                    case "te": return "సలోమి ముండా";
                    case "gu": return "સલોમી મુંડા";
                    case "mr": return "सलोमी मुंडा";
                    case "sat": return "ᱥᱟᱞᱳᱢᱤ ᱢᱩᱱᱰᱟ";
                }
                break;
            case "Sunita Marandi":
                switch (lang) {
                    case "hi": return "सुनीता मरांडी";
                    case "or": return "ସୁନୀତା ମାରାଣ୍ଡି";
                    case "bn": return "সুনীতা মারান্ডি";
                    case "te": return "సునీత మరాండి";
                    case "gu": return "સુનીતા મરાંડી";
                    case "mr": return "सुनीता मरांडी";
                    case "sat": return "ᱥᱩᱱᱤᱛᱟ ᱢᱟᱨᱟᱱᱰᱤ";
                }
                break;
            case "Kiran Kumar Jamatia":
                switch (lang) {
                    case "hi": return "किरण कुमार जमातिया";
                    case "or": return "କିରଣ କୁମାର ଜମାତିଆ";
                    case "bn": return "কিরণ কুমার জমাতিয়া";
                    case "te": return "కిరణ్ కుమార్ జమాతియా";
                    case "gu": return "કિરણ કુમાર જમાતિયા";
                    case "mr": return "किरण कुमार जमातिया";
                    case "sat": return "ᱠᱤᱨᱚᱱ ᱠᱩᱢᱟᱨ ᱡᱟᱢᱟᱛᱤᱭᱟ";
                }
                break;
            case "Anjali Gond":
                switch (lang) {
                    case "hi": return "अंजलि गोंड";
                    case "or": return "ଅଞ୍ଜଳି ଗୋଣ୍ଡ";
                    case "bn": return "অঞ্জলি গোন্ড";
                    case "te": return "అంజలి గోండ్";
                    case "gu": return "અંજલિ ગોંડ";
                    case "mr": return "अंजली गोंड";
                    case "sat": return "ᱚᱧᱡᱚᱞᱤ ᱜᱳᱱᱰ";
                }
                break;
            case "Rameshwar Uraon":
                switch (lang) {
                    case "hi": return "रामेश्वर उरांव";
                    case "or": return "ରାମେଶ୍ୱର ଉରାଓଁ";
                    case "bn": return "রামেশ্বর উরাওঁ";
                    case "te": return "రామేశ్వర్ ఉరాన్";
                    case "gu": return "રામેશ્વર ઉરાંવ";
                    case "mr": return "रामेश्वर उरांव";
                    case "sat": return "ᱨᱟᱢᱮᱥᱣᱚᱨ ᱩᱨᱟᱶ";
                }
                break;
        }
        return englishName;
    }

    /**
     * Translates state names according to current locale.
     */
    public static String getLocalizedState(Context context, String englishState) {
        if (englishState == null) return "";
        String lang = LocaleHelper.getPersistedLanguage(context);
        if ("en".equalsIgnoreCase(lang)) return englishState;

        switch (englishState.trim()) {
            case "Jharkhand":
                switch (lang) {
                    case "hi": case "mr": return "झारखंड";
                    case "or": return "ଝାଡ଼ଖଣ୍ଡ";
                    case "bn": return "ঝাড়খণ্ড";
                    case "te": return "జార్ఖండ్";
                    case "gu": return "ઝારખંડ";
                    case "sat": return "ᱡᱷᱟᱨᱠᱷᱚᱸᱰ";
                }
                break;
            case "Odisha":
                switch (lang) {
                    case "hi": case "mr": return "ओडिशा";
                    case "or": return "ଓଡ଼ିଶା";
                    case "bn": return "ওড়িশা";
                    case "te": return "ఒడిశా";
                    case "gu": return "ઓડિશા";
                    case "sat": return "ᱳᱰᱤᱥᱟ";
                }
                break;
            case "Tripura":
                switch (lang) {
                    case "hi": case "mr": return "त्रिपुरा";
                    case "or": return "ତ୍ରିପୁରା";
                    case "bn": return "ত্রিপুরা";
                    case "te": return "త్రిపుర";
                    case "gu": return "ત્રિપુરા";
                    case "sat": return "ᱛᱨᱤᱯᱩᱨᱟ";
                }
                break;
            case "Madhya Pradesh":
                switch (lang) {
                    case "hi": case "mr": return "मध्य प्रदेश";
                    case "or": return "ମଧ୍ୟପ୍ରଦେଶ";
                    case "bn": return "মধ্যপ্রদেশ";
                    case "te": return "మధ్యప్రదేశ్";
                    case "gu": return "મધ્યપ્રદેશ";
                    case "sat": return "ᱢᱚᱫᱷᱭᱚ ᱯᱨᱚᱫᱮᱥ";
                }
                break;
            case "Chhattisgarh":
                switch (lang) {
                    case "hi": case "mr": return "छत्तीसगढ़";
                    case "or": return "ଛତିଶଗଡ଼";
                    case "bn": return "ছত্তিশগড়";
                    case "te": return "ఛత్తీస్‌గఢ్";
                    case "gu": return "છત્તીસગઢ";
                    case "sat": return "ᱪᱷᱚᱛᱤᱥᱜᱚᱰ";
                }
                break;
        }
        return englishState;
    }

    /**
     * Translates institution names according to current locale.
     */
    public static String getLocalizedInstitution(Context context, String englishInst) {
        if (englishInst == null) return "";
        String lang = LocaleHelper.getPersistedLanguage(context);
        if ("en".equalsIgnoreCase(lang)) return englishInst;

        if (englishInst.contains("Birsa Institute of Technology")) {
            switch (lang) {
                case "hi": return "बिरसा प्रौद्योगिकी संस्थान, सिंदरी";
                case "or": return "ବିର୍ସା ପ୍ରଯୁକ୍ତିବିଦ୍ୟା ଅନୁଷ୍ଠାନ, ସିନ୍ଦ୍ରି";
                case "bn": return "বিরসা প্রযুক্তি সংস্থা, সিন্দ্রি";
                case "te": return "బిర్సా ఇన్స్టిట్యూట్ ఆఫ్ టెక్నాలజీ, సింద్రి";
                case "gu": return "બિરસા ઇન્સ્ટિટ્યૂટ ઑફ ટેકનોલોજી, સિંદરી";
                case "mr": return "बिरसा तंत्रज्ञान संस्था, सिंदरी";
                case "sat": return "ᱵᱤᱨᱥᱟ ᱤᱱᱥᱴᱤᱴᱤᱭᱩᱴ ᱚᱯᱷ ᱴᱮᱠᱱᱳᱞᱳᱡᱤ, ᱥᱤᱱᱫᱽᱨᱤ";
            }
        } else if (englishInst.contains("Sido Kanhu Murmu University")) {
            switch (lang) {
                case "hi": return "सिदो कान्हू मुर्मू विश्वविद्यालय, दुमका";
                case "or": return "ସିଦୋ କାହ୍ନୁ ମୁର୍ମୁ ବିଶ୍ୱବିଦ୍ୟାଳୟ, ଦୁମକା";
                case "bn": return "সিদো কানহু মুর্মু বিশ্ববিদ্যালয়, দুমকা";
                case "te": return "సిదో కాన్హు ముర్ము విశ్వవిద్యాలయం, దుమ్కా";
                case "gu": return "સિદો કાન્હુ મુર્મુ યુનિવર્સિટી, દુમકા";
                case "mr": return "सिदो कान्हू मुर्मू विद्यापीठ, दुमका";
                case "sat": return "ᱥᱤᱫᱳ ᱠᱟᱹᱱᱦᱩ ᱢᱩᱨᱢᱩ ᱡᱮᱜᱮᱛ ᱵᱤᱨᱫᱟᱹᱜᱟᱲ, ᱫᱩᱢᱠᱟ";
            }
        } else if (englishInst.contains("Government High School, Morabadi")) {
            switch (lang) {
                case "hi": return "शासकीय उच्च विद्यालय, मोराबादी, रांची";
                case "or": return "ସରକାରୀ ଉଚ୍ଚ ବିଦ୍ୟାଳୟ, ମୋରାବାଦୀ, ରାଞ୍ଚି";
                case "bn": return "সরকারি উচ্চ বিদ্যালয়, মোরাবাদী, রাঁচি";
                case "te": return "ప్రభుత్వ ఉన్నత పాఠశాల, మోరాబాది, రాంచీ";
                case "gu": return "સરકારી હાઇસ્કૂલ, મોરાબાદી, રાંચી";
                case "mr": return "शासकीय उच्च विद्यालय, मोराबादी, रांची";
                case "sat": return "ᱥᱚᱨᱠᱟᱨᱤ ᱩᱥᱩᱞ ᱟᱥᱲᱟ, ᱢᱳᱨᱟᱵᱟᱫᱤ, ᱨᱟᱺᱪᱤ";
            }
        } else if (englishInst.contains("North Orissa University")) {
            switch (lang) {
                case "hi": return "उत्तरी ओडिशा विश्वविद्यालय, बारीपदा";
                case "or": return "ଉତ୍ତର ଓଡ଼ିଶା ବିଶ୍ୱବିଦ୍ୟାଳୟ, ବାରିପଦା";
                case "bn": return "উত্তর ওড়িশা বিশ্ববিদ্যালয়, বারিপদা";
                case "te": return "ఉత్తర ఒరిస్సా విశ్వవిద్యాలయం, బారిపద";
                case "gu": return "ઉત્તર ઓડિશા યુનિવર્સિટી, બારીપદા";
                case "mr": return "उत्तर ओडिशा विद्यापीठ, बारीपदा";
                case "sat": return "ᱩᱛᱛᱚᱨ ᱳᱰᱤᱥᱟ ᱡᱮᱜᱮᱛ ᱵᱤᱨᱫᱟᱹᱜᱟᱲ, ᱵᱟᱨᱤᱯᱚᱫᱟ";
            }
        } else if (englishInst.contains("National Institute of Technology Agartala")) {
            switch (lang) {
                case "hi": return "राष्ट्रीय प्रौद्योगिकी संस्थान अगरतला";
                case "or": return "ଜାତୀୟ ପ୍ରଯୁକ୍ତିବିଦ୍ୟା ଅନୁଷ୍ଠାନ ଅଗରତାଲା";
                case "bn": return "ন্যাশনাল ইনস্টিটিউট অব টেকনোলজি আগরতলা";
                case "te": return "నేషనల్ ఇన్స్టిట్యూట్ ఆఫ్ టెక్నాలజీ అగర్తలా";
                case "gu": return "નેશનલ ઇન્સ્ટિટ્યૂટ ઑફ ટેકનોલોજી અગરતલા";
                case "mr": return "राष्ट्रीय तंत्रज्ञान संस्था अगरतळा";
                case "sat": return "ᱱᱮᱥᱱᱟᱞ ᱤᱱᱥᱴᱤᱴᱤᱭᱩᱴ ᱚᱯᱷ ᱴᱮᱠᱱᱳᱞᱳᱡᱤ ᱟᱜᱚᱨᱛᱚᱞᱟ";
            }
        } else if (englishInst.contains("University of Oxford")) {
            switch (lang) {
                case "hi": case "mr": return "ऑक्सफोर्ड विश्वविद्यालय, यूनाइटेड किंगडम";
                case "or": return "ଅକ୍ସଫୋର୍ଡ଼ ବିଶ୍ୱବିଦ୍ୟାଳୟ, ୟୁକେ";
                case "bn": return "অক্সফোর্ড বিশ্ববিদ্যালয়, যুক্তরাজ্য";
                case "te": return "ఆక్స్‌ఫర్డ్ విశ్వవిద్యాలయం, యునైటెడ్ కింగ్‌డమ్";
                case "gu": return "ઓક્સફર્ડ યુનિવર્સિટી, યુનાઇટેડ કિંગડમ";
                case "sat": return "ᱚᱠᱥᱯᱷᱳᱨᱰ ᱡᱮᱜᱮᱛ ᱵᱤᱨᱫᱟᱹᱜᱟᱲ, ᱭᱩᱠᱮ";
            }
        } else if (englishInst.contains("Eklavya Model Residential School")) {
            switch (lang) {
                case "hi": return "एकलव्य आदर्श आवासीय विद्यालय (EMRS), बस्तर";
                case "or": return "ଏକଲବ୍ୟ ଆଦର୍ଶ ଆବାସିକ ବିଦ୍ୟାଳୟ (EMRS), ବସ୍ତର";
                case "bn": return "একক্লব্য মডেল রেসিডেন্সিয়াল স্কুল, বস্তার";
                case "te": return "ఏకలవ్య మోడల్ రెసిడెన్షియల్ స్కూల్ (EMRS), బస్తర్";
                case "gu": return "એકલવ્ય મોડેલ રેસિડેન્શિયલ સ્કૂલ (EMRS), બસ્તર";
                case "mr": return "एकलव्य आदर्श निवासी शाळा (EMRS), बस्तर";
                case "sat": return "ᱮᱠᱞᱚᱵᱽᱭᱚ ᱢᱳᱰᱮᱞ ᱨᱮᱥᱤᱰᱮᱱᱥᱤᱭᱟᱞ ᱟᱥᱲᱟ, ᱵᱚᱥᱛᱚᱨ";
            }
        }
        return englishInst;
    }

    /**
     * Translates scheme title according to current locale.
     */
    public static String getLocalizedSchemeTitle(Context context, String schemeCode, String fallbackTitle) {
        if (schemeCode == null) return fallbackTitle;
        switch (schemeCode.toUpperCase()) {
            case "PRE_MATRIC":
                return context.getString(R.string.scheme_pre_matric);
            case "POST_MATRIC":
                return context.getString(R.string.scheme_post_matric);
            case "TOP_CLASS":
                return context.getString(R.string.scheme_top_class);
            case "NFST":
                return context.getString(R.string.scheme_nfst);
            case "NOS":
                return context.getString(R.string.scheme_nos);
            default:
                return fallbackTitle;
        }
    }
}
