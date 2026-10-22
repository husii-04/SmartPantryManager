package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

// handles all the SQLite stuff: creating tables, seeding recipes and CRUD for the pantry
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smartpantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // onCreate only runs the first time the app is installed, so the recipes get seeded once
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // simple approach for this assignment: wipe everything and rebuild
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    // ---------- PANTRY CRUD ----------

    // CREATE
    public long addItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry", item.getExpiry());
        return db.insert("pantry", null, values);
    }

    // READ (all items)
    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("pantry", null, null, null, null, null, "name ASC")) {
            while (c.moveToNext()) {
                list.add(cursorToItem(c));
            }
        }
        return list;
    }

    // READ (one item, used when editing)
    public PantryItem getItem(int id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("pantry", null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c.moveToFirst()) {
                return cursorToItem(c);
            }
        }
        return null;
    }

    // UPDATE
    public int updateItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry", item.getExpiry());
        return db.update("pantry", values, "id = ?", new String[]{String.valueOf(item.getId())});
    }

    // DELETE
    public int deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("pantry", "id = ?", new String[]{String.valueOf(id)});
    }

    // turns the current cursor row into a PantryItem object
    private PantryItem cursorToItem(Cursor c) {
        return new PantryItem(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry")));
    }

    // ---------- RECIPES ----------

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("recipes", null, null, null, null, null, "name ASC")) {
            while (c.moveToNext()) {
                Recipe r = new Recipe(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")));
                r.setIngredients(getIngredientsForRecipe(db, r.getId()));
                list.add(r);
            }
        }
        return list;
    }

    public Recipe getRecipe(int id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("recipes", null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c.moveToFirst()) {
                Recipe r = new Recipe(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")));
                r.setIngredients(getIngredientsForRecipe(db, r.getId()));
                return r;
            }
        }
        return null;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        try (Cursor c = db.query("recipe_ingredients", null, "recipe_id = ?",
                new String[]{String.valueOf(recipeId)}, null, null, null)) {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("quantity")),
                        c.getString(c.getColumnIndexOrThrow("unit"))));
            }
        }
        return list;
    }

    // helper so I don't have to repeat the insert code 20 times
    // ingredients are written as "name|amount|unit"
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert("recipes", null, rv);

        for (String ing : ingredients) {
            String[] parts = ing.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", parts[0]);
            iv.put("quantity", Double.parseDouble(parts[1]));
            iv.put("unit", parts[2]);
            db.insert("recipe_ingredients", null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Whisk the eggs with the salt.\n3. Melt the butter in a pan and scramble the eggs on low heat.\n4. Serve on the toast.",
                "egg|2|pcs", "bread|2|pcs", "butter|10|g", "salt|1|tsp");

        addRecipe(db, "Cheese Omelette",
                "1. Whisk eggs with salt.\n2. Melt butter in a pan and pour in the eggs.\n3. Add grated cheese, fold in half and cook until set.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g", "salt|1|tsp");

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until soft.\n2. Fry chopped onion and garlic in oil.\n3. Add chopped tomatoes and salt, simmer for 10 minutes.\n4. Mix with the pasta.",
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "oil|2|tbsp", "salt|1|tsp");

        addRecipe(db, "Garlic Butter Rice",
                "1. Cook the rice in salted water.\n2. Melt butter and fry crushed garlic for 1 minute.\n3. Stir the rice through the garlic butter.",
                "rice|200|g", "butter|20|g", "garlic|2|pcs", "salt|1|tsp");

        addRecipe(db, "Pancakes",
                "1. Mix flour, sugar, egg and milk into a smooth batter.\n2. Melt a little butter in a pan.\n3. Fry spoonfuls of batter until golden on both sides.",
                "flour|200|g", "milk|300|ml", "egg|2|pcs", "sugar|2|tbsp", "butter|20|g");

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Put cheese between the slices.\n3. Fry in a pan until golden and the cheese melts.",
                "bread|2|pcs", "cheese|60|g", "butter|10|g");

        addRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry chopped onion in oil.\n3. Push to the side, scramble the eggs.\n4. Add rice and soy sauce and fry for 5 minutes.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|2|tbsp");

        addRecipe(db, "Creamy Mashed Potato",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash.\n3. Mix in butter, warm milk and salt.",
                "potato|500|g", "butter|30|g", "milk|50|ml", "salt|1|tsp");

        addRecipe(db, "Chicken and Rice",
                "1. Fry the onion and garlic in oil.\n2. Add chicken pieces and brown them.\n3. Add rice, salt and enough water, cover and cook for 20 minutes.",
                "chicken|300|g", "rice|200|g", "onion|1|pcs", "garlic|2|pcs", "salt|1|tsp", "oil|2|tbsp");

        addRecipe(db, "Banana Oats",
                "1. Heat the milk and oats in a pot, stirring for 5 minutes.\n2. Slice the banana on top.\n3. Drizzle with honey.",
                "oats|80|g", "milk|250|ml", "banana|1|pcs", "honey|1|tbsp");

        addRecipe(db, "French Toast",
                "1. Whisk eggs, milk and sugar.\n2. Dip the bread slices in the mixture.\n3. Fry in butter until golden.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp", "butter|10|g");

        addRecipe(db, "Tuna Pasta",
                "1. Boil the pasta.\n2. Fry chopped onion in oil.\n3. Add the drained tuna and salt.\n4. Toss with the pasta.",
                "pasta|200|g", "tuna|1|pcs", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");

        addRecipe(db, "Lentil Soup",
                "1. Fry onion, carrot and garlic in oil.\n2. Add lentils and plenty of water.\n3. Simmer for 30 minutes, season with salt.",
                "lentil|200|g", "onion|1|pcs", "carrot|2|pcs", "garlic|2|pcs", "salt|1|tsp", "oil|1|tbsp");

        addRecipe(db, "Veggie Stir Fry",
                "1. Cook the rice.\n2. Slice the carrots, pepper and onion.\n3. Stir fry in hot oil for 5 minutes.\n4. Add soy sauce and serve over rice.",
                "rice|200|g", "carrot|2|pcs", "bell pepper|1|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");

        addRecipe(db, "Simple Guacamole",
                "1. Mash the avocados.\n2. Mix in chopped tomato, onion, lemon juice and salt.",
                "avocado|2|pcs", "tomato|1|pcs", "onion|1|pcs", "lemon|1|pcs", "salt|1|tsp");

        addRecipe(db, "Cheesy Baked Potatoes",
                "1. Bake the potatoes at 200C for about an hour.\n2. Cut open and add butter and salt.\n3. Top with cheese and bake 5 more minutes.",
                "potato|4|pcs", "cheese|100|g", "butter|20|g", "salt|1|tsp");

        addRecipe(db, "Chickpea Curry",
                "1. Fry onion and garlic in oil.\n2. Add curry powder and chopped tomatoes.\n3. Add chickpeas and simmer for 15 minutes.",
                "chickpea|400|g", "onion|1|pcs", "tomato|2|pcs", "garlic|2|pcs", "curry powder|2|tsp", "oil|1|tbsp");

        addRecipe(db, "Egg Noodles",
                "1. Boil the noodles and drain.\n2. Scramble eggs in oil.\n3. Add noodles and soy sauce and toss for 2 minutes.",
                "noodle|200|g", "egg|2|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");

        addRecipe(db, "Tomato Soup",
                "1. Fry onion and garlic in butter.\n2. Add chopped tomatoes and salt.\n3. Simmer for 20 minutes and blend until smooth.",
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "butter|20|g", "salt|1|tsp");

        addRecipe(db, "Macaroni Cheese",
                "1. Boil the pasta.\n2. Melt butter, stir in flour, then slowly add the milk to make a sauce.\n3. Add cheese until melted.\n4. Mix with the pasta.",
                "pasta|250|g", "cheese|150|g", "milk|300|ml", "butter|30|g", "flour|2|tbsp");
    }
}