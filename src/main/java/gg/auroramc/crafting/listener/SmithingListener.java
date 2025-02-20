package gg.auroramc.crafting.listener;

import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SmithingListener implements Listener {
    private final AuroraCrafting plugin;
    private final NamespacedKey smithingSoundKey = NamespacedKey.minecraft("block.smithing_table.use");
    private final List<SmithingRecipeWrapper> vanillaRecipes = new ArrayList<>();

    public SmithingListener(AuroraCrafting plugin) {
        this.plugin = plugin;
        for (@NotNull Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            var recipe = it.next();
            if (recipe instanceof SmithingTransformRecipe smithingRecipe) {
                vanillaRecipes.add(new SmithingTransformRecipeWrapper(smithingRecipe));
            } else if (recipe instanceof SmithingTrimRecipe smithingRecipe) {
                vanillaRecipes.add(new SmithingTrimRecipeWrapper(smithingRecipe));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (!(event.getViewers().getFirst() instanceof Player player)) return;

        var workbench = plugin.getWorkbenchRegistry().getSmithingTable();
        var context = workbench.createContext(player, event.getInventory());
        var blueprint = workbench.lookupBlueprint(context, BlueprintType.SMITHING);

        // Just because the blueprint is null, it doesn't mean vanilla didn't match our material choice recipes
        if (blueprint == null) {
            // If there is an actual vanilla recipe, we should let it go through
            var vanillaRecipe = getVanillaRecipe(event.getInventory());
            if (vanillaRecipe != null) {
                if (vanillaRecipe.getResult() != null && !vanillaRecipe.getResult().isEmpty()) {
                    // Trimming is hardcoded by mojang, so we only set the result if it is not empty
                    event.setResult(vanillaRecipe.getResult());
                } else if (workbench.matchesRegisteredVanillaRecipe(context)) {
                    // Users might register a recipe that overlaps with a vanilla trim recipe
                    // In this case we don't really have a choice, have to set the result to null
                    // Because I won't reimplement vanilla trimming logic
                    event.setResult(null);
                }
            } else {
                // If we don't have a vanilla recipe, just set the result to null
                event.setResult(null);
            }
            return;
        }

        if (!blueprint.hasAccess(player)) {
            event.setResult(null);
            return;
        }

        if (blueprint.getTimesCraftable(context) <= 0) {
            event.setResult(null);
            return;
        }

        event.setResult(blueprint.getResultItem(context));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSmithing(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getClickedInventory() instanceof SmithingInventory)) return;

        var workbench = plugin.getWorkbenchRegistry().getSmithingTable();
        if (event.getSlot() != workbench.getResultSlot()) return;

        var context = workbench.createContext(player, event.getInventory());
        var blueprint = workbench.lookupBlueprint(context, BlueprintType.SMITHING);

        if (blueprint == null) {
            return;
        }

        if (!blueprint.isStacked()) return;

        event.setCancelled(true);

        // Ignore dumb ways of crafting
        if (event.getClick() != ClickType.SHIFT_LEFT && event.getClick() != ClickType.LEFT) {
            return;
        }

        var timesCraftable = blueprint.getTimesCraftable(context);
        if (timesCraftable == 0) return;

        final var currentItem = event.getCurrentItem() != null ? event.getCurrentItem().clone() : ItemStack.empty();
        final var sound = Registry.SOUNDS.get(smithingSoundKey);

        if (event.isShiftClick()) {
            int currentSpace = InventoryUtils.calculateSpaceForItem(player.getInventory(), currentItem);
            if (currentSpace < blueprint.getResult().amount()) {
                event.setCancelled(true);
                return;
            }
            final int availableSpace = currentSpace - blueprint.getResult().amount();
            final int timesCrafted = Math.min((availableSpace / blueprint.getResult().amount()) + 1, timesCraftable);

            if (timesCrafted == 1) {
                updateMatrix(player, event.getInventory(), blueprint.calcRemainingIngredientMatrix(context, 1));
                player.getInventory().addItem(currentItem);
                player.playSound(player, sound, 1f, 1f);
            } else {
                var amount = timesCrafted * blueprint.getResult().amount();
                var stacks = ItemUtils.createStacksFromAmount(currentItem, amount);
                player.getInventory().addItem(stacks);
                updateMatrix(player, event.getInventory(), blueprint.calcRemainingIngredientMatrix(context, timesCrafted));
                player.playSound(player, sound, 1f, 1f);
            }
        } else {
            if (event.getCursor().isEmpty()) {
                updateMatrix(player, event.getInventory(), blueprint.calcRemainingIngredientMatrix(context, 1));
                player.getScheduler().run(plugin, (t) -> player.setItemOnCursor(currentItem), null);
                player.playSound(player, sound, 1f, 1f);
            } else {
                if (event.getCursor().isSimilar(currentItem)) {
                    var maxAmount = event.getCursor().getMaxStackSize() - event.getCursor().getAmount();
                    if (blueprint.getResult().amount() <= maxAmount) {
                        updateMatrix(player, event.getInventory(), blueprint.calcRemainingIngredientMatrix(context, 1));
                        player.playSound(player, sound, 1f, 1f);
                        player.getScheduler().run(plugin, (t) -> {
                            player.getItemOnCursor().setAmount(event.getCursor().getAmount() + blueprint.getResult().amount());
                        }, null);
                    }
                }
            }
        }

    }

    private void updateMatrix(Player player, Inventory inventory, ItemStack[] resultingMatrix) {
        var workbench = plugin.getWorkbenchRegistry().getSmithingTable();
        run(player, () -> {
            for (var i = 0; i < workbench.getMatrixSlots().size(); i++) {
                inventory.setItem(workbench.getMatrixSlots().get(i), resultingMatrix[i]);
            }
        });
    }


    private void run(Player player, Runnable runnable) {
        runnable.run();
        Bukkit.getRegionScheduler().run(plugin, player.getLocation(), (t) -> runnable.run());
    }

    private SmithingRecipeWrapper getVanillaRecipe(SmithingInventory inventory) {
        for (var recipe : vanillaRecipes) {
            if (recipe.matches(inventory)) {
                return recipe;
            }
        }
        return null;
    }

    public interface SmithingRecipeWrapper {
        boolean matches(SmithingInventory inventory);

        ItemStack getResult();
    }

    public static class SmithingTransformRecipeWrapper implements SmithingRecipeWrapper {
        private final SmithingTransformRecipe recipe;

        public SmithingTransformRecipeWrapper(SmithingTransformRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public boolean matches(SmithingInventory inventory) {
            return matchesChoice(recipe.getTemplate(), inventory.getItem(0)) &&
                    matchesChoice(recipe.getBase(), inventory.getItem(1)) &&
                    matchesChoice(recipe.getAddition(), inventory.getItem(2));
        }

        @Override
        public ItemStack getResult() {
            return recipe.getResult();
        }
    }

    public static class SmithingTrimRecipeWrapper implements SmithingRecipeWrapper {
        private final SmithingTrimRecipe recipe;

        public SmithingTrimRecipeWrapper(SmithingTrimRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public boolean matches(SmithingInventory inventory) {
            return matchesChoice(recipe.getTemplate(), inventory.getItem(0)) &&
                    matchesChoice(recipe.getBase(), inventory.getItem(1)) &&
                    matchesChoice(recipe.getAddition(), inventory.getItem(2));
        }

        @Override
        public ItemStack getResult() {
            return null;
        }
    }

    private static boolean matchesChoice(RecipeChoice choice, ItemStack item) {
        if (choice == null || item == null) {
            return false;
        }
        return choice.test(item);
    }
}
