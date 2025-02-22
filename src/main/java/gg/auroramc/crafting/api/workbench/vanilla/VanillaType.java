package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.BlueprintType;
import lombok.Getter;

@Getter
public enum VanillaType {
    CRAFTING_TABLE(BlueprintType.SHAPELESS, BlueprintType.SHAPED),
    SMITHING_TABLE(BlueprintType.SMITHING),
    FURNACE(BlueprintType.FURNACE),
    SMOKER(BlueprintType.SMOKER),
    BLAST_FURNACE(BlueprintType.BLASTING),
    CAMPFIRE(BlueprintType.CAMPFIRE),
    STONE_CUTTER(BlueprintType.STONE_CUTTER),
    BREWING_STAND(BlueprintType.BREWING),
    CAULDRON(BlueprintType.CAULDRON);

    private final BlueprintType[] blueprintTypes;

    VanillaType(BlueprintType... types) {
        this.blueprintTypes = types;
    }

    public BlueprintType getBlueprintType() {
        return this.blueprintTypes[0];
    }
}
