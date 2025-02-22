package gg.auroramc.crafting.hooks;

import gg.auroramc.crafting.hooks.auroraquests.AuroraQuestsHook;
import gg.auroramc.crafting.hooks.betonquests.BetonQuestHook;
import gg.auroramc.crafting.hooks.jobsreborn.JobsRebornHook;
import gg.auroramc.crafting.hooks.quests.QuestsHook;
import gg.auroramc.crafting.hooks.quests2.QuestsLmBishopHook;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Set;

@Getter
public enum Hooks {
    AURORA_QUESTS(AuroraQuestsHook.class, "AuroraQuests"),
    QUESTS(QuestsHook.class, "Quests"),
    QUESTS_LMBISHOP(QuestsLmBishopHook.class, "Quests"),
    BETON_QUEST(BetonQuestHook.class, "BetonQuest"),
    JOBS_REBORN(JobsRebornHook.class, "Jobs"),
    ;

    private final Class<? extends Hook> clazz;
    private final Set<String> plugins;

    Hooks(Class<? extends Hook> clazz, String plugin) {
        this.clazz = clazz;
        this.plugins = Set.of(plugin);
    }

    Hooks(Class<? extends Hook> clazz, Set<String> plugins) {
        this.clazz = clazz;
        this.plugins = plugins;
    }

    public boolean canHook() {
        for (String plugin : plugins) {
            if (Bukkit.getPluginManager().getPlugin(plugin) != null) {
                return true;
            }
        }
        return false;
    }
}