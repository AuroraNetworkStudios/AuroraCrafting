package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.workbench.Workbench;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

public class RecipeWrapperBlueprint extends Blueprint {
    private static final Set<Material> BUCKET = Set.of(Material.MILK_BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET);
    private static final Set<Material> LEATHER_ARMOR = Set.of(
            Material.LEATHER_HORSE_ARMOR, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS
    );

    private final CraftingRecipe backingRecipe;

    public RecipeWrapperBlueprint(Workbench workbench, CraftingRecipe recipe) {
        super(workbench, recipe.getKey().toString());
        this.backingRecipe = recipe;
        this.result = new ItemPair(AuroraAPI.getItemManager().resolveId(recipe.getResult()), recipe.getResult().getAmount());
        this.resultItem = recipe.getResult();

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            handleShaped(shapedRecipe);
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            handleShapeless(shapelessRecipe);
        }
    }

    private void handleShaped(ShapedRecipe recipe) {
        var choiceMap = recipe.getChoiceMap();
        for (var row : recipe.getShape()) {
            for (var character : row.toCharArray()) {
                if (character == ' ') continue;
                var choice = choiceMap.get(character);
                if (choice == null) continue;
                handleChoice(choice);
            }
        }
    }

    private void handleShapeless(ShapelessRecipe recipe) {
        var choiceList = recipe.getChoiceList();
        for (var choice : choiceList) {
            handleChoice(choice);
        }
    }

    private void handleChoice(RecipeChoice choice) {
        if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
            var id = TypeId.from(materialChoice.getItemStack().getType());
            addIngredient(new ItemPair(id, 1));
        } else if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
            var id = AuroraAPI.getItemManager().resolveId(exactChoice.getItemStack());
            addIngredient(new ItemPair(id, 1));
        }
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        return Stream.of(context.getMatrix()).filter(i -> i != null && i.getType() != Material.AIR).min(Comparator.comparingInt(ItemStack::getAmount)).map(ItemStack::getAmount).orElse(0);
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        return Stream.of(context.getMatrix()).map(item -> {
            var clone = item.clone();
            if (BUCKET.contains(clone.getType())) {
                return clone.withType(Material.BUCKET);
            }
            clone.setAmount(Math.max(clone.getAmount() - timesCrafted, 0));
            return clone;
        }).toArray(ItemStack[]::new);
    }

    @Override
    public ItemStack getResultItem(BlueprintContext context) {
        if (backingRecipe.getKey().getNamespace().equals("minecraft") && backingRecipe.getKey().getKey().equals("armor_dye")) {
            return getDyeResult(context.getMatrix());
        } else if (backingRecipe.getResult().getType().name().endsWith("SHULKER_BOX")) {
            return getShulkerResult(context.getMatrix());
        } else if (backingRecipe.getResult().getType().name().endsWith("BUNDLE")) {
            return getBundleResult(context.getMatrix());
        }

        return resultItem.clone();
    }

    private ItemStack getBundleResult(ItemStack[] matrix) {
        ItemStack originalBundle = null;

        // Find the bundle in the matrix
        for (var item : matrix) {
            if (item.getType().name().endsWith("BUNDLE")) { // Check for bundles by name
                originalBundle = item.clone();
                originalBundle.setAmount(1);
            }
        }

        if (originalBundle == null) {
            AuroraCrafting.logger().warning("Failed to find bundle in matrix");
            return null;
        }

        var result = backingRecipe.getResult().clone();

        // Check if the result is a bundle (supports colored/custom bundles)
        if (result.getType().name().endsWith("BUNDLE") && result.getItemMeta() instanceof BundleMeta bundleMeta) {
            try {
                // Get the contents of the original bundle
                BundleMeta originalMeta = (BundleMeta) originalBundle.getItemMeta();
                if (originalMeta == null) {
                    AuroraCrafting.logger().warning("Failed to get bundle meta from original bundle");
                    return null;
                }

                // Set the same contents on the result bundle
                bundleMeta.setItems(originalMeta.getItems());
                result.setItemMeta(bundleMeta);
                return result;
            } catch (Exception e) {
                AuroraCrafting.logger().warning("Failed to set bundle contents: " + e.getMessage());
                return null;
            }
        }

        return null;
    }

    private ItemStack getShulkerResult(ItemStack[] matrix) {
        ItemStack originalShulker = null;

        for (var item : matrix) {
            if (item.getType().name().endsWith("SHULKER_BOX")) {
                originalShulker = item.clone();
                originalShulker.setAmount(1);
            }
        }

        if (originalShulker == null) {
            AuroraCrafting.logger().warning("Failed to find shulker box in matrix");
            return null;
        }

        var result = backingRecipe.getResult().clone();

        if (result.getItemMeta() instanceof BlockStateMeta blockStateMeta) {
            if (blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
                try {
                    shulkerBox.getInventory().clear();
                    shulkerBox.getInventory().setContents(
                            ((ShulkerBox) ((BlockStateMeta) originalShulker.getItemMeta()).getBlockState()).getInventory().getContents()
                    );
                    blockStateMeta.setBlockState(shulkerBox);
                    result.setItemMeta(blockStateMeta);
                    return result;
                } catch (Exception e) {
                    AuroraCrafting.logger().warning("Failed to set shulker box inventory");
                    return null;
                }
            }
        }

        return null;
    }

    private ItemStack getDyeResult(ItemStack[] matrix) {
        ItemStack armor = null;
        ItemStack dye = null;

        for (var item : matrix) {
            if (LEATHER_ARMOR.contains(item.getType())) {
                armor = item.clone();
                armor.setAmount(1);
            } else if (item.getType().name().endsWith("_DYE")) {
                dye = item;
            }
        }

        if (armor == null || dye == null) {
            AuroraCrafting.logger().warning("Failed to find armor or dye in matrix");
            return null;
        }

        var armorMeta = armor.getItemMeta();
        var dyeMeta = dye.getItemMeta();

        AuroraCrafting.logger().info(dyeMeta.getClass().getSimpleName());

        if (armorMeta instanceof LeatherArmorMeta leatherArmorMeta) {
            try {
                var color = DyeColor.valueOf(dye.getType().name().replace("_DYE", "")).getColor();
                leatherArmorMeta.setColor(color);
                armor.setItemMeta(leatherArmorMeta);
                return armor;
            } catch (Exception e) {
                AuroraCrafting.logger().warning("Failed to parse dye color for " + dye.getType().name());
                return null;
            }
        } else {
            return null;
        }
    }
}
