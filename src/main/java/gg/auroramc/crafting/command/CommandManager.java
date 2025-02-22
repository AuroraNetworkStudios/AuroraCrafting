package gg.auroramc.crafting.command;

import co.aikar.commands.MessageKeys;
import co.aikar.commands.MinecraftMessageKeys;
import co.aikar.commands.PaperCommandManager;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.workbench.Workbench;
import gg.auroramc.crafting.config.WorkbenchConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommandManager {
    private final AuroraCrafting plugin;
    private final PaperCommandManager commandManager;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand().toBuilder()
            .hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    private boolean hasSetup = false;

    public CommandManager(AuroraCrafting plugin) {
        this.commandManager = new PaperCommandManager(plugin);
        this.plugin = plugin;
    }

    private void setupCommands() {
        if (!this.hasSetup) {
            commandManager.getLocales().setDefaultLocale(Locale.ENGLISH);
            commandManager.usePerIssuerLocale(false);

            var aliases = plugin.getConfigManager().getConfig().getCommandAliases();

            commandManager.getCommandReplacements().addReplacement("craftingAlias", a(aliases.getCraft()));
            commandManager.getCommandReplacements().addReplacement("recipesAlias", a(aliases.getRecipes()));
            commandManager.getCommandReplacements().addReplacement("merchantsAlias", a(aliases.getMerchants()));

            commandManager.getCommandCompletions().registerCompletion("recipes", c -> plugin.getBlueprintRegistry().getBlueprints().stream().map(Blueprint::getId).toList());
            commandManager.getCommandCompletions().registerCompletion("workbenches", c -> plugin.getWorkbenchRegistry().getCustomWorkbenches().stream().map(Workbench::getId).toList());
            commandManager.getCommandCompletions().registerCompletion("merchants", c -> plugin.getConfigManager().getMerchantsConfig()
                    .getMerchants().entrySet().stream().filter(e -> {
                        var merchant = e.getValue();
                        return merchant.getPermission() == null || c.getSender().hasPermission(merchant.getPermission());
                    }).map(Map.Entry::getKey).toList());
        }

        var msg = plugin.getConfigManager().getMessageConfig();
        commandManager.getLocales().addMessage(Locale.ENGLISH, MinecraftMessageKeys.NO_PLAYER_FOUND, m(msg.getPlayerNotFound()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MinecraftMessageKeys.NO_PLAYER_FOUND_OFFLINE, m(msg.getPlayerNotFound()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER, m(msg.getPlayerNotFound()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MinecraftMessageKeys.IS_NOT_A_VALID_NAME, m(msg.getPlayerNotFound()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.COULD_NOT_FIND_PLAYER, m(msg.getPlayerNotFound()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.PERMISSION_DENIED, m(msg.getNoPermission()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.PERMISSION_DENIED_PARAMETER, m(msg.getNoPermission()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.INVALID_SYNTAX, m(msg.getInvalidSyntax()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.MUST_BE_A_NUMBER, m(msg.getMustBeNumber()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.ERROR_PERFORMING_COMMAND, m(msg.getCommandError()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.ERROR_GENERIC_LOGGED, m(msg.getCommandError()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.NOT_ALLOWED_ON_CONSOLE, m(msg.getPlayerOnlyCommand()));

        if (!this.hasSetup) {
            this.commandManager.registerCommand(new CraftingCommand(plugin));
            this.commandManager.registerCommand(new RecipesCommand(plugin));
            this.commandManager.registerCommand(new MerchantsCommand(plugin));
            this.hasSetup = true;
        }
    }

    public void reload() {
        this.setupCommands();
    }

    private String a(List<String> aliases) {
        return String.join("|", aliases);
    }

    private String m(String msg) {
        return serializer.serialize(Text.component(Chat.translateToMM(msg)));
    }

    public void unregisterCommands() {
        this.commandManager.unregisterCommands();
    }
}
