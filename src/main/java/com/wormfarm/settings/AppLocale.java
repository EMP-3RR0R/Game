package com.wormfarm.settings;

import java.util.Locale;
import java.util.Set;

public class AppLocale {
    private static final Set<String> SUPPORTED_LANGS = Set.of("en", "ru");
    private static Locale currentLocale = detectDefaultLocale();

    private static Locale detectDefaultLocale() {
        String lang = Locale.getDefault().getLanguage();
        return getSupportedLocale(lang);
    }

    public static String detectDefaultLocaleLang() {
        String sysLang = Locale.getDefault().getLanguage();
        return SUPPORTED_LANGS.contains(sysLang) ? sysLang : "en";
    }

    public static void setLocale(String language) {
        currentLocale = getSupportedLocale(language);
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    private static Locale getSupportedLocale(String language) {
        return switch (language) {
            case "ru" -> new Locale("ru", "RU");
            case "en" -> new Locale("en", "US");
            default -> new Locale("en", "US");
        };
    }
}