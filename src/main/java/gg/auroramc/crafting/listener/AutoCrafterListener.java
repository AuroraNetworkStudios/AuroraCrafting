package gg.auroramc.crafting.listener;

import gg.auroramc.crafting.AuroraCrafting;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;

@RequiredArgsConstructor
public class AutoCrafterListener implements Listener {
    private final AuroraCrafting plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCrafterCraftItem(CrafterCraftEvent event) {
        if (event.getRecipe().getKey().getNamespace().equals("aurora")) {
            var blueprint = plugin.getBlueprintRegistry().getBlueprint(event.getRecipe().getKey().getKey());
            if (blueprint == null || blueprint.isStacked()) {
                event.setCancelled(true);
            }
        }
    }
}
