package gg.auroramc.crafting.config;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.workbench.custom.MenuOptions;
import gg.auroramc.crafting.config.menu.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class ConfigManager {
    private final AuroraCrafting plugin;

    private Config config;
    private RecipeBookConfig recipeBookConfig;
    private MessageConfig messageConfig;
    private MerchantsConfig merchantsConfig;
    private DisabledRecipesConfig disabledRecipesConfig;

    // menus
    private List<WorkbenchConfig> workbenchConfig;
    private RecipeViewConfig recipeViewConfig;
    private RecipeBookMenuConfig recipeBookMenuConfig;
    private RecipeBookCategoryConfig recipeBookCategoryConfig;
    private MerchantsMenuConfig merchantsMenuConfig;
    private WorkbenchDefaultConfig workbenchDefaultConfig;

    private List<CraftingRecipesConfig> customRecipes;

    private List<CookingRecipesConfig.RecipeConfig> blastingRecipes;
    private List<CookingRecipesConfig.RecipeConfig> smokingRecipes;
    private List<CookingRecipesConfig.RecipeConfig> furnaceRecipes;
    private List<CookingRecipesConfig.RecipeConfig> campfireRecipes;
    private List<SmithingRecipesConfig.RecipeConfig> smithingRecipes;

    public ConfigManager(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        Config.saveDefault(plugin);
        config = new Config(plugin);
        config.load();

        MessageConfig.saveDefault(plugin, config.getLanguage());
        messageConfig = new MessageConfig(plugin, config.getLanguage());
        messageConfig.load();

        MerchantsConfig.saveDefault(plugin);
        merchantsConfig = new MerchantsConfig(plugin);
        merchantsConfig.load();

        DisabledRecipesConfig.saveDefault(plugin);
        disabledRecipesConfig = new DisabledRecipesConfig(plugin);
        disabledRecipesConfig.load();

        RecipeBookConfig.saveDefault(plugin);
        recipeBookConfig = new RecipeBookConfig(plugin);
        recipeBookConfig.load();

        RecipeViewConfig.saveDefault(plugin);
        recipeViewConfig = new RecipeViewConfig(plugin);
        recipeViewConfig.load();

        RecipeBookMenuConfig.saveDefault(plugin);
        recipeBookMenuConfig = new RecipeBookMenuConfig(plugin);
        recipeBookMenuConfig.load();

        RecipeBookCategoryConfig.saveDefault(plugin);
        recipeBookCategoryConfig = new RecipeBookCategoryConfig(plugin);
        recipeBookCategoryConfig.load();

        MerchantsMenuConfig.saveDefault(plugin);
        merchantsMenuConfig = new MerchantsMenuConfig(plugin);
        merchantsMenuConfig.load();

        WorkbenchDefaultConfig.saveDefault(plugin);
        workbenchDefaultConfig = new WorkbenchDefaultConfig(plugin);
        workbenchDefaultConfig.load();

        MenuOptions.setDefaultSupplier(workbenchDefaultConfig);

        workbenchConfig = loadWorkBenches();

        customRecipes = getRecipesConfigs();

        blastingRecipes = getCookingRecipesConfigs("blueprints/vanilla/blast_furnace").stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());

        smokingRecipes = getCookingRecipesConfigs("blueprints/vanilla/smoker").stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());

        furnaceRecipes = getCookingRecipesConfigs("blueprints/vanilla/furnace").stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());

        campfireRecipes = getCookingRecipesConfigs("blueprints/vanilla/campfire").stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());

        smithingRecipes = getSmithingRecipesConfigs().stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private List<WorkbenchConfig> loadWorkBenches() {
        if (!new File(plugin.getDataFolder() + "workbenches").exists()) {
            if (new File(plugin.getDataFolder() + "/menus", "workbench.yml").exists()) {
                Files.createDirectories(Path.of(plugin.getDataFolder().getPath(), "workbenches"));
                Files.move(Path.of(plugin.getDataFolder().getPath(), "menus", "workbench.yml"), Path.of(plugin.getDataFolder().getPath(), "workbenches", "default.yml"));
            } else {
                if (!Files.exists(Path.of(plugin.getDataFolder().getPath(), "workbenches"))) {
                    plugin.saveResource("workbenches/default.yml", false);
                }

            }
        }

        var map = new ArrayList<WorkbenchConfig>();

        try (Stream<Path> paths = Files.walk(Path.of(plugin.getDataFolder().getPath(), "workbenches"), 1)) {
            var fileList = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .map(Path::toFile)
                    .toList();

            for (var file : fileList) {
                map.add(getWorkbenchConfig(file));
            }

            return map;
        }
    }

    private @NotNull WorkbenchConfig getWorkbenchConfig(File file) {
        var workbenchConfig = new WorkbenchConfig(file, file.getName().replace(".yml", ""));

        workbenchConfig.load();

        if (workbenchConfig.getTitle() == null) {
            workbenchConfig.setTitle(workbenchDefaultConfig.getTitle());
        }

        if (workbenchConfig.getRows() == null) {
            workbenchConfig.setRows(workbenchDefaultConfig.getRows());
        }

        if (workbenchConfig.getFiller() == null) {
            workbenchConfig.setFiller(workbenchDefaultConfig.getFiller());
        }

        if (workbenchConfig.getInvalidResultItem() == null) {
            workbenchConfig.setInvalidResultItem(workbenchDefaultConfig.getInvalidResultItem());
        }

        if (workbenchConfig.getEmptyQuickCraftItem() == null) {
            workbenchConfig.setEmptyQuickCraftItem(workbenchDefaultConfig.getEmptyQuickCraftItem());
        }

        if (workbenchConfig.getNoPermissionQuickCraftItem() == null) {
            workbenchConfig.setNoPermissionQuickCraftItem(workbenchDefaultConfig.getNoPermissionQuickCraftItem());
        }

        if (workbenchConfig.getBlueprintCompletedItem() == null) {
            workbenchConfig.setBlueprintCompletedItem(workbenchDefaultConfig.getBlueprintCompletedItem());
        }

        if (workbenchConfig.getBlueprintNotCompletedItem() == null) {
            workbenchConfig.setBlueprintNotCompletedItem(workbenchDefaultConfig.getBlueprintNotCompletedItem());
        }

        if (workbenchConfig.getNextRecipeItem() == null) {
            workbenchConfig.setNextRecipeItem(workbenchDefaultConfig.getNextRecipeItem());
        }

        if (workbenchConfig.getPreviousRecipeItem() == null) {
            workbenchConfig.setPreviousRecipeItem(workbenchDefaultConfig.getPreviousRecipeItem());
        }

        return workbenchConfig;
    }

    private List<CraftingRecipesConfig> getRecipesConfigs() {
        Path recipesFolder = Path.of(plugin.getDataFolder().getPath(), "blueprints/aurora");

        if (Files.notExists(recipesFolder)) {
            try {
                Files.createDirectories(recipesFolder); // Create folder if it doesn't exist
                plugin.saveResource("blueprints/aurora/_example.yml", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        var recipes = new ArrayList<CraftingRecipesConfig>();

        try (Stream<Path> paths = Files.walk(recipesFolder, 10)) {
            var fileList = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .map(Path::toFile)
                    .toList();

            for (var file : fileList) {
                try {
                    CraftingRecipesConfig recipesConfig = new CraftingRecipesConfig(file);
                    recipesConfig.load();
                    recipes.add(recipesConfig);
                } catch (Exception e) {
                    AuroraCrafting.logger().severe("Failed to load recipe file: " + file.getName());
                    e.printStackTrace();
                }
            }

            return recipes;
        } catch (IOException e) {
            e.printStackTrace();
            return recipes;
        }
    }

    @SneakyThrows
    private List<CookingRecipesConfig> getCookingRecipesConfigs(String folder) {
        Path recipesFolder = Path.of(plugin.getDataFolder().getPath(), folder);

        if (Files.notExists(recipesFolder)) {
            Files.createDirectories(recipesFolder); // Create folder if it doesn't exist
            plugin.saveResource(folder + "/_example.yml", false);
        }

        try (Stream<Path> paths = Files.walk(recipesFolder, 10)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .map(Path::toFile)
                    .map((file) -> {
                        CookingRecipesConfig recipesConfig = new CookingRecipesConfig(file);
                        recipesConfig.load();
                        return recipesConfig;
                    })
                    .collect(Collectors.toList());
        }
    }

    @SneakyThrows
    private List<SmithingRecipesConfig> getSmithingRecipesConfigs() {
        Path recipesFolder = Path.of(plugin.getDataFolder().getPath(), "blueprints/vanilla/smithing_table");

        if (Files.notExists(recipesFolder)) {
            Files.createDirectories(recipesFolder); // Create folder if it doesn't exist
            plugin.saveResource("blueprints/vanilla/smithing_table/_example.yml", false);
        }

        try (Stream<Path> paths = Files.walk(recipesFolder, 10)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .map(Path::toFile)
                    .map((file) -> {
                        SmithingRecipesConfig recipesConfig = new SmithingRecipesConfig(file);
                        recipesConfig.load();
                        return recipesConfig;
                    })
                    .collect(Collectors.toList());
        }
    }
}
