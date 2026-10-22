package com.example.smartpantrymanager;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// the 3 main screens extend this so they all share the same bottom nav bar
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupBottomNav(int selectedId) {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(selectedId); // set this BEFORE the listener or it fires straight away

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) {
                return true; // already on this screen
            }

            Intent intent;
            if (id == R.id.nav_pantry) {
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (id == R.id.nav_recipes) {
                intent = new Intent(this, SuggestedRecipesActivity.class);
            } else {
                intent = new Intent(this, SettingsActivity.class);
            }
            startActivity(intent);

            // close this screen unless it's the pantry (home) so the back stack stays tidy
            if (!(this instanceof MainActivity)) {
                finish();
            }
            return true;
        });
    }
}