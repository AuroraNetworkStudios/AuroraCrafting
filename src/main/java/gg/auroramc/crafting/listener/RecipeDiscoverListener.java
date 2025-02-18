package gg.auroramc.crafting.listener;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.event.RegistryLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
            for (var player : Bukkit.getOnlinePlayers()) {
                for (var workbench : plugin.getWorkbenchRegistry().getVanillaWorkbenches()) {
                    workbench.discoverRecipesFor(player);
                }
            }
        }
    }
}
