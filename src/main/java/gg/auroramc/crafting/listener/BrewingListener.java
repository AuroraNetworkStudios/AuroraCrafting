package gg.auroramc.crafting.listener;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;

public class BrewingListener implements Listener {
    private final AuroraCrafting plugin;

    public BrewingListener(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrew(BrewEvent event) {
        // Brewing finishes on its own, so there is no clicking player. We attribute the brew to
        // a player who is looking at the brewing stand when it finishes, otherwise we skip the actions.
        var player = event.getContents().getViewers().stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .findFirst()
                .orElse(null);
        if (player == null) return;

        var workbench = plugin.getWorkbenchRegistry().getBrewingStand();
        var inventory = event.getContents();
        var ingredient = inventory.getIngredient();
        if (ingredient == null || ingredient.isEmpty()) return;

        var brewed = new LinkedHashMap<Blueprint, Brewed>();

        // Slots 0-2 are the potion slots, results are in the same order
        for (var i = 0; i < 3; i++) {
            var input = inventory.getItem(i);
            if (input == null || input.isEmpty()) continue;

            var context = workbench.createContext(player, input, ingredient);
            var blueprint = workbench.lookupBlueprint(context, BlueprintType.BREWING);
            if (blueprint == null || blueprint.getCraftActions().isEmpty()) continue;

            var result = i < event.getResults().size() ? event.getResults().get(i) : null;
            var resultItem = result == null || result.isEmpty() ? blueprint.getResultItem().clone() : result.clone();
            brewed.merge(blueprint, new Brewed(resultItem, 1), (a, b) -> new Brewed(a.result(), a.amount() + b.amount()));
        }

        brewed.forEach((blueprint, entry) -> blueprint.executeCraftActions(player, entry.result(), entry.amount()));
    }

    private record Brewed(ItemStack result, int amount) {
    }
}
