package gg.auroramc.crafting.menu;

import gg.auroramc.crafting.AuroraCrafting;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class MenuListener implements Listener {
    private final AuroraCrafting plugin;
    // Written from every player's own thread (Folia)
    private final Map<UUID, Long> cache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> afterMatrixCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> shiftCache = new ConcurrentHashMap<>();

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        var holder = event.getInventory().getHolder();
        if (holder instanceof CraftMenu menu) {
            if (isOnCooldown(event.getWhoClicked().getUniqueId())) {
                event.setCancelled(true);
                return;
            }

            if (isOnShiftCooldown(event.getWhoClicked().getUniqueId())) {
                event.setCancelled(true);
                return;
            }

            if (menu.getResultSlot() == event.getSlot() && isOnAfterMatrixCooldown(event.getWhoClicked().getUniqueId())) {
                event.setCancelled(true);
                return;
            }

            menu.onClick(event);

            if (event.getClickedInventory() != menu.getInventory()) {
                if (event.isShiftClick()) {
                    afterMatrixCache.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
                }
                return;
            }

            if (menu.getMatrixLookup().contains(event.getSlot())) {
                afterMatrixCache.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
                return;
            }

            if (event.isShiftClick()) {
                shiftCache.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
            } else {
                cache.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    private boolean isOnCooldown(UUID uuid) {
        var time = cache.get(uuid);
        if (time == null) return false;
        return System.currentTimeMillis() - time < plugin.getConfigManager().getConfig().getClickCooldown();
    }

    private boolean isOnAfterMatrixCooldown(UUID uuid) {
        var time = afterMatrixCache.get(uuid);
        if (time == null) return false;
        return System.currentTimeMillis() - time < plugin.getConfigManager().getConfig().getClickCooldown();
    }

    public boolean isOnShiftCooldown(UUID uuid) {
        var time = shiftCache.get(uuid);
        if (time == null) return false;
        return System.currentTimeMillis() - time < plugin.getConfigManager().getConfig().getShiftClickCooldown();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        var holder = event.getInventory().getHolder();
        if (holder instanceof CraftMenu menu) {
            menu.onDrag(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        var holder = event.getInventory().getHolder();
        if (holder instanceof CraftMenu menu) {
            menu.onClose(event);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cache.remove(event.getPlayer().getUniqueId());
        shiftCache.remove(event.getPlayer().getUniqueId());
        afterMatrixCache.remove(event.getPlayer().getUniqueId());
    }
}
