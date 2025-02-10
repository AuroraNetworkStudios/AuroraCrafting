package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.crafting.api.vanilla.*;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;

public class BlueprintAdapter {
    public static ShapelessRecipe adapt(ShapelessBlueprint blueprint) {
        return ShapelessRecipeBuilder.shapelessRecipe(blueprint.getId(), blueprint.getVanillaOptions().getChoiceType())
                .category(blueprint.getVanillaOptions().getCategory())
                .group(blueprint.getVanillaOptions().getGroup())
                .ingredients(blueprint.getIngredientItems())
                .result(blueprint.getResultItem())
                .build();
    }

    public static ShapedRecipe adapt(ShapedBlueprint blueprint) {
        return ShapedRecipeBuilder.shapedRecipe(blueprint.getId(), blueprint.getVanillaOptions().getChoiceType())
                .category(blueprint.getVanillaOptions().getCategory())
                .group(blueprint.getVanillaOptions().getGroup())
                .ingredients(blueprint.getIngredientItems())
                .result(blueprint.getResultItem())
                .build();
    }

    public static SmithingTransformRecipe adapt(SmithingBlueprint blueprint) {
        return SmithingRecipeBuilder.smithingRecipe(blueprint.getId())
                .template(blueprint.getTemplateItem())
                .base(blueprint.getBaseItem())
                .addition(blueprint.getAdditionItem())
                .result(blueprint.getResultItem())
                .build();
    }

    public static CookingRecipe adapt(CookingBlueprint blueprint) {
        CookingRecipeBuilder<?> builder = switch (blueprint.getType()) {
            case FURNACE -> FurnaceRecipeBuilder.furnaceRecipe(blueprint.getId());
            case BLAST_FURNACE -> BlastingRecipeBuilder.blastingRecipe(blueprint.getId());
            case SMOKER -> SmokingRecipeBuilder.smokingRecipe(blueprint.getId());
            case CAMPFIRE -> CampfireRecipeBuilder.campfireRecipe(blueprint.getId());
        };

        return builder.cookingTime(blueprint.getVanillaOptions().cookingTime())
                .experience(blueprint.getVanillaOptions().experience())
                .input(blueprint.input())
                .result(blueprint.getResultItem())
                .build();
    }
}
