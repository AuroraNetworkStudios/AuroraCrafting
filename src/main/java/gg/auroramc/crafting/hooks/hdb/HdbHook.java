package gg.auroramc.crafting.hooks.hdb;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.crafting.AuroraCrafting;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HdbHook implements Hook<AuroraCrafting>, Listener {
    private AuroraCrafting plugin;

    @Override
    public void hook(AuroraCrafting plugin) {
        this.plugin = plugin;
        plugin.getItemLoader().addToWaitFor("HeadDatabase", 400);
    }

    @EventHandler
    public void onLoad(DatabaseLoadEvent event) {
        plugin.getItemLoader().setLoaded("HeadDatabase");
    }
}
