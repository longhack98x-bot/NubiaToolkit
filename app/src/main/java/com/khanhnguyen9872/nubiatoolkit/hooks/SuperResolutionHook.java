package com.khanhnguyen9872.nubiatoolkit.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Hook module for "Super Resolution" feature - enables Super Resolution on unsupported devices.
 */
public class SuperResolutionHook extends BaseHook {
    
    @Override
    public boolean shouldHook(LoadPackageParam lpparam) {
        return lpparam.packageName.equals("cn.nubia.gameassist");
    }
    
    @Override
    public void hook(LoadPackageParam lpparam) throws Throwable {
        hookPluginUtils(lpparam);
        hookSuperResolutionTypeDataManager(lpparam);
        hookZteFeature(lpparam);
        hookPluginConfig(lpparam);
    }
    
    /**
     * Hook PluginUtils methods to enable Super Resolution support
     */
    private void hookPluginUtils(LoadPackageParam lpparam) {
        try {
            // Hook getGfrcCapByPkg(String) to return 1 (supported)
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.plugin.PluginUtils",
                lpparam.classLoader,
                "getGfrcCapByPkg",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = HookUtils.getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean superResEnabled = (Boolean) settings[6];

                        if (globalEnabled && superResEnabled) {
                            param.setResult(1); // 1 = Supported
                        }
                    }
                });

            // Hook isSupportResolutionOld() -> true
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.plugin.PluginUtils",
                lpparam.classLoader,
                "isSupportResolutionOld",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = HookUtils.getSettings();
                        if ((Boolean) settings[0] && (Boolean) settings[6]) {
                            param.setResult(true);
                        }
                    }
                });

            // Hook isSupportResolutionSettingsInXml() -> true
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.plugin.PluginUtils",
                lpparam.classLoader,
                "isSupportResolutionSettingsInXml",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = HookUtils.getSettings();
                        if ((Boolean) settings[0] && (Boolean) settings[6]) {
                            param.setResult(true);
                        }
                    }
                });

            // Hook supportResolution(String) -> true
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.plugin.PluginUtils",
                lpparam.classLoader,
                "supportResolution",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = HookUtils.getSettings();
                        if ((Boolean) settings[0] && (Boolean) settings[6]) {
                            param.setResult(true);
                        }
                    }
                });

        } catch (Throwable t) {
            XposedBridge.log("SuperResolutionHook: PluginUtils hook failed: " + t.getMessage());
        }
    }
    
    /**
     * Hook SuperResolutionTypeDataManager.getItem() to return supported values
     */
    private void hookSuperResolutionTypeDataManager(LoadPackageParam lpparam) {
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
                        Object[] settings = HookUtils.getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean superResEnabled = (Boolean) settings[6];

                        if (globalEnabled && superResEnabled) {
                            String type = (String) param.args[1];
                            String result = (String) param.getResult();

                            // Override default/unsupported values with "1" (supported)
                            if ("imageQuality".equals(type) && ("origin".equals(result) || result == null)) {
                                param.setResult("1");
                            } else if ("frameRate".equals(type) && ("frameRate_origin".equals(result) || result == null)) {
                                param.setResult("1");
                            }
                        }
                    }
                });
        } catch (Throwable t) {
            XposedBridge.log("SuperResolutionHook: SuperResolutionTypeDataManager hook failed: " + t.getMessage());
        }
    }
    
    /**
     * Hook ZteFeature.isSupportSuperResolution() -> true
     */
    private void hookZteFeature(LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "com.zte.gameassist.config.ZteFeature",
                lpparam.classLoader,
                "isSupportSuperResolution",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = HookUtils.getSettings();
                        if ((Boolean) settings[0] && (Boolean) settings[6]) {
                            param.setResult(true);
                        }
                    }
                });
        } catch (Throwable t) {
            XposedBridge.log("SuperResolutionHook: ZteFeature hook failed: " + t.getMessage());
        }
    }
    
    /**
     * Hook PluginConfig.isPluginEnable() to force return true for "super_resolution"
     */
    private void hookPluginConfig(LoadPackageParam lpparam) {
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
                            Object[] settings = HookUtils.getSettings();
                            if ((Boolean) settings[0] && (Boolean) settings[6]) {
                                param.setResult(true);
                            }
                        }
                    }
                });
        } catch (Throwable t) {
            XposedBridge.log("SuperResolutionHook: PluginConfig hook failed: " + t.getMessage());
        }
    }
}
