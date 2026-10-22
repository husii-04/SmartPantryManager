package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;

// settings screen with the expiring-soon alert toggle
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SwitchCompat switchExpiry = findViewById(R.id.switchExpiry);
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);

        // load the saved setting (defaults to on)
        switchExpiry.setChecked(prefs.getBoolean(MainActivity.KEY_EXPIRY_ALERTS, true));

        // save it every time the switch is flipped
        switchExpiry.setOnCheckedChangeListener((button, isChecked) ->
                prefs.edit().putBoolean(MainActivity.KEY_EXPIRY_ALERTS, isChecked).apply());

        setupBottomNav(R.id.nav_settings);
    }
}