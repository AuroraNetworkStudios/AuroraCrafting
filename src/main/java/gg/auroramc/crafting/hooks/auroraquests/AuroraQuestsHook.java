package gg.auroramc.crafting.hooks.auroraquests;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.event.BlueprintCraftEvent;
import gg.auroramc.crafting.api.workbench.custom.CustomWorkbench;
import gg.auroramc.quests.api.event.objective.PlayerCraftedItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AuroraQuestsHook implements Hook<AuroraCrafting>, Listener {
    @Override
    public void hook(AuroraCrafting plugin) {
        AuroraCrafting.logger().info("Hooked into AuroraQuests to progress crafting tasks");
    }

    @EventHandler
    public void onCraft(BlueprintCraftEvent event) {
        if (!(event.getBlueprint().getWorkbench() instanceof CustomWorkbench)) return;

        Bukkit.getPluginManager().callEvent(
                new PlayerCraftedItemEvent(
                        event.getPlayer(),
                        AuroraAPI.getItemManager().resolveId(event.getItem()),
                        event.getAmount()
                )
        );
    }
}
