package gg.auroramc.crafting.loader;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.*;
import gg.auroramc.crafting.api.workbench.vanilla.Cauldron;
import gg.auroramc.crafting.parser.BlueprintParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlueprintLoader {
    public static void loadBlueprints(AuroraCrafting plugin) {
        var duplicates = new HashMap<String, List<String>>();

        var manager = plugin.getConfigManager();
        var groups = new HashMap<String, BlueprintGroup>();

        for (var config : manager.getCustomRecipes()) {
            for (var recipe : config.getRecipes()) {
                if (duplicates.containsKey(recipe.getId())) {
                    duplicates.get(recipe.getId()).add(recipe.getSourcePath());
                    AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + ": Duplicate recipe ID, skipping... Source: " + recipe.getSourcePath() + " other sources: " + duplicates.get(recipe.getId()));
                    continue;
                }

                var workbench = plugin.getWorkbenchRegistry().getWorkbench(recipe.getWorkbench());
                if (workbench == null) {
                    AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + ": Workbench " + recipe.getWorkbench() + " not found");
                    continue;
                }
                try {
                    var blueprint = BlueprintParser.from(workbench, null, recipe.getId()).parse(recipe);
                    if (recipe.getShapeless()) {
                        workbench.addBlueprint(BlueprintType.SHAPELESS, blueprint);
                    } else {
                        workbench.addBlueprint(BlueprintType.SHAPED, blueprint);
                    }

                    if (recipe.getVanillaOptions().getGroup() != null) {
                        var group = groups.computeIfAbsent(recipe.getVanillaOptions().getGroup(), (k) -> new BlueprintGroup());
                        group.addBlueprint(blueprint);
                        blueprint.group(group);
                    }

                    duplicates.computeIfAbsent(recipe.getId(), k -> new ArrayList<>()).add(recipe.getSourcePath());
                } catch (Exception e) {
                    AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + " in source: " + recipe.getSourcePath() + ", reason: " + e.getMessage());
                }
            }
        }

        groups = new HashMap<>();

        for (var config : manager.getCraftingTableRecipes()) {
            for (var recipe : config.getRecipes()) {
                if (duplicates.containsKey(recipe.getId())) {
                    duplicates.get(recipe.getId()).add(recipe.getSourcePath());
                    AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + ": Duplicate recipe ID, skipping... Source: " + recipe.getSourcePath() + " other sources: " + duplicates.get(recipe.getId()));
                    continue;
                }

                var workbench = plugin.getWorkbenchRegistry().getCraftingTable();
                try {
                    var blueprint = BlueprintParser.from(workbench, null, recipe.getId()).parse(recipe);
                    if (recipe.getShapeless()) {
                        workbench.addBlueprint(BlueprintType.SHAPELESS, blueprint);
                    } else {
                        workbench.addBlueprint(BlueprintType.SHAPED, blueprint);
                    }

                    if (recipe.getVanillaOptions().getGroup() != null) {
                        var group = groups.computeIfAbsent(recipe.getVanillaOptions().getGroup(), (k) -> new BlueprintGroup());
                        group.addBlueprint(blueprint);
                        blueprint.group(group);
                    }

                    duplicates.computeIfAbsent(recipe.getId(), k -> new ArrayList<>()).add(recipe.getSourcePath());
                } catch (Exception e) {
                    AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + " in source: " + recipe.getSourcePath() + ", reason: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        groups = new HashMap<>();

        for (var recipe : manager.getSmithingRecipes()) {
            if (duplicates.containsKey(recipe.getId())) {
                duplicates.get(recipe.getId()).add(recipe.getSourcePath());
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + ": Duplicate recipe ID, skipping... Source: " + recipe.getSourcePath() + " other sources: " + duplicates.get(recipe.getId()));
                continue;
            }
            var workbench = plugin.getWorkbenchRegistry().getSmithingTable();
            try {
                var blueprint = BlueprintParser.from(workbench, null, recipe.getId()).parse(recipe);
                workbench.addBlueprint(BlueprintType.SMITHING, blueprint);
                if (recipe.getVanillaOptions().getGroup() != null) {
                    var group = groups.computeIfAbsent(recipe.getVanillaOptions().getGroup(), (k) -> new BlueprintGroup());
                    group.addBlueprint(blueprint);
                    blueprint.group(group);
                }
                duplicates.computeIfAbsent(recipe.getId(), k -> new ArrayList<>()).add(recipe.getSourcePath());
            } catch (Exception e) {
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + " in source: " + recipe.getSourcePath() + ", reason: " + e.getMessage());
            }
        }

        groups = new HashMap<>();

        for (var recipe : manager.getFurnaceRecipes()) {
            if (duplicates.containsKey(recipe.getId())) {
                duplicates.get(recipe.getId()).add(recipe.getSourcePath());
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + ": Duplicate recipe ID, skipping... Source: " + recipe.getSourcePath() + " other sources: " + duplicates.get(recipe.getId()));
                continue;
            }
            var workbench = plugin.getWorkbenchRegistry().getFurnace();
            try {
                var blueprint = BlueprintParser.from(workbench, null, recipe.getId()).parse(recipe, CookingBlueprint.Type.FURNACE);
                workbench.addBlueprint(BlueprintType.FURNACE, blueprint);
                if (recipe.getGroup() != null) {
                    var group = groups.computeIfAbsent(recipe.getGroup(), (k) -> new BlueprintGroup());
                    group.addBlueprint(blueprint);
                    blueprint.group(group);
                }
                duplicates.computeIfAbsent(recipe.getId(), k -> new ArrayList<>()).add(recipe.getSourcePath());
            } catch (Exception e) {
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + " in source: " + recipe.getSourcePath() + ", reason: " + e.getMessage());
            }
        }

        groups = new HashMap<>();

        for (var recipe : manager.getCauldronRecipes()) {
            if (duplicates.containsKey(recipe.getId())) {
                duplicates.get(recipe.getId()).add(recipe.getSourcepath());
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + ": Duplicate recipe ID, skipping... Source: " + recipe.getSourcepath() + " other sources: " + duplicates.get(recipe.getId()));
                continue;
            }

            Cauldron workbench = plugin.getWorkbenchRegistry().getCauldron();
            try {
                Blueprint blueprint = BlueprintParser.from(workbench, null, recipe.getId()).parse(recipe);
                workbench.addBlueprint(BlueprintType.CAULDRON, blueprint);
                if (recipe.getGroup() != null) {
                    var group = groups.computeIfAbsent(recipe.getGroup(), (k) -> new BlueprintGroup());
                    group.addBlueprint(blueprint);
                    blueprint.group(group);
                }
                duplicates.computeIfAbsent(recipe.getId(), k -> new ArrayList<>());
            } catch (Exception e) {
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + " in source: " + recipe.getSourcepath() + ", reason:" + e.getMessage());
            }
        }

        groups = new HashMap<>();

        for (var recipe : manager.getBlastingRecipes()) {
            if (duplicates.containsKey(recipe.getId())) {
                duplicates.get(recipe.getId()).add(recipe.getSourcePath());
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + ": Duplicate recipe ID, skipping... Source: " + recipe.getSourcePath() + " other sources: " + duplicates.get(recipe.getId()));
                continue;
            }
            var workbench = plugin.getWorkbenchRegistry().getBlastFurnace();
            try {
                var blueprint = BlueprintParser.from(workbench, null, recipe.getId()).parse(recipe, CookingBlueprint.Type.BLAST_FURNACE);
                workbench.addBlueprint(BlueprintType.BLASTING, blueprint);
                if (recipe.getGroup() != null) {
                    var group = groups.computeIfAbsent(recipe.getGroup(), (k) -> new BlueprintGroup());
                    group.addBlueprint(blueprint);
                    blueprint.group(group);
                }
                duplicates.computeIfAbsent(recipe.getId(), k -> new ArrayList<>()).add(recipe.getSourcePath());
            } catch (Exception e) {
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + " in source: " + recipe.getSourcePath() + ", reason: " + e.getMessage());
            }
        }

        groups = new HashMap<>();

        for (var recipe : manager.getSmokingRecipes()) {
            if (duplicates.containsKey(recipe.getId())) {
                duplicates.get(recipe.getId()).add(recipe.getSourcePath());
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + ": Duplicate recipe ID, skipping... Source: " + recipe.getSourcePath() + " other sources: " + duplicates.get(recipe.getId()));
                continue;
            }
            var workbench = plugin.getWorkbenchRegistry().getSmoker();
            try {
                var blueprint = BlueprintParser.from(workbench, null, recipe.getId()).parse(recipe, CookingBlueprint.Type.SMOKER);
                workbench.addBlueprint(BlueprintType.SMOKER, blueprint);
                if (recipe.getGroup() != null) {
                    var group = groups.computeIfAbsent(recipe.getGroup(), (k) -> new BlueprintGroup());
                    group.addBlueprint(blueprint);
                    blueprint.group(group);
                }
                duplicates.computeIfAbsent(recipe.getId(), k -> new ArrayList<>()).add(recipe.getSourcePath());
            } catch (Exception e) {
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + " in source: " + recipe.getSourcePath() + ", reason: " + e.getMessage());
            }
        }

        groups = new HashMap<>();

        for (var recipe : manager.getCampfireRecipes()) {
            if (duplicates.containsKey(recipe.getId())) {
                duplicates.get(recipe.getId()).add(recipe.getSourcePath());
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + ": Duplicate recipe ID, skipping... Source: " + recipe.getSourcePath() + " other sources: " + duplicates.get(recipe.getId()));
                continue;
            }
            var workbench = plugin.getWorkbenchRegistry().getCampfire();
            try {
                var blueprint = BlueprintParser.from(workbench, null, recipe.getId()).parse(recipe, CookingBlueprint.Type.CAMPFIRE);
                workbench.addBlueprint(BlueprintType.CAMPFIRE, blueprint);
                if (recipe.getGroup() != null) {
                    var group = groups.computeIfAbsent(recipe.getGroup(), (k) -> new BlueprintGroup());
                    group.addBlueprint(blueprint);
                    blueprint.group(group);
                }
                duplicates.computeIfAbsent(recipe.getId(), k -> new ArrayList<>()).add(recipe.getSourcePath());
            } catch (Exception e) {
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + " in source: " + recipe.getSourcePath() + ", reason: " + e.getMessage());
            }
        }

        groups = new HashMap<>();

        for (var recipe : manager.getStoneCutterRecipes()) {
            if (duplicates.containsKey(recipe.getId())) {
                duplicates.get(recipe.getId()).add(recipe.getSourcePath());
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + ": Duplicate recipe ID, skipping... Source: " + recipe.getSourcePath() + " other sources: " + duplicates.get(recipe.getId()));
                continue;
            }
            var workbench = plugin.getWorkbenchRegistry().getStoneCutter();
            try {
                var blueprint = BlueprintParser.from(workbench, null, recipe.getId()).parse(recipe);
                workbench.addBlueprint(BlueprintType.STONE_CUTTER, blueprint);
                if (recipe.getVanillaOptions().getGroup() != null) {
                    var group = groups.computeIfAbsent(recipe.getVanillaOptions().getGroup(), (k) -> new BlueprintGroup());
                    group.addBlueprint(blueprint);
                    blueprint.group(group);
                }
                duplicates.computeIfAbsent(recipe.getId(), k -> new ArrayList<>()).add(recipe.getSourcePath());
            } catch (Exception e) {
                AuroraCrafting.logger().severe("Failed to load blueprint " + recipe.getId() + " in source: " + recipe.getSourcePath() + ", reason: " + e.getMessage());
            }
        }
    }
}
