package gg.auroramc.crafting.menu;

import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.book.BookCategory;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@AllArgsConstructor
public class BookCategoryListMenu {
    private final AuroraCrafting plugin;
    private final Player player;
    private final BookCategory category;

    public static BookCategoryListMenu bookCategoryListMenu(AuroraCrafting plugin, Player player, BookCategory category) {
        return new BookCategoryListMenu(plugin, player, category);
    }

    public void open() {
        if (AuroraCrafting.isLoading()) return;

        var mc = plugin.getConfigManager().getRecipeBookMenuConfig();
        var mcc = plugin.getConfigManager().getRecipeBookCategoryConfig();
        var categories = category.getCategories();

        var menu = new AuroraMenu(player, category.getMenuOptions().getTitle(), mc.getRows() * 9, false, NamespacedId.of("auroracrafting", "book_category_list"));

        menu.addFiller(ItemBuilder.of(mc.getFiller()).toItemStack(player));

        for (var category : categories) {
            var item = ItemBuilder.of(category.getMenuOptions().getItem()).loreCompute(() -> {
                var lore = new ArrayList<>(category.getMenuOptions().getItem().getLore());
                lore.addAll(mc.getAppendLore());
                return lore.stream().map(l -> Text.component(player, l)).toList();
            }).build(player);

            if (category.hasSubCategories()) {
                menu.addItem(item, (e) -> {
                    BookCategoryListMenu.bookCategoryListMenu(plugin, player, category).open();
                });
            } else {
                menu.addItem(item, (e) -> {
                    BookBlueprintListMenu.bookBlueprintListMenu(plugin, player, category).open();
                });
            }

        }

        if (category.getParent() == null) {
            for (var item : mc.getCustomItems().values()) {
                menu.addItem(ItemBuilder.of(item).build(player));
            }
        } else {
            menu.addItem(ItemBuilder.of(mcc.getItems().get("back")).build(player), (e) -> {
                BookCategoryListMenu.bookCategoryListMenu(plugin, player, category.getParent()).open();
            });
        }

        menu.open();
    }
}
