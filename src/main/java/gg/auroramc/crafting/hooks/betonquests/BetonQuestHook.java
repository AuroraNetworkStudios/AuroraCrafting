package gg.auroramc.crafting.hooks.betonquests;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.event.BlueprintCraftEvent;
import gg.auroramc.crafting.hooks.Hook;
import lombok.SneakyThrows;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.item.QuestItem;
import org.betonquest.betonquest.objectives.CraftingObjective;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

public class BetonQuestHook implements Hook, Listener {
    private static Field itemField;
    private static MethodHandle completeIfDoneOrNotifyMethod;

    @SneakyThrows
    @Override
    public void hook(AuroraCrafting plugin) {
        itemField = CraftingObjective.class.getDeclaredField("item");
        itemField.setAccessible(true);

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        completeIfDoneOrNotifyMethod = lookup.findVirtual(
                CraftingObjective.class,
                "completeIfDoneOrNotify",
                MethodType.methodType(boolean.class, Profile.class)
        );

        AuroraCrafting.logger().info("Hooked into BetonQuests to progress crafting tasks");
    }

    @SneakyThrows
    @EventHandler
    public void onCraft(BlueprintCraftEvent event) {
        OnlineProfile profile = PlayerConverter.getID(event.getPlayer());

        for (Objective obj : BetonQuest.getInstance().getPlayerObjectives(profile)) {
            if (obj instanceof CraftingObjective objective) {
                QuestItem item = (QuestItem) itemField.get(objective);

                if (objective.containsPlayer(profile) && item.compare(event.getItem()) && objective.checkConditions(profile)) {
                    objective.getCountingData(profile).progress(event.getAmount());
                    completeIfDoneOrNotifyMethod.invoke(objective, profile);
                }
            }
        }
    }
}
