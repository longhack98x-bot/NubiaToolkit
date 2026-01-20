package com.khanhnguyen9872.nubiatoolkit.hooks;

import android.content.Context;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Hook module for "Small Window" feature - allows all apps to open in windowed mode.
 */
public class SmallWindowHook extends BaseHook {
    
    @Override
    public boolean shouldHook(LoadPackageParam lpparam) {
        return lpparam.packageName.equals("cn.nubia.gameassist");
    }
    
    @Override
    public void hook(LoadPackageParam lpparam) throws Throwable {
        hookSmallWindow(lpparam);
    }
    
    private void hookSmallWindow(LoadPackageParam lpparam) {
        try {
            // 1. Bypass Task Support Check
            XposedHelpers.findAndHookMethod(
                "com.zte.shared.wrapper.ActivityManagerWrapper",
                lpparam.classLoader,
                "checkTaskSupportWr",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (isEnabled()) {
                            param.setResult(true);
                        }
                    }
                }
            );

            // 2. Bypass Hide List (Blacklist)
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.utils.TilesUtil",
                lpparam.classLoader,
                "getHideAppList",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (isEnabled()) {
                            param.setResult(new ArrayList<String>());
                        }
                    }
                }
            );

            // 3. Force Small Window Feature to be enabled
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.utils.Utils",
                lpparam.classLoader,
                "isSmallWindowOpen",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (isEnabled()) {
                            param.setResult(true);
                        }
                    }
                }
            );

            // 4. Force Small Window Tile to be active (visible/expandable)
            XposedHelpers.findAndHookMethod(
                "cn.nubia.gameassist.dessert.tiles.SmallWindowTile",
                lpparam.classLoader,
                "handleUpdateState",
                "cn.nubia.gameassist.common.QSTile$State",
                Object.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (isEnabled()) {
                            Object state = param.args[0];
                            if (state != null) {
                                // Enable the tile
                                XposedHelpers.setBooleanField(state, "value", true);
                                // Make it visible if there's a visible field (usually is)
                                try {
                                    XposedHelpers.setBooleanField(state, "visible", true);
                                } catch (Throwable ignored) {}
                            }
                        }
                    }
                }
            );

        } catch (Throwable t) {
            XposedBridge.log("SmallWindowHook: Hook failed: " + t.getMessage());
        }
    }

    private boolean isEnabled() {
        Object[] settings = HookUtils.getSettings();
        if (settings.length <= 8) return false;
        boolean globalEnabled = (Boolean) settings[0];
        boolean smallWindowEnabled = (Boolean) settings[8];
        return globalEnabled && smallWindowEnabled;
    }
}
