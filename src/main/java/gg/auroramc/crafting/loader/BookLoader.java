package gg.auroramc.crafting.loader;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.config.RecipeBookConfig;
import gg.auroramc.crafting.parser.BookParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookLoader {
    public static void loadBookCategories(AuroraCrafting plugin) {
        var config = plugin.getConfigManager().getRecipeBookConfig();

        for (var categoryConfig : config.getCategories()) {
            try {
                plugin.getBook().addSubCategory(BookParser.from(plugin.getBook(), categoryConfig).parse());
            } catch (Exception e) {
                AuroraCrafting.logger().severe("Failed to load book category " + categoryConfig.getId() + ": " + e.getMessage());
            }
        }
    }

    public static void fillBookCategories(AuroraCrafting plugin) {
        var configIndex = buildCategoryConfigIndex(plugin.getConfigManager().getRecipeBookConfig().getCategories(), new HashMap<>());
        var blueprintRegistry = plugin.getBlueprintRegistry();

        for (var blueprint : blueprintRegistry.getBlueprints()) {
            if (blueprint.getCategory() != null) continue;
            for (var category : plugin.getBook().getRegistry()) {
                var categoryConfig = configIndex.get(category.getId());
                if (categoryConfig == null) continue;

                if (categoryConfig.getRecipes().contains(blueprint.getId())) {
                    try {
                        category.addBlueprint(blueprint);
                        blueprint.category(category);
                    } catch (Exception e) {
                        AuroraCrafting.logger().severe("Failed to add blueprint " + blueprint.getId() + " to book category " + category.getId() + ": " + e.getMessage());
                    }
                } else if (blueprint.getSource() != null && categoryConfig.getFiles().stream().anyMatch(blueprint.getSource()::endsWith)) {
                    try {
                        category.addBlueprint(blueprint);
                        blueprint.category(category);
                    } catch (Exception e) {
                        AuroraCrafting.logger().severe("Failed to add blueprint " + blueprint.getId() + " to book category " + category.getId() + ": " + e.getMessage());
                    }
                }
            }

        }
    }

    private static Map<String, RecipeBookConfig.RecipeCategory> buildCategoryConfigIndex(List<RecipeBookConfig.RecipeCategory> categories, Map<String, RecipeBookConfig.RecipeCategory> index) {
        for (var category : categories) {
            index.put(category.getId(), category);
            if (!category.getCategories().isEmpty()) {
                buildCategoryConfigIndex(category.getCategories(), index);
            }
        }
        return index;
    }
}
