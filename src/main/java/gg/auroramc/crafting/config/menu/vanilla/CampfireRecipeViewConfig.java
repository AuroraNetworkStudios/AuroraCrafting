package gg.auroramc.crafting.config.menu.vanilla;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;

import java.io.File;
import java.util.Map;

import static gg.auroramc.crafting.config.ConfigManager.VANILLA_RECIPE_VIEW_PATH;

@Getter
public class CampfireRecipeViewConfig extends AuroraConfig {
    private String title = "Campfire Recipes";
    private Integer rows = 4;
    private Map<String, ItemConfig> customItems;

    private Items items;
    private Slots slots;

    @Getter
    public static final class Slots {
        private Integer result = 15;
        private Integer input = 11;
    }

    @Getter
    public static final class Items {
        private ItemConfig filler;
        private ItemConfig back;
    }

    public CampfireRecipeViewConfig(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder() + "/" + VANILLA_RECIPE_VIEW_PATH, "campfire.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource(VANILLA_RECIPE_VIEW_PATH + "/campfire.yml", false);
        }
    }
}