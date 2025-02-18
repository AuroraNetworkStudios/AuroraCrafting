package gg.auroramc.crafting.api.workbench;

import gg.auroramc.crafting.api.workbench.custom.CustomWorkbench;
import gg.auroramc.crafting.api.workbench.vanilla.*;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class WorkbenchRegistry {
    @Getter
    private boolean frozen = false;

    private final Map<String, CustomWorkbench> workbenches = new HashMap<>();

    private Map<VanillaWorkbench, Workbench> vanillaWorkbenches = vanillaWorkbenchInit();

    public void registerWorkbench(CustomWorkbench workbench) {
        if (frozen) throw new IllegalStateException("Cannot register workbench after freezing");
        workbench.validate();
        workbenches.put(workbench.getId(), workbench);
    }

    public @Nullable CustomWorkbench getWorkbench(String id) {
        return workbenches.get(id);
    }

    public CraftingTable getCraftingTable() {
        return (CraftingTable) vanillaWorkbenches.get(VanillaWorkbench.CRAFTING_TABLE);
    }

    public SmithingTable getSmithingTable() {
        return (SmithingTable) vanillaWorkbenches.get(VanillaWorkbench.SMITHING_TABLE);
    }

    public Furnace getFurnace() {
        return (Furnace) vanillaWorkbenches.get(VanillaWorkbench.FURNACE);
    }

    public BlastFurnace getBlastFurnace() {
        return (BlastFurnace) vanillaWorkbenches.get(VanillaWorkbench.BLAST_FURNACE);
    }

    public Smoker getSmoker() {
        return (Smoker) vanillaWorkbenches.get(VanillaWorkbench.SMOKER);
    }

    public Campfire getCampfire() {
        return (Campfire) vanillaWorkbenches.get(VanillaWorkbench.CAMPFIRE);
    }

    public Collection<CustomWorkbench> getCustomWorkbenches() {
        return workbenches.values();
    }

    public Collection<Workbench> getVanillaWorkbenches() {
        return vanillaWorkbenches.values();
    }

    public void freeze() {
        frozen = true;

        for (var workbench : workbenches.values()) {
            workbench.freeze();
        }

        for (var workbench : workbenches.values()) {
            workbench.freeze();
        }
    }

    public void unfreezeAndClear() {
        frozen = false;
        workbenches.clear();
        vanillaWorkbenches = vanillaWorkbenchInit();
    }

    private Map<VanillaWorkbench, Workbench> vanillaWorkbenchInit() {
        return Map.of(
                VanillaWorkbench.CRAFTING_TABLE, new CraftingTable(),
                VanillaWorkbench.SMITHING_TABLE, new SmithingTable(),
                VanillaWorkbench.FURNACE, new Furnace(),
                VanillaWorkbench.SMOKER, new Smoker(),
                VanillaWorkbench.BLAST_FURNACE, new BlastFurnace(),
                VanillaWorkbench.CAMPFIRE, new Campfire()
        );
    }
}
