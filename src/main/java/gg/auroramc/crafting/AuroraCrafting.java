package gg.auroramc.crafting;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.AuroraLogger;
import gg.auroramc.aurora.api.command.CommandDispatcher;
import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.util.Version;
import gg.auroramc.crafting.api.AuroraCraftingPlugin;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintRegistry;
import gg.auroramc.crafting.api.book.Book;
import gg.auroramc.crafting.api.book.BookCategory;
import gg.auroramc.crafting.api.event.BlueprintCraftEvent;
import gg.auroramc.crafting.api.event.RegistryLoadEvent;
import gg.auroramc.crafting.api.event.RegistryLoadedEvent;
import gg.auroramc.crafting.api.workbench.WorkbenchRegistry;
import gg.auroramc.crafting.command.CommandManager;
import gg.auroramc.crafting.config.ConfigManager;
import gg.auroramc.crafting.hooks.HookManager;
import gg.auroramc.crafting.listener.*;
import gg.auroramc.crafting.loader.BlueprintLoader;
import gg.auroramc.crafting.loader.BookLoader;
import gg.auroramc.crafting.loader.WorkbenchLoader;
import gg.auroramc.crafting.menu.BlueprintMenu;
import gg.auroramc.crafting.menu.BookCategoryListMenu;
import gg.auroramc.crafting.menu.CraftMenu;
import gg.auroramc.crafting.menu.MenuListener;
import gg.auroramc.crafting.util.RecipeFolderMigrator;
import gg.auroramc.crafting.util.RecipeUtil;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;

public class AuroraCrafting extends AuroraCraftingPlugin {
    private static final AtomicBoolean loading = new AtomicBoolean(true);

    public static boolean isLoading() {
        return loading.get();
    }

    @Getter
    private ConfigManager configManager;

    private CommandManager commandManager;

    @Getter
    private static AuroraCrafting instance;
    private static AuroraLogger l;

    public static AuroraLogger logger() {
        return l;
    }

    @Override
    public void onLoad() {
        RecipeFolderMigrator.tryMigrate(this);

        instance = this;
        AuroraCraftingPlugin.instance = this;

        configManager = new ConfigManager(this);
        l = AuroraAPI.createLogger("AuroraCrafting", () -> configManager.getConfig().getDebug());
        configManager.reload();

        HookManager.loadHooks(this);
    }

    @Override
    public void onEnable() {
        commandManager = new CommandManager(this);
        commandManager.reload();

        book = new Book(BookCategory.MenuOptions.builder().title(configManager.getRecipeBookMenuConfig().getTitle()).build());
        workbenchRegistry = new WorkbenchRegistry();

        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RecipeDiscoverListener(this), this);

        if (configManager.getConfig().getCraftHandlers().getSmithingTable()) {
            Bukkit.getPluginManager().registerEvents(new SmithingListener(this), this);
        }
        if (configManager.getConfig().getCraftHandlers().getCraftingTable()) {
            Bukkit.getPluginManager().registerEvents(new CraftingListener(this), this);
        }
        if (configManager.getConfig().getCraftHandlers().getCauldron()) {
            Bukkit.getPluginManager().registerEvents(new CauldronListener(this), this);
        }

        if (Version.isAtLeastVersion(21) && configManager.getConfig().getCraftHandlers().getCraftingTable()) {
            Bukkit.getPluginManager().registerEvents(new AutoCrafterListener(this), this);
        }
        if (configManager.getConfig().getOpenInsteadOfCraftingTable() || configManager.getConfig().getOpenShiftClickCraftingTable()) {
            Bukkit.getPluginManager().registerEvents(new CraftingTableInteractListener(this), this);
        }

        // Have to delay the initialization because mythic doesn't initialize enchants and some other stuff before
        Bukkit.getGlobalRegionScheduler().runDelayed(this, (t) -> {
            initState();
            loading.set(false);
        }, 40);

        HookManager.enableHooks(this);

        CommandDispatcher.registerActionHandler("recipe", (player, input) -> {
            var split = input.split("---");
            var recipeId = split[0].trim();
            var blueprint = blueprintRegistry.getBlueprint(recipeId);
            if (blueprint == null) return;
            if (split.length > 1) {
                BlueprintMenu.blueprintMenu(this, player, blueprint, () -> CommandDispatcher.dispatch(player, split[1].trim())).open();
            } else {
                BlueprintMenu.blueprintMenu(this, player, blueprint, null).open();
            }
        });

        CommandDispatcher.registerActionHandler("recipes", (player, input) -> {
            BookCategoryListMenu.bookCategoryListMenu(this, player, book).open();
        });

        CommandDispatcher.registerActionHandler("workbench", (player, input) -> {
            var workbenchId = input.trim();
            var workbench = workbenchRegistry.getWorkbench(workbenchId);
            if (workbench == null) return;
            if (player.hasPermission("aurora.crafting.use." + workbenchId)) {
                CraftMenu.craftMenu(this, player, workbench).open();
            } else {
                Chat.sendMessage(player, configManager.getMessageConfig().getNoPermission());
            }
        });

        new Metrics(this, 24580);
    }

    @Override
    public void onDisable() {
        commandManager.unregisterCommands();
    }

    public void reload() {
        loading.set(true);

        if (Version.isAtLeastVersion(21) && !Version.isFolia()) {
            for (var player : Bukkit.getOnlinePlayers()) {
                if (player.getOpenInventory().getTopInventory().getHolder() instanceof AuroraMenu menu) {
                    var id = menu.getId();
                    if (id != null && id.getNamespace().equals("auroracrafting")) {
                        player.closeInventory();
                    }
                } else if (player.getOpenInventory().getTopInventory().getHolder() instanceof CraftMenu menu) {
                    player.closeInventory();
                }
            }
        }

        var start = System.currentTimeMillis();
        configManager.reload();
        commandManager.reload();
        initState();
        loading.set(false);
        var end = System.currentTimeMillis();
        l.info("Reloaded configs in " + (end - start) + "ms");
    }

    private void initState() {
        // Disable vanilla recipes based on config
        RecipeUtil.removeVanillaRecipes(configManager.getDisabledRecipesConfig().getRecipes());
        // Initialize fields
        book.unfreezeAndClear();
        workbenchRegistry.unfreezeAndClear();
        book.setMenuOptions(BookCategory.MenuOptions.builder().title(configManager.getRecipeBookMenuConfig().getTitle()).build());
        // Load everything from configs
        BookLoader.loadBookCategories(this);
        WorkbenchLoader.loadWorkbenches(this);
        BlueprintLoader.loadBlueprints(this);
        // Fire RegistryLoadEvent for API users to register their own workbenches/blueprints
        Bukkit.getPluginManager().callEvent(new RegistryLoadEvent());
        // Freeze the registry to prevent further modifications
        workbenchRegistry.freeze();
        // Create blueprint registry (immutable)
        blueprintRegistry = BlueprintRegistry.createFrom(workbenchRegistry);
        // Fill book categories with blueprints from config
        BookLoader.fillBookCategories(this);
        // Freeze the book to prevent further modifications
        book.freeze();
        // Fire RegistryLoadedEvent to notify API users that the registry is now frozen
        Bukkit.getPluginManager().callEvent(new RegistryLoadedEvent());
    }

    public void callCraftEvent(Player player, ItemStack item, int amount, Blueprint blueprint) {
        Bukkit.getPluginManager().callEvent(new BlueprintCraftEvent(player, item, blueprint, amount));
    }
}
