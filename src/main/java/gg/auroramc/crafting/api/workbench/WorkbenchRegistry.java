package gg.auroramc.crafting.api.workbench;

import gg.auroramc.crafting.api.workbench.custom.CustomWorkbench;
import gg.auroramc.crafting.api.workbench.vanilla.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class WorkbenchRegistry {
    @Getter
    private boolean frozen = false;

    private final Map<String, CustomWorkbench> workbenches = new HashMap<>();

    private Map<VanillaType, VanillaWorkbench<?>> vanillaWorkbenches = vanillaWorkbenchInit();

    public void registerWorkbench(CustomWorkbench workbench) {
        if (frozen) throw new IllegalStateException("Cannot register workbench after freezing");
        workbench.validate();
        workbenches.put(workbench.getId(), workbench);
    }

    public @Nullable CustomWorkbench getWorkbench(String id) {
        return workbenches.get(id);
    }

    public CraftingTable getCraftingTable() {
        return (CraftingTable) vanillaWorkbenches.get(VanillaType.CRAFTING_TABLE);
    }

    public SmithingTable getSmithingTable() {
        return (SmithingTable) vanillaWorkbenches.get(VanillaType.SMITHING_TABLE);
    }

    public Furnace getFurnace() {
        return (Furnace) vanillaWorkbenches.get(VanillaType.FURNACE);
    }

    public BlastFurnace getBlastFurnace() {
        return (BlastFurnace) vanillaWorkbenches.get(VanillaType.BLAST_FURNACE);
    }

    public Smoker getSmoker() {
        return (Smoker) vanillaWorkbenches.get(VanillaType.SMOKER);
    }

    public Campfire getCampfire() {
        return (Campfire) vanillaWorkbenches.get(VanillaType.CAMPFIRE);
    }

    public StoneCutter getStoneCutter() {
        return (StoneCutter) vanillaWorkbenches.get(VanillaType.STONE_CUTTER);
    }

    public Collection<CustomWorkbench> getCustomWorkbenches() {
        return workbenches.values();
    }

    public Cauldron getCauldron() { return (Cauldron) vanillaWorkbenches.get(VanillaType.CAULDRON); }

    public Collection<VanillaWorkbench<?>> getVanillaWorkbenches() {
        return vanillaWorkbenches.values();
    }

    public void freeze() {
        frozen = true;

        for (var workbench : workbenches.values()) {
            workbench.freeze();
        }

        for (var workbench : vanillaWorkbenches.values()) {
            workbench.freeze();
        }

        Bukkit.updateRecipes();
    }

    public void unfreezeAndClear() {
        frozen = false;
        workbenches.clear();

        for (var workbench : vanillaWorkbenches.values()) {
            workbench.dispose();
        }

        vanillaWorkbenches = vanillaWorkbenchInit();
    }

    private Map<VanillaType, VanillaWorkbench<?>> vanillaWorkbenchInit() {
        return Map.of(
                VanillaType.CRAFTING_TABLE, new CraftingTable(),
                VanillaType.SMITHING_TABLE, new SmithingTable(),
                VanillaType.FURNACE, new Furnace(),
                VanillaType.SMOKER, new Smoker(),
                VanillaType.BLAST_FURNACE, new BlastFurnace(),
                VanillaType.CAMPFIRE, new Campfire(),
                VanillaType.CAULDRON, new Cauldron(),
                VanillaType.STONE_CUTTER, new StoneCutter()
        );
    }
}
