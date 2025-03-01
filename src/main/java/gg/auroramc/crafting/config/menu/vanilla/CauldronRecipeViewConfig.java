package gg.auroramc.crafting.config.menu.vanilla;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
        private Integer prevRecipe = 34;
        private Integer nextRecipe = 35;
        private FluidSlots fluidSlots = new FluidSlots();
    }

    @Getter
    public static final class FluidSlots {
        private List<Integer> one = List.of(22);
        private List<Integer> two = List.of(21, 23);
        private List<Integer> three = List.of(21, 22, 23);
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

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    yaml.set("slots.prev-recipe", 34);
                    yaml.set("slots.next-recipe", 35);
                    yaml.set("config-version", 1);
                }
        );
    }
}