package gg.auroramc.crafting.hooks.mythicmobs;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.crafting.AuroraCrafting;

public class MythicHook implements Hook<AuroraCrafting> {
    @Override
    public void hook(AuroraCrafting plugin) {
        // MythicMobs is loading enchants 40 ticks after the server starts
        plugin.getItemLoader().addToWaitFor("MythicMobs", 40);
    }
}
