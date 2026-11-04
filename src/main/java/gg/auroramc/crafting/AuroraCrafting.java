package gg.auroramc.crafting;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.AuroraLogger;
import gg.auroramc.aurora.api.command.CommandDispatcher;
import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.aurora.api.plugin.Hooks;
import gg.auroramc.crafting.api.AuroraCraftingPlugin;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintRegistry;
import gg.auroramc.crafting.api.book.Book;
import gg.auroramc.crafting.api.book.BookCategory;
import gg.auroramc.crafting.api.event.BlueprintCraftEvent;
import gg.auroramc.crafting.api.event.ItemsLoadedEvent;
import gg.auroramc.crafting.api.event.RegistryLoadEvent;
import gg.auroramc.crafting.api.event.RegistryLoadedEvent;
import gg.auroramc.crafting.api.item.ItemLoader;
import gg.auroramc.crafting.api.workbench.WorkbenchRegistry;
import gg.auroramc.crafting.command.CommandManager;
import gg.auroramc.crafting.config.ConfigManager;
import gg.auroramc.crafting.hooks.advancedenchantments.AEHook;
import gg.auroramc.crafting.hooks.auroraquests.AuroraQuestsHook;
import gg.auroramc.crafting.hooks.betonquests.BetonQuestHook;
import gg.auroramc.crafting.hooks.hdb.HdbHook;
import gg.auroramc.crafting.hooks.itemsadder.ItemsAdderHook;
import gg.auroramc.crafting.hooks.jobsreborn.JobsRebornHook;
import gg.auroramc.crafting.hooks.mythicmobs.MythicHook;
import gg.auroramc.crafting.hooks.quests.QuestsHook;
import gg.auroramc.crafting.hooks.quests2.QuestsLmBishopHook;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class AuroraCrafting extends AuroraCraftingPlugin implements Listener {
    private static final AtomicBoolean loading = new AtomicBoolean(true);

    public static boolean isLoading() {
        return loading.get();
    }

    private boolean firstInit = true;

    @Getter
    private ItemLoader itemLoader;

    @Getter
    private ConfigManager configManager;

    private CommandManager commandManager;
    // Created in onLoad, after checking that Aurora is new enough (an older one doesn't have Hooks)
    @Getter
    private Hooks<AuroraCrafting> hooks;

    @Getter
    private static AuroraCrafting instance;
    private static AuroraLogger l;

    public static AuroraLogger logger() {
        return l;
    }

    // Set when Aurora or the server is too old for this version, nothing else is done then
    private boolean incompatible = false;

    @Override
    public void onLoad() {
        if (!auroraIsCompatible() || !serverIsCompatible()) {
            incompatible = true;
            return;
        }
        instance = this;
        AuroraCraftingPlugin.instance = this;

        RecipeFolderMigrator.tryMigrate(this);

        configManager = new ConfigManager(this);
        l = AuroraAPI.createLogger("AuroraCrafting", () -> configManager.getConfig().getDebug());
        configManager.reload();

        hooks = createHooks();
        hooks.load();
    }

    @Override
    public void onEnable() {
        if (incompatible) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        itemLoader = new ItemLoader();

        Bukkit.getPluginManager().registerEvents(this, this);
        commandManager = new CommandManager(this);
        commandManager.reload();

        book = new Book(BookCategory.MenuOptions.builder().title(configManager.getRecipeBookMenuConfig().getTitle()).build());
        workbenchRegistry = new WorkbenchRegistry();

        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RecipeDiscoverListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlueprintListener(), this);
        Bukkit.getPluginManager().registerEvents(new BrewingListener(this), this);

        if (configManager.getConfig().getCraftHandlers().getSmithingTable()) {
            Bukkit.getPluginManager().registerEvents(new SmithingListener(this), this);
        }
        if (configManager.getConfig().getCraftHandlers().getCraftingTable()) {
            Bukkit.getPluginManager().registerEvents(new CraftingListener(this), this);
        }
        if (configManager.getConfig().getCraftHandlers().getCauldron()) {
            Bukkit.getPluginManager().registerEvents(new CauldronListener(this), this);
        }

        if (configManager.getConfig().getCraftHandlers().getCraftingTable()) {
            Bukkit.getPluginManager().registerEvents(new AutoCrafterListener(this), this);
        }
        if (configManager.getConfig().getOpenInsteadOfCraftingTable() || configManager.getConfig().getOpenShiftClickCraftingTable()) {
            Bukkit.getPluginManager().registerEvents(new CraftingTableInteractListener(this), this);
        }

        hooks.enable();

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
        if (commandManager != null) commandManager.unregisterCommands();
    }

    // Built for Aurora 3: with an older one, say so clearly instead of failing somewhere deep inside. Only Bukkit is
    // used here, an older Aurora doesn't have what this version needs.
    private boolean auroraIsCompatible() {
        var aurora = Bukkit.getPluginManager().getPlugin("Aurora");
        var version = aurora == null ? "none" : aurora.getPluginMeta().getVersion();
        int major;
        try {
            major = Integer.parseInt(version.split("[.-]")[0]);
        } catch (NumberFormatException e) {
            major = 0;
        }
        if (major >= 3) return true;
        getLogger().severe(getName() + " " + getPluginMeta().getVersion() + " needs Aurora 3.0 or newer, but Aurora " + version + " is installed. Update Aurora.");
        return false;
    }

    // Vanilla recipes with custom ingredients use RecipeChoice.predicateChoice, which some server builds don't have yet
    // (Folia 26.2 up to at least build 7)
    private boolean serverIsCompatible() {
        try {
            RecipeChoice.class.getMethod("predicateChoice", Predicate.class, ItemStack.class);
            return true;
        } catch (NoSuchMethodException e) {
            getLogger().severe(getName() + " needs a server with RecipeChoice.predicateChoice, which " + Bukkit.getName() + " "
                    + Bukkit.getVersion() + " doesn't have. Update the server (Paper 26.2 build 134 or newer, or Folia 26.3).");
            return false;
        }
    }

    private Hooks<AuroraCrafting> createHooks() {
        var hooks = new Hooks<>(this);
        addHook(hooks, AuroraQuestsHook.class, "AuroraQuests");
        // Two different plugins are called Quests
        var quests = Bukkit.getPluginManager().getPlugin("Quests");
        var questsClass = quests == null ? "" : quests.getClass().getName();
        if (questsClass.startsWith("me.pikamug.")) {
            addHook(hooks, QuestsHook.class, "Quests");
        } else if (questsClass.startsWith("com.leonardobishop.")) {
            addHook(hooks, QuestsLmBishopHook.class, "Quests");
        }
        addHook(hooks, BetonQuestHook.class, "BetonQuest");
        addHook(hooks, JobsRebornHook.class, "Jobs");
        addHook(hooks, ItemsAdderHook.class, "ItemsAdder");
        addHook(hooks, MythicHook.class, "MythicMobs");
        addHook(hooks, HdbHook.class, "HeadDatabase");
        addHook(hooks, AEHook.class, "AdvancedEnchantments");
        return hooks;
    }

    // Unless it's turned off in config.yml's hooks section
    private void addHook(Hooks<AuroraCrafting> hooks, Class<? extends Hook<AuroraCrafting>> type, String plugin) {
        if (configManager.getConfig().getHooks().getOrDefault(plugin, true)) {
            hooks.add(type, plugin);
        }
    }

    public void reload() {
        if (firstInit) {
            logger().warning("Reload was prevented since the plugin didn't even had a chance to load yet.");
            return;
        }
        loading.set(true);

        // Menus showing the old recipes are closed, on Folia on each player's own thread
        for (var player : Bukkit.getOnlinePlayers()) {
            if (Bukkit.isOwnedByCurrentRegion(player)) {
                closeCraftingMenu(player);
            } else {
                player.getScheduler().run(this, (t) -> closeCraftingMenu(player), null);
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

    private void closeCraftingMenu(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof AuroraMenu menu) {
            var id = menu.getId();
            if (id != null && id.getNamespace().equals("auroracrafting")) {
                player.closeInventory();
            }
        } else if (player.getOpenInventory().getTopInventory().getHolder() instanceof CraftMenu) {
            player.closeInventory();
        }
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

    @EventHandler
    public void onItemsLoaded(ItemsLoadedEvent event) {
        if (firstInit) {
            initState();
            loading.set(false);
            firstInit = false;
        }
    }
}
