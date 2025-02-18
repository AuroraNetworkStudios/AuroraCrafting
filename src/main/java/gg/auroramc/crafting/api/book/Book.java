package gg.auroramc.crafting.api.book;

import gg.auroramc.crafting.api.blueprint.Blueprint;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Book extends BookCategory {
    private final Map<String, BookCategory> registry = new HashMap<>();

    public Book() {
        super("root", null, null);
    }

    @Override
    protected void registerCategory(String id, BookCategory category) {
        if (registry.containsKey(id)) {
            throw new IllegalArgumentException("Category with ID: " + id + " already exists");
        }
        registry.put(id, category);
    }

    public @Nullable BookCategory getCategory(String id) {
        return registry.get(id);
    }

    public Collection<BookCategory> getRegistry() {
        return registry.values();
    }

    public void unfreezeAndClear() {
        categories.clear();
        registry.clear();
        frozen = false;
    }

    @Override
    public void addBlueprint(Blueprint blueprint) {
        throw new UnsupportedOperationException("Cannot add blueprint to root category");
    }
}
