package gg.auroramc.crafting.loader;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.book.BookCategory;
import gg.auroramc.crafting.config.RecipeBookConfig;
import gg.auroramc.crafting.parser.BookParser;

import java.util.*;

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

        // Print the tree of book categories
        AuroraCrafting.logger().info("-------------------------------------------");
        AuroraCrafting.logger().info("Recipe book structure:");
        AuroraCrafting.logger().info("  " + plugin.getBook().getId());
        printTree(plugin.getBook().getCategories(), 2);
        AuroraCrafting.logger().info("-------------------------------------------");
    }

    private static void printTree(Collection<BookCategory> categories, int depth) {
        for (var category : categories) {
            var indent = " ".repeat(depth * 2);
            AuroraCrafting.logger().info(indent + category.getId());
            printTree(category.getCategories(), depth + 1);
        }

    }

    public static void fillBookCategories(AuroraCrafting plugin) {
        var configIndex = buildCategoryConfigIndex(plugin.getConfigManager().getRecipeBookConfig().getCategories(), new HashMap<>());
        var blueprintRegistry = plugin.getBlueprintRegistry();

        for (var category : plugin.getBook().getRegistry()) {
            var categoryConfig = configIndex.get(category.getId());
            if (categoryConfig == null) continue;

            // Preserve ordering from config
            var addedBlueprints = new HashSet<String>();

            // Add blueprints by recipe ID order
            for (String recipeId : categoryConfig.getRecipes()) {
                var blueprint = blueprintRegistry.getBlueprint(recipeId);
                if (blueprint != null) {
                    try {
                        category.addBlueprint(blueprint);
                        blueprint.category(category);
                        addedBlueprints.add(blueprint.getId());
                    } catch (Exception e) {
                        AuroraCrafting.logger().severe("Failed to add blueprint " + blueprint.getId() + " to book category " + category.getId() + ": " + e.getMessage());
                    }
                }
            }

            // Add blueprints by file order
            for (String file : categoryConfig.getFiles()) {
                for (var blueprint : blueprintRegistry.getBlueprints()) {
                    if (!addedBlueprints.contains(blueprint.getId()) && blueprint.getSource() != null && blueprint.getSource().endsWith(file)) {
                        try {
                            category.addBlueprint(blueprint);
                            blueprint.category(category);
                            addedBlueprints.add(blueprint.getId());
                        } catch (Exception e) {
                            AuroraCrafting.logger().severe("Failed to add blueprint " + blueprint.getId() + " to book category " + category.getId() + ": " + e.getMessage());
                        }
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
