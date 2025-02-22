package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.blueprint.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmithingTable extends VanillaWorkbench<SmithingBlueprint> {
    private final Map<String, Blueprint> vanillaLookup = new HashMap<>();

    public SmithingTable() {
        super("vanilla-smithing-table", 3, List.of(0, 1, 2), VanillaType.SMITHING_TABLE);
    }

    @Override
    public void addBlueprint(BlueprintType type, Blueprint blueprint) {
        super.addBlueprint(type, blueprint);
        if (blueprint instanceof SmithingBlueprint smithingBlueprint && smithingBlueprint.getVanillaOptions().getChoiceType() == ChoiceType.ITEM_TYPE) {
            vanillaLookup.put(
                    BlueprintLookupGenerator.toShapedKey(blueprint.getIngredientItems().stream().map(i -> new ItemPair(TypeId.from(i.getType()), 1)).toArray(ItemPair[]::new)),
                    blueprint
            );
        }
    }

    public boolean matchesRegisteredVanillaRecipe(BlueprintContext context) {
        return vanillaLookup.containsKey(context.getShapedLookupKey());
    }
}
