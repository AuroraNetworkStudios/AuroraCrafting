package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class StoneCutterBlueprint extends Blueprint {
    private VanillaOptions vanillaOptions = VanillaOptions.builder().build();

    public static StoneCutterBlueprint stoneCutterBlueprint(Workbench workbench, String id) {
        return new StoneCutterBlueprint(workbench, id);
    }

    @Getter
    @AllArgsConstructor
    public static final class VanillaOptions {
        private String group;

        public static VanillaOptions.VanillaOptionsBuilder builder() {
            return new VanillaOptions.VanillaOptionsBuilder();
        }

        public static final class VanillaOptionsBuilder {
            private String group = null;


            public VanillaOptions.VanillaOptionsBuilder group(String group) {
                if (group != null) {
                    this.group = group;
                }
                return this;
            }

            public VanillaOptions build() {
                return new VanillaOptions(group);
            }
        }
    }

    public StoneCutterBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    public StoneCutterBlueprint vanillaOptions(VanillaOptions vanillaOptions) {
        this.vanillaOptions = vanillaOptions;
        return this;
    }

    public StoneCutterBlueprint input(ItemPair input) {
        if (!this.ingredients.isEmpty()) {
            throw new IllegalStateException("Input already set");
        }
        this.addIngredient(input);
        return this;
    }

    public ItemStack getInputItem() {
        return this.ingredientItems.getFirst();
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        // Not applicable, since stonecutter recipes handled purely by vanilla
        return 0;
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        // Not applicable, since stonecutter recipes handled purely by vanilla
        return new ItemStack[0];
    }
}
