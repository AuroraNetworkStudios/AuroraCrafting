package gg.auroramc.crafting.parser;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.command.CommandDispatcher;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.blueprint.*;
import gg.auroramc.crafting.api.book.BookCategory;
import gg.auroramc.crafting.api.workbench.Workbench;
import gg.auroramc.crafting.config.CauldronRecipesConfig;
import gg.auroramc.crafting.config.CookingRecipesConfig;
import gg.auroramc.crafting.config.CraftingRecipesConfig;
import gg.auroramc.crafting.config.SmithingRecipesConfig;
import gg.auroramc.crafting.config.StoneCutterRecipesConfig;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class BlueprintParser {
    private final Workbench workbench;
    private final BookCategory category;
    private final String recipeId;

    public static BlueprintParser from(Workbench workbench, @Nullable BookCategory category, String recipeId) {
        return new BlueprintParser(workbench, category, recipeId);
    }

    public Blueprint parse(CraftingRecipesConfig.RecipeConfig config) {
        CraftingBookCategory vanillaCategory;
        ChoiceType choiceType;

        try {
            if (config.getVanillaOptions().getCategory() == null) {
                vanillaCategory = CraftingBookCategory.MISC;
            } else {
                vanillaCategory = CraftingBookCategory.valueOf(config.getVanillaOptions().getCategory().toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            vanillaCategory = CraftingBookCategory.MISC;
            AuroraCrafting.logger().warning("Invalid cooking category: " + config.getVanillaOptions().getCategory() + " in recipe: " + recipeId);
        }

        try {
            choiceType = ChoiceType.valueOf(config.getVanillaOptions().getChoiceType().toUpperCase());
        } catch (IllegalArgumentException e) {
            choiceType = ChoiceType.EXACT;
            AuroraCrafting.logger().warning("Invalid choice type: " + config.getVanillaOptions().getChoiceType() + " in recipe: " + recipeId);
        }

        CraftingBlueprint<?> blueprint;

        if (config.getShapeless()) {
            blueprint = ShapelessBlueprint.shapelessBlueprint(workbench, config.getId());
        } else {
            blueprint = ShapedBlueprint.shapedBlueprint(workbench, config.getId()).symmetrical(config.getSymmetry());
        }

        var ret = blueprint.vanillaOptions(CraftingBlueprint.VanillaOptions.builder()
                        .category(vanillaCategory)
                        .choiceType(choiceType)
                        .group(config.getVanillaOptions().getGroup())
                        .build())
                .displayOptions(Blueprint.DisplayOptions.builder()
                        .items(config.getDisplayOptions().getItems())
                        .lockedLore(config.getDisplayOptions().getLockedLore().isEmpty() ? config.getLockedLore() : config.getDisplayOptions().getLockedLore())
                        .build())
                .permission(config.getPermission())
                .ingredients(config.getIngredients().stream().map(i -> parseItemPair(i, Material.BARRIER)).toList())
                .category(category)
                .source(config.getSourcePath())
                .onCraft((player, result, amount) -> {
                    for (var cmd : config.getOnCraft()) {
                        CommandDispatcher.dispatch(player, cmd,
                                Placeholder.of("amount", amount),
                                Placeholder.of("amount_formatted", AuroraAPI.formatNumber(amount)),
                                Placeholder.of("result", getLocalizedResultName(result)));
                    }
                });

        if (!config.getMergeOptions().isEmpty()) {
            for (var entry : config.getMergeOptions().entrySet()) {
                var mergeOptions = entry.getValue();
                var index = entry.getKey() - 1;
                ret.mergeOptions(index, new Blueprint.MergeOptions(
                        mergeOptions.getEnchants(),
                        mergeOptions.getTrim(),
                        mergeOptions.getPdc(),
                        mergeOptions.getRestoreDurability(),
                        mergeOptions.getMergeDurability(),
                        mergeOptions.getCopyDurability())
                );
            }
        }

        return withResult(ret, config.getResult());
    }

    public Blueprint parse(CookingRecipesConfig.RecipeConfig config, CookingBlueprint.Type type) {
        CookingBookCategory vanillaCategory;

        try {
            if (config.getCategory() == null) {
                vanillaCategory = CookingBookCategory.MISC;
            } else {
                vanillaCategory = CookingBookCategory.valueOf(config.getCategory().toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            vanillaCategory = CookingBookCategory.MISC;
            AuroraCrafting.logger().warning("Invalid cooking category: " + config.getCategory() + " in recipe: " + recipeId);
        }

        return CookingBlueprint.cookingBlueprint(workbench, config.getId())
                .type(type)
                .input(parseItemPair(config.getInput(), Material.BARRIER))
                .vanillaOptions(
                        CookingBlueprint.VanillaOptions.builder()
                                .cookingTime(config.getCookingTime())
                                .experience(config.getExperience())
                                .group(config.getGroup())
                                .category(vanillaCategory)
                                .build())
                .category(category)
                .source(config.getSourcePath())
                .displayOptions(Blueprint.DisplayOptions.builder()
                        .items(config.getDisplayOptions().getItems())
                        .lockedLore(config.getDisplayOptions().getLockedLore())
                        .build())
                .result(parseItemPair(config.getResult(), Material.AIR)).complete();
    }

    public Blueprint parse(CauldronRecipesConfig.RecipeConfig config) {
        Material fluid = Material.getMaterial(config.getFluid());
        if(fluid == null) {
            throw new IllegalArgumentException("Invalid fluid material: " + config.getFluid() + " in recipe: " + recipeId);
        }


        return CauldronBlueprint.cauldronBlueprint(workbench, config.getId())
                .addIngredient(parseItemPair(config.getInput(), Material.BARRIER))
                .vanillaOptions(
                        CauldronBlueprint.VanillaOptions.builder()
                                .experience(config.getExperience())
                                .fluidLevel(config.getFluidLevel())
                                .fluid(fluid)
                                .build()
                )
                .category(category)
                .source(config.getSourcepath())
                .displayOptions(Blueprint.DisplayOptions.builder()
                        .items(config.getDisplayOptions().getItems())
                        .lockedLore(config.getDisplayOptions().getLockedLore())
                        .build())
                .result(parseItemPair(config.getResult(), Material.AIR));
    }

    public Blueprint parse(SmithingRecipesConfig.RecipeConfig config) {
        ChoiceType choiceType;

        try {
            choiceType = ChoiceType.valueOf(config.getVanillaOptions().getChoiceType().toUpperCase());
        } catch (IllegalArgumentException e) {
            choiceType = ChoiceType.EXACT;
            AuroraCrafting.logger().warning("Invalid choice type: " + config.getVanillaOptions().getChoiceType() + " in recipe: " + recipeId);
        }

        var ret = SmithingBlueprint.smithingBlueprint(workbench, config.getId())
                .template(parseItemPair(config.getTemplate(), Material.BARRIER))
                .base(parseItemPair(config.getBase(), Material.BARRIER))
                .addition(parseItemPair(config.getAddition(), Material.BARRIER))
                .vanillaOptions(SmithingBlueprint.VanillaOptions.builder().choiceType(choiceType).build())
                .permission(config.getPermission())
                .displayOptions(Blueprint.DisplayOptions.builder()
                        .items(config.getDisplayOptions().getItems())
                        .lockedLore(config.getDisplayOptions().getLockedLore())
                        .build())
                .category(category)
                .source(config.getSourcePath())
                .onCraft(config.getOnCraft() != null ? (player, result, amount) -> {
                    for (var cmd : config.getOnCraft()) {
                        CommandDispatcher.dispatch(player, cmd,
                                Placeholder.of("amount", amount),
                                Placeholder.of("amount_formatted", AuroraAPI.formatNumber(amount)),
                                Placeholder.of("result", getLocalizedResultName(result)));
                    }
                } : null);

        if (!config.getMergeOptions().isEmpty()) {
            for (var entry : config.getMergeOptions().entrySet()) {
                var mergeOptions = entry.getValue();
                var index = entry.getKey() - 1;
                ret.mergeOptions(index, new Blueprint.MergeOptions(
                        mergeOptions.getEnchants(),
                        mergeOptions.getTrim(),
                        mergeOptions.getPdc(),
                        mergeOptions.getRestoreDurability(),
                        mergeOptions.getMergeDurability(),
                        mergeOptions.getCopyDurability())
                );
            }
        }

        return withResult(ret, config.getResult());
    }

    public Blueprint parse(StoneCutterRecipesConfig.RecipeConfig config) {
        return StoneCutterBlueprint.stoneCutterBlueprint(workbench, config.getId())
                .input(parseItemPair(config.getInput(), Material.BARRIER))
                .vanillaOptions(StoneCutterBlueprint.VanillaOptions.builder().group(config.getVanillaOptions().getGroup()).build())
                .category(category)
                .source(config.getSourcePath())
                .displayOptions(Blueprint.DisplayOptions.builder()
                        .items(config.getDisplayOptions().getItems())
                        .lockedLore(config.getDisplayOptions().getLockedLore())
                        .build())
                .result(parseItemPair(config.getResult(), Material.AIR)).complete();
    }

    private Blueprint withResult(Blueprint blueprint, String result) {
        var resultIndex = parseResultIngredientIndex(result);
        if (resultIndex != null) {
            blueprint.result(resultIndex);
        } else {
            blueprint.result(parseItemPair(result, Material.AIR));
        }
        return blueprint.complete();
    }

    private Integer parseResultIngredientIndex(String result) {
        var split = result.split(":");
        if (split[0].equals("ingredient")) {
            try {
                return (split.length > 1 ? Integer.parseInt(split[1]) : 1) - 1;
            } catch (NumberFormatException e) {
                AuroraCrafting.logger().severe("Invalid ingredient index: " + split[1] + " in recipe: " + recipeId);
            }
        }
        return null;
    }

    private ItemPair parseItemPair(String input, Material invalidMaterial) {
        if (input == null) {
            return new ItemPair(TypeId.from(Material.AIR), 0);
        }
        var split = input.split("/");
        if (split[0].isEmpty()) {
            return new ItemPair(TypeId.from(Material.AIR), 0);
        }
        var pair = split.length > 1
                ? new ItemPair(TypeId.fromDefault(split[0]), Integer.parseInt(split[1]))
                : new ItemPair(TypeId.fromDefault(split[0]), 1);

        var itemStack = AuroraAPI.getItemManager().resolveItem(pair.id());
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            AuroraCrafting.logger().severe("Can't validate item id: " + pair.id() + " in recipe: " + recipeId);
            return new ItemPair(TypeId.from(invalidMaterial), 1);
        }
        return pair;
    }

    private String getLocalizedResultName(ItemStack result) {
        String resultName;

        if (result.hasItemMeta()) {
            var meta = result.getItemMeta();
            if (meta.hasDisplayName()) {
                resultName = MiniMessage.miniMessage().serialize(result.displayName());
            } else {
                resultName = "<lang:" + result.getType().translationKey() + ">";
            }
        } else {
            resultName = "<lang:" + result.getType().translationKey() + ">";
        }

        return resultName;
    }
}
