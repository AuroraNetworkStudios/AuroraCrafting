package gg.auroramc.crafting.listener;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.event.RegistryLoadedEvent;
import gg.auroramc.crafting.api.workbench.vanilla.VanillaWorkbench;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class RecipeDiscoverListener implements Listener {
    private final AuroraCrafting plugin;

    public RecipeDiscoverListener(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getConfigManager().getConfig().getAutoDiscoverVanillaRecipes()) {
            for (var workbench : plugin.getWorkbenchRegistry().getVanillaWorkbenches()) {
                workbench.discoverRecipesFor(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegistryLoad(RegistryLoadedEvent event) {
        if (plugin.getConfigManager().getConfig().getAutoDiscoverVanillaRecipes()) {
            var workbenches = List.copyOf(plugin.getWorkbenchRegistry().getVanillaWorkbenches());
            // On each player's own thread (Folia)
            for (var player : Bukkit.getOnlinePlayers()) {
                if (Bukkit.isOwnedByCurrentRegion(player)) {
                    discover(player, workbenches);
                } else {
                    player.getScheduler().run(plugin, (t) -> discover(player, workbenches), null);
                }
            }
        }
    }

    private void discover(Player player, List<VanillaWorkbench<?>> workbenches) {
        for (var workbench : workbenches) {
            workbench.discoverRecipesFor(player);
        }
    }
}
