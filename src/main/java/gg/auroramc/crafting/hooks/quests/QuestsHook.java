package gg.auroramc.crafting.hooks.quests;

import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.event.BlueprintCraftEvent;
import gg.auroramc.crafting.hooks.Hook;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.enums.ObjectiveType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;

public class QuestsHook implements Hook, Listener {
    private BukkitQuestsPlugin qp;

    @Override
    public void hook(AuroraCrafting plugin) {
        qp = (BukkitQuestsPlugin) Bukkit.getServer().getPluginManager().getPlugin("Quests");
        AuroraCrafting.logger().info("Hooked into Quests to progress crafting tasks");
    }

    @EventHandler
    public void onCraft(BlueprintCraftEvent event) {
        if (qp.canUseQuests(event.getPlayer().getUniqueId())) return;

        var quester = qp.getQuester(event.getPlayer().getUniqueId());
        var type = ObjectiveType.CRAFT_ITEM;

        var craftedItems = ItemUtils.createStacksFromAmount(event.getItem(), event.getAmount());

        for (var quest : qp.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) {
                continue;
            }

            for (var craftedItem : craftedItems) {
                var dispatchedQuestIDs = new HashSet<String>();

                if (quester.getCurrentQuests().containsKey(quest) && quester.getCurrentStage(quest).containsObjective(type)) {
                    quester.craftItem(quest, craftedItem);
                }

                dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, (q, cq) -> {
                    if (!dispatchedQuestIDs.contains(cq.getId())) {
                        q.craftItem(cq, craftedItem);
                    }
                    return null;
                }));
            }

        }
    }
}
