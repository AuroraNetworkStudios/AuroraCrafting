package gg.auroramc.crafting.listener;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.event.RegistryLoadEvent;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SmithingListener implements Listener {
    private final AuroraCrafting plugin;
    private final NamespacedKey smithingSoundKey = NamespacedKey.minecraft("block.smithing_table.use");
    private final Set<TypeId> disabledResults = new HashSet<>();

    public SmithingListener(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegistryLoad(RegistryLoadEvent event) {
        disabledResults.clear();
        disabledResults.addAll(plugin.getConfigManager().getDisabledRecipesConfig().getSmithingRecipes()
                .stream().map(TypeId::fromString).collect(Collectors.toSet()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (!(event.getViewers().getFirst() instanceof Player player)) return;

        var workbench = plugin.getWorkbenchRegistry().getSmithingTable();
        var context = workbench.createContext(player, event.getInventory());
        var blueprint = workbench.lookupBlueprint(context, BlueprintType.SMITHING);

        // Just because the blueprint is null, it doesn't mean vanilla didn't match our material choice recipes
        if (blueprint == null) {
            // We handle vanilla overlaps by not registering overlapping material choice recipes :)
            // So we can safely set the result to null here if vanilla matched our recipe but we don't.
            if (workbench.matchesRegisteredVanillaRecipe(context)) {
                event.setResult(null);
            } else {
                if (event.getInventory().getInputTemplate() != null && event.getInventory().getInputTemplate().getType().name().endsWith("TRIM_SMITHING_TEMPLATE")) {
                    return;
                }
                if (event.getResult() != null && !event.getResult().isEmpty()) {
                    if (disabledResults.contains(AuroraAPI.getItemManager().resolveId(event.getResult()))) {
                        event.setResult(null);
                    }
                }
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
}
