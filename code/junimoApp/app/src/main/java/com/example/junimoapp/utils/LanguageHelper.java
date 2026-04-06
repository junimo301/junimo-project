package com.example.junimoapp.utils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class LanguageHelper {
    public static void setLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        LocaleListCompat appLocale = LocaleListCompat.create(locale);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }
}
