package com.khanhnguyen9872.nubiatoolkit.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Hook module for "Hide Energy Cube" feature - prevents Energy Cube overlay.
 */
public class HideEnergyCubeHook extends BaseHook {
    
    @Override
    public boolean shouldHook(LoadPackageParam lpparam) {
        return lpparam.packageName.equals("cn.nubia.gameassist");
    }
    
    @Override
    public void hook(LoadPackageParam lpparam) throws Throwable {
        hookGameAssistLaunchTips(lpparam);
    }
    
    /**
     * Hook GameAssistLaunchTips.createAndShowTips() to block Energy Cube overlay creation
     */
    private void hookGameAssistLaunchTips(LoadPackageParam lpparam) {
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
                        Object[] settings = HookUtils.getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean hideEnergyCube = (Boolean) settings[5];

                        if (globalEnabled && hideEnergyCube) {
                            param.setResult(null);
                        }
                    }
                });
        } catch (Throwable t) {
            XposedBridge.log("HideEnergyCubeHook: createAndShowTips hook failed: " + t.getMessage());
        }
    }
}
