package com.wormfarm.settings;

import java.util.Locale;
import java.util.Set;

public class AppLocale {
    private static final Set<String> SUPPORTED_LANGS = Set.of("en", "ru", "de", "fr", "es");
    private static Locale currentLocale = detectDefaultLocale();

    private static Locale detectDefaultLocale() {
        Locale sys = Locale.getDefault();
        String lang = sys.getLanguage();
        if (SUPPORTED_LANGS.contains(lang)) {
            return switch (lang) {
                case "ru" -> new Locale("ru", "RU");
                case "de" -> new Locale("de", "DE");
                case "fr" -> new Locale("fr", "FR");
                case "es" -> new Locale("es", "ES");
                default -> new Locale("en", "EN");
            };
        } else {
            return new Locale("en", "EN");
        }
    }

    public static String detectDefaultLocaleLang() {
        String sysLang = Locale.getDefault().getLanguage();
        return SUPPORTED_LANGS.contains(sysLang) ? sysLang : "en";
    }

    public static void setLocale(String language) {
        currentLocale = switch (language) {
            case "ru" -> new Locale("ru", "RU");
            case "de" -> new Locale("de", "DE");
            case "fr" -> new Locale("fr", "FR");
            case "es" -> new Locale("es", "ES");
            default -> new Locale("en", "EN");
        };
    }

    public static Locale getLocale() {
        return currentLocale;
    }
}