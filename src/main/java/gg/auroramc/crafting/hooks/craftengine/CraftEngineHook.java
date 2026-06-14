package gg.auroramc.crafting.hooks.craftengine;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.hooks.Hook;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CraftEngineHook implements Hook, Listener {
    private AuroraCrafting plugin;

    @Override
    public void hook(AuroraCrafting plugin) {
        this.plugin = plugin;
        plugin.getItemLoader().addToWaitFor("CraftEngine", 400);

        var resolver = new CraftEngineItemResolver();
        AuroraAPI.getItemManager().registerResolver("craftengine", resolver);
        AuroraAPI.getItemManager().registerResolver("craft-engine", resolver);
    }

    @EventHandler
    public void onCraftEngineReload(CraftEngineReloadEvent event) {
        if (event.isFirstReload()) {
            plugin.getItemLoader().setLoaded("CraftEngine");
        }
    }
}
