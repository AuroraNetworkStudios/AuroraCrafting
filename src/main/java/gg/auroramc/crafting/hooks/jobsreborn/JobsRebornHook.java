package gg.auroramc.crafting.hooks.jobsreborn;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.ItemActionInfo;
import com.gamingmesh.jobs.actions.ItemNameActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.event.BlueprintCraftEvent;
import gg.auroramc.crafting.hooks.Hook;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JobsRebornHook implements Hook, Listener {
    @Override
    public void hook(AuroraCrafting plugin) {

    }

    @EventHandler
    public void onCraft(BlueprintCraftEvent event) {
        var player = event.getPlayer();

        if (!Jobs.getPermissionHandler().hasWorldPermission(player, player.getLocation().getWorld().getName()))
            return;

        if (Jobs.getGCManager().disablePaymentIfRiding && player.isInsideVehicle())
            return;

        if (!payIfCreative(player))
            return;

        var jPlayer = Jobs.getPlayerManager().getJobsPlayer(player);

        if (jPlayer == null)
            return;

        var resultItemMeta = event.getItem().getItemMeta();

        var stacks = ItemUtils.createStacksFromAmount(event.getItem(), event.getAmount());

        if (resultItemMeta != null && resultItemMeta.hasDisplayName()) {
            var name = PlainTextComponentSerializer.plainText().serialize(resultItemMeta.displayName());
            for (var ignored : stacks) {
                Jobs.action(jPlayer, new ItemNameActionInfo(name, ActionType.CRAFT));
            }
        } else {
            for (var stack : stacks) {
                Jobs.action(jPlayer, new ItemActionInfo(stack, ActionType.CRAFT));
            }
        }
    }

    private static boolean payIfCreative(Player player) {
        return player.getGameMode() != GameMode.CREATIVE || Jobs.getGCManager().payInCreative() || Jobs.getPermissionManager().hasPermission(Jobs.getPlayerManager().getJobsPlayer(player),
                "jobs.paycreative");
    }
}
