package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BrewingBlueprint;
import gg.auroramc.crafting.api.blueprint.CauldronBlueprint;
import gg.auroramc.crafting.api.workbench.custom.CustomWorkbench;
import gg.auroramc.crafting.api.workbench.vanilla.VanillaWorkbench;
import gg.auroramc.crafting.config.menu.vanilla.*;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

/**
 * Shows a recipe: its ingredients and result laid out like its workbench. Clicking an ingredient that has a recipe of
 * its own shows that recipe (whose back button comes back here), and the recipes of a group can be paged through.
 */
@RequiredArgsConstructor
public class BlueprintMenu {
    private static final NamespacedId key = NamespacedId.of("auroracrafting", "blueprint_view");

    private final AuroraCrafting plugin;
    private final Player player;
    private final Blueprint blueprint;
    private final Runnable backAction;

    public static BlueprintMenu blueprintMenu(AuroraCrafting plugin, Player player, Blueprint recipe, Runnable backAction) {
        return new BlueprintMenu(plugin, player, recipe, backAction);
    }

    public void open() {
        if (AuroraCrafting.isLoading()) return;
        var menu = switch (blueprint.getWorkbench()) {
            case CustomWorkbench workbench -> customWorkbenchView(workbench);
            case VanillaWorkbench<?> workbench -> vanillaView(workbench);
            default -> null;
        };
        if (menu != null) menu.open();
    }

    private AuroraMenu customWorkbenchView(CustomWorkbench workbench) {
        var menuOptions = workbench.getMenuOptions();
        var bookOptions = workbench.getRecipeBookOptions();
        var menu = menu(bookOptions.getTitle(), menuOptions.getRows(), menuOptions.getFillerItem());
        var matrix = workbench.getMatrixSlots();
        for (int i = 0; i < matrix.size(); i++) {
            addIngredient(menu, i, matrix.get(i), true);
        }
        addResult(menu, bookOptions.getResultSlot() != null ? bookOptions.getResultSlot() : workbench.getResultSlot());
        addBack(menu, ItemBuilder.of(menuOptions.getBackItem()).defaultSlot(bookOptions.getBackSlot()));
        addPaging(menu, menuOptions.getNextRecipeItem(), bookOptions.getNextRecipeSlot(),
                menuOptions.getPreviousRecipeItem(), bookOptions.getPrevRecipeSlot());
        addCustomItems(menu, bookOptions.getCustomItems().values());
        return menu;
    }

    private AuroraMenu vanillaView(VanillaWorkbench<?> workbench) {
        var configs = plugin.getConfigManager();
        return switch (workbench.getType()) {
            case FURNACE -> simpleView(configs.getFurnaceRecipeViewConfig());
            case BLAST_FURNACE -> simpleView(configs.getBlastFurnaceRecipeViewConfig());
            case SMOKER -> simpleView(configs.getSmokerRecipeViewConfig());
            case CAMPFIRE -> simpleView(configs.getCampfireRecipeViewConfig());
            case STONE_CUTTER -> simpleView(configs.getStoneCutterRecipeViewConfig());
            case SMITHING_TABLE -> smithingView(configs.getSmithingRecipeViewConfig());
            case CRAFTING_TABLE -> craftingTableView(configs.getCraftingTableRecipeViewConfig());
            case CAULDRON -> cauldronView(configs.getCauldronRecipeViewConfig());
            case BREWING_STAND -> brewingView(configs.getBrewingStandRecipeViewConfig());
        };
    }

    // One input and one result, and fuel for the furnaces. Vanilla uses one of the input, so one is shown.
    private AuroraMenu simpleView(SimpleRecipeViewConfig config) {
        var slots = config.getSlots();
        var menu = vanillaMenu(config);
        var fuel = config.getItems().getFuel();
        if (fuel != null && slots.getFuel() != null) {
            menu.addItem(ItemBuilder.of(fuel).slot(slots.getFuel()).build(player));
        }
        addResult(menu, slots.getResult());
        addIngredient(menu, 0, slots.getInput(), false);
        return addVanillaPagingAndCustomItems(menu, config);
    }

    private AuroraMenu smithingView(SmithingRecipeViewConfig config) {
        var slots = config.getSlots();
        var menu = vanillaMenu(config);
        addResult(menu, slots.getResult());
        addIngredient(menu, 0, slots.getTemplate(), true);
        addIngredient(menu, 1, slots.getBase(), true);
        addIngredient(menu, 2, slots.getAddition(), true);
        return addVanillaPagingAndCustomItems(menu, config);
    }

    private AuroraMenu craftingTableView(CraftingTableRecipeViewConfig config) {
        var slots = config.getSlots();
        var menu = vanillaMenu(config);
        addResult(menu, slots.getResult());
        // A 3x3 grid
        for (int i = 0; i < Math.min(9, slots.getMatrix().size()); i++) {
            addIngredient(menu, i, slots.getMatrix().get(i), true);
        }
        return addVanillaPagingAndCustomItems(menu, config);
    }

