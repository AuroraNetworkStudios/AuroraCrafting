package gg.auroramc.crafting.api.item;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.event.ItemsLoadedEvent;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class ItemLoader {
    private final Map<String, Boolean> toWaitFor = new HashMap<>();
    private volatile boolean loaded = false;

    public ItemLoader() {
        addToWaitFor("AuroraCrafting", 5);
    }

    public void addToWaitFor(String pluginName, int maxWaitTicks) {
        synchronized (this) {
            toWaitFor.put(pluginName, false);
        }
        Bukkit.getGlobalRegionScheduler().runDelayed(AuroraCrafting.getInstance(), (t) -> {
            if (loaded) return;
            setLoaded(pluginName);
        }, maxWaitTicks);
    }

    public void setLoaded(String pluginName) {
        synchronized (this) {
            if (loaded || toWaitFor.get(pluginName)) return;
            toWaitFor.put(pluginName, true);
            if (!isAllLoaded()) return;
            loaded = true;
        }
        // Item plugins may say they're loaded from any thread; the recipes are registered on the global one
        if (Bukkit.isGlobalTickThread()) {
            Bukkit.getPluginManager().callEvent(new ItemsLoadedEvent());
        } else {
            Bukkit.getGlobalRegionScheduler().execute(AuroraCrafting.getInstance(), () -> Bukkit.getPluginManager().callEvent(new ItemsLoadedEvent()));
        }
    }

    public synchronized boolean isAllLoaded() {
        for (var entry : toWaitFor.entrySet()) {
            if (!entry.getValue()) {
                return false;
            }
        }
        return true;
    }
}
