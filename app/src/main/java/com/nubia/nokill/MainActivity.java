package com.nubia.nokill;

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
import java.io.File;

public class MainActivity extends Activity {
    
    private static final String PREF_NAME = "com.nubia.nokill_preferences";
    private static final String KEY_GLOBAL_ENABLED = "pref_global_enabled";
    private static final String KEY_NOKILL_ENABLED = "pref_nokill_enabled";
    private static final String KEY_GLOBAL_MODE_ENABLED = "pref_global_mode_enabled";
    private static final String KEY_HIDE_ENERGY_CUBE = "pref_hide_energy_cube";
    private static final String KEY_USE_ROOT = "pref_use_root";
    private static final String KEY_FORCE_STOP = "pref_force_stop_on_apply";
    
    private Switch switchGlobal;
    private Switch switchNoKill;
    private Switch switchGlobalMode;
    private Switch switchHideEnergyCube;
    private TextView titleFeatures;
    private TextView descNoKill;
    private TextView descGlobalMode;
    private TextView textStatus;

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
        titleFeatures = findViewById(R.id.title_feature_nokill);
        descNoKill = findViewById(R.id.desc_feature_nokill);
        descGlobalMode = findViewById(R.id.desc_feature_global_mode);
        textStatus = findViewById(R.id.text_status);
        
        final SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        fixPermissions();
        
        // Load state
        boolean isGlobalEnabled = prefs.getBoolean(KEY_GLOBAL_ENABLED, true);
        boolean isNoKillEnabled = prefs.getBoolean(KEY_NOKILL_ENABLED, true);
        boolean isGlobalModeEnabled = prefs.getBoolean(KEY_GLOBAL_MODE_ENABLED, false);
        boolean isHideEnergyCubeEnabled = prefs.getBoolean(KEY_HIDE_ENERGY_CUBE, false);

        switchGlobal.setChecked(isGlobalEnabled);
        switchNoKill.setChecked(isNoKillEnabled);
        switchGlobalMode.setChecked(isGlobalModeEnabled);
        switchHideEnergyCube.setChecked(isHideEnergyCubeEnabled);
        
        // Apply initial visual state
        updateFeatureState(isGlobalEnabled);

        // Global Toggle Listener
        switchGlobal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_GLOBAL_ENABLED, isChecked).apply();
            fixPermissions();
            updateFeatureState(isChecked);
            killGameAssist();
        });

        // Feature Toggle Listener
        switchNoKill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOKILL_ENABLED, isChecked).apply();
            fixPermissions();
            killGameAssist();
        });

        switchGlobalMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_GLOBAL_MODE_ENABLED, isChecked).apply();
            fixPermissions();
            killGameAssist();
        });

        switchHideEnergyCube.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_HIDE_ENERGY_CUBE, isChecked).apply();
            fixPermissions();
            killGameAssist();
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

    private void killGameAssist() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                boolean useRoot = prefs.getBoolean(KEY_USE_ROOT, false);
                boolean forceStop = prefs.getBoolean(KEY_FORCE_STOP, false);

                if (useRoot && forceStop) {
                    // Requires Root
                    Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "am force-stop cn.nubia.gameassist"});
                    p.waitFor();
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(MainActivity.this, 
                            R.string.msg_force_stop_applied, 
                            android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateFeatureState(boolean isEnabled) {
        switchNoKill.setEnabled(isEnabled);
        switchGlobalMode.setEnabled(isEnabled);
        
        float alpha = isEnabled ? 1.0f : 0.4f;
        switchNoKill.setAlpha(alpha);
        switchGlobalMode.setAlpha(alpha);
        switchHideEnergyCube.setEnabled(isEnabled);
        switchHideEnergyCube.setAlpha(alpha);
        titleFeatures.setAlpha(alpha);
        descNoKill.setAlpha(alpha);
        descGlobalMode.setAlpha(alpha);
    }
    
    // This method is hooked by HookEntry to return true if module is active
    public boolean isModuleActive() {
        return false;
    }

    private void checkStatus() {
        boolean isXposedActive = isModuleActive();
        boolean isGameAssistInstalled = false;
        boolean isGameAssistEnabled = false;
        
        try {
            android.content.pm.PackageInfo pi = getPackageManager().getPackageInfo("cn.nubia.gameassist", 0);
            isGameAssistInstalled = true;
            isGameAssistEnabled = pi.applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            isGameAssistInstalled = false;
        }

        StringBuilder statusMsg = new StringBuilder();
        boolean hasError = false;

        if (!isXposedActive) {
            statusMsg.append(getString(R.string.status_error_xposed)).append("\n");
            statusMsg.append(getString(R.string.status_hint_xposed)).append("\n\n");
            hasError = true;
        }

        if (!isGameAssistInstalled) {
            statusMsg.append(getString(R.string.status_error_pkg_missing)).append("\n");
            hasError = true;
        } else if (!isGameAssistEnabled) {
            statusMsg.append(getString(R.string.status_error_pkg_disabled)).append("\n");
            statusMsg.append(getString(R.string.status_hint_enable)).append("\n\n");
            hasError = true;
        }
        
        if (!hasError) {
             textStatus.setVisibility(View.GONE);
             switchGlobal.setEnabled(true);
             switchGlobal.setAlpha(1.0f);
             updateFeatureState(switchGlobal.isChecked());
        } else {
             textStatus.setVisibility(View.VISIBLE);
             textStatus.setText(statusMsg.toString().trim());
             switchGlobal.setEnabled(false);
             switchGlobal.setAlpha(0.5f);
             updateFeatureState(false);
        }
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
