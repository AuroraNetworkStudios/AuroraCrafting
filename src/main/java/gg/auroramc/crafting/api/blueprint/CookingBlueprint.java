package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CookingBookCategory;

@Getter
public class CookingBlueprint extends Blueprint {
    private VanillaOptions vanillaOptions = VanillaOptions.builder().build();
    private Type type = Type.FURNACE;

    @Getter
    @AllArgsConstructor
    public static final class VanillaOptions {
        private CookingBookCategory category;
        private String group;
        private int cookingTime;
        private float experience;

        public static VanillaOptionsBuilder builder() {
            return new VanillaOptionsBuilder();
        }

        public static final class VanillaOptionsBuilder {
            private CookingBookCategory category = CookingBookCategory.MISC;
            private String group = null;
            private int cookingTime = 200;
            private float experience = 0;

            public VanillaOptionsBuilder category(CookingBookCategory category) {
                if (category != null) {
                    this.category = category;
                }
                return this;
            }

            public VanillaOptionsBuilder group(String group) {
                if (group != null) {
                    this.group = group;
                }
                return this;
            }

            public VanillaOptionsBuilder cookingTime(int cookingTime) {
                if (cookingTime > 0) {
                    this.cookingTime = cookingTime;
                }
                return this;
            }

            public VanillaOptionsBuilder experience(float experience) {
                if (experience >= 0) {
                    this.experience = experience;
                }
                return this;
            }

            public VanillaOptions build() {
                return new VanillaOptions(category, group, cookingTime, experience);
            }
        }
    }

    public enum Type {
        FURNACE,
        BLAST_FURNACE,
        SMOKER,
        CAMPFIRE
    }

    public CookingBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    public static CookingBlueprint cookingBlueprint(Workbench workbench, String id) {
        return new CookingBlueprint(workbench, id);
    }

    public CookingBlueprint vanillaOptions(VanillaOptions vanillaOptions) {
        this.vanillaOptions = vanillaOptions;
        return this;
    }

    public CookingBlueprint type(Type type) {
        this.type = type;
        return this;
    }

    public CookingBlueprint input(ItemPair input) {
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
        return 0;
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        return new ItemStack[0];
    }

    @Override
    public Blueprint complete() {
        super.complete();
        this.mergeOptionsEnabled = false;
        return this;
    }
}
