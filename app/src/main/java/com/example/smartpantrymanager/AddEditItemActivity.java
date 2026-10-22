package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

// screen used for BOTH adding a new item and editing an existing one
public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private EditText etName, etQuantity;
    private Spinner spUnit;
    private TextView tvExpiry, tvTitle;
    private DatabaseHelper db;

    private int itemId = -1;       // -1 means we are adding a new item
    private String expiryDate = ""; // empty = no expiry date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        db = new DatabaseHelper(this);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        spUnit = findViewById(R.id.spUnit);
        tvExpiry = findViewById(R.id.tvExpiry);
        tvTitle = findViewById(R.id.tvFormTitle);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnClear = findViewById(R.id.btnClearExpiry);

        // fill the spinner with units from strings.xml
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);

        // check if the intent has an item id, if yes we are in edit mode
        itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            tvTitle.setText("Edit Ingredient");
            loadExistingItem();
        }

        tvExpiry.setOnClickListener(v -> showDatePicker());

        btnClear.setOnClickListener(v -> {
            expiryDate = "";
            tvExpiry.setText("No expiry date (tap to set)");
        });

        btnSave.setOnClickListener(v -> saveItem());
    }

    // gets the item from the database and fills in the form
    private void loadExistingItem() {
        PantryItem item = db.getItem(itemId);
        if (item == null) {
            finish();
            return;
        }
        etName.setText(item.getName());
        etQuantity.setText(RecipeMatcher.formatQuantity(item.getQuantity()));

        // find which position in the spinner matches the saved unit
        String[] units = getResources().getStringArray(R.array.units);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(item.getUnit())) {
                spUnit.setSelection(i);
                break;
            }
        }

        if (item.getExpiry() != null && !item.getExpiry().isEmpty()) {
            expiryDate = item.getExpiry();
            tvExpiry.setText("Expires: " + expiryDate);
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            // month starts at 0 so I have to add 1
            expiryDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            tvExpiry.setText("Expires: " + expiryDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        String qtyText = etQuantity.getText().toString().trim();

        // ---- input validation ----
        if (name.isEmpty()) {
            etName.setError("Please enter an ingredient name");
            return;
        }
        if (name.length() < 2) {
            etName.setError("Name is too short");
            return;
        }
        if (!name.matches("[A-Za-z ]+")) {
            etName.setError("Use letters only");
            return;
        }
        if (qtyText.isEmpty()) {
            etQuantity.setError("Please enter a quantity");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid number");
            return;
        }
        if (quantity <= 0) {
            etQuantity.setError("Quantity must be more than 0");
            return;
        }

        String unit = spUnit.getSelectedItem().toString();

        if (itemId == -1) {
            // CREATE
            db.addItem(new PantryItem(0, name, quantity, unit, expiryDate));
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            // UPDATE
            db.updateItem(new PantryItem(itemId, name, quantity, unit, expiryDate));
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // go back to the pantry list
    }
}