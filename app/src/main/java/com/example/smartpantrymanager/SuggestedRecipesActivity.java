package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// shows ONLY the recipes the user can make right now (strict matching)
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView rvRecipes;
    private TextView tvNoRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        db = new DatabaseHelper(this);
        rvRecipes = findViewById(R.id.rvRecipes);
        tvNoRecipes = findViewById(R.id.tvNoRecipes);

        adapter = new RecipeAdapter(this);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvRecipes.setAdapter(adapter);

        setupBottomNav(R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = db.getAllItems();
        List<Recipe> allRecipes = db.getAllRecipes();
        List<Recipe> makeable = new ArrayList<>();

        // run every recipe through the strict matcher, only keep the ones that pass
        for (Recipe recipe : allRecipes) {
            if (RecipeMatcher.canMake(recipe, pantry)) {
                makeable.add(recipe);
            }
        }

        adapter.setRecipes(makeable);

        // feedback message when nothing matches
        if (makeable.isEmpty()) {
            tvNoRecipes.setVisibility(View.VISIBLE);
            rvRecipes.setVisibility(View.GONE);
        } else {
            tvNoRecipes.setVisibility(View.GONE);
            rvRecipes.setVisibility(View.VISIBLE);
        }
    }

    // open the detail screen and send the recipe id with the intent
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}