    private AuroraMenu cauldronView(CauldronRecipeViewConfig config) {
        var slots = config.getSlots();
        var menu = vanillaMenu(config);
        addResult(menu, slots.getResult());
        addIngredient(menu, 0, slots.getInput(), true);
        if (blueprint instanceof CauldronBlueprint cauldron) {
            var options = cauldron.getVanillaOptions();
            var fluidSlots = switch (options.getFluidLevel()) {
                case 1 -> slots.getFluidSlots().getOne();
                case 2 -> slots.getFluidSlots().getTwo();
                case 3 -> slots.getFluidSlots().getThree();
                default -> List.<Integer>of();
            };
            var fluids = config.getFluidMaterials();
            var fluid = switch (options.getFluid()) {
                case WATER_CAULDRON -> fluids.getWater();
                case LAVA_CAULDRON -> fluids.getLava();
                case POWDER_SNOW_CAULDRON -> fluids.getPowderSnow();
                default -> null;
            };
            if (fluid != null) {
                for (int slot : fluidSlots) {
                    menu.addItem(ItemBuilder.of(fluid).slot(slot).build(player));
                }
            }
        }
        return addVanillaPagingAndCustomItems(menu, config);
    }

    private AuroraMenu brewingView(BrewingStandRecipeViewConfig config) {
        var slots = config.getSlots();
        var menu = vanillaMenu(config);
        var brewing = (BrewingBlueprint) blueprint;
        for (int slot : slots.getResult()) {
            addResult(menu, slot);
        }
        addIngredient(menu, ItemBuilder.item(brewing.getIngredientItem()), brewing.getIngredient(), slots.getIngredient());
        for (int slot : slots.getInput()) {
            addIngredient(menu, ItemBuilder.item(brewing.getInputItem()), brewing.getInput(), slot);
        }
        return addVanillaPagingAndCustomItems(menu, config);
    }

    private AuroraMenu vanillaMenu(RecipeViewConfig config) {
        var menu = menu(config.getTitle(), config.getRows(), config.getItems().getFiller());
        addBack(menu, ItemBuilder.of(config.getItems().getBack()));
        return menu;
    }

    private AuroraMenu addVanillaPagingAndCustomItems(AuroraMenu menu, RecipeViewConfig config) {
        // The paging buttons look the same in every vanilla view, like in the workbenches
        var defaults = plugin.getConfigManager().getWorkbenchDefaultConfig();
        addPaging(menu, defaults.getNextRecipe(), config.getSlots().getNextRecipe(),
                defaults.getPreviousRecipe(), config.getSlots().getPrevRecipe());
        addCustomItems(menu, config.getCustomItems().values());
        return menu;
    }

    private AuroraMenu menu(String title, int rows, ItemConfig filler) {
        var menu = new AuroraMenu(player, title, rows * 9, false, key);
        menu.addFiller(ItemBuilder.of(filler).toItemStack(player));
        return menu;
    }

    /**
     * The ingredient at that index of the recipe (empty if it has fewer).
     *
     * @param withAmount the amount the recipe takes, otherwise one
     */
    private void addIngredient(AuroraMenu menu, int index, int slot, boolean withAmount) {
        var items = blueprint.getIngredientItems();
        var ingredients = blueprint.getIngredients();
        var item = index < items.size() ? items.get(index) : ItemStack.empty();
        var type = index < ingredients.size() ? ingredients.get(index).getItemPair() : null;
        var builder = ItemBuilder.item(item);
        if (withAmount) builder.amount(type != null ? type.amount() : item.getAmount());
        addIngredient(menu, builder, type != null ? type.id() : null, slot);
    }

    // Clicking it shows the ingredient's own recipe, if it has one
    private void addIngredient(AuroraMenu menu, ItemBuilder item, TypeId id, int slot) {
        var built = item.slot(slot).build(player);
        var recipe = id != null ? recipeFor(id) : null;
        if (recipe != null) {
            menu.addItem(built, (e) -> {
                showFromHere(recipe);
            });
        } else {
            menu.addItem(built);
        }
    }

    // The recipe of an ingredient, if it has one this player may see
    private Blueprint recipeFor(TypeId item) {
        var recipe = plugin.getBlueprintRegistry().getBlueprintFor(item);
        if (recipe == null) return null;
        return recipe.hasAccess(player) || !plugin.getConfigManager().getRecipeBookCategoryConfig().getSecretRecipeDisplay().getEnabled() ? recipe : null;
    }

    private void addResult(AuroraMenu menu, int slot) {
        menu.addItem(ItemBuilder.item(blueprint.getResultItem()).amount(blueprint.getResult().amount()).slot(slot).build(player));
    }

    private void addBack(AuroraMenu menu, ItemBuilder back) {
        if (backAction == null) return;
        menu.addItem(back.build(player), (e) -> {
            backAction.run();
        });
    }

    // Through the recipe's group, keeping this view's back button
    private void addPaging(AuroraMenu menu, ItemConfig nextItem, Integer nextSlot, ItemConfig prevItem, Integer prevSlot) {
        var group = blueprint.getGroup();
        if (group == null || group.getBlueprints().size() < 2 || nextSlot == null || prevSlot == null) return;
        var recipes = group.getBlueprints();
        var index = Math.max(0, recipes.indexOf(blueprint));
        var next = recipes.get((index + 1) % recipes.size());
        var previous = recipes.get((index - 1 + recipes.size()) % recipes.size());
        menu.addItem(ItemBuilder.of(nextItem).slot(nextSlot).build(player), (e) -> {
            blueprintMenu(plugin, player, next, backAction).open();
        });
        menu.addItem(ItemBuilder.of(prevItem).slot(prevSlot).build(player), (e) -> {
            blueprintMenu(plugin, player, previous, backAction).open();
        });
    }

    private void addCustomItems(AuroraMenu menu, Collection<ItemConfig> items) {
        for (var item : items) {
            menu.addItem(ItemBuilder.of(item).build(player));
        }
    }

    // Shows another recipe, whose back button comes back to this one
    private void showFromHere(Blueprint recipe) {
        blueprintMenu(plugin, player, recipe, this::open).open();
    }
}
