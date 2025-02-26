package gg.auroramc.crafting.config.menu.vanilla;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;

import java.io.File;
import java.util.List;
import java.util.Map;

import static gg.auroramc.crafting.config.ConfigManager.VANILLA_RECIPE_VIEW_PATH;

@Getter
public class CauldronRecipeViewConfig extends AuroraConfig {
    private String title = "Cauldron Recipes";
    private Integer rows = 6;
    private Map<String, ItemConfig> customItems;

    private Items items;
    private Slots slots;
    private Fluids fluidMaterials;

    @Getter
    public static final class Fluids {
        private ItemConfig water;
        private ItemConfig lava;
        private ItemConfig powderSnow;
    }

    @Getter
    public static final class Slots {
        private Integer input = 10;
        private Integer result = 16;
        private FluidSlots fluidSlots;
    }

    @Getter
    public static final class FluidSlots {
        private List<Integer> one;
        private List<Integer> two;
        private List<Integer> three;
    }


    @Getter
    public static final class Items {
        private ItemConfig filler;
        private ItemConfig back;
    }

    public CauldronRecipeViewConfig(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder() + "/" + VANILLA_RECIPE_VIEW_PATH, "cauldron.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource(VANILLA_RECIPE_VIEW_PATH + "/cauldron.yml", false);
        }
    }
}