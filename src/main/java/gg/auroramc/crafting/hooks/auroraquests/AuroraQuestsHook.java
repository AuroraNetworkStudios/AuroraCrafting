package gg.auroramc.crafting.hooks.auroraquests;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.event.BlueprintCraftEvent;
import gg.auroramc.crafting.api.workbench.custom.CustomWorkbench;
import gg.auroramc.crafting.hooks.Hook;
import gg.auroramc.quests.api.AuroraQuestsProvider;
import gg.auroramc.quests.api.quest.TaskType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class AuroraQuestsHook implements Hook, Listener {
    @Override
    public void hook(AuroraCrafting plugin) {
        AuroraCrafting.logger().info("Hooked into AuroraQuests to progress crafting tasks");
    }

    @EventHandler
    public void onCraft(BlueprintCraftEvent event) {
        if (!(event.getBlueprint().getWorkbench() instanceof CustomWorkbench)) return;

        AuroraQuestsProvider.getQuestManager().progress(
                event.getPlayer(),
                TaskType.CRAFT,
                event.getAmount(),
                Map.of("type", AuroraAPI.getItemManager().resolveId(event.getItem()))
        );
    }
}
