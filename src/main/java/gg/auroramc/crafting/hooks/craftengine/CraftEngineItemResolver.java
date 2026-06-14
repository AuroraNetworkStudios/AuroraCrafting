package gg.auroramc.crafting.hooks.craftengine;

import gg.auroramc.aurora.api.item.ItemResolver;
import gg.auroramc.aurora.api.item.TypeId;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CraftEngineItemResolver implements ItemResolver {

    @Override
    public boolean matches(ItemStack item) {
        return CraftEngineItems.isCustomItem(item);
    }

    @Override
    public TypeId resolveId(ItemStack item) {
        Key key = CraftEngineItems.getCustomItemId(item);
        if (key != null) {
            return new TypeId("craftengine", key.asString());
        }
        return null;
    }

    @Override
    public ItemStack resolveItem(String id, Player player) {
        var definition = CraftEngineItems.byId(id);
        if (definition != null) {
            return definition.buildBukkitItem(player);
        }
        return null;
    }

    @Override
    public ItemStack resolveItem(String id) {
        var definition = CraftEngineItems.byId(id);
        if (definition != null) {
            return definition.buildBukkitItem();
        }
        return null;
    }
}
