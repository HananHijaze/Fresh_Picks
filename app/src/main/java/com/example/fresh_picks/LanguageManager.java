package com.example.fresh_picks;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class LanguageManager {
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_LANGUAGE = "language";
    private SharedPreferences preferences;

    public LanguageManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setLanguage(String langCode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_LANGUAGE, langCode);
        editor.apply();
    }
    public String getLanguage() {
        return preferences.getString(KEY_LANGUAGE, "en"); // Default is English if not set
    }


}
