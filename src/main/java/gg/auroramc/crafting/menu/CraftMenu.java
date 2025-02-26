package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.menu.MenuEntry;
import gg.auroramc.aurora.api.menu.MenuItem;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintContext;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.blueprint.RecipeWrapperBlueprint;
import gg.auroramc.crafting.api.workbench.custom.CustomWorkbench;
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
    private final MenuItem completedItem;
    private final MenuItem notCompletedItem;
    private final Map<Integer, Blueprint> quickCraftBlueprints = new HashMap<>();
    private final Map<Integer, MenuEntry> customItems = new HashMap<>();
    private boolean updateQuickCraftOnPlace = false;
    private final CustomWorkbench workbench;

    public static CraftMenu craftMenu(AuroraCrafting plugin, Player player, CustomWorkbench workbench) {
        return new CraftMenu(plugin, player, workbench);
    }

    public CraftMenu(AuroraCrafting plugin, Player player, CustomWorkbench workbench) {
        this.plugin = plugin;
        this.player = player;
        this.workbench = workbench;

        this.matrixSlots = workbench.getMatrixSlots();
        this.matrixLookup = Set.copyOf(matrixSlots);
        this.resultSlot = workbench.getResultSlot();
        this.quickCraftSlots = new LinkedHashSet<>(workbench.getQuickCraftSlots());

        this.inventory = Bukkit.createInventory(this, workbench.getMenuOptions().getRows() * 9, Text.component(workbench.getMenuOptions().getTitle()));
        this.invalidResultItem = ItemBuilder.of(workbench.getMenuOptions().getInvalidResultItem()).toItemStack(player);
        this.fillerItem = ItemBuilder.of(workbench.getMenuOptions().getFillerItem()).toItemStack(player);
        this.noPermQuickCraftItem = ItemBuilder.of(workbench.getMenuOptions().getNoPermissionQuickCraftItem()).toItemStack(player);
        this.emptyQuickCraftItem = ItemBuilder.of(workbench.getMenuOptions().getEmptyQuickCraftItem()).toItemStack(player);
        this.completedItem = ItemBuilder.of(workbench.getMenuOptions().getBlueprintCompletedItem()).build(player);
        this.notCompletedItem = ItemBuilder.of(workbench.getMenuOptions().getBlueprintNotCompletedItem()).build(player);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (!matrixLookup.contains(i)) {
                inventory.setItem(i, fillerItem);
            }
        }
        setInvalidResult();
        setUpQuickCraft();

        for (var itemConfig : workbench.getMenuOptions().getCustomItems()) {
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
        this.quickCraftBlueprints.clear();
        var quickCraftRecipes = workbench.getCraftableBlueprints(player, workbench.getQuickCraftSlots().size(), BlueprintType.SHAPED, BlueprintType.SHAPELESS);
        var quickCraftSlots = new ArrayList<>(this.quickCraftSlots);
        quickCraftSlots.sort(Integer::compareTo);

        for (int i = 0; i < quickCraftSlots.size(); i++) {
            var slot = quickCraftSlots.get(i);
            if (i < quickCraftRecipes.size()) {
                if (player.hasPermission("aurora.quickcraft." + workbench.getId() + "." + slot)) {
                    var recipe = quickCraftRecipes.get(i);
                    inventory.setItem(slot, recipe.getResultItem());
                    this.quickCraftBlueprints.put(slot, recipe);
                } else {
                    inventory.setItem(slot, noPermQuickCraftItem);
                }
            } else {
                if (player.hasPermission("aurora.quickcraft." + workbench.getId() + "." + slot)) {
                    inventory.setItem(slot, emptyQuickCraftItem);
                } else {
                    inventory.setItem(slot, noPermQuickCraftItem);
                }
            }
        }
    }

    public void open() {
        if (AuroraCrafting.isLoading()) return;
        player.openInventory(inventory);
    }

    private boolean isCustomSlotClick(InventoryClickEvent event) {
        return event.getClickedInventory() == inventory
                && !matrixLookup.contains(event.getSlot()) && event.getSlot() != resultSlot
                && !quickCraftSlots.contains(event.getSlot())
                && !completedItem.getSlots().contains(event.getSlot())
                && !notCompletedItem.getSlots().contains(event.getSlot());
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

        if (completedItem.getItemStack().isSimilar(event.getCurrentItem())) {
            result = true;
        }

        if (notCompletedItem.getItemStack().isSimilar(event.getCurrentItem())) {
            result = true;
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

        if (!player.hasPermission("aurora.quickcraft." + workbench.getId() + "." + event.getSlot())) {
            event.setCancelled(true);
            return;
        }

        if (event.getCurrentItem() == emptyQuickCraftItem || event.getCurrentItem() == noPermQuickCraftItem) {
            event.setCancelled(true);
            return;
        }

        var blueprint = quickCraftBlueprints.get(event.getSlot());
        if (blueprint == null) {
            event.setCancelled(true);
            return;
        }

        var timesCraftable = blueprint.getQuickCraftTimes(InventoryUtils.buildItemCounts(player));
        if (timesCraftable == 0) {
            event.setCancelled(true);
            return;
        }

        final var currentItem = event.getCurrentItem() != null ? event.getCurrentItem().clone() : ItemStack.empty();

        if (event.isShiftClick()) {
            // Check the player inventory for space. If one crafting result fits, allow the shift click
            int currentSpace = InventoryUtils.calculateSpaceForItem(player.getInventory(), event.getCurrentItem());
            if (currentSpace < blueprint.getResult().amount()) {
                event.setCancelled(true);
                return;
            }
            // Add the remaining items to the player inventory, but only the amount that fits, deduct the matrix
            final int availableSpace = currentSpace - blueprint.getResult().amount();
            final int timesCrafted = Math.min((availableSpace / blueprint.getResult().amount()) + 1, timesCraftable);

            // If only the shift click is craftable, just update the matrix and return
            if (timesCrafted == 1) {
                player.getScheduler().run(plugin, (t) -> {
                    blueprint.quickCraft(context(inventory), 1, true);
                    setUpQuickCraft();
                    player.updateInventory();
                    plugin.callCraftEvent(player, currentItem, blueprint.getResult().amount(), blueprint);
                }, null);
                return;
            }

            // If there is more space, add the remaining items to the player inventory and update the matrix
            player.getScheduler().run(plugin, (t) -> {
                blueprint.quickCraft(context(inventory), timesCrafted, true);
                setUpQuickCraft();
                player.updateInventory();
                plugin.callCraftEvent(player, currentItem, timesCrafted * blueprint.getResult().amount(), blueprint);
            }, null);
        } else {
            if (event.getCursor().isEmpty()) {
                // Allow taking the result and deduct the matrix
                updateQuickCraftOnPlace = true;
                player.getScheduler().run(plugin,
                        (t) -> {
                            blueprint.quickCraft(context(inventory), 1, true);
                            setUpQuickCraft();
                            player.updateInventory();
                            plugin.callCraftEvent(player, currentItem, blueprint.getResult().amount(), blueprint);
                        }, null);
            } else {
                var cursor = event.getCursor();

                // Allow stacking the result and deduct the matrix
                if (cursor.isSimilar(currentItem)) {
                    var maxAmount = cursor.getMaxStackSize() - cursor.getAmount();
                    if (blueprint.getResult().amount() <= maxAmount) {
                        updateQuickCraftOnPlace = true;
                        player.getScheduler().run(plugin, (t) -> {
                            if (player.getItemOnCursor().isSimilar(currentItem)) {
                                player.getItemOnCursor().setAmount(cursor.getAmount() + blueprint.getResult().amount());
                            }
                            blueprint.quickCraft(context(inventory), 1, true);
                            setUpQuickCraft();
                            player.updateInventory();
                            plugin.callCraftEvent(player, currentItem, blueprint.getResult().amount(), blueprint);
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
        var context = this.context(event.getInventory());

        // If we don't have a blueprint cancel the event
        var maybeBlueprint = workbench.lookupBlueprint(context, BlueprintType.SHAPED, BlueprintType.SHAPELESS);

        if (maybeBlueprint == null && plugin.getConfigManager().getConfig().getIncludeVanillaRecipes().contains(workbench.getId())) {
            var vanillaRecipe = Bukkit.getServer().getCraftingRecipe(context.getMatrix(), player.getWorld());
            if (vanillaRecipe instanceof CraftingRecipe craftingRecipe) {
                if (craftingRecipe.getKey().getNamespace().equals("minecraft")) {
                    if (isVanillaCompatibleMatrix(context, craftingRecipe)) {
                        maybeBlueprint = new RecipeWrapperBlueprint(workbench, craftingRecipe);
                    }
                } else if (plugin.getConfigManager().getConfig().getIncludeOtherPluginRecipes().contains(workbench.getId())) {
                    if (!craftingRecipe.getKey().getNamespace().equals("aurora")) {
                        maybeBlueprint = new RecipeWrapperBlueprint(workbench, craftingRecipe);
                    }
                }
            }
        }

        final var blueprint = maybeBlueprint;

        if (blueprint == null || !blueprint.hasAccess(player)) {
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

        // Based on the crafting matrix, let's see how many times can we craft the blueprint
        var timesCraftable = blueprint.getTimesCraftable(context);
        if (timesCraftable == 0) {
            event.setCancelled(true);
            return;
        }

        final var currentItem = event.getCurrentItem() != null ? event.getCurrentItem().clone() : ItemStack.empty();

        // Handle crafting when shift clicking
        if (event.isShiftClick()) {
            // Check the player inventory for space. If one crafting result fits, allow the shift click
            int currentSpace = InventoryUtils.calculateSpaceForItem(player.getInventory(), currentItem);
            int resultAmount = currentItem.getAmount();
            if (currentSpace < resultAmount) {
                event.setCancelled(true);
                return;
            }
            // Add the remaining items to the player inventory, but only the amount that fits, deduct the matrix
            final int availableSpace = currentSpace - resultAmount;
            final int timesCrafted = Math.min((availableSpace / resultAmount) + 1, timesCraftable);

            // If only the shift click is craftable, just update the matrix and return
            if (timesCrafted == 1) {
                player.getScheduler().run(plugin, (t) -> {
                    setUpQuickCraft();
                    updateMatrix(blueprint, timesCraftable, 1, context);
                    plugin.callCraftEvent(player, currentItem, blueprint.getResult().amount(), blueprint);
                }, null);
                return;
            }

            // If there is more space, add the remaining items to the player inventory and update the matrix
            player.getScheduler().run(plugin, (t) -> {
                var amount = (timesCrafted - 1) * resultAmount;
                var stacks = ItemUtils.createStacksFromAmount(currentItem, amount);
                player.getInventory().addItem(stacks);
                setUpQuickCraft();
                updateMatrix(blueprint, timesCraftable, timesCrafted, context);
                plugin.callCraftEvent(player, currentItem, amount + resultAmount, blueprint);
            }, null);

            // Handle crafting for regular clicks
        } else {
            int resultAmount = currentItem.getAmount();

            if (event.getCursor().isEmpty()) {
                // Allow taking the result and deduct the matrix
                updateQuickCraftOnPlace = true;
                player.getScheduler().run(plugin,
                        (t) -> {
                            setUpQuickCraft();
                            updateMatrix(blueprint, timesCraftable, 1, context);
                            plugin.callCraftEvent(player, currentItem, resultAmount, blueprint);
                        }, null);
            } else {
                var cursor = event.getCursor();

                // Allow stacking the result and deduct the matrix
                if (cursor.isSimilar(currentItem)) {
                    var maxAmount = cursor.getMaxStackSize() - cursor.getAmount();
                    if (resultAmount <= maxAmount) {
                        updateQuickCraftOnPlace = true;
                        player.getScheduler().run(plugin, (t) -> {

                            if (player.getItemOnCursor().isSimilar(currentItem)) {
                                player.getItemOnCursor().setAmount(cursor.getAmount() + resultAmount);
                            }
                            setUpQuickCraft();
                            updateMatrix(blueprint, timesCraftable, 1, context);
                            plugin.callCraftEvent(player, currentItem, resultAmount, blueprint);
                        }, null);
                    }
                }

                event.setCancelled(true);
            }
        }
    }

    public void onDrag(InventoryDragEvent event) {
        for (var rawSlot : event.getRawSlots()) {
            if (rawSlot < event.getInventory().getSize()) {
                if (event.getInventory() == inventory) {
                    if (!matrixLookup.contains(rawSlot)) {
                        event.setCancelled(true);
                        return;
                    }
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

    private void updateMatrix(Blueprint blueprint, int timesCraftable, int timesCrafted, BlueprintContext context) {
        var newMatrix = blueprint.calcRemainingIngredientMatrix(context, timesCrafted);

        for (int i = 0; i < matrixSlots.size(); i++) {
            inventory.setItem(matrixSlots.get(i), newMatrix[i]);
        }
//        if (timesCraftable > timesCrafted) {
//            setResult(blueprint.getResultItem(this.context(inventory)));
//        } else {
//            setInvalidResult();
//        }

        updateResult();
    }

    private void updateResult() {
        inventory.setItem(resultSlot, invalidResultItem);
        // Calc new potential result
        var context = context(inventory);

        var blueprint = workbench.lookupBlueprint(context, BlueprintType.SHAPED, BlueprintType.SHAPELESS);

        if (blueprint != null && blueprint.hasAccess(player)) {
            var timesCraftable = blueprint.getTimesCraftable(context);
            if (timesCraftable > 0) {
                setResult(blueprint.getResultItem(context));
            } else {
                setInvalidResult();
            }
        } else {
            if (plugin.getConfigManager().getConfig().getIncludeVanillaRecipes().contains(workbench.getId())) {
                var vanillaRecipe = Bukkit.getCraftingRecipe(context.getMatrix(), player.getWorld());
                if (vanillaRecipe instanceof CraftingRecipe craftingRecipe) {
                    if (craftingRecipe.getKey().getNamespace().equals("minecraft")) {
                        if (isVanillaCompatibleMatrix(context, craftingRecipe)) {
                            var result = new RecipeWrapperBlueprint(workbench, craftingRecipe).getResultItem(context);
                            setResult(result);
                        } else {
                            setInvalidResult();
                        }
                    } else if (plugin.getConfigManager().getConfig().getIncludeOtherPluginRecipes().contains(workbench.getId())) {
                        if (craftingRecipe.getKey().getNamespace().equals("aurora")) {
                            setInvalidResult();
                        } else {
                            var result = new RecipeWrapperBlueprint(workbench, craftingRecipe).getResultItem(context);
                            setResult(result);
                        }

                    } else {
                        setInvalidResult();
                    }
                } else {
                    setInvalidResult();
                }
            } else {
                setInvalidResult();
            }
        }
        player.updateInventory();
    }

    private void setInvalidResult() {
        inventory.setItem(resultSlot, invalidResultItem);

        for (var slot : workbench.getCompletionIndicatorSlots()) {
            inventory.setItem(slot, notCompletedItem.getItemStack());
        }
    }

    private void setResult(ItemStack item) {
        inventory.setItem(resultSlot, item);

        for (var slot : workbench.getCompletionIndicatorSlots()) {
            inventory.setItem(slot, completedItem.getItemStack());
        }
    }

    private ItemStack[] getMatrix(Inventory inventory) {
        var matrix = new ItemStack[matrixSlots.size()];
        int i = 0;
        for (var slot : matrixSlots) {
            var item = inventory.getItem(slot);
            matrix[i] = Objects.requireNonNullElseGet(item, ItemStack::empty);
            i++;
        }
        return matrix;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private BlueprintContext context(Inventory inventory) {
        return new BlueprintContext(player, getMatrix(inventory));
    }

    private boolean isVanillaCompatibleMatrix(BlueprintContext context, CraftingRecipe recipe) {
        if (recipe.getKey().getNamespace().equals("minecraft")) {
            if (recipe.getKey().getKey().equals("armor_dye")) {
                return true;
            } else if (recipe.getResult().getType().name().endsWith("BUNDLE")) {
                return true;
            } else if (recipe.getResult().getType().name().endsWith("SHULKER_BOX")) {
                return true;
            }
        }
        for (var item : context.getIdMatrix()) {
            if (!item.id().namespace().equals("minecraft")) {
                return false;
            }
        }
        return true;
    }
}
