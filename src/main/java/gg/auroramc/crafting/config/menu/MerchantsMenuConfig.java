package gg.auroramc.crafting.config.menu;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;

import java.util.Map;

@Getter
public class MerchantsMenuConfig {
    private String title = "Merchants";
    private Integer rows = 6;
    private ItemConfig filler;
    private Map<String, ItemConfig> customItems;
}
