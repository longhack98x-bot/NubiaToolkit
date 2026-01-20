package com.khanhnguyen9872.nubiatoolkit.hooks;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Abstract base class for all hook modules.
 * Provides common functionality and defines interface for hook implementations.
 */
public abstract class BaseHook {
    
    /**
     * Implement hook logic for this module.
     * @param lpparam LoadPackageParam from Xposed
     * @throws Throwable if hook fails
     */
    public abstract void hook(LoadPackageParam lpparam) throws Throwable;
    
    /**
     * Determine if this hook should be executed for the given package.
     * @param lpparam LoadPackageParam from Xposed
     * @return true if hook should be executed
     */
    public abstract boolean shouldHook(LoadPackageParam lpparam);
    
    /**
     * Get the name of this hook module for logging purposes.
     * @return hook module name
     */
    public String getHookName() {
        return this.getClass().getSimpleName();
    }
}
