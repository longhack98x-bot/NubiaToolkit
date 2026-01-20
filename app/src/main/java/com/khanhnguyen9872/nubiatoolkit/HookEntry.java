package com.khanhnguyen9872.nubiatoolkit;

import com.khanhnguyen9872.nubiatoolkit.hooks.*;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Main entry point for Nubia Toolkit Xposed module.
 * Coordinates all feature-specific hook modules.
 */
public class HookEntry implements IXposedHookLoadPackage {
    
    private final List<BaseHook> hooks = new ArrayList<>();
    
    public HookEntry() {
        // Register all hook modules
        hooks.add(new NoKillHook());
        hooks.add(new GlobalGameModeHook());
        hooks.add(new HideEnergyCubeHook());
        hooks.add(new SuperResolutionHook());
        hooks.add(new WatermarkLengthHook());
        hooks.add(new SmallWindowHook());
    }
    
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // Hook toolkit's own methods to verify module is active
        if (lpparam.packageName.equals("com.khanhnguyen9872.nubiatoolkit")) {
            hookToolkit(lpparam);
            return;
        }
        
        // Execute all registered hooks
        for (BaseHook hook : hooks) {
            if (hook.shouldHook(lpparam)) {
                try {
                    hook.hook(lpparam);
                    XposedBridge.log("NubiaToolkit: " + hook.getHookName() + " loaded successfully for " + lpparam.packageName);
                } catch (Throwable t) {
                    XposedBridge.log("NubiaToolkit: " + hook.getHookName() + " failed: " + t.getMessage());
                }
            }
        }
    }
    
    /**
     * Hook toolkit's own methods to signal that the module is active.
     */
    private void hookToolkit(LoadPackageParam lpparam) {
        try {
            // Hook to verify module is active
            XposedHelpers.findAndHookMethod(
                "com.khanhnguyen9872.nubiatoolkit.MainActivity", 
                lpparam.classLoader, 
                "isModuleActive", 
                new XC_MethodHook() { 
                    @Override 
                    protected void beforeHookedMethod(MethodHookParam param) { 
                        param.setResult(true); 
                    }
                });
            
            // Hook to verify GameHelper hook is active
            XposedHelpers.findAndHookMethod(
                "com.khanhnguyen9872.nubiatoolkit.MainActivity", 
                lpparam.classLoader, 
                "isHelperActive", 
                new XC_MethodHook() { 
                    @Override 
                    protected void beforeHookedMethod(MethodHookParam param) { 
                        param.setResult(true); 
                    }
                });
            
            // Hook to verify GameSpace hook is active
            XposedHelpers.findAndHookMethod(
                "com.khanhnguyen9872.nubiatoolkit.MainActivity", 
                lpparam.classLoader, 
                "isSpaceActive", 
                new XC_MethodHook() { 
                    @Override 
                    protected void beforeHookedMethod(MethodHookParam param) { 
                        param.setResult(true); 
                    }
                });
                
            XposedBridge.log("NubiaToolkit: Self-hook successful");
        } catch (Throwable t) {
            XposedBridge.log("NubiaToolkit: Toolkit self-hook failed: " + t.getMessage());
        }
    }
}
