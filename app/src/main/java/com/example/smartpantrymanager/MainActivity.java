package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// pantry list screen (the home screen of the app)
public class MainActivity extends BaseActivity implements PantryAdapter.OnItemActionListener {

    public static final String PREFS_NAME = "pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView rvPantry;
    private TextView tvEmpty, tvBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        rvPantry = findViewById(R.id.rvPantry);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvBanner = findViewById(R.id.tvExpiryBanner);
        Button btnAdd = findViewById(R.id.btnAddItem);

        // set up the recycler view with my adapter
        adapter = new PantryAdapter(this);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        rvPantry.setAdapter(adapter);

        // intent to open the add screen (no item_id extra means "add mode")
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditItemActivity.class)));

        setupBottomNav(R.id.nav_pantry);
    }

    // onResume runs every time we come back to this screen, so the list is always fresh
    // after adding/editing/deleting something
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = db.getAllItems();
        adapter.setItems(items);

        // show the empty message instead of a blank screen
        if (items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvPantry.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvPantry.setVisibility(View.VISIBLE);
        }

        // only show the banner if the user has the setting turned on
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean alertsOn = prefs.getBoolean(KEY_EXPIRY_ALERTS, true);
        int expiring = countExpiringSoon(items);
        if (alertsOn && expiring > 0) {
            tvBanner.setText(expiring + " item(s) expiring within 3 days or already expired!");
            tvBanner.setVisibility(View.VISIBLE);
        } else {
            tvBanner.setVisibility(View.GONE);
        }
    }

    // counts items whose expiry date is within the next 3 days (or already passed)
    private int countExpiringSoon(List<PantryItem> items) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        long limit = System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000;
        int count = 0;
        for (PantryItem item : items) {
            String exp = item.getExpiry();
            if (exp != null && !exp.isEmpty()) {
                try {
                    Date d = sdf.parse(exp);
                    if (d != null && d.getTime() <= limit) {
                        count++;
                    }
                } catch (ParseException e) {
                    // bad date format, just ignore it
                }
            }
        }
        return count;
    }

    // adapter callback: user tapped a row so open the edit screen and pass the id
    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    // adapter callback: ask first before deleting
    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteItem(item.getId());
                    loadItems();
                    Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}