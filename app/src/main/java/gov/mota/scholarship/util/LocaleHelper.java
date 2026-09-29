package gov.mota.scholarship.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_NAME = "mota_language_prefs";
    private static final String KEY_LANGUAGE = "selected_language_code";

    public static class SupportedLanguage {
        public final String code;
        public final String nativeName;
        public final String englishName;

        public SupportedLanguage(String code, String nativeName, String englishName) {
            this.code = code;
            this.nativeName = nativeName;
            this.englishName = englishName;
        }

        public String getDisplayName() {
            return nativeName + " (" + englishName + ")";
        }
    }

    public static List<SupportedLanguage> getSupportedLanguages() {
        List<SupportedLanguage> list = new ArrayList<>();
        list.add(new SupportedLanguage("en", "English", "English"));
        list.add(new SupportedLanguage("hi", "हिन्दी", "Hindi"));
        list.add(new SupportedLanguage("or", "ଓଡ଼ିଆ", "Odia"));
        list.add(new SupportedLanguage("bn", "বাংলা", "Bengali"));
        list.add(new SupportedLanguage("te", "తెలుగు", "Telugu"));
        list.add(new SupportedLanguage("gu", "ગુજરાતી", "Gujarati"));
        list.add(new SupportedLanguage("mr", "मराठी", "Marathi"));
        list.add(new SupportedLanguage("sat", "ᱥᱟᱱᱛᱟᱲᱤ", "Santali"));
        return list;
    }

    public static String getPersistedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public static void persistLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    public static Context applyLocale(Context context) {
        String lang = getPersistedLanguage(context);
        return updateResources(context, lang);
    }

    public static Context setLocale(Context context, String languageCode) {
        persistLanguage(context, languageCode);
        return updateResources(context, languageCode);
    }

    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
            return context;
        }
    }
}
