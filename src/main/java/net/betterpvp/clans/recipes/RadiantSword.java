package net.betterpvp.clans.recipes;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class RadiantSword implements CustomRecipe{

    public RadiantSword(){
        Iterator<Recipe> iter = Bukkit.getServer().recipeIterator();
        while (iter.hasNext()) {
            Recipe r = iter.next();
            // May not be safe to depend on == here for recipe comparison
            // Probably safer to compare the recipe result (an ItemStack)
            if (r.getResult().getType() == Material.GOLD_SWORD) {
                iter.remove();
            }
        }

        ItemStack sword = new ItemStack(Material.GOLD_SWORD);
        ShapedRecipe  powerSword = new ShapedRecipe(sword);
        powerSword.shape(" * ", " * ", " / ");
        powerSword.setIngredient('*', Material.GOLD_BLOCK);
        powerSword.setIngredient('/', Material.STICK);
        Bukkit.getServer().addRecipe(powerSword);
        System.out.println("Radiant Sword recipe added");
    }

}
