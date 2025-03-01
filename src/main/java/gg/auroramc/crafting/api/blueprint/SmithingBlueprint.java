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

public class SmithingBlueprint extends Blueprint {
    @Getter
    private VanillaOptions vanillaOptions = VanillaOptions.builder().build();

    private static final Ingredient air = new Ingredient(new ItemPair(TypeId.from(Material.AIR), 0));

    private final boolean[] slots = new boolean[3];

    public SmithingBlueprint(Workbench workbench, String id) {
        super(workbench, id);
        this.ingredients.addAll(List.of(air, air, air));
        this.ingredientItems.addAll(List.of(new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)));
    }

    public static SmithingBlueprint smithingBlueprint(Workbench workbench, String id) {
        return new SmithingBlueprint(workbench, id);
    }

    @Override
    public SmithingBlueprint addIngredient(ItemPair itemPair) {
        if (slots[0] && slots[1] && slots[2]) {
            throw new IllegalArgumentException("Smithing recipes can only have 3 ingredients");
        }

        var item = AuroraAPI.getItemManager().resolveItem(itemPair.id());
        item.setAmount(itemPair.amount());

        var index = 0;

        if (!slots[0]) {
            index = 0;
        } else if (!slots[1]) {
            index = 1;
        } else if (!slots[2]) {
            index = 2;
        }

        slots[index] = true;
        ingredients.set(index, new Ingredient(itemPair));
        ingredientItems.set(index, item);
        this.ingredientCount.merge(itemPair.id(), itemPair.amount(), Integer::sum);
        if (itemPair.id().equals(TypeId.from(Material.AIR))) {
            this.ingredientCount.remove(itemPair.id());
        }

        return this;
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        int maxCraftable = Integer.MAX_VALUE;

        var matches = true;
        var items = context.getMatrix();

        for (int i = 0; i < items.length; i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i).getItemPair() : BlueprintContext.AIR;
            var item = items[i];
            var itemTypeId = item.isEmpty() ? TypeId.from(Material.AIR) : context.getIdMatrix()[i].id();
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
            var ingredient = ingredients.size() > i ? ingredients.get(i).getItemPair() : BlueprintContext.AIR;
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

    public Ingredient getTemplate() {
        return ingredients.getFirst();
    }

    public SmithingBlueprint template(ItemPair itemPair) {
        var item = AuroraAPI.getItemManager().resolveItem(itemPair.id());
        item.setAmount(itemPair.amount());

        ingredients.set(0, new Ingredient(itemPair));
        ingredientItems.set(0, item);
        return this;
    }

    public ItemStack getTemplateItem() {
        return ingredientItems.getFirst();
    }

    public Ingredient getBase() {
        return ingredients.get(1);
    }

    public SmithingBlueprint base(ItemPair itemPair) {
        var item = AuroraAPI.getItemManager().resolveItem(itemPair.id());
        item.setAmount(itemPair.amount());

        ingredients.set(1, new Ingredient(itemPair));
        ingredientItems.set(1, item);
        return this;
    }

    public ItemStack getBaseItem() {
        return ingredientItems.get(1);
    }

    public Ingredient getAddition() {
        return ingredients.getLast();
    }

    public SmithingBlueprint addition(ItemPair itemPair) {
        var item = AuroraAPI.getItemManager().resolveItem(itemPair.id());
        item.setAmount(itemPair.amount());

        ingredients.set(2, new Ingredient(itemPair));
        ingredientItems.set(2, item);
        return this;
    }

    public ItemStack getAdditionItem() {
        return ingredientItems.getLast();
    }


    @Getter
    @Setter
    @AllArgsConstructor
    public static final class VanillaOptions {
        private ChoiceType choiceType;

        public static VanillaOptions.VanillaOptionsBuilder builder() {
            return new VanillaOptions.VanillaOptionsBuilder();
        }

        public static class VanillaOptionsBuilder {
            private ChoiceType choiceType = ChoiceType.ITEM_TYPE;

            public VanillaOptions.VanillaOptionsBuilder choiceType(ChoiceType choiceType) {
                if (choiceType != null) {
                    this.choiceType = choiceType;
                }
                return this;
            }

            public VanillaOptions build() {
                return new VanillaOptions(choiceType);
            }
        }
    }

    public SmithingBlueprint vanillaOptions(VanillaOptions vanillaOptions) {
        this.vanillaOptions = vanillaOptions;
        return this;
    }
}
