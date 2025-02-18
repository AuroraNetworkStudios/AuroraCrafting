package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.menu.MenuEntry;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.AuroraRecipe;
import gg.auroramc.crafting.api.VanillaRecipeWrapper;
import gg.auroramc.crafting.util.InventoryUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CraftMenu implements InventoryHolder {
    private final AuroraCrafting plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<Integer> matrixSlots;
    @Getter
    private final Set<Integer> quickCraftSlots;
    @Getter
    private final Set<Integer> matrixLookup;
    @Getter
    private final int resultSlot;
    private final ItemStack invalidResultItem;
    private final ItemStack fillerItem;
    private final ItemStack noPermQuickCraftItem;
    private final ItemStack emptyQuickCraftItem;
    private Map<Integer, AuroraRecipe> quickCraftRecipes = new HashMap<>();
    private Map<Integer, MenuEntry> customItems = new HashMap<>();
    private boolean updateQuickCraftOnPlace = false;
    private final String workbenchId;

    public static CraftMenu craftMenu(AuroraCrafting plugin, Player player, String workbenchId) {
        return new CraftMenu(plugin, player, workbenchId);
    }

    public CraftMenu(AuroraCrafting plugin, Player player, String workbenchId) {
        this.plugin = plugin;
        this.player = player;
        this.workbenchId = workbenchId;

        var config = plugin.getWorkbenchRegistry().getWorkbench(workbenchId);
        this.matrixSlots = config.getMatrixSlots();
        this.matrixLookup = Set.copyOf(matrixSlots);
        this.resultSlot = config.getResultSlot();
        this.quickCraftSlots = new HashSet<>(config.getQuickCraftSlots());

        this.inventory = Bukkit.createInventory(this, config.getMenuOptions().getRows() * 9, Text.component(config.getMenuOptions().getTitle()));
        this.invalidResultItem = ItemBuilder.of(config.getMenuOptions().getInvalidResultItem()).toItemStack(player);
        this.fillerItem = ItemBuilder.of(config.getMenuOptions().getFillerItem()).toItemStack(player);
        this.noPermQuickCraftItem = ItemBuilder.of(config.getMenuOptions().getNoPermissionQuickCraftItem()).toItemStack(player);
        this.emptyQuickCraftItem = ItemBuilder.of(config.getMenuOptions().getEmptyQuickCraftItem()).toItemStack(player);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (!matrixLookup.contains(i)) {
                inventory.setItem(i, fillerItem);
            }
        }
        inventory.setItem(resultSlot, invalidResultItem);
        setUpQuickCraft();

        for (var itemConfig : config.getMenuOptions().getCustomItems()) {
            var menuItem = ItemBuilder.of(itemConfig).build(player);
            for (var slot : menuItem.getSlots()) {
                if (matrixLookup.contains(slot) || slot == resultSlot || quickCraftSlots.contains(slot)) {
                    continue;
                }
                customItems.put(slot, new MenuEntry(menuItem));
            }
        }

        for (var entry : customItems.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getItem().getItemStack());
        }
    }

    private void setUpQuickCraft() {
        this.quickCraftRecipes.clear();
        var quickCraftRecipes = plugin.getRecipeManager().getCraftableRecipes(player, quickCraftSlots.size(), workbenchId);
        var quickCraftSlots = new ArrayList<>(this.quickCraftSlots);
        quickCraftSlots.sort(Integer::compareTo);

        for (int i = 0; i < quickCraftSlots.size(); i++) {
            var slot = quickCraftSlots.get(i);
            if (i < quickCraftRecipes.size()) {
                if (player.hasPermission("aurora.quickcraft." + slot)) {
                    var recipe = quickCraftRecipes.get(i);
                    inventory.setItem(slot, recipe.getResultItem());
                    this.quickCraftRecipes.put(slot, recipe);
                } else {
                    inventory.setItem(slot, noPermQuickCraftItem);
                }
            } else {
                if (player.hasPermission("aurora.quickcraft." + slot)) {
                    inventory.setItem(slot, emptyQuickCraftItem);
                } else {
                    inventory.setItem(slot, noPermQuickCraftItem);
                }
            }
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    private boolean isCustomSlotClick(InventoryClickEvent event) {
        return event.getClickedInventory() == inventory
                && !matrixLookup.contains(event.getSlot()) && event.getSlot() != resultSlot && !quickCraftSlots.contains(event.getSlot());
    }

    private boolean isUpdateRequired(InventoryClickEvent event) {
        return matrixLookup.contains(event.getSlot()) || (event.getClickedInventory() != inventory && event.isShiftClick());
    }

    private boolean isDumbAssClick(InventoryClickEvent event) {
        return event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY
                && event.getAction() != InventoryAction.PICKUP_ALL
                && event.getAction() != InventoryAction.PLACE_SOME
                && event.getAction() != InventoryAction.PLACE_ALL
                && event.getAction() != InventoryAction.PLACE_ONE;
    }

    private boolean isIllegalShiftClick(InventoryClickEvent event) {
        boolean result = event.getClickedInventory() != inventory
                && event.isShiftClick()
                && event.getCurrentItem() != null
                && (event.getCurrentItem().isSimilar(inventory.getItem(resultSlot)) || event.getCurrentItem().isSimilar(fillerItem));

        for (int slot : quickCraftSlots) {
            if (event.getClickedInventory() != inventory && event.isShiftClick()
                    && event.getCurrentItem() != null
                    && event.getCurrentItem().isSimilar(inventory.getItem(slot))) {
                result = true;
                break;
            }
        }

        return result;
    }

    public void onClick(InventoryClickEvent event) {
        if (event.getClick() == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }

        // Stop dumb shift clicks
        if (isIllegalShiftClick(event)) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() == player.getInventory()) {
            if (updateQuickCraftOnPlace && (event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_SOME)) {
                player.getScheduler().run(plugin, (t) -> setUpQuickCraft(), null);
                updateQuickCraftOnPlace = false;
            }
        }

        // Otherwise we don't care what the players do in their inventory
        if (event.getClickedInventory() != inventory) {
            // Should cancel DROP actions though to make quick crafting safe
            if (event.getAction().name().startsWith("DROP")) {
                event.setCancelled(true);
            } else if (isUpdateRequired(event)) {
                player.getScheduler().run(plugin, (t) -> updateResult(), null);
                return;
            }
            return;
        }

        // Prevent interacting with the other parts of the custom gui
        if (isCustomSlotClick(event)) {
            event.setCancelled(true);

            if (customItems.containsKey(event.getSlot())) {
                customItems.get(event.getSlot()).handleEvent(event);
            }
            return;
        }

        // Update the result if the matrix slots has changed or valid shift click happened
        // We don't need to cancel this
        if (isUpdateRequired(event)) {
            player.getScheduler().run(plugin, (t) -> updateResult(), null);
            return;
        }

        // At this point, we checked everything. If the player didn't click on the result we don't care.
        if (event.getSlot() == resultSlot) {
            handleResultClick(event);
        } else if (quickCraftSlots.contains(event.getSlot())) {
            handleQuickCraftSlot(event);
        }
    }

    private void handleQuickCraftSlot(InventoryClickEvent event) {
        // If the player clicked on the quick craft slot but used some weird ass click action, cancel the event
        if (isDumbAssClick(event)) {
            event.setCancelled(true);
            return;
        }
        if (event.getClick() == ClickType.RIGHT) {
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("aurora.quickcraft." + event.getSlot())) {
            event.setCancelled(true);
            return;
        }

        if (event.getCurrentItem() == emptyQuickCraftItem || event.getCurrentItem() == noPermQuickCraftItem) {
            event.setCancelled(true);
            return;
        }

        var recipe = quickCraftRecipes.get(event.getSlot());
        if (recipe == null) {
            event.setCancelled(true);
            return;
        }

        var timesCraftable = recipe.getQuickCraftTimes(player);
        if (timesCraftable == 0) {
            event.setCancelled(true);
            return;
        }

        if (event.isShiftClick()) {
            // Check the player inventory for space. If one crafting result fits, allow the shift click
            int currentSpace = InventoryUtils.calculateSpaceForItem(player.getInventory(), event.getCurrentItem());
            if (currentSpace < recipe.getResult().amount()) {
                event.setCancelled(true);
                return;
            }
            // Add the remaining items to the player inventory, but only the amount that fits, deduct the matrix
            final int availableSpace = currentSpace - recipe.getResult().amount();
            final int timesCrafted = Math.min((availableSpace / recipe.getResult().amount()) + 1, timesCraftable);

            // If only the shift click is craftable, just update the matrix and return
            if (timesCrafted == 1) {
                player.getScheduler().run(plugin, (t) -> {
                    recipe.quickCraft(player, 1, true);
                    setUpQuickCraft();
                    player.updateInventory();
                    plugin.callCraftEvent(player, recipe.getResultItem(), recipe.getResult().amount());
                }, null);
                return;
            }

            // If there is more space, add the remaining items to the player inventory and update the matrix
            player.getScheduler().run(plugin, (t) -> {
                recipe.quickCraft(player, timesCrafted, true);
                setUpQuickCraft();
                player.updateInventory();
                plugin.callCraftEvent(player, recipe.getResultItem(), timesCrafted * recipe.getResult().amount());
            }, null);
        } else {
            if (event.getCursor().isEmpty()) {
                // Allow taking the result and deduct the matrix
                updateQuickCraftOnPlace = true;
                player.getScheduler().run(plugin,
                        (t) -> {
                            recipe.quickCraft(player, 1, true);
                            setUpQuickCraft();
                            player.updateInventory();
                            plugin.callCraftEvent(player, recipe.getResultItem(), recipe.getResult().amount());
                        }, null);
            } else {
                var cursor = event.getCursor();
                var result = event.getCurrentItem();

                // Allow stacking the result and deduct the matrix
                if (cursor.isSimilar(result)) {
                    var maxAmount = cursor.getMaxStackSize() - cursor.getAmount();
                    if (recipe.getResult().amount() <= maxAmount) {
                        updateQuickCraftOnPlace = true;
                        player.getScheduler().run(plugin, (t) -> {
                            if (player.getItemOnCursor().isSimilar(result)) {
                                player.getItemOnCursor().setAmount(cursor.getAmount() + recipe.getResult().amount());
                            }
                            recipe.quickCraft(player, 1, true);
                            setUpQuickCraft();
                            player.updateInventory();
                            plugin.callCraftEvent(player, recipe.getResultItem(), recipe.getResult().amount());
                        }, null);
                    }
                }

                event.setCancelled(true);
            }
        }

    }

    private void handleResultClick(InventoryClickEvent event) {
        // If the player clicked on the result slot but used some weird ass click action, cancel the event
        if (isDumbAssClick(event)) {
            event.setCancelled(true);
            return;
        }
        if (event.getClick() == ClickType.RIGHT) {
            event.setCancelled(true);
            return;
        }

        // Get the crafting matrix
        var matrix = getMatrix(event.getInventory(), matrixSlots);

        // If we don't have a recipe cancel the event
        var maybeRecipe = plugin.getRecipeManager().getRecipeByMatrix(matrix);

        if (maybeRecipe == null && plugin.getConfigManager().getConfig().getIncludeVanillaRecipes().contains(workbenchId)) {
            var matrixArray = matrix.toArray(ItemStack[]::new);
            var vanillaRecipe = Bukkit.getServer().getCraftingRecipe(matrixArray, player.getWorld());
            if (vanillaRecipe instanceof CraftingRecipe craftingRecipe) {
                if (craftingRecipe.getKey().getNamespace().equals("minecraft") || plugin.getConfigManager().getConfig().getIncludeOtherPluginRecipes().contains(workbenchId)) {
                    maybeRecipe = new VanillaRecipeWrapper(craftingRecipe, matrixArray);
                }
            }
        }

        final var recipe = maybeRecipe;

        if (recipe == null || !recipe.hasPermission(player, workbenchId)) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory().getItem(resultSlot) == invalidResultItem) {
            event.setCancelled(true);
            return;
        }

        if (event.getCurrentItem() == invalidResultItem) {
            event.setCancelled(true);
            return;
        }

        // Based on the crafting matrix, let's see how many times can we craft the recipe
        var timesCraftable = recipe.getTimesCraftable(matrix);
        if (timesCraftable == 0) {
            event.setCancelled(true);
            return;
        }

        // Handle crafting when shift clicking
        if (event.isShiftClick()) {
            // Check the player inventory for space. If one crafting result fits, allow the shift click
            int currentSpace = InventoryUtils.calculateSpaceForItem(player.getInventory(), event.getCurrentItem());
            if (currentSpace < maybeRecipe.getResult().amount()) {
                event.setCancelled(true);
                return;
            }
            // Add the remaining items to the player inventory, but only the amount that fits, deduct the matrix
            final int availableSpace = currentSpace - maybeRecipe.getResult().amount();
            final int timesCrafted = Math.min((availableSpace / maybeRecipe.getResult().amount()) + 1, timesCraftable);

            // If only the shift click is craftable, just update the matrix and return
            if (timesCrafted == 1) {
                player.getScheduler().run(plugin, (t) -> {
                    setUpQuickCraft();
                    updateMatrix(recipe, timesCraftable, 1, matrix);
                    plugin.callCraftEvent(player, recipe.getResultItem(), recipe.getResult().amount());
                }, null);
                return;
            }

            // If there is more space, add the remaining items to the player inventory and update the matrix
            player.getScheduler().run(plugin, (t) -> {
                var amount = (timesCrafted - 1) * recipe.getResult().amount();
                var stacks = ItemUtils.createStacksFromAmount(recipe.getResultItem(), amount);
                player.getInventory().addItem(stacks);
                setUpQuickCraft();
                updateMatrix(recipe, timesCraftable, timesCrafted, matrix);
                plugin.callCraftEvent(player, recipe.getResultItem(), amount + recipe.getResult().amount());
            }, null);

            // Handle crafting for regular clicks
        } else {
            if (event.getCursor().isEmpty()) {
                // Allow taking the result and deduct the matrix
                updateQuickCraftOnPlace = true;
                player.getScheduler().run(plugin,
                        (t) -> {
                            setUpQuickCraft();
                            updateMatrix(recipe, timesCraftable, 1, matrix);
                            plugin.callCraftEvent(player, recipe.getResultItem(), recipe.getResult().amount());
                        }, null);
            } else {
                var cursor = event.getCursor();
                var result = event.getCurrentItem();

                // Allow stacking the result and deduct the matrix
                if (cursor.isSimilar(result)) {
                    var maxAmount = cursor.getMaxStackSize() - cursor.getAmount();
                    if (maybeRecipe.getResult().amount() <= maxAmount) {
                        updateQuickCraftOnPlace = true;
                        player.getScheduler().run(plugin, (t) -> {
                            if (player.getItemOnCursor().isSimilar(result)) {
                                player.getItemOnCursor().setAmount(cursor.getAmount() + recipe.getResult().amount());
                            }
                            setUpQuickCraft();
                            updateMatrix(recipe, timesCraftable, 1, matrix);
                            plugin.callCraftEvent(player, recipe.getResultItem(), recipe.getResult().amount());
                        }, null);
                    }
                }

                event.setCancelled(true);
            }
        }
    }

    public void onDrag(InventoryDragEvent event) {
        for (var rawSlot : event.getRawSlots()) {
            var inv = event.getView().getInventory(rawSlot);
            if (inv == inventory) {
                if (!matrixLookup.contains(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (matrixLookup.stream().anyMatch(s -> event.getInventorySlots().contains(s))) {
            player.getScheduler().run(plugin, (t) -> updateResult(), null);
        }
    }

    public void onClose(InventoryCloseEvent event) {
        var items = new ArrayList<ItemStack>();

        for (var slot : matrixSlots) {
            var item = event.getInventory().getItem(slot);
            if (item != null) {
                items.add(item);
            }
        }

        if (items.isEmpty()) {
            return;
        }

        var failed = player.getInventory().addItem(items.toArray(new ItemStack[0]));

        if (!failed.isEmpty()) {
            failed.forEach((s, i) -> player.getWorld().dropItem(player.getLocation(), i));
        }
    }

    private void updateMatrix(AuroraRecipe recipe, int timesCraftable, int timesCrafted, List<ItemStack> matrix) {
        var newMatrix = recipe.calcRemainingIngredientMatrix(timesCrafted, matrix);

        for (int i = 0; i < matrixSlots.size(); i++) {
            inventory.setItem(matrixSlots.get(i), newMatrix[i]);
        }
        if (timesCraftable > timesCrafted) {
            inventory.setItem(resultSlot, recipe.getResultItem());
        } else {
            inventory.setItem(resultSlot, invalidResultItem);
        }

        updateResult();
    }

    private void updateResult() {
        inventory.setItem(resultSlot, invalidResultItem);
        // Calc new potential result
        var matrix = getMatrix(inventory, matrixSlots);

        var recipe = plugin.getRecipeManager().getRecipeByMatrix(matrix);

        if (recipe != null && recipe.hasPermission(player, workbenchId)) {
            var timesCraftable = recipe.getTimesCraftable(matrix);
            if (timesCraftable > 0) {
                inventory.setItem(resultSlot, recipe.getResultItem());
            }
        } else {
            if (plugin.getConfigManager().getConfig().getIncludeVanillaRecipes().contains(workbenchId)) {
                var vanillaRecipe = Bukkit.getCraftingRecipe(matrix.toArray(ItemStack[]::new), player.getWorld());
                if (vanillaRecipe instanceof CraftingRecipe craftingRecipe) {
                    if (!craftingRecipe.getKey().getNamespace().equals("minecraft") && !plugin.getConfigManager().getConfig().getIncludeOtherPluginRecipes().contains(workbenchId)) {
                        inventory.setItem(resultSlot, invalidResultItem);
                    } else {
                        var result = new VanillaRecipeWrapper(craftingRecipe, matrix.toArray(ItemStack[]::new)).getResultItem();
                        inventory.setItem(resultSlot, result);
                    }
                } else {
                    inventory.setItem(resultSlot, invalidResultItem);
                }
            } else {
                inventory.setItem(resultSlot, invalidResultItem);
            }
        }
        player.updateInventory();
    }

    private List<ItemStack> getMatrix(Inventory inventory, List<Integer> matrixSlots) {
        var matrix = new ArrayList<ItemStack>();
        for (var slot : matrixSlots) {
            var item = inventory.getItem(slot);
            if (item != null) {
                matrix.add(item);
            } else {
                matrix.add(ItemStack.empty());
            }
        }
        return matrix;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
