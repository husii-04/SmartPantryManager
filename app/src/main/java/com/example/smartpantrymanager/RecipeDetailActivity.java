package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// shows the full ingredient list and method of one recipe
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvIngredients = findViewById(R.id.tvDetailIngredients);
        TextView tvSteps = findViewById(R.id.tvDetailSteps);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // get the id that SuggestedRecipesActivity sent through the intent
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(recipeId);

        if (recipe == null) {
            finish(); // something went wrong, just go back
            return;
        }

        tvName.setText(recipe.getName());

        // build the ingredient list as one string with a bullet on each line
        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            sb.append("\u2022 ")
                    .append(RecipeMatcher.formatQuantity(ing.getQuantity()))
                    .append(" ").append(ing.getUnit())
                    .append(" ").append(ing.getName())
                    .append("\n");
        }
        tvIngredients.setText(sb.toString().trim());
        tvSteps.setText(recipe.getSteps());
    }
}