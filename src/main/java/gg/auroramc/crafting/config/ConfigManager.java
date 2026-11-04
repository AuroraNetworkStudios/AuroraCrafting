package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.ConfigFile;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.workbench.custom.MenuOptions;
import gg.auroramc.crafting.config.menu.*;
import gg.auroramc.crafting.config.menu.vanilla.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigManager {
    public static final String VANILLA_RECIPE_VIEW_PATH = "menus/vanilla_recipe_view";

    private final AuroraCrafting plugin;

    private final ConfigFile<Config> config;
    // By language: only the configured language is used, a file stays loaded so a reload can keep it if it's broken
    private final Map<String, ConfigFile<MessageConfig>> messageConfigs = new ConcurrentHashMap<>();
    private volatile String language;
    // A server's own content: created from the bundled example, which isn't used as defaults (that would bring the
    // example merchants and categories back when a server removes them), as before
    private final ConfigFile<RecipeBookConfig> recipeBookConfig;
    private final ConfigFile<MerchantsConfig> merchantsConfig;
    private final ConfigFile<DisabledRecipesConfig> disabledRecipesConfig;

    // menus
    private final ConfigFile<RecipeBookMenuConfig> recipeBookMenuConfig;
    private final ConfigFile<RecipeBookCategoryConfig> recipeBookCategoryConfig;
    private final ConfigFile<MerchantsMenuConfig> merchantsMenuConfig;
    private final ConfigFile<WorkbenchDefaultConfig> workbenchDefaultConfig;
    // Under every workbench file, with the server's workbench_defaults.yml over it
    private final YamlConfiguration bundledWorkbenchDefaults;
    // By workbench id, kept across reloads like the other files
    private final Map<String, ConfigFile<WorkbenchConfig>> workbenchFiles = new ConcurrentHashMap<>();

    // recipe views
    private final ConfigFile<BlastFurnaceRecipeViewConfig> blastFurnaceRecipeViewConfig;
    private final ConfigFile<CampfireRecipeViewConfig> campfireRecipeViewConfig;
    private final ConfigFile<CraftingTableRecipeViewConfig> craftingTableRecipeViewConfig;
    private final ConfigFile<FurnaceRecipeViewConfig> furnaceRecipeViewConfig;
    private final ConfigFile<SmithingRecipeViewConfig> smithingRecipeViewConfig;
    private final ConfigFile<SmokerRecipeViewConfig> smokerRecipeViewConfig;
    private final ConfigFile<StoneCutterRecipeViewConfig> stoneCutterRecipeViewConfig;
    private final ConfigFile<CauldronRecipeViewConfig> cauldronRecipeViewConfig;
    private final ConfigFile<BrewingStandRecipeViewConfig> brewingStandRecipeViewConfig;

    @Getter
    private volatile List<WorkbenchConfig> workbenchConfig;

    @Getter
    private volatile List<CraftingRecipesConfig> customRecipes;
    @Getter
    private volatile List<CraftingRecipesConfig> craftingTableRecipes;

    @Getter
    private volatile List<CookingRecipesConfig.RecipeConfig> blastingRecipes;
    @Getter
    private volatile List<CauldronRecipesConfig.RecipeConfig> cauldronRecipes;
    @Getter
    private volatile List<CookingRecipesConfig.RecipeConfig> smokingRecipes;
    @Getter
    private volatile List<CookingRecipesConfig.RecipeConfig> furnaceRecipes;
    @Getter
    private volatile List<CookingRecipesConfig.RecipeConfig> campfireRecipes;
    @Getter
    private volatile List<SmithingRecipesConfig.RecipeConfig> smithingRecipes;
    @Getter
    private volatile List<StoneCutterRecipesConfig.RecipeConfig> stoneCutterRecipes;
    @Getter
    private volatile List<BrewingRecipesConfig.RecipeConfig> brewingStandRecipes;

    public ConfigManager(AuroraCrafting plugin) {
        this.plugin = plugin;
        config = ConfigFile.builder(plugin, "config.yml", Config.class).migrations(Config.migrations()).build();
        recipeBookConfig = ConfigFile.builder(plugin, "recipe_book.yml", RecipeBookConfig.class).bundled(null).build();
        merchantsConfig = ConfigFile.builder(plugin, "merchants.yml", MerchantsConfig.class).bundled(null).build();
        disabledRecipesConfig = ConfigFile.builder(plugin, "disabled_recipes.yml", DisabledRecipesConfig.class).bundled(null)
                .migrations(DisabledRecipesConfig.migrations()).build();
        recipeBookMenuConfig = ConfigFile.builder(plugin, "menus/recipe_book.yml", RecipeBookMenuConfig.class).build();
        recipeBookCategoryConfig = ConfigFile.builder(plugin, "menus/recipe_book_category.yml", RecipeBookCategoryConfig.class).build();
        merchantsMenuConfig = ConfigFile.builder(plugin, "menus/merchants.yml", MerchantsMenuConfig.class).build();
        workbenchDefaultConfig = ConfigFile.builder(plugin, "menus/workbench_defaults.yml", WorkbenchDefaultConfig.class)
                .migrations(WorkbenchDefaultConfig.migrations()).build();
        bundledWorkbenchDefaults = bundledYaml("menus/workbench_defaults.yml");
        blastFurnaceRecipeViewConfig = view("blast_furnace.yml", BlastFurnaceRecipeViewConfig.class, BlastFurnaceRecipeViewConfig.migrations());
        campfireRecipeViewConfig = view("campfire.yml", CampfireRecipeViewConfig.class, CampfireRecipeViewConfig.migrations());
        craftingTableRecipeViewConfig = view("crafting_table.yml", CraftingTableRecipeViewConfig.class, CraftingTableRecipeViewConfig.migrations());
        furnaceRecipeViewConfig = view("furnace.yml", FurnaceRecipeViewConfig.class, FurnaceRecipeViewConfig.migrations());
        smithingRecipeViewConfig = view("smithing_table.yml", SmithingRecipeViewConfig.class, SmithingRecipeViewConfig.migrations());
        smokerRecipeViewConfig = view("smoker.yml", SmokerRecipeViewConfig.class, SmokerRecipeViewConfig.migrations());
        stoneCutterRecipeViewConfig = view("stone_cutter.yml", StoneCutterRecipeViewConfig.class, StoneCutterRecipeViewConfig.migrations());
        cauldronRecipeViewConfig = view("cauldron.yml", CauldronRecipeViewConfig.class, CauldronRecipeViewConfig.migrations());
        brewingStandRecipeViewConfig = view("brewing_stand.yml", BrewingStandRecipeViewConfig.class, BrewingStandRecipeViewConfig.migrations());
    }

    private <T> ConfigFile<T> view(String name, Class<T> type, List<Consumer<YamlConfiguration>> migrations) {
        return ConfigFile.builder(plugin, VANILLA_RECIPE_VIEW_PATH + "/" + name, type).migrations(migrations).build();
    }

    /**
     * Reads every file again. A file that's broken keeps the config it had before (the defaults on the first load).
     */
    public void reload() {
        config.reload();
        loadMessages();
        saveExample("merchants.yml");
        merchantsConfig.reload();
        saveExample("disabled_recipes.yml");
        disabledRecipesConfig.reload();
        saveExample("recipe_book.yml");
        recipeBookConfig.reload();

        recipeBookMenuConfig.reload();
        recipeBookCategoryConfig.reload();
        merchantsMenuConfig.reload();
        workbenchDefaultConfig.reload();

        blastFurnaceRecipeViewConfig.reload();
        campfireRecipeViewConfig.reload();
        craftingTableRecipeViewConfig.reload();
        furnaceRecipeViewConfig.reload();
        smithingRecipeViewConfig.reload();
        smokerRecipeViewConfig.reload();
        stoneCutterRecipeViewConfig.reload();
        cauldronRecipeViewConfig.reload();
        brewingStandRecipeViewConfig.reload();

        // For workbenches added through the API
        MenuOptions.setDefaultSupplier(getWorkbenchDefaultConfig());

        workbenchConfig = loadWorkbenches();

        customRecipes = loadRecipes("blueprints/aurora", file -> CraftingRecipesConfig.load(file, false));
        craftingTableRecipes = loadRecipes("blueprints/vanilla/crafting_table", file -> CraftingRecipesConfig.load(file, true));

        blastingRecipes = flatten(loadRecipes("blueprints/vanilla/blast_furnace", CookingRecipesConfig::load), CookingRecipesConfig::getRecipes);
        cauldronRecipes = flatten(loadRecipes("blueprints/vanilla/cauldron", CauldronRecipesConfig::load), CauldronRecipesConfig::getRecipes);
        smokingRecipes = flatten(loadRecipes("blueprints/vanilla/smoker", CookingRecipesConfig::load), CookingRecipesConfig::getRecipes);
        furnaceRecipes = flatten(loadRecipes("blueprints/vanilla/furnace", CookingRecipesConfig::load), CookingRecipesConfig::getRecipes);
        campfireRecipes = flatten(loadRecipes("blueprints/vanilla/campfire", CookingRecipesConfig::load), CookingRecipesConfig::getRecipes);
        smithingRecipes = flatten(loadRecipes("blueprints/vanilla/smithing_table", SmithingRecipesConfig::load), SmithingRecipesConfig::getRecipes);
        stoneCutterRecipes = flatten(loadRecipes("blueprints/vanilla/stone_cutter", StoneCutterRecipesConfig::load), StoneCutterRecipesConfig::getRecipes);
        brewingStandRecipes = flatten(loadRecipes("blueprints/vanilla/brewing_stand", BrewingRecipesConfig::load), BrewingRecipesConfig::getRecipes);

        // Its contents were moved into the workbench files by their migrations
        var oldRecipeViewConfigFile = new File(plugin.getDataFolder() + "/menus", "recipe_view.yml");
        if (oldRecipeViewConfigFile.exists()) {
            oldRecipeViewConfigFile.delete();
        }
    }

    private void loadMessages() {
        var language = getConfig().getLanguage();
        var existing = messageConfigs.get(language);
        if (existing != null) {
            existing.reload();
        } else {
            var name = "messages_" + language + ".yml";
            if (!hasResource(name) && !new File(plugin.getDataFolder(), name).exists()) {
                AuroraCrafting.logger().warning("Internal message file for language: " + language + " not found! Creating a new one from english...");
            }
            var file = ConfigFile.builder(plugin, name, MessageConfig.class)
                    .bundled(hasResource(name) ? name : "messages_en.yml")
                    .migrations(MessageConfig.migrations(language))
                    .build();
            file.get();
            messageConfigs.put(language, file);
        }
        this.language = language;
    }

    private boolean hasResource(String name) {
        try (var in = plugin.getResource(name)) {
            return in != null;
        } catch (IOException e) {
            return false;
        }
    }

    private YamlConfiguration bundledYaml(String name) {
        try (var in = plugin.getResource(name)) {
            if (in == null) return new YamlConfiguration();
            return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new YamlConfiguration();
        }
    }

    private void saveExample(String name) {
        if (!new File(plugin.getDataFolder(), name).exists()) {
            plugin.saveResource(name, false);
        }
    }

    /**
     * Each workbench file, with the workbench defaults under it: whatever a workbench doesn't set, including single
     * keys of an item, comes from workbench_defaults.yml.
     */
    @SneakyThrows
    private List<WorkbenchConfig> loadWorkbenches() {
        var folder = Path.of(plugin.getDataFolder().getPath(), "workbenches");
        if (!Files.exists(folder)) {
            if (new File(plugin.getDataFolder() + "/menus", "workbench.yml").exists()) {
                Files.createDirectories(folder);
                Files.move(Path.of(plugin.getDataFolder().getPath(), "menus", "workbench.yml"), folder.resolve("default.yml"));
            } else {
                plugin.saveResource("workbenches/default.yml", false);
            }
        }

        var workbenches = new ArrayList<WorkbenchConfig>();
        var ids = new HashSet<String>();
        try (Stream<Path> paths = Files.walk(folder, 1)) {
            var fileList = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .map(Path::toFile)
                    .toList();

            for (var file : fileList) {
                var id = file.getName().replace(".yml", "");
                var workbench = workbenchFiles.computeIfAbsent(id, k -> ConfigFile.builder(plugin, "workbenches/" + file.getName(), WorkbenchConfig.class)
                        .bundled(null)
                        .migrations(WorkbenchConfig.migrations())
                        .defaults(() -> bundledWorkbenchDefaults)
                        .defaults(() -> workbenchDefaultConfig.getYaml())
                        .build()).reload();
                workbench.setId(id);
                workbenches.add(workbench);
                ids.add(id);
            }
        }
        workbenchFiles.keySet().retainAll(ids);
        return workbenches;
    }

    /**
     * Every recipe file in a folder (and below it), creating the folder with an example if it doesn't exist. A file
     * that can't be loaded is skipped.
     */
    private <T> List<T> loadRecipes(String folder, Function<File, T> loader) {
        Path recipesFolder = Path.of(plugin.getDataFolder().getPath(), folder);

        if (Files.notExists(recipesFolder)) {
            try {
                Files.createDirectories(recipesFolder); // Create folder if it doesn't exist
                plugin.saveResource(folder + "/_example.yml", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        var recipes = new ArrayList<T>();

        try (Stream<Path> paths = Files.walk(recipesFolder, 10)) {
            var fileList = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .map(Path::toFile)
                    .toList();

            for (var file : fileList) {
                try {
                    recipes.add(loader.apply(file));
                } catch (Exception e) {
                    AuroraCrafting.logger().severe("Failed to load recipe file: " + file.getName());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recipes;
    }

    private static <C, R> List<R> flatten(List<C> configs, Function<C, List<R>> recipes) {
        return configs.stream().flatMap(config -> recipes.apply(config).stream()).collect(Collectors.toList());
    }

    /**
     * Where a recipe file is under blueprints/, without the extension, like "vanilla/furnace/my_recipes".
     */
    static String sourcePath(File file) {
        var target = "blueprints" + File.separator;
        var absPath = file.getAbsolutePath();
        var index = absPath.indexOf(target);
        return absPath.substring(index + target.length()).replace(".yml", "").replace(File.separator, "/");
    }

    public AuroraCrafting getPlugin() {
        return plugin;
    }

    public Config getConfig() {
        return config.get();
    }

    public MessageConfig getMessageConfig() {
        return messageConfigs.get(language).get();
    }

    public RecipeBookConfig getRecipeBookConfig() {
        return recipeBookConfig.get();
    }

    public MerchantsConfig getMerchantsConfig() {
        return merchantsConfig.get();
    }

    public DisabledRecipesConfig getDisabledRecipesConfig() {
        return disabledRecipesConfig.get();
    }

    public RecipeBookMenuConfig getRecipeBookMenuConfig() {
        return recipeBookMenuConfig.get();
    }

    public RecipeBookCategoryConfig getRecipeBookCategoryConfig() {
        return recipeBookCategoryConfig.get();
    }

    public MerchantsMenuConfig getMerchantsMenuConfig() {
        return merchantsMenuConfig.get();
    }

    public WorkbenchDefaultConfig getWorkbenchDefaultConfig() {
        return workbenchDefaultConfig.get();
    }

    public BlastFurnaceRecipeViewConfig getBlastFurnaceRecipeViewConfig() {
        return blastFurnaceRecipeViewConfig.get();
    }

    public CampfireRecipeViewConfig getCampfireRecipeViewConfig() {
        return campfireRecipeViewConfig.get();
    }

    public CraftingTableRecipeViewConfig getCraftingTableRecipeViewConfig() {
        return craftingTableRecipeViewConfig.get();
    }

    public FurnaceRecipeViewConfig getFurnaceRecipeViewConfig() {
        return furnaceRecipeViewConfig.get();
    }

    public SmithingRecipeViewConfig getSmithingRecipeViewConfig() {
        return smithingRecipeViewConfig.get();
    }

    public SmokerRecipeViewConfig getSmokerRecipeViewConfig() {
        return smokerRecipeViewConfig.get();
    }

    public StoneCutterRecipeViewConfig getStoneCutterRecipeViewConfig() {
        return stoneCutterRecipeViewConfig.get();
    }

    public CauldronRecipeViewConfig getCauldronRecipeViewConfig() {
        return cauldronRecipeViewConfig.get();
    }

    public BrewingStandRecipeViewConfig getBrewingStandRecipeViewConfig() {
        return brewingStandRecipeViewConfig.get();
    }
}
