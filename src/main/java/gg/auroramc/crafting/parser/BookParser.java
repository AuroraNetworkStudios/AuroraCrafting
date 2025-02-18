package gg.auroramc.crafting.parser;

import gg.auroramc.crafting.api.book.Book;
import gg.auroramc.crafting.api.book.BookCategory;
import gg.auroramc.crafting.config.RecipeBookConfig;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BookParser {
    private final Book book;
    private final RecipeBookConfig.RecipeCategory config;

    public static BookParser from(Book book, RecipeBookConfig.RecipeCategory config) {
        return new BookParser(book, config);
    }

    public BookCategory parse() {
        var bookCategory = new BookCategory(config.getId(), book, BookCategory.MenuOptions.builder()
                .title(config.getMenu().getTitle())
                .item(config.getMenu().getItem())
                .build());

        if (!config.getCategories().isEmpty()) {
            for (var category : config.getCategories()) {
                bookCategory.addSubCategory(BookParser.from(book, category).parse());
            }
        }

        return bookCategory;
    }
}
