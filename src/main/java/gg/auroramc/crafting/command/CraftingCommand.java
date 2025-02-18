package gg.auroramc.crafting.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.menu.CraftMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%craftingAlias")
public class CraftingCommand extends BaseCommand {
    private final AuroraCrafting plugin;

    public CraftingCommand(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @Default
    @Description("Opens the crafting menu")
    @CommandCompletion("@workbenches @nothing")
    @CommandPermission("aurora.crafting.use")
    public void onMenu(Player player, @Default("default") String workbenchId) {
        if (player.hasPermission("aurora.crafting.use." + workbenchId)) {
            var workbench = plugin.getWorkbenchRegistry().getWorkbench(workbenchId);
            if (workbench != null) {
                CraftMenu.craftMenu(plugin, player, workbench).open();
            } else {
                Chat.sendMessage(player, plugin.getConfigManager().getMessageConfig().getWorkbenchNotFound(), Placeholder.of("{workbench}", workbenchId));
            }
        } else {
            Chat.sendMessage(player, plugin.getConfigManager().getMessageConfig().getNoPermission());
        }
    }

    @Subcommand("reload")
    @Description("Reloads crafting configurations")
    @CommandPermission("aurora.crafting.admin.reload")
    public void onReload(CommandSender sender) {
        plugin.reload();
        Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getReloaded());
    }

    @Subcommand("open")
    @Description("Force open a crafting menu")
    @CommandCompletion("@players @workbenches true|false @nothing")
    @CommandPermission("aurora.crafting.admin.open")
    public void onOpen(CommandSender sender, @Flags("other") Player target, @Default("default") String workbenchId, @Default("false") Boolean silent) {
        if (plugin.getWorkbenchRegistry().getWorkbench(workbenchId) != null) {
            if (target.hasPermission("aurora.crafting.use." + workbenchId)) {
                target.getScheduler().run(plugin, (t) -> {
                    var workbench = plugin.getWorkbenchRegistry().getWorkbench(workbenchId);
                    CraftMenu.craftMenu(plugin, target, workbench).open();
                    if (!silent) {
                        Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getForceOpened(), Placeholder.of("{workbench}", workbenchId), Placeholder.of("{player}", target.getName()));
                    }
                }, null);
            }
        } else {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getWorkbenchNotFound(), Placeholder.of("{workbench}", workbenchId));
        }
    }
}