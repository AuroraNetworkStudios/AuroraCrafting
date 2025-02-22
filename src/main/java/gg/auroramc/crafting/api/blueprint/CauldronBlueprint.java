package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
public class CauldronBlueprint extends Blueprint {

    private VanillaOptions vanillaOptions = VanillaOptions.builder().build();
    private static final ItemPair air = new ItemPair(TypeId.from(Material.AIR), 0);
    private final boolean[] slots = new boolean[3];

    public static CauldronBlueprint cauldronBlueprint(Workbench workbench, String id) {
        return new CauldronBlueprint(workbench, id);
    }

    public CauldronBlueprint(Workbench workbench, String id) {
        super(workbench, id);
        this.ingredients.addAll(List.of(air, air, air));
        this.ingredientItems.addAll(List.of(new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)));
    }

    @Override
    public CauldronBlueprint addIngredient(ItemPair itemPair) {
        if (itemPair.id().equals(TypeId.from(Material.AIR))) {
            throw new IllegalArgumentException("Cauldron recipes cannot have air as an ingredient");
        }

        if (!this.ingredients.isEmpty()) {
            throw new IllegalArgumentException("Cauldron recipes can only have 1 ingredient");
        }

        this.ingredients.add(itemPair);
        return this;
    }

    public static enum Type {
        CAULDRON
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        int maxCraftable = Integer.MAX_VALUE;

        var matches = true;
        var items = context.getMatrix();

        for (int i = 0; i < items.length; i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i) : new ItemPair(TypeId.from(Material.AIR), 0);
            var item = items[i];
            var itemTypeId = item.isEmpty() ? TypeId.from(Material.AIR) : AuroraAPI.getItemManager().resolveId(item);
            if (!itemTypeId.equals(ingredient.id())) {
                matches = false;
                break;
            } else if (item.getAmount() < ingredient.amount()) {
                matches = false;
                break;
            } else if (!ingredient.id().id().equals("air")) {
                maxCraftable = Math.min(maxCraftable, Math.max(1, item.getAmount()) / Math.max(1, ingredient.amount()));
            }
        }

        if (!matches) return 0;

        return maxCraftable;
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        var items = new ItemStack[context.getMatrix().length];
        var currentMatrix = context.getMatrix();

        for (int i = 0; i < context.getMatrix().length; i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i) : new ItemPair(TypeId.from(Material.AIR), 0);
            var item = currentMatrix[i];
            if (item.getAmount() <= ingredient.amount() * timesCrafted) {
                items[i] = null;
            } else {
                var newItem = item.clone();
                newItem.setAmount(item.getAmount() - ingredient.amount() * timesCrafted);
                items[i] = newItem;
            }
        }

        return items;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static final class VanillaOptions {
        private ChoiceType choiceType;
        private float experience = 0.0F;
        private ItemPair input = new ItemPair(TypeId.from(Material.AIR), 0);


        private VanillaOptions experience(float experience) {
            this.experience = experience;
            return this;
        }


        private VanillaOptions(ChoiceType choiceType) {
            this.choiceType = choiceType;
        }

        public static VanillaOptionsBuilder builder() {
            return new VanillaOptionsBuilder();
        }

        public static class VanillaOptionsBuilder {
            private ChoiceType choiceType = ChoiceType.ITEM_TYPE;
            private float experience = 0.0F;
            private String input = "AIR";
            private int fluidLevel = 0;
            private String fluid = "WATER_CAULDRON";

            public VanillaOptionsBuilder choiceType(ChoiceType choiceType) {
                this.choiceType = choiceType;
                return this;
            }

            public VanillaOptionsBuilder experience(float experience) {
                this.experience = experience;
                return this;
            }

            public VanillaOptionsBuilder input(String input) {
                this.input = input;
                return this;
            }

            public VanillaOptionsBuilder fluidLevel(int fluidLevel) {
                this.fluidLevel = fluidLevel;
                return this;
            }

            public VanillaOptionsBuilder fluid(String fluid) {
                this.fluid = fluid;
                return this;
            }

            public VanillaOptions build() {
                return new VanillaOptions(choiceType);
            }
        }
    }

    public CauldronBlueprint vanillaOptions(VanillaOptions vanillaOptions) {
        this.vanillaOptions = vanillaOptions;
        return this;
    }
}