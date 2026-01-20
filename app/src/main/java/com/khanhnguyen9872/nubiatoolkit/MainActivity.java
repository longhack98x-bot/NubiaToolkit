package com.khanhnguyen9872.nubiatoolkit;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import java.io.File;

public class MainActivity extends Activity {
    
    private static final String PREF_NAME = "com.khanhnguyen9872.nubiatoolkit_preferences";
    private static final String KEY_GLOBAL_ENABLED = "pref_global_enabled";
    private static final String KEY_NOKILL_ENABLED = "pref_nokill_enabled";
    private static final String KEY_GLOBAL_MODE_ENABLED = "pref_global_mode_enabled";
    private static final String KEY_HIDE_ENERGY_CUBE = "pref_hide_energy_cube";
    private static final String KEY_SUPER_RESOLUTION = "pref_super_resolution_enabled";
    private static final String KEY_WATERMARK_LENGTH = "pref_watermark_length_enabled";
    private static final String KEY_SMALL_WINDOW = "pref_small_window_enabled";
    private static final String KEY_USE_ROOT = "pref_use_root";
    private static final String KEY_FORCE_STOP = "pref_force_stop_on_apply";
    
    private Switch switchGlobal;
    private Switch switchNoKill;
    private Switch switchGlobalMode;
    private Switch switchHideEnergyCube;
    private Switch switchSuperResolution;
    private Switch switchWatermarkLength;
    private Switch switchSmallWindow;
    private TextView titleGameHelper;
    private TextView titleGameSpace;
    private ImageView imgGameHelper;
    private ImageView imgGameSpace;
    private TextView descNoKill;
    private TextView descGlobalMode;
    private TextView descHideEnergyCube;
    private TextView descSuperResolution;
    private TextView descWatermarkLength;
    private TextView descSmallWindow;
    private TextView textStatus;
    private TextView textStatusHelper;
    private TextView textStatusSpace;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private String currentLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Store current lang to detect changes later
        currentLang = LocaleHelper.getLanguage(this);

        switchGlobal = findViewById(R.id.switch_global_enable);
        switchNoKill = findViewById(R.id.switch_feature_nokill);
        switchGlobalMode = findViewById(R.id.switch_feature_global_mode);
        switchHideEnergyCube = findViewById(R.id.switch_feature_hide_energy_cube);
        titleGameHelper = findViewById(R.id.title_category_game_helper);
        titleGameSpace = findViewById(R.id.title_category_game_space);
        imgGameHelper = findViewById(R.id.img_category_game_helper);
        imgGameSpace = findViewById(R.id.img_category_game_space);
        descNoKill = findViewById(R.id.desc_feature_nokill);
        descGlobalMode = findViewById(R.id.desc_feature_global_mode);
        descHideEnergyCube = findViewById(R.id.desc_feature_hide_energy_cube);
        descSuperResolution = findViewById(R.id.desc_feature_super_resolution);
        descWatermarkLength = findViewById(R.id.desc_feature_watermark_length);
        descSmallWindow = findViewById(R.id.desc_feature_small_window);
        switchSuperResolution = findViewById(R.id.switch_feature_super_resolution);
        switchWatermarkLength = findViewById(R.id.switch_feature_watermark_length);
        switchSmallWindow = findViewById(R.id.switch_feature_small_window);
        textStatus = findViewById(R.id.text_status);
        textStatusHelper = findViewById(R.id.text_status_helper);
        textStatusSpace = findViewById(R.id.text_status_space);
        
        final SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        fixPermissions();
        
        // Load state
        boolean isGlobalEnabled = prefs.getBoolean(KEY_GLOBAL_ENABLED, true);
        boolean isNoKillEnabled = prefs.getBoolean(KEY_NOKILL_ENABLED, true);
        boolean isGlobalModeEnabled = prefs.getBoolean(KEY_GLOBAL_MODE_ENABLED, false);
        boolean isHideEnergyCubeEnabled = prefs.getBoolean(KEY_HIDE_ENERGY_CUBE, false);
        boolean isSuperResolutionEnabled = prefs.getBoolean(KEY_SUPER_RESOLUTION, false);
        boolean isWatermarkLengthEnabled = prefs.getBoolean(KEY_WATERMARK_LENGTH, false);
        boolean isSmallWindowEnabled = prefs.getBoolean(KEY_SMALL_WINDOW, false);

        switchGlobal.setChecked(isGlobalEnabled);
        switchNoKill.setChecked(isNoKillEnabled);
        switchGlobalMode.setChecked(isGlobalModeEnabled);
        switchHideEnergyCube.setChecked(isHideEnergyCubeEnabled);
        switchSuperResolution.setChecked(isSuperResolutionEnabled);
        switchWatermarkLength.setChecked(isWatermarkLengthEnabled);
        switchSmallWindow.setChecked(isSmallWindowEnabled);
        
        // Apply initial visual state
        updateFeatureState(isGlobalEnabled);

