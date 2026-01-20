package com.nubia.nokill;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;

public class SettingsActivity extends Activity {

    private static final String PREF_NAME = "com.nubia.nokill_preferences";
    private static final String KEY_SHOW_TOAST = "pref_show_toasts";
    private static final String KEY_USE_ROOT = "pref_use_root";
    private static final String KEY_FORCE_STOP = "pref_force_stop_on_apply";
    
    private Switch switchToast;
    private Switch switchUseRoot;
    private Switch switchForceStop;
    private TextView textUseRootDesc;
    private TextView textLanguage;
    private TextView textVersion;
    private TextView textForceStopDesc;
    private LinearLayout cardForceStop;
    private LinearLayout cardUseRoot;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchToast = findViewById(R.id.switch_show_toasts);
        switchUseRoot = findViewById(R.id.switch_use_root);
        switchForceStop = findViewById(R.id.switch_force_stop);
        textUseRootDesc = findViewById(R.id.text_use_root_desc);
        cardUseRoot = findViewById(R.id.card_use_root);
        textForceStopDesc = findViewById(R.id.text_force_stop_desc);
        cardForceStop = findViewById(R.id.card_force_stop);
        textLanguage = findViewById(R.id.text_language_value);
        textVersion = findViewById(R.id.text_version_value);
        
        // Initialize UI Logic
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean rootAvailable = checkRootAvailable();
        
        // --- Use Root ---
        boolean useRoot = prefs.getBoolean(KEY_USE_ROOT, false); // Default false for safety
        
        if (!rootAvailable) {
            useRoot = false;
            useRoot = false;
            switchUseRoot.setEnabled(false);
            cardUseRoot.setEnabled(false);
            cardUseRoot.setAlpha(0.5f);
            switchUseRoot.setChecked(false);
            textUseRootDesc.setText(R.string.desc_root_unavailable);
            textUseRootDesc.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            prefs.edit().putBoolean(KEY_USE_ROOT, false).apply();
        } else {
            switchUseRoot.setChecked(useRoot);
            switchUseRoot.setEnabled(true);
        }

        switchUseRoot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_USE_ROOT, isChecked).apply();
            updateForceStopState(isChecked);
            fixPermissions();
        });

        // --- Force Stop ---
        boolean forceStop = prefs.getBoolean(KEY_FORCE_STOP, false);
        switchForceStop.setChecked(forceStop);
        updateForceStopState(switchUseRoot.isChecked());

        switchForceStop.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_FORCE_STOP, isChecked).apply();
            fixPermissions();
        });
        
        // Show Toasts Toggle
        boolean showToast = prefs.getBoolean(KEY_SHOW_TOAST, true);
        switchToast.setChecked(showToast);
        
        switchToast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_SHOW_TOAST, isChecked).apply();
            fixPermissions();
        });

        // Language Card Click
        findViewById(R.id.card_language).setOnClickListener(v -> showLanguageDialog());
        
        // Author Card Click
        findViewById(R.id.card_author).setOnClickListener(v -> {
            android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, 
                android.net.Uri.parse("https://github.com/KhanhNguyen9872"));
            startActivity(browserIntent);
        });

        // Back Button Logic
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Set Version
        textVersion.setText("1.0");

        // Update dynamic labels (only those that are not static in XML)
        updateDynamicLabels();
    }
    
    private void updateDynamicLabels() {
        // Only need to update the Language Value text (English vs Tiếng Việt)
        // The rest of the UI (Titles, Descriptions) are handled automatically by Resources + LocaleHelper
        String code = LocaleHelper.getLanguage(this);
        textLanguage.setText(code.equals("vi") ? "Tiếng Việt" : "English");
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Tiếng Việt"};
        final String[] codes = {"en", "vi"};
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.label_language)
            .setItems(languages, (dialog, which) -> {
                String selectedCode = codes[which];
                // Check if changed
                if (!selectedCode.equals(LocaleHelper.getLanguage(this))) {
                    LocaleHelper.setLocale(this, selectedCode);
                    fixPermissions();
                    recreate(); // Recreate activity to apply new language resources
                }
            })
            .show();
    }
    
    private void fixPermissions() {
        try {
            java.io.File prefsFile = new java.io.File(getDataDir(), "shared_prefs/" + PREF_NAME + ".xml");
            if (prefsFile.exists()) {
                prefsFile.setReadable(true, false);
                Runtime.getRuntime().exec(new String[]{"chmod", "664", prefsFile.getAbsolutePath()});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateForceStopState(boolean useRoot) {
        switchForceStop.setEnabled(useRoot);
        cardForceStop.setAlpha(useRoot ? 1.0f : 0.5f);
        cardForceStop.setEnabled(useRoot);
        
        if (useRoot) {
            textForceStopDesc.setText(R.string.desc_force_stop);
        } else {
            switchForceStop.setChecked(false);
            getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_FORCE_STOP, false).apply();
                
            // Append red warning
            String originalDesc = getString(R.string.desc_force_stop);
            String warning = getString(R.string.warning_need_root);
            String html = originalDesc + "<br/><font color='#FF0000'>" + warning + "</font>";
            textForceStopDesc.setText(android.text.Html.fromHtml(html));
        }
    }

    private boolean checkRootAvailable() {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(new String[]{"su", "-c", "ls /data"}); // Try a simple root command
            int status = p.waitFor();
            return status == 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (p != null) p.destroy();
        }
    }
}
