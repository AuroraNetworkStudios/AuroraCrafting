package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlueprintLookupGenerator {
    /**
     * Generates a matrix lookup key for a blueprint
     *
     * @param blueprint the blueprint to generate the lookup key for
     * @return the generated key
     */
    public static String toKey(Blueprint blueprint) {
        return switch (blueprint) {
            case ShapedBlueprint shapedBlueprint -> toKey(shapedBlueprint);
            case ShapelessBlueprint shapelessBlueprint -> toKey(shapelessBlueprint);
            case CookingBlueprint cookingBlueprint -> toKey(cookingBlueprint);
            case SmithingBlueprint smithingBlueprint -> toKey(smithingBlueprint);
            case CauldronBlueprint cauldronBlueprint -> toKey(cauldronBlueprint);
            case StoneCutterBlueprint stoneCutterBlueprint -> toKey(stoneCutterBlueprint);
            case null, default ->
                    throw new IllegalArgumentException("Unknown blueprint type: " + blueprint.getClass().getSimpleName());
        };
    }

    /**
     * Generates a matrix lookup key for a shaped blueprint
     *
     * @param blueprint the shaped blueprint to generate the lookup key for
     * @return the generated key
     */
    public static String toKey(ShapedBlueprint blueprint) {
        var key = new StringBuilder();

        for (var ingredient : blueprint.getIngredients()) {
            key.append(ingredient.getItemPair().id().toString());
            key.append(";");
        }

        return key.toString();
    }

    public static String toKey(CauldronBlueprint blueprint) {
        return blueprint.getIngredients().getFirst().getItemPair().id().toString() + ";";
    }

    public static String toKey(SmithingBlueprint blueprint) {
        var key = new StringBuilder();

        for (var ingredient : blueprint.getIngredients()) {
            key.append(ingredient.getItemPair().id().toString());
            key.append(";");
        }

        return key.toString();
    }

    public static String toKey(CookingBlueprint cookingBlueprint) {
        return cookingBlueprint.getIngredients().getFirst().getItemPair().id().toString() + ";";
    }

    public static String toKey(StoneCutterBlueprint stoneCutterBlueprint) {
        return stoneCutterBlueprint.getIngredients().getFirst().getItemPair().id().toString() + ";";
    }

    /**
     * Generates a matrix lookup key for a shapeless blueprint
     *
     * @param blueprint the shapeless blueprint to generate the lookup key for
     * @return the generated key
     */
    public static String toKey(ShapelessBlueprint blueprint) {
        var key = new StringBuilder();
        var ingredients = new ArrayList<>(blueprint.getIngredients());
        ingredients.sort(Comparator.comparing(a -> a.getItemPair().id().toString()));

        for (var ingredient : ingredients) {
            key.append(ingredient.getItemPair().id().toString());
            key.append(";");
        }

        return key.toString();
    }

    /**
     * Generates a matrix lookup key from the given crafting matrix
     * that can be passed to the blueprint registry to get the corresponding
     * ShapedBlueprint
     *
     * @param matrix the crafting matrix to generate the key for
     * @return the generated key
     */
    public static String toShapedKey(ItemStack[] matrix) {
        var key = new StringBuilder(matrix.length);
        for (var ingredient : matrix) {
            key.append(AuroraAPI.getItemManager().resolveId(ingredient));
            key.append(";");
        }
        return key.toString();
    }

    /**
     * Generates a matrix lookup key from the given crafting matrix
     * that can be passed to the blueprint registry to get the corresponding
     * ShapelessBlueprint
     *
     * @param matrix the crafting matrix to generate the key for
     * @return the generated key
     */
    public static String toShapelessKey(ItemStack[] matrix) {
        var idList = new ArrayList<TypeId>(matrix.length);

        for (var ingredient : matrix) {
            idList.add(AuroraAPI.getItemManager().resolveId(ingredient));
        }

        idList.sort(Comparator.comparing(TypeId::toString));

        var key = new StringBuilder();

        for (var id : idList) {
            key.append(id.toString());
            key.append(";");
        }

        return key.toString();
    }

    public static String toShapedKey(ItemPair[] matrix) {
        var key = new StringBuilder(matrix.length);
        for (var ingredient : matrix) {
            key.append(ingredient.id().toString());
            key.append(";");
        }
        return key.toString();
    }

    public static String toShapedKey(List<ItemPair> ingredients) {
        var key = new StringBuilder(ingredients.size());
        for (var ingredient : ingredients) {
            key.append(ingredient.id().toString());
            key.append(";");
        }
        return key.toString();
    }

    public static String toShapelessKey(ItemPair[] matrix) {
        var idList = new ArrayList<TypeId>(matrix.length);

        for (var ingredient : matrix) {
            idList.add(ingredient.id());
        }

        idList.sort(Comparator.comparing(TypeId::toString));

        var key = new StringBuilder();

        for (var id : idList) {
            key.append(id.toString());
            key.append(";");
        }

        return key.toString();
    }


    public static List<String> toShapedVariations(ShapedBlueprint blueprint) {
        return new ArrayList<>(blueprint.getVariations().keySet());
    }
}
