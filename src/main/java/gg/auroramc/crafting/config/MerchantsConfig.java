package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MerchantsConfig {
    private Map<String, MerchantConfig> merchants = new HashMap<>();

    @Getter
    public static final class MerchantConfig {
        private String name = "";
        private String permission;
        private List<MerchantOfferConfig> offers = List.of();
        private MenuConfig menu;
    }

    @Getter
    public static final class MerchantOfferConfig {
        private List<String> ingredients = List.of();
        private String result = "";
        private String permission;
    }

    @Getter
    public static final class MenuConfig {
        private String title = "";
        private ItemConfig item;
        private List<String> lockedLore = new ArrayList<>();
    }
}
