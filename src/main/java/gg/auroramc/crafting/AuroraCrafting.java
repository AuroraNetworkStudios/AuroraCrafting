package gg.auroramc.crafting;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.AuroraLogger;
import gg.auroramc.aurora.api.command.CommandDispatcher;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.crafting.api.AuroraCraftingPlugin;
import gg.auroramc.crafting.api.RecipeManager;
import gg.auroramc.crafting.api.blueprint.BlueprintRegistry;
import gg.auroramc.crafting.api.book.Book;
import gg.auroramc.crafting.api.event.PlayerCraftItemEvent;
import gg.auroramc.crafting.api.event.RegistryLoadEvent;
import gg.auroramc.crafting.api.event.RegistryLoadedEvent;
import gg.auroramc.crafting.api.workbench.WorkbenchRegistry;
import gg.auroramc.crafting.command.CommandManager;
import gg.auroramc.crafting.config.ConfigManager;
import gg.auroramc.crafting.hooks.HookManager;
import gg.auroramc.crafting.listener.ConnectionListener;
import gg.auroramc.crafting.listener.CraftingTableInteractListener;
import gg.auroramc.crafting.loader.BlueprintLoader;
import gg.auroramc.crafting.loader.BookLoader;
import gg.auroramc.crafting.loader.WorkbenchLoader;
import gg.auroramc.crafting.menu.CraftMenu;
import gg.auroramc.crafting.menu.MenuListener;
import gg.auroramc.crafting.menu.RecipeBookMenu;
import gg.auroramc.crafting.menu.RecipeMenu;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuroraCrafting extends AuroraCraftingPlugin {
    @Getter
    private ConfigManager configManager;

    private CommandManager commandManager;
    @Getter
    private RecipeManager recipeManager;

    @Getter
    private static AuroraCrafting instance;
    private static AuroraLogger l;

    public static AuroraLogger logger() {
        return l;
    }

    @Override
    public void onLoad() {
        instance = this;

        AuroraCraftingPlugin.instance = this;
        book = new Book();
        workbenchRegistry = new WorkbenchRegistry();

        configManager = new ConfigManager(this);
        configManager.reload();
        l = AuroraAPI.createLogger("AuroraCrafting", () -> configManager.getConfig().getDebug());

        HookManager.loadHooks(this);
    }

    @Override
    public void onEnable() {
        recipeManager = new RecipeManager(this);
        commandManager = new CommandManager(this);
        commandManager.reload();
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
        if (configManager.getConfig().getOpenInsteadOfCraftingTable() || configManager.getConfig().getOpenShiftClickCraftingTable()) {
            Bukkit.getPluginManager().registerEvents(new CraftingTableInteractListener(this), this);
        }

        Bukkit.getGlobalRegionScheduler().runDelayed(this, (t) -> initState(), 2);

        HookManager.enableHooks(this);

        CommandDispatcher.registerActionHandler("recipe", (player, input) -> {
            var split = input.split("---");
            var recipeId = split[0].trim();
            var recipe = recipeManager.getRecipeById(recipeId);
            if (recipe == null) return;
            if (split.length > 1) {
                RecipeMenu.recipeMenu(this, player, recipe, () -> {
                    CommandDispatcher.dispatch(player, split[1].trim());
                }).open();
            } else {
                RecipeMenu.recipeMenu(this, player, recipe, null).open();
            }
        });

        CommandDispatcher.registerActionHandler("recipes", (player, input) -> {
            RecipeBookMenu.recipeBookMenu(this, player).open();
        });

        CommandDispatcher.registerActionHandler("workbench", (player, input) -> {
            var workbenchId = input.trim();
            var workbench = workbenchRegistry.getWorkbench(workbenchId);
            if(workbench == null) return;
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
        configManager.reload();
        commandManager.reload();
        initState();
    }

    private void initState() {
        // Initialize fields
        book.unfreezeAndClear();
        workbenchRegistry.unfreezeAndClear();
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

    public void callCraftEvent(Player player, ItemStack item, int amount) {
        Bukkit.getPluginManager().callEvent(new PlayerCraftItemEvent(player, item, null, amount));
    }
}
