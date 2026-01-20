package com.khanhnguyen9872.nubiatoolkit.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Hook module for "Watermark Length" feature - removes watermark text length limit.
 */
public class WatermarkLengthHook extends BaseHook {
    
    @Override
    public boolean shouldHook(LoadPackageParam lpparam) {
        return lpparam.packageName.equals("cn.nubia.gamelauncher");
    }
    
    @Override
    public void hook(LoadPackageParam lpparam) throws Throwable {
        hookWaterMarkWatcher(lpparam);
    }
    
    /**
     * Hook WaterMarkWatcher constructor to increase maxLength limit
     */
    private void hookWaterMarkWatcher(LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookConstructor(
                "cn.nubia.gamecenter.settings.watermark.WaterMarkWatcher",
                lpparam.classLoader,
                android.widget.EditText.class,
                int.class, // maxLength
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] settings = HookUtils.getSettings();
                        boolean globalEnabled = (Boolean) settings[0];
                        boolean watermarkLengthEnabled = (Boolean) settings[7];
                        
                        if (globalEnabled && watermarkLengthEnabled) {
                            // Increase maxLength to 1000 (from default limit)
                            param.args[1] = 1000;
                        }
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log("WatermarkLengthHook: WaterMarkWatcher hook failed: " + t.getMessage());
        }
    }
}
