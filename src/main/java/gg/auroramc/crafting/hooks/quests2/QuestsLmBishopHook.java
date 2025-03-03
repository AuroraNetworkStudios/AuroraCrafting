package gg.auroramc.crafting.hooks.quests2;


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.item.QuestItem;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.bukkit.util.constraint.TaskConstraintSet;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.event.BlueprintCraftEvent;
import gg.auroramc.crafting.hooks.Hook;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class QuestsLmBishopHook implements Hook, Listener {
    private BukkitQuestsPlugin qp;
    private final Table<String, String, QuestItem> fixedQuestItemCache = HashBasedTable.create();

    @Override
    public void hook(AuroraCrafting plugin) {
        qp = (BukkitQuestsPlugin) Bukkit.getServer().getPluginManager().getPlugin("Quests");
        AuroraCrafting.logger().info("Hooked into Quests (by LMBishop) to progress crafting tasks");
    }

    @EventHandler
    public void onCraft(BlueprintCraftEvent event) {
        var player = event.getPlayer();
        var qPlayer = qp.getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        var item = event.getItem();
        var eventAmount = event.getAmount();

        var taskType = qp.getTaskTypeManager().getTaskType("crafting");
        if (taskType == null) {
            AuroraCrafting.logger().debug("Task type 'crafting' not found in Quests, skipping task progression");
            return;
        }

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, taskType, TaskConstraintSet.ALL)) {
            var quest = pendingTask.quest();
            var task = pendingTask.task();
            var taskProgress = pendingTask.taskProgress();

            if (task.hasConfigKey("item")) {
                QuestItem qi;
                if ((qi = fixedQuestItemCache.get(quest.getId(), task.getId())) == null) {
                    QuestItem fetchedItem = TaskUtils.getConfigQuestItem(task, "item", "data");
                    fixedQuestItemCache.put(quest.getId(), task.getId(), fetchedItem);
                    qi = fetchedItem;
                }

                boolean exactMatch = TaskUtils.getConfigBoolean(task, "exact-match", true);
                if (!qi.compareItemStack(item, exactMatch)) {
                    continue;
                }
            }

            int progress = TaskUtils.incrementIntegerTaskProgress(taskProgress, eventAmount);

            int amount = (int) task.getConfigValue("amount");

            if ((int) taskProgress.getProgress() >= amount) {
                taskProgress.setProgress(amount);
                taskProgress.setCompleted(true);
            }

            TaskUtils.sendTrackAdvancement(player, quest, task, pendingTask, amount);
        }
    }
}