        // Global Toggle Listener
        switchGlobal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_GLOBAL_ENABLED, isChecked).apply();
            fixPermissions();
            updateFeatureState(isChecked);
            forceStopPackages(R.string.msg_force_stop_both, R.string.msg_reminder_force_stop_both, "cn.nubia.gameassist", "cn.nubia.gamelauncher");
        });

        // Feature Toggle Listener
        switchNoKill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOKILL_ENABLED, isChecked).apply();
            fixPermissions();
            forceStopPackage("cn.nubia.gameassist", R.string.msg_force_stop_helper, R.string.msg_reminder_force_stop_helper);
        });

        switchGlobalMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_GLOBAL_MODE_ENABLED, isChecked).apply();
            fixPermissions();
            forceStopPackage("cn.nubia.gameassist", R.string.msg_force_stop_helper, R.string.msg_reminder_force_stop_helper);
        });

        switchHideEnergyCube.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_HIDE_ENERGY_CUBE, isChecked);
            if (isChecked) {
                // Auto-enable Prevent Cleanup (No Kill)
                editor.putBoolean(KEY_NOKILL_ENABLED, true);
                if (switchNoKill != null) switchNoKill.setChecked(true);
            }
            editor.apply();
            
            fixPermissions();
            // Update UI to reflect locked state
            updateFeatureState(switchGlobal.isChecked());
            forceStopPackage("cn.nubia.gameassist", R.string.msg_force_stop_helper, R.string.msg_reminder_force_stop_helper);
        });

        switchSuperResolution.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_SUPER_RESOLUTION, isChecked).apply();
            fixPermissions();
            forceStopPackage("cn.nubia.gameassist", R.string.msg_force_stop_helper, R.string.msg_reminder_force_stop_helper);
        });

        switchWatermarkLength.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_WATERMARK_LENGTH, isChecked).apply();
            fixPermissions();
            forceStopPackage("cn.nubia.gamelauncher", R.string.msg_force_stop_space, R.string.msg_reminder_force_stop_space);
        });

        switchSmallWindow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_SMALL_WINDOW, isChecked).apply();
            fixPermissions();
            forceStopPackage("cn.nubia.gameassist", R.string.msg_force_stop_helper, R.string.msg_reminder_force_stop_helper);
        });

        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            startActivity(new android.content.Intent(MainActivity.this, SettingsActivity.class));
        });

        checkStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check if language changed while we were away (e.g. in Settings)
        String newLang = LocaleHelper.getLanguage(this);
        if (!newLang.equals(currentLang)) {
            recreate();
            return;
        }
        checkStatus(); 
    }

    private void forceStopPackage(String packageName, int successMsgResId, int reminderMsgResId) {
        forceStopPackages(successMsgResId, reminderMsgResId, packageName);
    }

    private void forceStopPackages(int successMsgResId, int reminderMsgResId, String... packageNames) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                boolean useRoot = prefs.getBoolean(KEY_USE_ROOT, false);
                boolean forceStop = prefs.getBoolean(KEY_FORCE_STOP, false);

                if (useRoot && forceStop) {
                    // Force stop is enabled - automatically stop apps
                    for (String pkg : packageNames) {
                        Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "am force-stop " + pkg});
                        p.waitFor();
                    }
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(MainActivity.this, 
                            successMsgResId, 
                            android.widget.Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Force stop is disabled - show reminder toast
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(MainActivity.this, 
                            reminderMsgResId, 
                            android.widget.Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateHelperGroup(boolean globalActive, boolean categoryActive) {
        boolean active = globalActive && categoryActive;
        float alpha = active ? 1.0f : 0.4f;

        if (switchNoKill != null) {
            // Check if Energy Cube is hidden - if so, lock No Kill to enabled
            boolean hideEnergyCube = switchHideEnergyCube != null && switchHideEnergyCube.isChecked();
            if (active && hideEnergyCube) {
                switchNoKill.setEnabled(false); // Disable interaction
                switchNoKill.setAlpha(0.5f);    // Grey out to indicate sealed state
            } else {
                switchNoKill.setEnabled(active);
                switchNoKill.setAlpha(alpha);
            }
        }
        if (switchGlobalMode != null) {
            switchGlobalMode.setEnabled(active);
            switchGlobalMode.setAlpha(alpha);
        }
        if (switchHideEnergyCube != null) {
            switchHideEnergyCube.setEnabled(active);
            switchHideEnergyCube.setAlpha(alpha);
        }
        if (switchSuperResolution != null) {
            switchSuperResolution.setEnabled(active);
            switchSuperResolution.setAlpha(alpha);
        }
        if (switchSmallWindow != null) {
            switchSmallWindow.setEnabled(active);
            switchSmallWindow.setAlpha(alpha);
        }
        
        if (titleGameHelper != null) titleGameHelper.setAlpha(alpha);
        if (imgGameHelper != null) imgGameHelper.setAlpha(alpha);
        if (descNoKill != null) descNoKill.setAlpha(alpha);
        if (descGlobalMode != null) descGlobalMode.setAlpha(alpha);
        if (descHideEnergyCube != null) descHideEnergyCube.setAlpha(alpha);
        if (descSuperResolution != null) descSuperResolution.setAlpha(alpha);
        if (descSmallWindow != null) descSmallWindow.setAlpha(alpha);
    }

    private void updateSpaceGroup(boolean globalActive, boolean categoryActive) {
        boolean active = globalActive && categoryActive;
        float alpha = active ? 1.0f : 0.4f;

        if (switchWatermarkLength != null) {
            switchWatermarkLength.setEnabled(active);
            switchWatermarkLength.setAlpha(alpha);
        }
        
        if (titleGameSpace != null) titleGameSpace.setAlpha(alpha);
        if (imgGameSpace != null) imgGameSpace.setAlpha(alpha);
        if (descWatermarkLength != null) descWatermarkLength.setAlpha(alpha);
    }

    private void updateFeatureState(boolean isEnabled) {
        checkStatus();
    }
    
    // Standard check for toolkit hook
    public boolean isModuleActive() {
        return false;
    }

    // Granular checks (can be hooked to return true if module is active for these apps)
    public boolean isHelperActive() {
        return isModuleActive(); 
    }

    public boolean isSpaceActive() {
        return isModuleActive();
    }

    private void checkStatus() {
        boolean isXposedActive = isModuleActive();
        boolean isHelperActive = isHelperActive();
        boolean isSpaceActive = isSpaceActive();

        boolean isHelperInstalled = false;
        boolean isHelperEnabled = false;
        try {
            android.content.pm.PackageInfo pi = getPackageManager().getPackageInfo("cn.nubia.gameassist", 0);
            isHelperInstalled = true;
            isHelperEnabled = pi.applicationInfo.enabled;
        } catch (android.content.pm.PackageManager.NameNotFoundException ignored) {}

        boolean isSpaceInstalled = false;
        boolean isSpaceEnabled = false;
        try {
            android.content.pm.PackageInfo pi = getPackageManager().getPackageInfo("cn.nubia.gamelauncher", 0);
            isSpaceInstalled = true;
            isSpaceEnabled = pi.applicationInfo.enabled;
        } catch (android.content.pm.PackageManager.NameNotFoundException ignored) {}

        // Reset visibility
        if (textStatus != null) textStatus.setVisibility(View.GONE);
        if (textStatusHelper != null) textStatusHelper.setVisibility(View.GONE);
        if (textStatusSpace != null) textStatusSpace.setVisibility(View.GONE);

        if (!isXposedActive) {
            if (textStatus != null) {
                textStatus.setVisibility(View.VISIBLE);
                textStatus.setText(getString(R.string.status_error_xposed) + "\n" + getString(R.string.status_hint_xposed));
            }
            switchGlobal.setEnabled(false);
            switchGlobal.setAlpha(0.5f);
            updateHelperGroup(false, false);
            updateSpaceGroup(false, false);
            return;
        }

        switchGlobal.setEnabled(true);
        switchGlobal.setAlpha(1.0f);
        boolean globalChecked = switchGlobal.isChecked();

        // Check Helper Group
        boolean helperValid = isHelperActive && isHelperInstalled && isHelperEnabled;
        if (!helperValid && textStatusHelper != null) {
            textStatusHelper.setVisibility(View.VISIBLE);
            if (!isHelperInstalled) textStatusHelper.setText(R.string.status_error_pkg_missing);
            else if (!isHelperEnabled) textStatusHelper.setText(R.string.status_error_pkg_disabled);
            else textStatusHelper.setText(R.string.status_error_xposed);
        }
        updateHelperGroup(globalChecked, helperValid);

        // Check Space Group
        boolean spaceValid = isSpaceActive && isSpaceInstalled && isSpaceEnabled;
        if (!spaceValid && textStatusSpace != null) {
            textStatusSpace.setVisibility(View.VISIBLE);
            if (!isSpaceInstalled) textStatusSpace.setText(R.string.status_error_space_pkg_missing);
            else if (!isSpaceEnabled) textStatusSpace.setText(R.string.status_error_space_pkg_disabled);
            else textStatusSpace.setText(R.string.status_error_xposed);
        }
        updateSpaceGroup(globalChecked, spaceValid);
    }

    private void fixPermissions() {
        try {
            File prefsFile = new File(getDataDir(), "shared_prefs/" + PREF_NAME + ".xml");
            File prefsDir = prefsFile.getParentFile();

            if (prefsFile.exists()) {
                // Allow world read
                prefsFile.setReadable(true, false);
                // Try shell command for broader access (chmod 664)
                Runtime.getRuntime().exec(new String[]{"chmod", "664", prefsFile.getAbsolutePath()});
            }
            
            if (prefsDir.exists()) {
                // Ensure directory is traversable (chmod 771)
                prefsDir.setExecutable(true, false);
                prefsDir.setReadable(true, false);
                Runtime.getRuntime().exec(new String[]{"chmod", "771", prefsDir.getAbsolutePath()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
