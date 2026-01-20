package com.khanhnguyen9872.nubiatoolkit.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Hook module for "Global Game Mode" feature - treats all apps as game apps.
 */
public class GlobalGameModeHook extends BaseHook {
    
    @Override
    public boolean shouldHook(LoadPackageParam lpparam) {
        return lpparam.packageName.equals("cn.nubia.gameassist");
    }
    
    @Override
    public void hook(LoadPackageParam lpparam) throws Throwable {
        hookGameCheckSingleArg(lpparam);
        hookGameCheckTwoArgs(lpparam);
    }
    
    /**
     * Hook GameCheck.isGameSpaceListApp(String) - single argument variant
     */
    private void hookGameCheckSingleArg(LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "com.zte.gameassist.common.GameCheck",
                lpparam.classLoader,
                "isGameSpaceListApp",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = HookUtils.getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean globalModeEnabled = (Boolean) settings[4];

                        if (globalEnabled && globalModeEnabled) {
                            param.setResult(true);
                        }
                    }
                });
        } catch (Throwable t) {
            // Silently fail - method might not exist on all variants
        }
    }
    
    /**
     * Hook GameCheck.isGameSpaceListApp(String, int) - two arguments variant
     */
    private void hookGameCheckTwoArgs(LoadPackageParam lpparam) {
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
                        Object[] settings = HookUtils.getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean globalModeEnabled = (Boolean) settings[4];

                        if (globalEnabled && globalModeEnabled) {
                            param.setResult(true);
                        }
                    }
                });
        } catch (Throwable t) {
            // Silently fail - method might not exist on all variants
        }
    }
}
