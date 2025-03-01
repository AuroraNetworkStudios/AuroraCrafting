package gg.auroramc.crafting.config;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.workbench.custom.MenuOptions;
import gg.auroramc.crafting.config.menu.*;
import gg.auroramc.crafting.config.menu.vanilla.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class ConfigManager {

    public static final String VANILLA_RECIPE_VIEW_PATH = "menus/vanilla_recipe_view";

    private final AuroraCrafting plugin;

    private Config config;
    private RecipeBookConfig recipeBookConfig;
    private MessageConfig messageConfig;
    private MerchantsConfig merchantsConfig;
    private DisabledRecipesConfig disabledRecipesConfig;

    // menus
    private List<WorkbenchConfig> workbenchConfig;
    private RecipeBookMenuConfig recipeBookMenuConfig;
    private RecipeBookCategoryConfig recipeBookCategoryConfig;
    private MerchantsMenuConfig merchantsMenuConfig;
    private WorkbenchDefaultConfig workbenchDefaultConfig;


    // recipe views
    private BlastFurnaceRecipeViewConfig blastFurnaceRecipeViewConfig;
    private CampfireRecipeViewConfig campfireRecipeViewConfig;
    private CraftingTableRecipeViewConfig craftingTableRecipeViewConfig;
    private FurnaceRecipeViewConfig furnaceRecipeViewConfig;
    private SmithingRecipeViewConfig smithingRecipeViewConfig;
    private SmokerRecipeViewConfig smokerRecipeViewConfig;
    private StoneCutterRecipeViewConfig stoneCutterRecipeViewConfig;
    private CauldronRecipeViewConfig cauldronRecipeViewConfig;

    private List<CraftingRecipesConfig> customRecipes;
    private List<CraftingRecipesConfig> craftingTableRecipes;

    private List<CookingRecipesConfig.RecipeConfig> blastingRecipes;
    private List<CauldronRecipesConfig.RecipeConfig> cauldronRecipes;
    private List<CookingRecipesConfig.RecipeConfig> smokingRecipes;
    private List<CookingRecipesConfig.RecipeConfig> furnaceRecipes;
    private List<CookingRecipesConfig.RecipeConfig> campfireRecipes;
    private List<SmithingRecipesConfig.RecipeConfig> smithingRecipes;
    private List<StoneCutterRecipesConfig.RecipeConfig> stoneCutterRecipes;

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


        // recipe views
        BlastFurnaceRecipeViewConfig.saveDefault(plugin);
        blastFurnaceRecipeViewConfig = new BlastFurnaceRecipeViewConfig(plugin);
        blastFurnaceRecipeViewConfig.load();

        CampfireRecipeViewConfig.saveDefault(plugin);
        campfireRecipeViewConfig = new CampfireRecipeViewConfig(plugin);
        campfireRecipeViewConfig.load();

        CraftingTableRecipeViewConfig.saveDefault(plugin);
        craftingTableRecipeViewConfig = new CraftingTableRecipeViewConfig(plugin);
        craftingTableRecipeViewConfig.load();

        FurnaceRecipeViewConfig.saveDefault(plugin);
        furnaceRecipeViewConfig = new FurnaceRecipeViewConfig(plugin);
        furnaceRecipeViewConfig.load();

        SmithingRecipeViewConfig.saveDefault(plugin);
        smithingRecipeViewConfig = new SmithingRecipeViewConfig(plugin);
        smithingRecipeViewConfig.load();

        SmokerRecipeViewConfig.saveDefault(plugin);
        smokerRecipeViewConfig = new SmokerRecipeViewConfig(plugin);
        smokerRecipeViewConfig.load();

        StoneCutterRecipeViewConfig.saveDefault(plugin);
        stoneCutterRecipeViewConfig = new StoneCutterRecipeViewConfig(plugin);
        stoneCutterRecipeViewConfig.load();

        CauldronRecipeViewConfig.saveDefault(plugin);
        cauldronRecipeViewConfig = new CauldronRecipeViewConfig(plugin);
        cauldronRecipeViewConfig.load();

        MenuOptions.setDefaultSupplier(workbenchDefaultConfig);

        workbenchConfig = loadWorkBenches();

        customRecipes = getCraftingRecipesConfigs("blueprints/aurora", false);
        craftingTableRecipes = getCraftingRecipesConfigs("blueprints/vanilla/crafting_table", true);

        blastingRecipes = getCookingRecipesConfigs("blueprints/vanilla/blast_furnace").stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());

        cauldronRecipes = getCauldronRecipesConfigs("blueprints/vanilla/cauldron").stream()
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

        stoneCutterRecipes = getStoneCutterRecipesConfigs().stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());

        var oldRecipeViewConfigFile = new File(plugin.getDataFolder() + "/menus", "recipe_view.yml");
        if (oldRecipeViewConfigFile.exists()) {
            oldRecipeViewConfigFile.delete();
        }
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
        } else {
            workbenchConfig.setFiller(workbenchDefaultConfig.getFiller().merge(workbenchConfig.getFiller()));
        }

        if (workbenchConfig.getInvalidResultItem() == null) {
            workbenchConfig.setInvalidResultItem(workbenchDefaultConfig.getInvalidResultItem());
        } else {
            workbenchConfig.setInvalidResultItem(workbenchDefaultConfig.getInvalidResultItem().merge(workbenchConfig.getInvalidResultItem()));
        }

        if (workbenchConfig.getEmptyQuickCraftItem() == null) {
            workbenchConfig.setEmptyQuickCraftItem(workbenchDefaultConfig.getEmptyQuickCraftItem());
        } else {
            workbenchConfig.setEmptyQuickCraftItem(workbenchDefaultConfig.getEmptyQuickCraftItem().merge(workbenchConfig.getEmptyQuickCraftItem()));
        }

        if (workbenchConfig.getNoPermissionQuickCraftItem() == null) {
            workbenchConfig.setNoPermissionQuickCraftItem(workbenchDefaultConfig.getNoPermissionQuickCraftItem());
        } else {
            workbenchConfig.setNoPermissionQuickCraftItem(workbenchDefaultConfig.getNoPermissionQuickCraftItem().merge(workbenchConfig.getNoPermissionQuickCraftItem()));
        }

        if (workbenchConfig.getBlueprintCompletedItem() == null) {
            workbenchConfig.setBlueprintCompletedItem(workbenchDefaultConfig.getBlueprintCompletedItem());
        } else {
            workbenchConfig.setBlueprintCompletedItem(workbenchDefaultConfig.getBlueprintCompletedItem().merge(workbenchConfig.getBlueprintCompletedItem()));
        }

        if (workbenchConfig.getBlueprintNotCompletedItem() == null) {
            workbenchConfig.setBlueprintNotCompletedItem(workbenchDefaultConfig.getBlueprintNotCompletedItem());
        } else {
            workbenchConfig.setBlueprintNotCompletedItem(workbenchDefaultConfig.getBlueprintNotCompletedItem().merge(workbenchConfig.getBlueprintNotCompletedItem()));
        }

        if (workbenchConfig.getNextRecipeItem() == null) {
            workbenchConfig.setNextRecipeItem(workbenchDefaultConfig.getNextRecipeItem());
        } else {
            workbenchConfig.setNextRecipeItem(workbenchDefaultConfig.getNextRecipeItem().merge(workbenchConfig.getNextRecipeItem()));
        }

        if (workbenchConfig.getPreviousRecipeItem() == null) {
            workbenchConfig.setPreviousRecipeItem(workbenchDefaultConfig.getPreviousRecipeItem());
        } else {
            workbenchConfig.setPreviousRecipeItem(workbenchDefaultConfig.getPreviousRecipeItem().merge(workbenchConfig.getPreviousRecipeItem()));
        }

        if (workbenchConfig.getBackItem() == null) {
            workbenchConfig.setBackItem(workbenchDefaultConfig.getBackItem());
        } else {
            workbenchConfig.setBackItem(workbenchDefaultConfig.getBackItem().merge(workbenchConfig.getBackItem()));
        }

        return workbenchConfig;
    }

    private List<CraftingRecipesConfig> getCraftingRecipesConfigs(String basePath, boolean vanilla) {
        Path recipesFolder = Path.of(plugin.getDataFolder().getPath(), basePath);

        if (Files.notExists(recipesFolder)) {
            try {
                Files.createDirectories(recipesFolder); // Create folder if it doesn't exist
                plugin.saveResource(basePath + "/_example.yml", false);
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
                    CraftingRecipesConfig recipesConfig = new CraftingRecipesConfig(file, vanilla);
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
    private List<CauldronRecipesConfig> getCauldronRecipesConfigs(String folder) {
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
                        CauldronRecipesConfig recipesConfig = new CauldronRecipesConfig(file);
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

    @SneakyThrows
    private List<StoneCutterRecipesConfig> getStoneCutterRecipesConfigs() {
        Path recipesFolder = Path.of(plugin.getDataFolder().getPath(), "blueprints/vanilla/stone_cutter");

        if (Files.notExists(recipesFolder)) {
            Files.createDirectories(recipesFolder); // Create folder if it doesn't exist
            plugin.saveResource("blueprints/vanilla/stone_cutter/_example.yml", false);
        }

        try (Stream<Path> paths = Files.walk(recipesFolder, 10)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .map(Path::toFile)
                    .map((file) -> {
                        StoneCutterRecipesConfig recipesConfig = new StoneCutterRecipesConfig(file);
                        recipesConfig.load();
                        return recipesConfig;
                    })
                    .collect(Collectors.toList());
        }
    }
}
