package com.khanhnguyen9872.nubiatoolkit.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Hook module for "No Kill" feature - prevents automatic app cleanup.
 */
public class NoKillHook extends BaseHook {
    
    @Override
    public boolean shouldHook(LoadPackageParam lpparam) {
        return lpparam.packageName.equals("cn.nubia.gameassist");
    }
    
    @Override
    public void hook(LoadPackageParam lpparam) throws Throwable {
        hookCleanAnimationController(lpparam);
        hookMindSyncManager(lpparam);
        hookOneMoreThingManager(lpparam);
    }
    
    /**
     * Hook CleanAnimationController.startClean() - primary prevention
     */
    private void hookCleanAnimationController(LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.dessert.policy.clean.CleanAnimationController",
                lpparam.classLoader,
                "startClean",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = HookUtils.getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean featureEnabled = (Boolean) settings[1];
                        boolean showToastEnabled = (Boolean) settings[2];
                        String lang = (String) settings[3];

                        if (!globalEnabled) return;

                        if (featureEnabled) {
                            if (showToastEnabled) {
                                HookUtils.showToast(HookUtils.getLocalizedNoKillToast(lang));
                            }
                            param.setResult(null); 
                        }
                    }
                });
        } catch (Throwable t) {
            XposedBridge.log("NoKillHook: CleanAnimationController hook failed: " + t.getMessage());
        }
    }
    
    /**
     * Hook MindSyncManager.startBgAppCleanupFromGameMode() - secondary protection
     */
    private void hookMindSyncManager(LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "com.zte.performance.mindsync.MindSyncManager",
                lpparam.classLoader,
                "startBgAppCleanupFromGameMode",
                java.util.List.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = HookUtils.getSettings();
                        if (!(Boolean)settings[0]) return; // Check global

                        if ((Boolean)settings[1]) { // Check feature
                            param.setResult(null);
                        }
                    }
                });
        } catch (Throwable t) {
            XposedBridge.log("NoKillHook: MindSyncManager hook failed/class not found (optional hook)");
        }
    }
    
    /**
     * Hook OneMoreThingManager.linkOMTProvider() - intercept "kill" command
     */
    private void hookOneMoreThingManager(LoadPackageParam lpparam) {
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
                            Object[] settings = HookUtils.getSettings();
                            boolean globalEnabled = (Boolean) settings[0];
                            boolean featureEnabled = (Boolean) settings[1];

                            if (globalEnabled && featureEnabled) {
                                XposedBridge.log("NoKillHook: Intercepted OneMoreThingManager.kill");
                                param.setResult(null); 
                            }
                        }
                    }
                });
        } catch (Throwable t) {
            // Optional hook - silently fail
        }
    }
}
