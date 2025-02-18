package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.recipe.CraftingBookCategory;

@Getter
public abstract class CraftingBlueprint<T extends CraftingBlueprint<T>> extends Blueprint {
    private VanillaOptions vanillaOptions = VanillaOptions.builder().build();

    @Getter
    @Setter
    @AllArgsConstructor
    public static final class VanillaOptions {
        private CraftingBookCategory category;
        private ChoiceType choiceType;
        private String group;

        public static VanillaOptionsBuilder builder() {
            return new VanillaOptionsBuilder();
        }

        public static class VanillaOptionsBuilder {
            private CraftingBookCategory category = CraftingBookCategory.MISC;
            private ChoiceType choiceType = ChoiceType.ITEM_TYPE;
            private String group = null;

            public VanillaOptionsBuilder category(CraftingBookCategory category) {
                if (category != null) {
                    this.category = category;
                }
                return this;
            }

            public VanillaOptionsBuilder choiceType(ChoiceType choiceType) {
                if (choiceType != null) {
                    this.choiceType = choiceType;
                }
                return this;
            }

            public VanillaOptionsBuilder group(String group) {
                if (group != null) {
                    this.group = group;
                }
                return this;
            }

            public VanillaOptions build() {
                return new VanillaOptions(category, choiceType, group);
            }
        }
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
