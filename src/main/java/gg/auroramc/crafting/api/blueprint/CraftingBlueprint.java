package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.Getter;
import org.bukkit.inventory.recipe.CraftingBookCategory;

@Getter
public abstract class CraftingBlueprint<T extends CraftingBlueprint<T>> extends Blueprint {
    private VanillaOptions vanillaOptions = new VanillaOptions(CraftingBookCategory.MISC, null);

    public record VanillaOptions(CraftingBookCategory category, String group) {
    }

    @SuppressWarnings("unchecked")
    public T vanillaOptions(VanillaOptions vanillaOptions) {
        this.vanillaOptions = vanillaOptions;
        return (T) this;
    }


    public CraftingBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }
}
