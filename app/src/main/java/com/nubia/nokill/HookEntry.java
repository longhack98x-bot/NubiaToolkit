package com.nubia.nokill;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookEntry implements IXposedHookLoadPackage {

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(AndroidAppHelper.currentApplication(), message, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    XposedBridge.log("NubiaNoKill: Failed to show toast: " + e.getMessage());
                }
            }
        });
    }

    // Helper to get settings: [global(bool), nokill(bool), showToast(bool), langCode(String), globalMode(bool), hideEnergyCube(bool)]
    private Object[] getSettings() {
        boolean global = true; 
        boolean nokill = true;
        boolean showToast = true;
        boolean globalMode = false;
        boolean hideEnergyCube = false;
        String langCode = "en";
        
        try {
            Context context = AndroidAppHelper.currentApplication();
            if (context != null) {
                Cursor cursor = context.getContentResolver().query(
                    Uri.parse("content://com.nubia.nokill.provider/settings"), 
                    null, null, null, null);
                
                if (cursor != null && cursor.moveToFirst()) {
                    global = cursor.getInt(0) == 1;
                    nokill = cursor.getInt(1) == 1;
                    if (cursor.getColumnCount() > 2) showToast = cursor.getInt(2) == 1;
                    if (cursor.getColumnCount() > 3) langCode = cursor.getString(3);
                    if (cursor.getColumnCount() > 4) globalMode = cursor.getInt(4) == 1;
                    if (cursor.getColumnCount() > 5) hideEnergyCube = cursor.getInt(5) == 1;
                    cursor.close();
                }
            }
        } catch (Throwable t) {
            XposedBridge.log("NubiaNoKill: Failed to query provider: " + t.getMessage());
        }
        return new Object[]{global, nokill, showToast, langCode, globalMode, hideEnergyCube};
    }
    
    private String getLocalizedToast(String lang) {
        if ("vi".equals(lang)) return "Đã chặn dọn dẹp tự động";
        return "Prevent Automatic Cleaning";
    }

    private String getLocalizedGlobalToast(String lang) {
        if ("vi".equals(lang)) return "Chế độ Game Toàn Cầu đang bật";
        return "Global Game Mode Active";
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.nubia.nokill")) {
             XposedHelpers.findAndHookMethod("com.nubia.nokill.MainActivity", lpparam.classLoader, "isModuleActive", 
                 new XC_MethodHook() { @Override protected void beforeHookedMethod(MethodHookParam param) { param.setResult(true); }});
             return;
        }

        if (!lpparam.packageName.equals("cn.nubia.gameassist"))
            return;

        XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.dessert.policy.clean.CleanAnimationController",
                lpparam.classLoader,
                "startClean",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean featureEnabled = (Boolean) settings[1];
                        boolean showToastEnabled = (Boolean) settings[2];
                        String lang = (String) settings[3];

                        if (!globalEnabled) return;

                        if (featureEnabled) {
                            if (showToastEnabled) {
                                showToast(getLocalizedToast(lang));
                            }
                            param.setResult(null); 
                        }
                    }
                });

        // Hook MindSyncManager as a secondary layer of protection
        try {
            XposedHelpers.findAndHookMethod(
                    "com.zte.performance.mindsync.MindSyncManager",
                    lpparam.classLoader,
                    "startBgAppCleanupFromGameMode",
                    java.util.List.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            Object[] settings = getSettings();
                            if (!(Boolean)settings[0]) return; // Check global

                            if ((Boolean)settings[1]) { // Check feature
                                param.setResult(null);
                            }
                        }
                    });
        } catch (Throwable t) {
            XposedBridge.log("NubiaNoKill: MindSyncManager hook failed/class not found (optional hook)");
        }

        // Hook OneMoreThingManager to prevent "kill" command
        try {
            XposedHelpers.findAndHookMethod(
                    "cn.nubia.gameassist.onemorething.OneMoreThingManager",
                    lpparam.classLoader,
                    "linkOMTProvider",
                    String.class,
                    android.os.Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            String method = (String) param.args[0];
                            if ("kill".equals(method)) {
                                Object[] settings = getSettings();
                                boolean globalEnabled = (Boolean) settings[0];
                                boolean featureEnabled = (Boolean) settings[1];
                                String lang = (String) settings[3];

                                if (globalEnabled && featureEnabled) {
                                    XposedBridge.log("NubiaNoKill: Intercepted OneMoreThingManager.kill");
                                    // Optionally show toast if you want transparency, but users might find it annoying if it happens often.
                                    // For now, silent block.
                                    param.setResult(null); 
                                }
                            }
                        }
                    });
        } catch (Throwable t) {
            XposedBridge.log("NubiaNoKill: OneMoreThingManager hook failed: " + t.getMessage());
        }

        // Hook GameCheck for Global Game Mode (Single Arg)
        try {
            XposedHelpers.findAndHookMethod(
                "com.zte.gameassist.common.GameCheck",
                lpparam.classLoader,
                "isGameSpaceListApp",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean globalModeEnabled = (Boolean) settings[4];

                        if (globalEnabled && globalModeEnabled) {
                            param.setResult(true);
                            // No toast here to avoid spam if called frequently, 
                            // or maybe add a "once per app launch" logic?
                            // For now, let's rely on the user seeing the GameAssist UI appear.
                        }
                    }
                });
        } catch (Throwable t) {
            XposedBridge.log("NubiaNoKill: GameCheck(String) hook failed: " + t.getMessage());
        }

        // Hook GameCheck for Global Game Mode (Two Args) - The actual impl
        try {
            XposedHelpers.findAndHookMethod(
                "com.zte.gameassist.common.GameCheck",
                lpparam.classLoader,
                "isGameSpaceListApp",
                String.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean globalModeEnabled = (Boolean) settings[4];
                        boolean showToastEnabled = (Boolean) settings[2];
                        String lang = (String) settings[3];

                        if (globalEnabled && globalModeEnabled) {
                            param.setResult(true);
                            // Toast removed as per user request
                            // if (showToastEnabled) {
                            //    showToast(getLocalizedGlobalToast(lang));
                            // }
                        }
                    }
                });
        } catch (Throwable t) {
            XposedBridge.log("NubiaNoKill: GameCheck(String, int) hook failed: " + t.getMessage());
        }

        // Hook GameAssistLaunchTips.showTips() and auto-hide if from Energy Cube
        try {
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.tips.GameAssistLaunchTips",
                lpparam.classLoader,
                "showTips",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Object[] settings = getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean hideEnergyCube = (Boolean) settings[5];

                        if (globalEnabled && hideEnergyCube) {
                            try {
                                Object tipsObj = param.getResult(); // This is the GameAssistLaunchTips object
                                Boolean isFromCube = (Boolean) XposedHelpers.callMethod(tipsObj, "launchFromCube");
                                
                                if (isFromCube != null && isFromCube) {
                                    // Immediately hide the tips by calling hideTips()
                                    XposedHelpers.callMethod(tipsObj, "hideTips");
                                    showToast("Energy Cube hidden!");
                                    XposedBridge.log("NubiaNoKill: Auto-hidden Energy Cube overlay!");
                                }
                            } catch (Throwable t) {
                                XposedBridge.log("NubiaNoKill: Error in showTips hook: " + t.getMessage());
                                t.printStackTrace();
                            }
                        }
                    }
                });
            XposedBridge.log("NubiaNoKill: ✓ GameAssistLaunchTips.showTips hook installed");
        } catch (Throwable t) {
            XposedBridge.log("NubiaNoKill: ✗ showTips hook FAILED: " + t.getMessage());
        }

    }
}
