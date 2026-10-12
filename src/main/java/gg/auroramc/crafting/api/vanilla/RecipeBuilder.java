package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;

public abstract class RecipeBuilder<T extends RecipeBuilder<T, R>, R extends Recipe> {
    private static final TypeId AIR = TypeId.from(Material.AIR);

    protected final NamespacedKey key;
    protected ItemStack result;

    public RecipeBuilder(String id) {
        this.key = new NamespacedKey("aurora", id);
    }

    public RecipeBuilder<T, R> result(ItemStack result) {
        this.result = result;
        return this;
    }

    public abstract R build();

    protected static boolean isEmpty(TypeId itemID) {
        return itemID == null || itemID.equals(AIR);
    }

    protected RecipeChoice predicateChoiceFor(TypeId itemID, ItemStack preview) {
        return isEmpty(itemID)
                ? RecipeChoice.empty()
                : RecipeChoice.predicateChoice(
                i -> AuroraAPI.getItemManager().resolveId(i).equals(itemID),
                preview == null || preview.isEmpty() ? AuroraAPI.getItemManager().resolveItem(itemID) : preview);
    }

    protected RecipeChoice predicateChoiceFor(TypeId itemID) {
        return predicateChoiceFor(itemID, null);
    }
}
