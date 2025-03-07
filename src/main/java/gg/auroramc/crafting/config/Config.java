package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public class Config extends AuroraConfig {
    private Boolean debug = false;
    private String language = "en";
    private String defaultWorkbench = "default";
    private CommandAliasConfig commandAliases;
    private Set<String> includeVanillaRecipes = new HashSet<>();
    private Set<String> includeOtherPluginRecipes = new HashSet<>();
    private Boolean openInsteadOfCraftingTable = false;
    private Boolean openShiftClickCraftingTable = false;
    private Integer clickCooldown = 75;
    private Integer shiftClickCooldown = 200;
    private Boolean autoDiscoverVanillaRecipes = false;
    private CraftHandlersConfig craftHandlers;

    @Getter
    public static final class CommandAliasConfig {
        private List<String> craft = List.of("craft");
        private List<String> recipes = List.of("recipes");
        private List<String> merchants = List.of("merchants");
    }

    @Getter
    public static final class CraftHandlersConfig {
        private Boolean craftingTable = true;
        private Boolean smithingTable = true;
        private Boolean cauldron = true;
    }

    public Config(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder(), "config.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    yaml.set("config-version", 1);
                    yaml.set("auto-discover-vanilla-recipes", false);
                    yaml.setComments("auto-discover-vanilla-recipes", List.of("Should players auto discover vanilla recipes?"));
                },
                (yaml) -> {
                    yaml.set("config-version", 2);
                    yaml.set("default-workbench", "default");
                    yaml.setComments("default-workbench", List.of("This will be opened if the above options are enabled"));

                    if (yaml.getBoolean("include-vanilla-recipes", false)) {
                        yaml.set("include-vanilla-recipes", List.of("default"));
                    } else {
                        yaml.set("include-vanilla-recipes", List.of());
                    }

                    if (yaml.getBoolean("include-other-plugin-recipes", false)) {
                        yaml.set("include-other-plugin-recipes", List.of("default"));
                    } else {
                        yaml.set("include-other-plugin-recipes", List.of());
                    }

                    yaml.setComments("include-vanilla-recipes", List.of(
                            "Should vanilla recipes be craftable in the custom crafting table?",
                            "List the workbenches you want to include vanilla recipes in"
                    ));

                    yaml.setComments("include-other-plugin-recipes", List.of(
                            "Should \"vanilla\" recipes registered by other plugins be craftable in the custom crafting table?",
                            "List the workbenches you want to include vanilla recipes in"
                    ));
                },
                (yaml) -> {
                    yaml.set("config-version", 3);
                    yaml.set("craft-handlers.crafting-table", true);
                    yaml.set("craft-handlers.smithing-table", true);
                    yaml.set("craft-handlers.cauldron", true);
                    yaml.setComments("craft-handlers", List.of(
                            "You can turn off the listeners completely to improve performance",
                            "if you don't have any recipes that are using these.",
                            "Changing these, requires a server restart to take effect."
                    ));
                }
        );
    }
}
