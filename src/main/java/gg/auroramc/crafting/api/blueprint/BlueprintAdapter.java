package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.crafting.api.vanilla.*;
import org.bukkit.inventory.*;

public class BlueprintAdapter {
    public static Recipe adapt(Blueprint blueprint) {
        if (blueprint instanceof ShapelessBlueprint shapelessBlueprint) {
            return adapt(shapelessBlueprint);
        } else if (blueprint instanceof ShapedBlueprint shapedBlueprint) {
            return adapt(shapedBlueprint);
        } else if (blueprint instanceof SmithingBlueprint smithingBlueprint) {
            return adapt(smithingBlueprint);
        } else if (blueprint instanceof CookingBlueprint cookingBlueprint) {
            return adapt(cookingBlueprint);
        } else if (blueprint instanceof StoneCutterBlueprint stoneCutterBlueprint) {
            return adapt(stoneCutterBlueprint);
        }
        return null;
    }

    public static ShapelessRecipe adapt(ShapelessBlueprint blueprint) {
        return ShapelessRecipeBuilder.shapelessRecipe(blueprint.getId(), blueprint.getVanillaOptions().getChoiceType())
                .category(blueprint.getVanillaOptions().getCategory())
                .group(blueprint.getVanillaOptions().getGroup())
                .ingredients(blueprint.getIngredientItems())
                .result(blueprint.getResultItem())
                .build();
    }

    public static ShapedRecipe adapt(ShapedBlueprint blueprint) {
        return ShapedRecipeBuilder.shapedRecipe(blueprint.getId(), blueprint.getVanillaOptions().getChoiceType(), blueprint.isSymmetrical())
                .category(blueprint.getVanillaOptions().getCategory())
                .group(blueprint.getVanillaOptions().getGroup())
                .ingredients(blueprint.getIngredientItems())
                .result(blueprint.getResultItem())
                .build();
    }

    public static SmithingTransformRecipe adapt(SmithingBlueprint blueprint) {
        return SmithingRecipeBuilder.smithingRecipe(blueprint.getId(), blueprint.getVanillaOptions().getChoiceType())
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

        return builder.cookingTime(blueprint.getVanillaOptions().getCookingTime())
                .experience(blueprint.getVanillaOptions().getExperience())
                .input(blueprint.getInputItem())
                .result(blueprint.getResultItem())
                .build();
    }

    public static StonecuttingRecipe adapt(StoneCutterBlueprint blueprint) {
        return StoneCutterRecipeBuilder.stoneCutterRecipe(blueprint.getId())
                .group(blueprint.getVanillaOptions().getGroup())
                .input(blueprint.getInputItem())
                .result(blueprint.getResultItem())
                .build();
    }
}
