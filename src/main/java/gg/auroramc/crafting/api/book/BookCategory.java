package gg.auroramc.crafting.api.book;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintGroup;
import lombok.*;

import java.util.*;

@Getter
public class BookCategory {
    private final String id;
    private final BookCategory parent;
    private final List<BlueprintGroup> blueprints = new ArrayList<>();
    protected MenuOptions menuOptions;
    protected boolean frozen = false;

    @Getter(AccessLevel.NONE)
    protected final Map<String, BookCategory> categories = new HashMap<>();

    public BookCategory(String id, BookCategory parent, MenuOptions menuOptions) {
        this.id = id;
        this.parent = parent;
        this.menuOptions = menuOptions;
    }

    public void addSubCategory(BookCategory category) {
        if (frozen) {
            throw new IllegalStateException("Cannot add subcategory to frozen category: " + id);
        }
        if (!blueprints.isEmpty()) {
            throw new IllegalStateException("Cannot add subcategory to category: " + id + " with blueprints already added");
        }
        registerCategory(category.getId(), category);
        categories.put(category.getId(), category);
    }

    public void addBlueprint(Blueprint blueprint) {
        if (frozen) {
            throw new IllegalStateException("Cannot add blueprint to frozen category: " + id);
        }
        if (!categories.isEmpty()) {
            throw new IllegalStateException("Cannot add blueprint to category: " + id + " with subcategories");
        }

        if (blueprint.getGroup() != null) {
            if (!blueprints.contains(blueprint.getGroup())) {
                blueprints.add(blueprint.getGroup());
            }
        } else {
            var group = new BlueprintGroup();
            group.addBlueprint(blueprint);
            blueprints.add(group);
        }
    }

    public Collection<BookCategory> getCategories() {
        return categories.values();
    }

    public boolean hasSubCategories() {
        return !categories.isEmpty();
    }

    public void freeze() {
        frozen = true;
        for (var category : categories.values()) {
            category.freeze();
        }
    }

    protected void registerCategory(String id, BookCategory category) {
        parent.registerCategory(id, category);
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class MenuOptions {
        private String title;
        private ItemConfig item;
    }
}
