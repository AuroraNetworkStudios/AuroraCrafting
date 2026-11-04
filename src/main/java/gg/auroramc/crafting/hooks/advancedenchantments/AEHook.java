package gg.auroramc.crafting.hooks.advancedenchantments;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.enchant.CustomEnchantMerger;
import net.advancedplugins.ae.api.AEAPI;

import java.util.Map;

public class AEHook implements Hook<AuroraCrafting> {
    @Override
    public void hook(AuroraCrafting plugin) {
        CustomEnchantMerger.registerMerger("advancedenchantments", (from, to) -> {
            Map<String, Integer> ingredientEnchants = AEAPI.getEnchantmentsOnItem(from);
            Map<String, Integer> resultEnchants = AEAPI.getEnchantmentsOnItem(to);
            if (resultEnchants.isEmpty()) resultEnchants = Map.of();

            for (var enchant : ingredientEnchants.entrySet()) {
                if (resultEnchants.containsKey(enchant.getKey())) {
                    int currentLevel = resultEnchants.get(enchant.getKey());
                    int otherLevel = enchant.getValue();
                    if (currentLevel == otherLevel) {
                        AEAPI.applyEnchant(enchant.getKey(), currentLevel + 1, to);
                    } else {
                        AEAPI.applyEnchant(enchant.getKey(), Math.max(currentLevel, otherLevel), true, false, to);
                    }
                } else {
                    AEAPI.applyEnchant(enchant.getKey(), enchant.getValue(), true, false, to);
                }
            }

            return to;
        });
    }
}
