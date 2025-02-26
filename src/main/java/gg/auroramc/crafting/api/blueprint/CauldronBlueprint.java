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

@Getter
public class CauldronBlueprint extends Blueprint {

    private VanillaOptions vanillaOptions = VanillaOptions.builder().build();

    public static CauldronBlueprint cauldronBlueprint(Workbench workbench, String id) {
        return new CauldronBlueprint(workbench, id);
    }

    public CauldronBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    @Override
    public CauldronBlueprint addIngredient(ItemPair itemPair) {
        if (itemPair.id().equals(TypeId.from(Material.AIR))) {
            throw new IllegalArgumentException("Cauldron recipes cannot have air as an ingredient");
        }

        if (!this.ingredients.isEmpty()) {
            throw new IllegalArgumentException("Cauldron recipes can only have 1 ingredient");
        }

        this.ingredients.add(new Ingredient(itemPair));
        this.ingredientItems.add(AuroraAPI.getItemManager().resolveItem(itemPair.id()));
        return this;
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        if (ingredients.isEmpty()) return 0;

        ItemPair ingredient = ingredients.getFirst().getItemPair();
        ItemStack item = context.getMatrix()[0];
        TypeId itemTypeId = item.isEmpty() ? TypeId.from(Material.AIR) : AuroraAPI.getItemManager().resolveId(item);

        if (!itemTypeId.equals(ingredient.id()) || item.getAmount() < ingredient.amount()) {
            return 0;
        }

        return ingredient.id().id().equals("air") ? Integer.MAX_VALUE
                : item.getAmount() / Math.max(1, ingredient.amount());
    }


    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        var items = new ItemStack[context.getMatrix().length];
        var currentMatrix = context.getMatrix();

        for (int i = 0; i < context.getMatrix().length; i++) {
            ItemPair ingredient = ingredients.size() > i ? ingredients.get(i).getItemPair() : new ItemPair(TypeId.from(Material.AIR), 0);
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
        private float experience = 0.0F;
        private int fluidLevel = 0;
        private String fluid = "WATER_CAULDRON";


        private VanillaOptions experience(float experience) {
            this.experience = experience;
            return this;
        }

        public static VanillaOptionsBuilder builder() {
            return new VanillaOptionsBuilder();
        }

        public static class VanillaOptionsBuilder {
            private float experience = 0.0F;
            private int fluidLevel = 0;
            private String fluid = "WATER_CAULDRON";

            public VanillaOptionsBuilder experience(float experience) {
                this.experience = experience;
                return this;
            }

            public VanillaOptionsBuilder fluidLevel(int fluidLevel) {
                if(fluid == null) {
                    throw new IllegalArgumentException("Fluid cannot be null when setting fluid level");
                }

                if("LAVA_CAULDRON".equals(fluid) && fluidLevel != 1) {
                    throw new IllegalArgumentException("Lava cauldrons can only have a fluid level of 1");
                }

                this.fluidLevel = fluidLevel;
                return this;
            }

            public VanillaOptionsBuilder fluid(String fluid) {
                this.fluid = fluid;
                return this;
            }

            public VanillaOptions build() {
                return new VanillaOptions(experience, fluidLevel, fluid);
            }
        }
    }

    public CauldronBlueprint vanillaOptions(VanillaOptions vanillaOptions) {
        this.vanillaOptions = vanillaOptions;
        return this;
    }
}