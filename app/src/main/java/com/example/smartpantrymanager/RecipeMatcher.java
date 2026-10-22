package com.example.smartpantrymanager;

import java.util.List;
import java.util.Locale;

// this is the core logic of the app (section 2.3 of the brief).
// a recipe only counts as makeable if EVERY ingredient is in the pantry in a big enough amount.
public class RecipeMatcher {

    // makes names comparable: "Tomatoes " and "tomato" both become "tomato"
    public static String normaliseName(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z ]", "").replaceAll("\\s+", " ");

        // simple singular rules, not perfect but good enough for food names
        if (s.endsWith("ies") && s.length() > 4) {
            s = s.substring(0, s.length() - 3) + "y";      // berries -> berry
        } else if (s.endsWith("oes")) {
            s = s.substring(0, s.length() - 2);             // tomatoes -> tomato
        } else if (s.endsWith("ss") || s.endsWith("us")) {
            // leave words like "hummus" or "molasss" alone
        } else if (s.endsWith("s") && s.length() > 2) {
            s = s.substring(0, s.length() - 1);             // onions -> onion
        }
        return s;
    }

    // "pcs" is a count, everything else is a measured amount
    private static boolean isCount(String unit) {
        return unit.equalsIgnoreCase("pcs");
    }

    // converts any unit into a base amount (g / ml / pcs) so they can be compared.
    // I treat 1 ml as roughly 1 g to keep it simple.
    private static double toBase(double qty, String unit) {
        switch (unit.toLowerCase(Locale.ROOT)) {
            case "kg":   return qty * 1000;
            case "l":    return qty * 1000;
            case "tbsp": return qty * 15;
            case "tsp":  return qty * 5;
            case "cup":  return qty * 240;
            default:     return qty; // g, ml and pcs stay the same
        }
    }

    // returns true only if the whole recipe can be made with the current pantry
    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        for (RecipeIngredient needed : recipe.getIngredients()) {
            String neededName = normaliseName(needed.getName());
            double have = 0;

            // add up every pantry item with the same name (user might have 2 entries of rice)
            for (PantryItem item : pantry) {
                boolean sameName = normaliseName(item.getName()).equals(neededName);
                // can't compare pieces to grams, so the unit types must match
                boolean sameType = isCount(item.getUnit()) == isCount(needed.getUnit());
                if (sameName && sameType) {
                    have += toBase(item.getQuantity(), item.getUnit());
                }
            }

            double need = toBase(needed.getQuantity(), needed.getUnit());
            if (have < need) {
                return false; // one ingredient missing or not enough = recipe is out
            }
        }
        return true;
    }

    // shows 2 instead of 2.0 but keeps 1.5 as 1.5
    public static String formatQuantity(double q) {
        if (q == Math.floor(q)) {
            return String.valueOf((long) q);
        }
        return String.valueOf(q);
    }
}