package com.khanhnguyen9872.nubiatoolkit;

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

    // Helper to get settings: [global(bool), nokill(bool), showToast(bool), langCode(String), globalMode(bool), hideEnergyCube(bool), superResolution(bool)]
    private Object[] getSettings() {
        boolean global = true; 
        boolean nokill = true;
        boolean showToast = true;
        boolean globalMode = false;
        boolean hideEnergyCube = false;
        boolean superResolution = false;
        boolean watermarkLength = false;
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
                    cursor.close();
                }
            }
        } catch (Throwable t) {
            XposedBridge.log("NubiaNoKill: Failed to query provider: " + t.getMessage());
        }
        return new Object[]{global, nokill, showToast, langCode, globalMode, hideEnergyCube, superResolution, watermarkLength};
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
        if (lpparam.packageName.equals("com.khanhnguyen9872.nubiatoolkit")) {
            try {
                XposedHelpers.findAndHookMethod("com.khanhnguyen9872.nubiatoolkit.MainActivity", lpparam.classLoader, "isModuleActive", 
                     new XC_MethodHook() { @Override protected void beforeHookedMethod(MethodHookParam param) { param.setResult(true); }});
                XposedHelpers.findAndHookMethod("com.khanhnguyen9872.nubiatoolkit.MainActivity", lpparam.classLoader, "isHelperActive", 
                     new XC_MethodHook() { @Override protected void beforeHookedMethod(MethodHookParam param) { param.setResult(true); }});
                XposedHelpers.findAndHookMethod("com.khanhnguyen9872.nubiatoolkit.MainActivity", lpparam.classLoader, "isSpaceActive", 
                     new XC_MethodHook() { @Override protected void beforeHookedMethod(MethodHookParam param) { param.setResult(true); }});
            } catch (Throwable t) {
                XposedBridge.log("NubiaNoKill: Toolkit activity hooks failed: " + t.getMessage());
            }
            return;
        }


        if (lpparam.packageName.equals("cn.nubia.gamelauncher")) {
            // Hook for Watermark Length in GameSpace (cn.nubia.gamelauncher)
            try {
                XposedHelpers.findAndHookConstructor(
                    "cn.nubia.gamecenter.settings.watermark.WaterMarkWatcher",
                    lpparam.classLoader,
                    android.widget.EditText.class,
                    int.class, // maxLength
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            Object[] settings = getSettings();
                            boolean globalEnabled = (Boolean) settings[0];
                            boolean watermarkLengthEnabled = (Boolean) settings[7];
                            
                            if (globalEnabled && watermarkLengthEnabled) {
                                // Increase maxLength to something large (e.g., 100)
                                param.args[1] = 100;
                            }
                        }
                    }
                );
            } catch (Throwable t) {
                XposedBridge.log("NubiaNoKill: WaterMarkWatcher hook failed: " + t.getMessage());
            }
        }

        if (!lpparam.packageName.equals("cn.nubia.gameassist") && !lpparam.packageName.equals("cn.nubia.gamelauncher"))
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
                                    param.setResult(null); 
                                }
                            }
                        }
                    });
        } catch (Throwable t) {
            // Ignored or logged if needed
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
                        }
                    }
                });
        } catch (Throwable t) {
             // Ignored
        }

        // Hook GameCheck for Global Game Mode (Two Args) 
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
                        // boolean showToastEnabled = (Boolean) settings[2];
                        // String lang = (String) settings[3];

                        if (globalEnabled && globalModeEnabled) {
                            param.setResult(true);
                        }
                    }
                });
        } catch (Throwable t) {
             // Ignored
        }

        // Hook GameAssistLaunchTips.createAndShowTips() to block Energy Cube overlay creation
        try {
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.tips.GameAssistLaunchTips",
                lpparam.classLoader,
                "createAndShowTips",
                android.content.Context.class,
                android.os.Handler.class,
                android.os.Handler.class,
                String.class,
                String.class,
                java.util.List.class,
                Runnable.class,
                String.class, // launchWay
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean hideEnergyCube = (Boolean) settings[5];

                        if (globalEnabled && hideEnergyCube) {
                            param.setResult(null);
                        }
                    }
                });
        } catch (Throwable t) {
            XposedBridge.log("NubiaNoKill: createAndShowTips hook failed: " + t.getMessage());
        }

        // --- Super Resolution Hooks ---
        
        // Hook PluginUtils.getGfrcCapByPkg(String) to return 1 (supported)
        try {
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.plugin.PluginUtils",
                lpparam.classLoader,
                "getGfrcCapByPkg",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean superResEnabled = (Boolean) settings[6];

                        if (globalEnabled && superResEnabled) {
                            param.setResult(1); // 1 = Supported
                        }
                    }
                });

            // Hook PluginUtils.isSupportResolutionOld() -> true
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.plugin.PluginUtils",
                lpparam.classLoader,
                "isSupportResolutionOld",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = getSettings();
                        if ((Boolean) settings[0] && (Boolean) settings[6]) {
                            param.setResult(true);
                        }
                    }
                });

            // Hook PluginUtils.isSupportResolutionSettingsInXml() -> true
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.plugin.PluginUtils",
                lpparam.classLoader,
                "isSupportResolutionSettingsInXml",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = getSettings();
                        if ((Boolean) settings[0] && (Boolean) settings[6]) {
                            param.setResult(true);
                        }
                    }
                });

             // Hook PluginUtils.supportResolution(String) -> true
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.plugin.PluginUtils",
                lpparam.classLoader,
                "supportResolution",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = getSettings();
                        if ((Boolean) settings[0] && (Boolean) settings[6]) {
                            param.setResult(true);
                        }
                    }
                });

        } catch (Throwable t) {
            XposedBridge.log("NubiaNoKill: PluginUtils hook failed: " + t.getMessage());
        }

        // Hook SuperResolutionTypeDataManager.getItem(String pkg, String type) to return "1" (supported)
        try {
            XposedHelpers.findAndHookMethod(
                "cn.nubia.plugin.superresolution.SuperResolutionTypeDataManager",
                lpparam.classLoader,
                "getItem",
                String.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Object[] settings = getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean superResEnabled = (Boolean) settings[6];

                        if (globalEnabled && superResEnabled) {
                            String pkg = (String) param.args[0];
                            String type = (String) param.args[1];
                            String result = (String) param.getResult();

                            // If result is null or "origin" or "frameRate_origin" (which mean unsupported/off), override it.
                            // Based on smali: "imageQuality" expects "1" or "2" for high/super? 
                            // "frameRate" expects "1"? 
                            // Actually, let's look at smali again.
                            // getItem returns "origin" or "frameRate_origin" if not found.
                            // If found, it returns the value stored.
                            
                            // We want to pretend the device SUPPORTS these modes.
                            // Usually "1" implies supported/on in these Nubia plugins.
                            
                            // Wait, getItem in smali returns the current SETTING value for that package? 
                            // Or the CAPABILITY?
                            // Smali: 
                            // public getItem(String pkg, String type) { ... return itemData.getImageQualityItem() or getFrameRateItem() ... }
                            // If invalid, returns "origin" (0) or "frameRate_origin".
                            
                            // If this manages the SELECTION state, forcing it might lock it to ON.
                            // If this manages the CAPABILITY/AVAILABLE OPTIONS, forcing it enables the UI.
                            
                            // Let's assume this is the stored configuration. 
                            // If the list is empty (because loadList found nothing), getItem returns default.
                            // The Controller likely calls this to see "what is the current state" or "supports what".
                            
                            // Let's try forcing a return of "1" (which usually means enabled/supported level 1).
                            // But better logic: check if it's "imageQuality" or "frameRate".
                            
                            if ("imageQuality".equals(type) && ("origin".equals(result) || result == null)) {
                                param.setResult("1");
                            } else if ("frameRate".equals(type) && ("frameRate_origin".equals(result) || result == null)) {
                                param.setResult("1");
                            }
                        }
                    }
                });
        } catch (Throwable t) {
            XposedBridge.log("NubiaNoKill: SuperResolutionTypeDataManager hook failed: " + t.getMessage());
        }

        // --- New Hooks for Tile Registration ---
        
        // Hook ZteFeature.isSupportSuperResolution() -> true
        try {
            XposedHelpers.findAndHookMethod(
                "com.zte.gameassist.config.ZteFeature",
                lpparam.classLoader,
                "isSupportSuperResolution",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                         Object[] settings = getSettings();
                        if ((Boolean) settings[0] && (Boolean) settings[6]) {
                            param.setResult(true);
                        }
                    }
                });
        } catch (Throwable t) {
             XposedBridge.log("NubiaNoKill: ZteFeature hook failed: " + t.getMessage());
        }

        // Hook PluginConfig.isPluginEnable to force return true for "super_resolution"
        try {
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.plugin.config.PluginConfig",
                lpparam.classLoader,
                "isPluginEnable",
                android.content.Context.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String pluginName = (String) param.args[1];
                        if ("super_resolution".equals(pluginName) || "super_resolution_old".equals(pluginName)) {
                             Object[] settings = getSettings();
                            if ((Boolean) settings[0] && (Boolean) settings[6]) {
                                param.setResult(true);
                            }
                        }
                    }
                });
        } catch (Throwable t) {
             XposedBridge.log("NubiaNoKill: PluginConfig hook failed: " + t.getMessage());
        }
    }
}
