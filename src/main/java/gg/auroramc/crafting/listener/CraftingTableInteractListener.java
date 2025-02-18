package gg.auroramc.crafting.listener;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.menu.CraftMenu;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CraftingTableInteractListener implements Listener {
    private final AuroraCrafting plugin;

    public CraftingTableInteractListener(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CRAFTING_TABLE) {
            if (plugin.getConfigManager().getConfig().getOpenInsteadOfCraftingTable()) {
                if (!event.getPlayer().hasPermission("aurora.crafting.use.interact")) return;
                event.setCancelled(true);
                var workbench = plugin.getWorkbenchRegistry().getWorkbench(plugin.getConfigManager().getConfig().getDefaultWorkbench());
                CraftMenu.craftMenu(plugin, event.getPlayer(), workbench).open();
            } else if (plugin.getConfigManager().getConfig().getOpenShiftClickCraftingTable() && event.getPlayer().isSneaking()) {
                if (!event.getPlayer().hasPermission("aurora.crafting.use.interact")) return;
                event.setCancelled(true);
                var workbench = plugin.getWorkbenchRegistry().getWorkbench(plugin.getConfigManager().getConfig().getDefaultWorkbench());
                CraftMenu.craftMenu(plugin, event.getPlayer(), workbench).open();
            }
        }
    }
}
