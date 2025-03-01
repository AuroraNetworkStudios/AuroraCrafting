package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintGroup;
import gg.auroramc.crafting.api.book.BookCategory;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BookBlueprintListMenu {
    private final AuroraCrafting plugin;
    private final Player player;
    private final BookCategory category;
    private int page = 0;

    public BookBlueprintListMenu(AuroraCrafting plugin, Player player, BookCategory category, int page) {
        this.plugin = plugin;
        this.player = player;
        this.category = category;
        this.page = page;
    }

    public static BookBlueprintListMenu bookBlueprintListMenu(AuroraCrafting plugin, Player player, BookCategory category) {
        return new BookBlueprintListMenu(plugin, player, category, 0);
    }

    public static BookBlueprintListMenu bookBlueprintListMenu(AuroraCrafting plugin, Player player, BookCategory category, int page) {
        return new BookBlueprintListMenu(plugin, player, category, page);
    }

    public void open() {
        if (AuroraCrafting.isLoading()) return;

        var mc = plugin.getConfigManager().getRecipeBookCategoryConfig();

        var menu = new AuroraMenu(player, category.getMenuOptions().getTitle(), mc.getRows() * 9, false, NamespacedId.of("auroracrafting", "book_blueprint_list"));
        menu.addFiller(ItemBuilder.of(mc.getFiller()).toItemStack(player));

        for (var item : mc.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(item).build(player));
        }

        if (mc.getCategoryIcon().getEnabled()) {
            var itemConfig = category.getMenuOptions().getItem().merge(mc.getCategoryIcon().getItem());
            menu.addItem(ItemBuilder.of(itemConfig).defaultSlot(4).build(player));
        }

        menu.addItem(ItemBuilder.of(mc.getItems().get("back")).build(player), (e) -> {
            BookCategoryListMenu.bookCategoryListMenu(plugin, player, category.getParent()).open();
        });

        var recipes = getPageRecipes(mc.getDisplayArea().size());

        for (int i = 0; i < mc.getDisplayArea().size(); i++) {
            var slot = mc.getDisplayArea().get(i);
            if (i < recipes.size()) {
                var blueprint = recipes.get(i).getFirst();
                var item = blueprint.getResultItem();
                if (item.getType() == Material.AIR) {
                    item = new ItemStack(Material.BARRIER);
                    var meta = item.getItemMeta();
                    meta.displayName(Text.component("&c&lInvalid blueprint result"));
                    item.setItemMeta(meta);
                }
                if (mc.getSecretRecipeDisplay().getEnabled() && !blueprint.hasAccess(player)) {
                    menu.addItem(ItemBuilder.of(mc.getSecretRecipeDisplay().getItem()).slot(slot).loreCompute(() -> {
                        var lore = new ArrayList<>(mc.getSecretRecipeDisplay().getItem().getLore());
                        lore.addAll(blueprint.getDisplayOptions().getLockedLore());
                        return lore.stream().map(l -> Text.component(player, l)).toList();
                    }).build(player));
                } else {
                    var builder = ItemBuilder.item(item).slot(slot);

                    if (item.hasItemMeta()) {
                        ItemStack finalItem = item;
                        builder.loreCompute(() -> {
                            var lore = new ArrayList<Component>();
                            var meta = finalItem.getItemMeta();
                            if (meta.hasLore()) {
                                lore.addAll(meta.lore());
                            }

                            if (!blueprint.hasAccess(player)) {
                                lore.addAll(blueprint.getDisplayOptions().getLockedLore().stream().map(l -> Text.component(player, l)).toList());
                            }

                            lore.addAll(mc.getAppendLore().stream().map(l -> Text.component(player, l)).toList());
                            return lore;
                        });
                    }

                    menu.addItem(builder.build(player), (e) -> {
                        BlueprintMenu.blueprintMenu(plugin, player, blueprint, () -> BookBlueprintListMenu.bookBlueprintListMenu(plugin, player, category, page).open()).open();
                    });
                }
            } else {
                menu.addItem(ItemBuilder.item(ItemBuilder.filler(Material.AIR)).slot(slot).build(player));
            }
        }

        var blueprints = category.getBlueprints();

        if (blueprints.size() > mc.getDisplayArea().size()) {
            var maxPage = (int) Math.ceil(blueprints.size() / (double) mc.getDisplayArea().size());

            List<Placeholder<?>> placeholders = List.of(
                    Placeholder.of("{current}", page + 1),
                    Placeholder.of("{max}", maxPage)
            );

            menu.addItem(ItemBuilder.of(mc.getItems().get("previous-page")).placeholder(placeholders).build(player), (e) -> {
                if (page > 0) {
                    page--;
                    open();
                }
            });

            menu.addItem(ItemBuilder.of(mc.getItems().get("current-page"))
                    .placeholder(placeholders).build(player));

            menu.addItem(ItemBuilder.of(mc.getItems().get("next-page")).placeholder(placeholders).build(player), (e) -> {
                if (page < maxPage - 1) {
                    page++;
                    open();
                }
            });
        }

        menu.open();
    }

    public List<BlueprintGroup> getPageRecipes(int pageSize) {
        var recipes = category.getBlueprints();
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, recipes.size());
        return recipes.subList(startIndex, endIndex);
    }
}
