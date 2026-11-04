package gg.auroramc.crafting.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.menu.MerchantMainMenu;
import gg.auroramc.crafting.menu.MerchantMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%merchantsAlias")
public class MerchantsCommand extends BaseCommand {
    private AuroraCrafting plugin;

    public MerchantsCommand(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@merchants @nothing")
    @CommandPermission("aurora.crafting.merchants")
    public void onOpen(Player player, @Optional String merchantId) {
        openMerchant(player, merchantId);
    }

    @Subcommand("open")
    @CommandPermission("aurora.crafting.merchants.admin.open")
    @CommandCompletion("@players @merchants true|false @nothing")
    public void onOpenAdmin(CommandSender sender, @Flags("other") Player player, @Optional String merchantId, @Default("false") Boolean silent) {
        // On the player's own thread (Folia)
        player.getScheduler().run(plugin, (t) -> {
            var opened = openMerchant(player, merchantId);

            if (!silent) {
                var openedMerchantId = merchantId == null ? "main-gui" : merchantId;

                if (opened) {
                    Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getMerchantOpened(),
                            Placeholder.of("{player}", player.getName()), Placeholder.of("{id}", openedMerchantId));
                } else {
                    Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getMerchantFailedToOpen(),
                            Placeholder.of("{player}", player.getName()), Placeholder.of("{id}", openedMerchantId));
                }
            }
        }, null);
    }

    private boolean openMerchant(Player player, String merchantId) {
        if (merchantId == null) {
            MerchantMainMenu.merchantMainMenu(plugin, player).open();
            return true;
        }

        var merchant = plugin.getConfigManager().getMerchantsConfig().getMerchants().get(merchantId);

        if (merchant == null) {
            return false;
        }
        if (merchant.getPermission() != null && !player.hasPermission(merchant.getPermission())) {
            Chat.sendMessage(player, plugin.getConfigManager().getMessageConfig().getNoPermission());
            return false;
        }

        MerchantMenu.merchantMenu(player, merchant).open();
        return true;
    }
}