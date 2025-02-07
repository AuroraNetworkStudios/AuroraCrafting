package gg.auroramc.crafting.api;

import gg.auroramc.crafting.api.blueprint.BlueprintRegistry;
import gg.auroramc.crafting.api.book.Book;
import gg.auroramc.crafting.api.workbench.WorkbenchRegistry;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class AuroraCraftingPlugin extends JavaPlugin {
    protected static AuroraCraftingPlugin instance;

    public static AuroraCraftingPlugin inst() {
        return instance;
    }

    protected BlueprintRegistry blueprintRegistry;
    protected WorkbenchRegistry workbenchRegistry;
    protected Book book;
}
