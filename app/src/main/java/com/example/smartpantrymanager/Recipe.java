package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;

// a recipe with its steps and the list of ingredients it needs
public class Recipe {
    private int id;
    private String name;
    private String steps;
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(int id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSteps() { return steps; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
}