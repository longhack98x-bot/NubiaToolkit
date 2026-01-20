package com.khanhnguyen9872.nubiatoolkit;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.content.Context;

public class SettingsProvider extends ContentProvider {

    public static final String AUTHORITY = "com.khanhnguyen9872.nubiatoolkit.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/settings");
    public static final String KEY_GLOBAL = "pref_global_enabled";
    public static final String KEY_NOKILL = "pref_nokill_enabled";
    public static final String KEY_SHOW_TOAST = "pref_show_toasts";
    public static final String KEY_GLOBAL_MODE = "pref_global_mode_enabled";
    public static final String KEY_HIDE_ENERGY_CUBE = "pref_hide_energy_cube";
    public static final String KEY_SUPER_RESOLUTION = "pref_super_resolution_enabled";
    public static final String KEY_WATERMARK_LENGTH = "pref_watermark_length_enabled";
    public static final String KEY_SMALL_WINDOW = "pref_small_window_enabled";
    public static final String KEY_LANGUAGE = "pref_language";
    private static final String PREF_NAME = "com.khanhnguyen9872.nubiatoolkit_preferences";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{KEY_GLOBAL, KEY_NOKILL, KEY_SHOW_TOAST, KEY_LANGUAGE, KEY_GLOBAL_MODE, KEY_HIDE_ENERGY_CUBE, KEY_SUPER_RESOLUTION, KEY_WATERMARK_LENGTH, KEY_SMALL_WINDOW});
        
        // Use standard context to read prefs
        SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean global = prefs.getBoolean(KEY_GLOBAL, true);
        boolean nokill = prefs.getBoolean(KEY_NOKILL, true);
        boolean showToast = prefs.getBoolean(KEY_SHOW_TOAST, true);
        boolean globalMode = prefs.getBoolean(KEY_GLOBAL_MODE, false);
        boolean hideEnergyCube = prefs.getBoolean(KEY_HIDE_ENERGY_CUBE, false);
        boolean superResolution = prefs.getBoolean(KEY_SUPER_RESOLUTION, false);
        boolean watermarkLength = prefs.getBoolean(KEY_WATERMARK_LENGTH, false);
        boolean smallWindow = prefs.getBoolean(KEY_SMALL_WINDOW, false);
        String language = prefs.getString("pref_language", "English");
        int langCode = language.equals("Tiếng Việt") ? 1 : 0;

        cursor.addRow(new Object[]{
            global ? 1 : 0, 
            nokill ? 1 : 0, 
            showToast ? 1 : 0, 
            langCode, 
            globalMode ? 1 : 0, 
            hideEnergyCube ? 1 : 0,
            superResolution ? 1 : 0,
            watermarkLength ? 1 : 0,
            smallWindow ? 1 : 0
        });
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".settings";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) { return null; }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
