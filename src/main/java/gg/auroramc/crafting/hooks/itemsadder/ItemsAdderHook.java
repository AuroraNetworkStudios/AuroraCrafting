package gg.auroramc.crafting.hooks.itemsadder;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.crafting.AuroraCrafting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderHook implements Hook<AuroraCrafting>, Listener {
    private AuroraCrafting plugin;

    @Override
    public void hook(AuroraCrafting plugin) {
        this.plugin = plugin;
        plugin.getItemLoader().addToWaitFor("ItemsAdder", 400);
    }

    @EventHandler
    public void onDataLoad(ItemsAdderLoadDataEvent event) {
        if (event.getCause() == ItemsAdderLoadDataEvent.Cause.FIRST_LOAD) {
            plugin.getItemLoader().setLoaded("ItemsAdder");
        }
    }
}
