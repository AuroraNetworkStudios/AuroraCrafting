package gg.auroramc.crafting.config;

import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class MessageConfig {
    private String reloaded = "&aReloaded configuration!";
    private String dataNotLoadedYet = "&cData for this player hasn't loaded yet, try again later!";
    private String dataNotLoadedYetSelf = "&cYour data isn't loaded yet, please try again later!";
    private String playerOnlyCommand = "&cThis command can only be executed by a player!";
    private String noPermission = "&cYou don't have permission to execute this command!";
    private String invalidSyntax = "&cInvalid command syntax!";
    private String mustBeNumber = "&cArgument must be a number!";
    private String playerNotFound = "&cPlayer not found!";
    private String commandError = "&cAn error occurred while executing this command!";
    private String merchantOpened = "&aOpened merchant: &2{id} &afor player: &2{player}&a!";
    private String merchantFailedToOpen = "&cFailed to open merchant: &4{id} &cfor player: &4{player}&c!";
    private String workbenchNotFound = "&cWorkbench not found!";
    private String forceOpened = "&aWorkbench opened!";

    // Not versioned: it runs on every load and copies the keys missing from the file
    public static List<Consumer<YamlConfiguration>> migrations(String language) {
        return List.of(
                (yaml) -> {
                    var plugin = AuroraCrafting.getInstance();
                    var resource = plugin.getResource("messages_" + language + ".yml");
                    if (resource == null) {
                        resource = plugin.getResource("messages_en.yml");
                    }
                    try (var in = resource) {
                        var original = YamlConfiguration.loadConfiguration(new InputStreamReader(in));

                        for (var key : original.getKeys(false)) {
                            if (yaml.contains(key)) continue;
                            yaml.set(key, original.get(key));
                        }
                    } catch (Exception e) {
                        AuroraCrafting.logger().severe("Failed to run migrations on the message file.");
                        e.printStackTrace();
                    }
                }
        );
    }
}
