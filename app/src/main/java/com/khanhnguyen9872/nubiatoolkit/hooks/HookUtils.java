package com.khanhnguyen9872.nubiatoolkit.hooks;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import de.robv.android.xposed.XposedBridge;

/**
 * Utility class providing shared helper methods for all hooks.
 */
public class HookUtils {
    
    /**
     * Get settings from the provider.
     * @return Object array: [global(bool), nokill(bool), showToast(bool), langCode(String), 
     *                        globalMode(bool), hideEnergyCube(bool), superResolution(bool), watermarkLength(bool), smallWindow(bool)]
     */
    public static Object[] getSettings() {
        boolean global = true; 
        boolean nokill = true;
        boolean showToast = true;
        boolean globalMode = false;
        boolean hideEnergyCube = false;
        boolean superResolution = false;
        boolean watermarkLength = false;
        boolean smallWindow = false;
        String langCode = "en";
        
        try {
            Context context = AndroidAppHelper.currentApplication();
            if (context != null) {
                Cursor cursor = context.getContentResolver().query(
                    Uri.parse("content://com.khanhnguyen9872.nubiatoolkit.provider/settings"), 
                    null, null, null, null);
                
                if (cursor != null && cursor.moveToFirst()) {
                    global = cursor.getInt(0) == 1;
                    nokill = cursor.getInt(1) == 1;
                    if (cursor.getColumnCount() > 2) showToast = cursor.getInt(2) == 1;
                    if (cursor.getColumnCount() > 3) langCode = cursor.getString(3);
                    if (cursor.getColumnCount() > 4) globalMode = cursor.getInt(4) == 1;
                    if (cursor.getColumnCount() > 5) hideEnergyCube = cursor.getInt(5) == 1;
                    if (cursor.getColumnCount() > 6) superResolution = cursor.getInt(6) == 1;
                    if (cursor.getColumnCount() > 7) watermarkLength = cursor.getInt(7) == 1;
                    if (cursor.getColumnCount() > 8) smallWindow = cursor.getInt(8) == 1;
                    cursor.close();
                }
            }
        } catch (Throwable t) {
            XposedBridge.log("NubiaToolkit: Failed to query provider: " + t.getMessage());
        }
        return new Object[]{global, nokill, showToast, langCode, globalMode, hideEnergyCube, superResolution, watermarkLength, smallWindow};
    }
    
    /**
     * Display a toast message on the main thread.
     * @param message Message to display
     */
    public static void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(AndroidAppHelper.currentApplication(), message, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    XposedBridge.log("NubiaToolkit: Failed to show toast: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get localized message for "Prevent Automatic Cleaning" toast.
     * @param lang Language code ("vi" or "en")
     * @return Localized message
     */
    public static String getLocalizedNoKillToast(String lang) {
        if ("vi".equals(lang)) return "Đã chặn dọn dẹp tự động";
        return "Prevent Automatic Cleaning";
    }
    
    /**
     * Get localized message for "Global Game Mode" toast.
     * @param lang Language code ("vi" or "en")
     * @return Localized message
     */
    public static String getLocalizedGlobalGameModeToast(String lang) {
        if ("vi".equals(lang)) return "Chế độ Game Toàn Cầu đang bật";
        return "Global Game Mode Active";
    }
}
