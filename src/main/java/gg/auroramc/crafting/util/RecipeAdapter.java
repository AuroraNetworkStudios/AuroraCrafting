package gg.auroramc.crafting.util;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.blueprint.ChoiceType;
import gg.auroramc.crafting.api.vanilla.*;
import gg.auroramc.crafting.config.CookingRecipesConfig;
import gg.auroramc.crafting.config.SmithingRecipesConfig;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.recipe.CookingBookCategory;

import java.util.Locale;

public class RecipeAdapter {
    public static Recipe adapt(RecipeType type, CookingRecipesConfig.RecipeConfig config) {
        CookingRecipeBuilder<?> builder = null;

        switch (type) {
            case FURNACE -> builder = FurnaceRecipeBuilder.furnaceRecipe(config.getId());
            case SMOKING -> builder = SmokingRecipeBuilder.smokingRecipe(config.getId());
            case BLASTING -> builder = BlastingRecipeBuilder.blastingRecipe(config.getId());
            case CAMPFIRE -> builder = CampfireRecipeBuilder.campfireRecipe(config.getId());
        }

        if (builder == null) {
            return null;
        }

        if (config.getInput() != null) {
            builder.input(AuroraAPI.getItemManager().resolveItem(TypeId.fromDefault(config.getInput())));
        }
        if (config.getCategory() != null) {
            builder.category(CookingBookCategory.valueOf(config.getCategory().toUpperCase(Locale.ROOT)));
        }
        if (config.getGroup() != null) {
            builder.group(config.getGroup());
        }
        if (config.getExperience() != null) {
            builder.experience(config.getExperience());
        }
        if (config.getCookingTime() != null) {
            builder.cookingTime(config.getCookingTime());
        }

        builder.result(AuroraAPI.getItemManager().resolveItem(TypeId.fromDefault(config.getResult())));

        return builder.build();
    }

    public static Recipe adapt(RecipeType type, SmithingRecipesConfig.RecipeConfig config) {
        var builder = SmithingRecipeBuilder.smithingRecipe(config.getId(), ChoiceType.EXACT);

        if (config.getAddition() != null) {
            builder.addition(AuroraAPI.getItemManager().resolveItem(TypeId.fromDefault(config.getAddition())));
        }
        if (config.getBase() != null) {
            builder.base(AuroraAPI.getItemManager().resolveItem(TypeId.fromDefault(config.getBase())));
        }
        if (config.getTemplate() != null) {
            builder.template(AuroraAPI.getItemManager().resolveItem(TypeId.fromDefault(config.getTemplate())));
        }

        builder.result(AuroraAPI.getItemManager().resolveItem(TypeId.fromDefault(config.getResult())));

        return builder.build();
    }
}
