package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import io.papermc.paper.potion.PotionMix;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class BrewingRecipeBuilder {
    private final NamespacedKey key;
    protected ItemStack result;
    protected TypeId input;
    protected TypeId ingredient;

    public BrewingRecipeBuilder(String id) {
        this.key = new NamespacedKey("aurora", id);
    }

    public static BrewingRecipeBuilder brewingRecipe(String id) {
        return new BrewingRecipeBuilder(id);
    }

    public BrewingRecipeBuilder result(ItemStack result) {
        this.result = result;
        return this;
    }

    public BrewingRecipeBuilder input(TypeId input) {
        this.input = input;
        return this;
    }

    public BrewingRecipeBuilder ingredient(TypeId ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public PotionMix build() {
        var inputItem = AuroraAPI.getItemManager().resolveItem(input);
        var ingredientItem = AuroraAPI.getItemManager().resolveItem(ingredient);

        return new PotionMix(
                key,
                result,
                RecipeChoice.predicateChoice(item -> AuroraAPI.getItemManager().resolveId(item).equals(input), inputItem),
                RecipeChoice.predicateChoice(item -> AuroraAPI.getItemManager().resolveId(item).equals(ingredient), ingredientItem)
        );
    }
}
