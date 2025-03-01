package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.menu.MenuItem;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.CauldronBlueprint;
import gg.auroramc.crafting.api.workbench.custom.CustomWorkbench;
import gg.auroramc.crafting.api.workbench.vanilla.VanillaWorkbench;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class BlueprintMenu {
    private static final NamespacedId key = NamespacedId.of("auroracrafting", "blueprint_view");

    private final AuroraCrafting plugin;
    private final Player player;
    private final Blueprint blueprint;
    private final Runnable backAction;
    private Integer groupIndex = 0;

    public static BlueprintMenu blueprintMenu(AuroraCrafting plugin, Player player, Blueprint recipe, Runnable backAction) {
        return new BlueprintMenu(plugin, player, recipe, backAction);
    }

    public void open() {
        if (AuroraCrafting.isLoading()) return;
        var workbench = blueprint.getWorkbench();

        if (workbench instanceof CustomWorkbench customWorkbench) {
            open(customWorkbench);
        } else if (workbench instanceof VanillaWorkbench<?> vanillaWorkbench) {
            switch (vanillaWorkbench.getType()) {
                case FURNACE -> openFurnace();
                case BLAST_FURNACE -> openBlastFurnace();
                case SMOKER -> openSmoker();
                case CAMPFIRE -> openCampfire();
                case SMITHING_TABLE -> openSmithingTable();
                case CRAFTING_TABLE -> openCraftingTable();
                case STONE_CUTTER -> openStoneCutter();
                case CAULDRON -> openCauldron();
            }
        }
    }

    private void openStoneCutter() {
        var config = plugin.getConfigManager().getStoneCutterRecipeViewConfig();
        var wb = plugin.getConfigManager().getWorkbenchDefaultConfig();

        var menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, key);
        menu.addFiller(ItemBuilder.of(config.getItems().getFiller()).toItemStack(player));

        if (backAction != null) {
            menu.addItem(ItemBuilder.of(config.getItems().getBack()).build(player), (e) -> {
                backAction.run();
            });
        }

        menu.addItem(ItemBuilder.item(blueprint.getResultItem()).slot(config.getSlots().getResult()).build(player));

        var input = ItemBuilder.item(blueprint.getIngredientItems().getFirst()).slot(config.getSlots().getInput()).build(player);
        var recipe = plugin.getBlueprintRegistry().getBlueprintFor(blueprint.getIngredients().getFirst().getItemPair().id());

        if (recipe != null) {
            menu.addItem(input, (e) -> {
                BlueprintMenu.blueprintMenu(plugin, player, recipe, () -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                    m.groupIndex = groupIndex;
                    m.open();
                }).open();
            });
        } else {
            menu.addItem(input);
        }

        var group = blueprint.getGroup();

        if (group != null && group.getBlueprints().size() > 1 && config.getSlots().getNextRecipe() != null && config.getSlots().getPrevRecipe() != null) {
            var next = group.getBlueprints().size() > groupIndex + 1 ? group.getBlueprints().get(groupIndex + 1) : null;
            var prev = groupIndex > 0 ? group.getBlueprints().get(groupIndex - 1) : null;

            if (next != null) {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, next, this.backAction);
                    m.groupIndex = groupIndex + 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player));
            }

            if (prev != null) {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipe()).slot(config.getSlots().getPrevRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, prev, this.backAction);
                    m.groupIndex = groupIndex - 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipeItem()).slot(config.getSlots().getPrevRecipe()).build(player));
            }
        }

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).build(player));
        }

        menu.open();
    }

    private void openCauldron() {
        var config = plugin.getConfigManager().getCauldronRecipeViewConfig();
        var wb = plugin.getConfigManager().getWorkbenchDefaultConfig();

        AuroraMenu menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, key);
        menu.addFiller(ItemBuilder.of(config.getItems().getFiller()).toItemStack(player));

        if (backAction != null) {
            menu.addItem(ItemBuilder.of(config.getItems().getBack()).build(player), (e) -> {
                backAction.run();
            });
        }

        menu.addItem(ItemBuilder.item(blueprint.getResultItem()).slot(config.getSlots().getResult()).build(player));

        MenuItem input = ItemBuilder.item(blueprint.getIngredientItems().getFirst()).slot(config.getSlots().getInput()).build(player);
        Blueprint ingredientRecipe = plugin.getBlueprintRegistry().getBlueprintFor(blueprint.getIngredients().getFirst().getItemPair().id());

        if (ingredientRecipe != null) {
            menu.addItem(input, (e) -> {
                var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                m.groupIndex = groupIndex;
                m.open();
            });
        } else {
            menu.addItem(input);
        }


        if (blueprint instanceof CauldronBlueprint cauldronBlueprint) {
            List<Integer> fluidSlots = switch (cauldronBlueprint.getVanillaOptions().getFluidLevel()) {
                case 1 -> config.getSlots().getFluidSlots().getOne();
                case 2 -> config.getSlots().getFluidSlots().getTwo();
                case 3 -> config.getSlots().getFluidSlots().getThree();
                default ->
                        throw new IllegalArgumentException("Invalid fluid level: " + cauldronBlueprint.getVanillaOptions().getFluidLevel());
            };


            Material fluidKey = cauldronBlueprint.getVanillaOptions().getFluid();
            for (int fluidSlot : fluidSlots) {
                ItemConfig fluidConfig = null;
                switch (fluidKey) {
                    case Material.WATER_CAULDRON -> fluidConfig = config.getFluidMaterials().getWater();
                    case Material.LAVA_CAULDRON -> fluidConfig = config.getFluidMaterials().getLava();
                    case Material.POWDER_SNOW_CAULDRON -> fluidConfig = config.getFluidMaterials().getPowderSnow();
                }

                if (fluidConfig != null) {
                    fluidConfig.setSlot(fluidSlot);
                    menu.addItem(ItemBuilder.of(fluidConfig).build(player));
                }
            }
        }

        var group = blueprint.getGroup();

        if (group != null && group.getBlueprints().size() > 1 && config.getSlots().getNextRecipe() != null && config.getSlots().getPrevRecipe() != null) {
            var next = group.getBlueprints().size() > groupIndex + 1 ? group.getBlueprints().get(groupIndex + 1) : null;
            var prev = groupIndex > 0 ? group.getBlueprints().get(groupIndex - 1) : null;

            if (next != null) {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, next, this.backAction);
                    m.groupIndex = groupIndex + 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player));
            }

            if (prev != null) {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipe()).slot(config.getSlots().getPrevRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, prev, this.backAction);
                    m.groupIndex = groupIndex - 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipeItem()).slot(config.getSlots().getPrevRecipe()).build(player));
            }
        }

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).build(player));
        }

        menu.open();
    }

    private void openFurnace() {
        var config = plugin.getConfigManager().getFurnaceRecipeViewConfig();
        var wb = plugin.getConfigManager().getWorkbenchDefaultConfig();

        var menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, key);
        menu.addFiller(ItemBuilder.of(config.getItems().getFiller()).toItemStack(player));

        if (backAction != null) {
            menu.addItem(ItemBuilder.of(config.getItems().getBack()).build(player), (e) -> {
                backAction.run();
            });
        }

        menu.addItem(ItemBuilder.of(config.getItems().getFuel()).slot(config.getSlots().getFuel()).build(player));
        menu.addItem(ItemBuilder.item(blueprint.getResultItem()).slot(config.getSlots().getResult()).build(player));

        var input = ItemBuilder.item(blueprint.getIngredientItems().getFirst()).slot(config.getSlots().getInput()).build(player);
        var recipe = plugin.getBlueprintRegistry().getBlueprintFor(blueprint.getIngredients().getFirst().getItemPair().id());

        if (recipe != null) {
            menu.addItem(input, (e) -> {
                var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                m.groupIndex = groupIndex;
                m.open();
            });
        } else {
            menu.addItem(input);
        }

        var group = blueprint.getGroup();

        if (group != null && group.getBlueprints().size() > 1 && config.getSlots().getNextRecipe() != null && config.getSlots().getPrevRecipe() != null) {
            var next = group.getBlueprints().size() > groupIndex + 1 ? group.getBlueprints().get(groupIndex + 1) : null;
            var prev = groupIndex > 0 ? group.getBlueprints().get(groupIndex - 1) : null;

            if (next != null) {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, next, this.backAction);
                    m.groupIndex = groupIndex + 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player));
            }

            if (prev != null) {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipe()).slot(config.getSlots().getPrevRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, prev, this.backAction);
                    m.groupIndex = groupIndex - 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipeItem()).slot(config.getSlots().getPrevRecipe()).build(player));
            }
        }

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).build(player));
        }

        menu.open();
    }

    private void openBlastFurnace() {
        var config = plugin.getConfigManager().getBlastFurnaceRecipeViewConfig();
        var wb = plugin.getConfigManager().getWorkbenchDefaultConfig();

        var menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, key);
        menu.addFiller(ItemBuilder.of(config.getItems().getFiller()).toItemStack(player));

        if (backAction != null) {
            menu.addItem(ItemBuilder.of(config.getItems().getBack()).build(player), (e) -> {
                backAction.run();
            });
        }

        menu.addItem(ItemBuilder.of(config.getItems().getFuel()).slot(config.getSlots().getFuel()).build(player));
        menu.addItem(ItemBuilder.item(blueprint.getResultItem()).slot(config.getSlots().getResult()).build(player));

        var input = ItemBuilder.item(blueprint.getIngredientItems().getFirst()).slot(config.getSlots().getInput()).build(player);
        var recipe = plugin.getBlueprintRegistry().getBlueprintFor(blueprint.getIngredients().getFirst().getItemPair().id());

        if (recipe != null) {
            menu.addItem(input, (e) -> {
                var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                m.groupIndex = groupIndex;
                m.open();
            });
        } else {
            menu.addItem(input);
        }

        var group = blueprint.getGroup();

        if (group != null && group.getBlueprints().size() > 1 && config.getSlots().getNextRecipe() != null && config.getSlots().getPrevRecipe() != null) {
            var next = group.getBlueprints().size() > groupIndex + 1 ? group.getBlueprints().get(groupIndex + 1) : null;
            var prev = groupIndex > 0 ? group.getBlueprints().get(groupIndex - 1) : null;

            if (next != null) {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, next, this.backAction);
                    m.groupIndex = groupIndex + 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player));
            }

            if (prev != null) {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipe()).slot(config.getSlots().getPrevRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, prev, this.backAction);
                    m.groupIndex = groupIndex - 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipeItem()).slot(config.getSlots().getPrevRecipe()).build(player));
            }
        }

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).build(player));
        }

        menu.open();
    }

    private void openSmoker() {
        var config = plugin.getConfigManager().getSmokerRecipeViewConfig();
        var wb = plugin.getConfigManager().getWorkbenchDefaultConfig();

        var menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, key);
        menu.addFiller(ItemBuilder.of(config.getItems().getFiller()).toItemStack(player));

        if (backAction != null) {
            menu.addItem(ItemBuilder.of(config.getItems().getBack()).build(player), (e) -> {
                backAction.run();
            });
        }

        menu.addItem(ItemBuilder.of(config.getItems().getFuel()).slot(config.getSlots().getFuel()).build(player));
        menu.addItem(ItemBuilder.item(blueprint.getResultItem()).slot(config.getSlots().getResult()).build(player));

        var input = ItemBuilder.item(blueprint.getIngredientItems().getFirst()).slot(config.getSlots().getInput()).build(player);
        var recipe = plugin.getBlueprintRegistry().getBlueprintFor(blueprint.getIngredients().getFirst().getItemPair().id());

        if (recipe != null) {
            menu.addItem(input, (e) -> {
                var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                m.groupIndex = groupIndex;
                m.open();
            });
        } else {
            menu.addItem(input);
        }

        var group = blueprint.getGroup();

        if (group != null && group.getBlueprints().size() > 1 && config.getSlots().getNextRecipe() != null && config.getSlots().getPrevRecipe() != null) {
            var next = group.getBlueprints().size() > groupIndex + 1 ? group.getBlueprints().get(groupIndex + 1) : null;
            var prev = groupIndex > 0 ? group.getBlueprints().get(groupIndex - 1) : null;

            if (next != null) {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, next, this.backAction);
                    m.groupIndex = groupIndex + 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player));
            }

            if (prev != null) {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipe()).slot(config.getSlots().getPrevRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, prev, this.backAction);
                    m.groupIndex = groupIndex - 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipeItem()).slot(config.getSlots().getPrevRecipe()).build(player));
            }
        }

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).build(player));
        }

        menu.open();
    }

    private void openCampfire() {
        var config = plugin.getConfigManager().getCampfireRecipeViewConfig();
        var wb = plugin.getConfigManager().getWorkbenchDefaultConfig();

        var menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, key);
        menu.addFiller(ItemBuilder.of(config.getItems().getFiller()).toItemStack(player));

        if (backAction != null) {
            menu.addItem(ItemBuilder.of(config.getItems().getBack()).build(player), (e) -> {
                backAction.run();
            });
        }

        menu.addItem(ItemBuilder.item(blueprint.getResultItem()).slot(config.getSlots().getResult()).build(player));

        var input = ItemBuilder.item(blueprint.getIngredientItems().getFirst()).slot(config.getSlots().getInput()).build(player);
        var recipe = plugin.getBlueprintRegistry().getBlueprintFor(blueprint.getIngredients().getFirst().getItemPair().id());

        if (recipe != null) {
            menu.addItem(input, (e) -> {
                var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                m.groupIndex = groupIndex;
                m.open();
            });
        } else {
            menu.addItem(input);
        }

        var group = blueprint.getGroup();

        if (group != null && group.getBlueprints().size() > 1 && config.getSlots().getNextRecipe() != null && config.getSlots().getPrevRecipe() != null) {
            var next = group.getBlueprints().size() > groupIndex + 1 ? group.getBlueprints().get(groupIndex + 1) : null;
            var prev = groupIndex > 0 ? group.getBlueprints().get(groupIndex - 1) : null;

            if (next != null) {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, next, this.backAction);
                    m.groupIndex = groupIndex + 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player));
            }

            if (prev != null) {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipe()).slot(config.getSlots().getPrevRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, prev, this.backAction);
                    m.groupIndex = groupIndex - 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipeItem()).slot(config.getSlots().getPrevRecipe()).build(player));
            }
        }

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).build(player));
        }

        menu.open();
    }

    private void openSmithingTable() {
        var config = plugin.getConfigManager().getSmithingRecipeViewConfig();
        var wb = plugin.getConfigManager().getWorkbenchDefaultConfig();

        var menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, key);
        menu.addFiller(ItemBuilder.of(config.getItems().getFiller()).toItemStack(player));

        if (backAction != null) {
            menu.addItem(ItemBuilder.of(config.getItems().getBack()).build(player), (e) -> {
                backAction.run();
            });
        }

        menu.addItem(ItemBuilder.item(blueprint.getResultItem()).amount(blueprint.getResult().amount()).slot(config.getSlots().getResult()).build(player));

        var template = ItemBuilder.item(blueprint.getIngredientItems().get(0)).amount(blueprint.getIngredients().get(0).getItemPair().amount()).slot(config.getSlots().getTemplate()).build(player);
        var templateRecipe = plugin.getBlueprintRegistry().getBlueprintFor(blueprint.getIngredients().get(0).getItemPair().id());

        if (templateRecipe != null) {
            menu.addItem(template, (e) -> {
                BlueprintMenu.blueprintMenu(plugin, player, templateRecipe, () -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                    m.groupIndex = groupIndex;
                    m.open();
                }).open();
            });
        } else {
            menu.addItem(template);
        }

        var base = ItemBuilder.item(blueprint.getIngredientItems().get(1)).amount(blueprint.getIngredients().get(1).getItemPair().amount()).slot(config.getSlots().getBase()).build(player);
        var baseRecipe = plugin.getBlueprintRegistry().getBlueprintFor(blueprint.getIngredients().get(1).getItemPair().id());

        if (baseRecipe != null) {
            menu.addItem(base, (e) -> {
                BlueprintMenu.blueprintMenu(plugin, player, baseRecipe, () -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                    m.groupIndex = groupIndex;
                    m.open();
                }).open();
            });
        } else {
            menu.addItem(base);
        }

        var addition = ItemBuilder.item(blueprint.getIngredientItems().get(2)).amount(blueprint.getIngredients().get(2).getItemPair().amount()).slot(config.getSlots().getAddition()).build(player);
        var additionRecipe = plugin.getBlueprintRegistry().getBlueprintFor(blueprint.getIngredients().get(2).getItemPair().id());

        if (additionRecipe != null) {
            menu.addItem(addition, (e) -> {
                BlueprintMenu.blueprintMenu(plugin, player, additionRecipe, () -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                    m.groupIndex = groupIndex;
                    m.open();
                }).open();
            });
        } else {
            menu.addItem(addition);
        }

        var group = blueprint.getGroup();

        if (group != null && group.getBlueprints().size() > 1 && config.getSlots().getNextRecipe() != null && config.getSlots().getPrevRecipe() != null) {
            var next = group.getBlueprints().size() > groupIndex + 1 ? group.getBlueprints().get(groupIndex + 1) : null;
            var prev = groupIndex > 0 ? group.getBlueprints().get(groupIndex - 1) : null;

            if (next != null) {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, next, this.backAction);
                    m.groupIndex = groupIndex + 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player));
            }

            if (prev != null) {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipe()).slot(config.getSlots().getPrevRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, prev, this.backAction);
                    m.groupIndex = groupIndex - 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipeItem()).slot(config.getSlots().getPrevRecipe()).build(player));
            }
        }

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).build(player));
        }

        menu.open();
    }

    private void openCraftingTable() {
        var config = plugin.getConfigManager().getCraftingTableRecipeViewConfig();
        var wb = plugin.getConfigManager().getWorkbenchDefaultConfig();

        var menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, key);
        menu.addFiller(ItemBuilder.of(config.getItems().getFiller()).toItemStack(player));

        if (backAction != null) {
            menu.addItem(ItemBuilder.of(config.getItems().getBack()).build(player), (e) -> {
                backAction.run();
            });
        }

        menu.addItem(ItemBuilder.item(blueprint.getResultItem()).amount(blueprint.getResult().amount()).slot(config.getSlots().getResult()).build(player));

        for (int i = 0; i < 9; i++) {
            var slot = config.getSlots().getMatrix().get(i);
            var item = ItemBuilder.item(blueprint.getIngredientItems().get(i)).amount(blueprint.getIngredients().get(i).getItemPair().amount()).slot(slot).build(player);
            var type = blueprint.getIngredients().get(i).getItemPair().id();
            var recipe = plugin.getBlueprintRegistry().getBlueprintFor(type);

            if (recipe != null) {
                menu.addItem(item, (e) -> {
                    BlueprintMenu.blueprintMenu(plugin, player, recipe, () -> {
                        var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                        m.groupIndex = groupIndex;
                        m.open();
                    }).open();
                });
            } else {
                menu.addItem(item);
            }
        }

        var group = blueprint.getGroup();

        if (group != null && group.getBlueprints().size() > 1 && config.getSlots().getNextRecipe() != null && config.getSlots().getPrevRecipe() != null) {
            var next = group.getBlueprints().size() > groupIndex + 1 ? group.getBlueprints().get(groupIndex + 1) : null;
            var prev = groupIndex > 0 ? group.getBlueprints().get(groupIndex - 1) : null;

            if (next != null) {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, next, this.backAction);
                    m.groupIndex = groupIndex + 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getNextRecipe()).slot(config.getSlots().getNextRecipe()).build(player));
            }

            if (prev != null) {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipe()).slot(config.getSlots().getPrevRecipe()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, prev, this.backAction);
                    m.groupIndex = groupIndex - 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(wb.getPreviousRecipeItem()).slot(config.getSlots().getPrevRecipe()).build(player));
            }
        }

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).build(player));
        }

        menu.open();
    }


    private void open(CustomWorkbench workbench) {
        var mcc = plugin.getConfigManager().getRecipeBookCategoryConfig();

        var menu = new AuroraMenu(player, workbench.getRecipeBookOptions().getTitle(), workbench.getMenuOptions().getRows() * 9, false, key);
        menu.addFiller(ItemBuilder.of(workbench.getMenuOptions().getFillerItem()).toItemStack(player));

        var ingredientItems = blueprint.getIngredientItems();
        var ingredientTypes = blueprint.getIngredients();

        for (int i = 0; i < workbench.getMatrixSlots().size(); i++) {
            var slot = workbench.getMatrixSlots().get(i);
            var item = i < ingredientItems.size() ? ingredientItems.get(i) : ItemStack.empty();
            var type = i < ingredientTypes.size() ? ingredientTypes.get(i) : null;
            if (type != null) {
                var recipe = plugin.getBlueprintRegistry().getBlueprintFor(type.getItemPair().id());
                if (recipe != null && (recipe.hasAccess(player) || !mcc.getSecretRecipeDisplay().getEnabled())) {
                    menu.addItem(ItemBuilder.item(item).amount(item.getAmount()).slot(slot).build(player), (e) -> {
                        BlueprintMenu.blueprintMenu(plugin, player, recipe, () -> {
                            var m = BlueprintMenu.blueprintMenu(plugin, player, this.blueprint, this.backAction);
                            m.groupIndex = groupIndex;
                            m.open();
                        }).open();
                    });
                    continue;
                }
            }

            menu.addItem(ItemBuilder.item(item).amount(item.getAmount()).slot(slot).build(player));
        }

        Integer resultSlot;
        resultSlot = workbench.getRecipeBookOptions().getResultSlot();
        if (resultSlot == null) {
            resultSlot = workbench.getResultSlot();
        }

        menu.addItem(ItemBuilder.item(blueprint.getResultItem()).amount(blueprint.getResult().amount()).slot(resultSlot).build(player));

        if (backAction != null) {
            menu.addItem(ItemBuilder.of(workbench.getMenuOptions().getBackItem()).defaultSlot(workbench.getRecipeBookOptions().getBackSlot()).build(player), (e) -> {
                backAction.run();
            });
        }

        var group = blueprint.getGroup();

        if (group != null && group.getBlueprints().size() > 1 && workbench.getRecipeBookOptions().getNextRecipeSlot() != null && workbench.getRecipeBookOptions().getPrevRecipeSlot() != null) {
            var next = group.getBlueprints().size() > groupIndex + 1 ? group.getBlueprints().get(groupIndex + 1) : null;
            var prev = groupIndex > 0 ? group.getBlueprints().get(groupIndex - 1) : null;

            if (next != null) {
                menu.addItem(ItemBuilder.of(workbench.getMenuOptions().getNextRecipeItem()).slot(workbench.getRecipeBookOptions().getNextRecipeSlot()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, next, this.backAction);
                    m.groupIndex = groupIndex + 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(workbench.getMenuOptions().getNextRecipeItem()).slot(workbench.getRecipeBookOptions().getNextRecipeSlot()).build(player));
            }

            if (prev != null) {
                menu.addItem(ItemBuilder.of(workbench.getMenuOptions().getPreviousRecipeItem()).slot(workbench.getRecipeBookOptions().getPrevRecipeSlot()).build(player), (e) -> {
                    var m = BlueprintMenu.blueprintMenu(plugin, player, prev, this.backAction);
                    m.groupIndex = groupIndex - 1;
                    m.open();
                });
            } else {
                menu.addItem(ItemBuilder.of(workbench.getMenuOptions().getPreviousRecipeItem()).slot(workbench.getRecipeBookOptions().getPrevRecipeSlot()).build(player));
            }
        }

        for (var item : workbench.getRecipeBookOptions().getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(item).build(player));
        }

        menu.open();
    }
}
