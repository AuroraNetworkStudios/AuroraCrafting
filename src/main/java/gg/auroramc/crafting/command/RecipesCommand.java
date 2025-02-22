package gg.auroramc.crafting.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.menu.BookCategoryListMenu;
import gg.auroramc.crafting.menu.BookBlueprintListMenu;
import gg.auroramc.crafting.menu.BlueprintMenu;
import org.bukkit.entity.Player;

@CommandAlias("%recipesAlias")
public class RecipesCommand extends BaseCommand {
    private final AuroraCrafting plugin;

    public RecipesCommand(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @Default
    @Description("Opens the recipes menu")
    @CommandPermission("aurora.crafting.recipes")
    @CommandCompletion("@recipes @nothing")
    public void onMenu(Player player, @Optional String recipeId) {
        if (recipeId == null) {
            BookCategoryListMenu.bookCategoryListMenu(plugin, player, plugin.getBook()).open();
            return;
        }
        var blueprint = plugin.getBlueprintRegistry().getBlueprint(recipeId);
        if (blueprint == null) return;

        if (blueprint.hasAccess(player) || !plugin.getConfigManager().getRecipeBookCategoryConfig().getSecretRecipeDisplay().getEnabled()) {
            BlueprintMenu.blueprintMenu(plugin, player, blueprint, null).open();
        }
    }
}
