package gg.auroramc.crafting.util;

import com.leonardobishop.quests.bukkit.menu.itemstack.QItemStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class FireworkRecipeMaker {
    private static Color getDyeColor(Material dye) {
        return switch (dye) {
            case RED_DYE -> Color.RED;
            case BLUE_DYE -> Color.BLUE;
            case GREEN_DYE -> Color.LIME;
            case YELLOW_DYE -> Color.YELLOW;
            case ORANGE_DYE -> Color.ORANGE;
            case WHITE_DYE -> Color.WHITE;
            case BLACK_DYE -> Color.BLACK;
            case PURPLE_DYE -> Color.PURPLE;
            case LIGHT_BLUE_DYE -> Color.AQUA;
            case MAGENTA_DYE -> Color.FUCHSIA;
            case LIME_DYE -> Color.LIME;
            case PINK_DYE -> Color.PURPLE;
            case LIGHT_GRAY_DYE -> Color.GRAY;
            case GRAY_DYE -> Color.GRAY;
            case CYAN_DYE -> Color.TEAL;
            case BROWN_DYE -> Color.MAROON;
            default -> null;
        };
    }

    public ItemStack craftFireStar(ItemStack[] matrix) {

        FireStarSettings settings = getFireStarSettings(matrix);

        // must have gunpowder and at least one color for the recipe to be valid
        if (!settings.isGunPowder() || settings.getColors().isEmpty()) return null;


        ItemStack fireWorkStar = new ItemStack(Material.FIREWORK_STAR);

        FireworkEffectMeta fireMeta = (FireworkEffectMeta) fireWorkStar.getItemMeta();

        if (fireMeta == null) return null;

        FireworkEffect.Builder builder = FireworkEffect.builder()
                .withColor(settings.getColors());

        if(settings.isGlowStone()) builder.withFlicker();
        if(settings.isDiamond()) builder.withTrail();
        if(settings.getType() != null) builder.with(settings.getType());

        fireMeta.setEffect(builder.build());
        fireWorkStar.setItemMeta(fireMeta);

        return fireWorkStar;
    }

    private static FireStarSettings getFireStarSettings(ItemStack[] matrix) {
        FireStarSettings settings = new FireStarSettings();

        for (ItemStack item : matrix) {
            if (item == null) continue;
            if(item.isEmpty() || item.getType().equals(Material.AIR)) continue;

            switch (item.getType()) {
                case GUNPOWDER -> settings.setGunPowder(true);
                case GLOWSTONE_DUST -> settings.setGlowStone(true);
                case DIAMOND -> settings.setDiamond(true);

                case FIRE_CHARGE -> settings.setType(FireworkEffect.Type.BALL_LARGE);
                case GOLD_NUGGET -> settings.setType(FireworkEffect.Type.STAR);
                case FEATHER -> settings.setType(FireworkEffect.Type.BURST);
                case SKELETON_SKULL, WITHER_SKELETON_SKULL, PLAYER_HEAD, CREEPER_HEAD -> settings.setType(FireworkEffect.Type.CREEPER);

                default -> {
                    if(item.getType().name().toLowerCase().endsWith("dye")) {
                        Color color = getDyeColor(item.getType());
                        if(color != null) settings.getColors().add(color);
                    }
                }
            }
        }

        return settings;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    private static class FireStarSettings {
        private boolean gunPowder = false;
        private boolean glowStone = false;
        private boolean diamond = false;
        private FireworkEffect.Type type = null;
        private List<Color> colors = new ArrayList<>();
    }


    /*
     * |------------------------|
     * |       FIREWORK         |
     * |------------------------|
     */

    public ItemStack craftFireworkRocket(ItemStack[] matrix) {
        FireWorkSettings settings = getFireWorkSettings(matrix);

        // must have at least one gunpowder and one paper
        if(settings.getGunPowderCount() == 0 || settings.getPaperCount() == 0) return null;

        ItemStack fireWorkRocket = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta rocketMeta = (FireworkMeta) fireWorkRocket.getItemMeta();
        if(rocketMeta == null) return null;

        rocketMeta.setPower(settings.getGunPowderCount());

        for(FireworkEffect fireworkEffect : settings.getEffects()) {
            rocketMeta.addEffect(fireworkEffect);
        }

        fireWorkRocket.setItemMeta(rocketMeta);
        return fireWorkRocket;
    }

    private FireWorkSettings getFireWorkSettings(ItemStack[] matrix) {
        FireWorkSettings settings = new FireWorkSettings();

        int gunPowderCount = 0;

        for (ItemStack item : matrix) {
            if (item == null) continue;
            if(item.isEmpty() || item.getType().equals(Material.AIR)) continue;


            // todo, config option to ignore gunpowder vanilla limits
            switch (item.getType()) {
                case GUNPOWDER -> gunPowderCount = Math.min(gunPowderCount + item.getAmount(), 3);
                case PAPER -> settings.setPaperCount(settings.getPaperCount() + item.getAmount());

                case FIREWORK_STAR -> {
                    FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
                    if (meta != null && meta.hasEffect()) {
                        settings.getEffects().add(meta.getEffect());
                    }
                }
            }
        }
        settings.setGunPowderCount(gunPowderCount);

        return settings;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    private static class FireWorkSettings {
        private int gunPowderCount = 0;
        private int paperCount = 0;
        private List<FireworkEffect> effects = new ArrayList<>();
    }
}
