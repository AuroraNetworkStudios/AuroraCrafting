package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.AuroraRecipe;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class RecipeMenu {
    private final AuroraCrafting plugin;
    private final Player player;
    private final AuroraRecipe recipe;
    private final Runnable backAction;

    public static RecipeMenu recipeMenu(AuroraCrafting plugin, Player player, AuroraRecipe recipe, Runnable backAction) {
        return new RecipeMenu(plugin, player, recipe, backAction);
    }

    public void open() {
        var workbenchConfig = plugin.getWorkbenchRegistry().getWorkbench(recipe.getWorkbench());
        var mc = plugin.getConfigManager().getRecipeViewConfig();
        var mcc = plugin.getConfigManager().getRecipeBookCategoryConfig();

        var menu = new AuroraMenu(player, mc.getTitle(), workbenchConfig.getMenuOptions().getRows() * 9, false);
        menu.addFiller(ItemBuilder.of(workbenchConfig.getMenuOptions().getFillerItem()).toItemStack(player));

        var ingredientItems = recipe.getIngredientItems();
        var ingredientTypes = recipe.getIngredients();

        for (int i = 0; i < workbenchConfig.getMatrixSlots().size(); i++) {
            var slot = workbenchConfig.getMatrixSlots().get(i);
            var item = i < ingredientItems.size() ? ingredientItems.get(i) : ItemStack.empty();
            var type = i < ingredientTypes.size() ? ingredientTypes.get(i) : null;
            if (type != null) {
                var recipe = plugin.getRecipeManager().getRecipeByResult(type.id());
                if (recipe != null && (recipe.hasPermission(player) || !mcc.getSecretRecipeDisplay().getEnabled())) {
                    menu.addItem(ItemBuilder.item(item).amount(item.getAmount()).slot(slot).build(player), (e) -> {
                        RecipeMenu.recipeMenu(plugin, player, recipe, () -> RecipeMenu.recipeMenu(plugin, player, this.recipe, this.backAction).open()).open();
                    });
                    continue;
                }
            }

            menu.addItem(ItemBuilder.item(item).amount(item.getAmount()).slot(slot).build(player));
        }

        Integer resultSlot;
        resultSlot = mc.getResultSlot().get(recipe.getWorkbench());
        if (resultSlot == null) {
            resultSlot = mc.getResultSlot().get("default");
        }
        if (resultSlot == null) {
            resultSlot = workbenchConfig.getResultSlot();
        }

        menu.addItem(ItemBuilder.item(recipe.getResultItem()).amount(recipe.getResult().amount()).slot(resultSlot).build(player));

        if (backAction != null && recipe.getCategory() != null) {
            menu.addItem(ItemBuilder.of(mc.getItems().get("back")).build(player), (e) -> {
                backAction.run();
            });
        }

        for (var item : mc.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(item).build(player));
        }

        menu.open();
    }
}
