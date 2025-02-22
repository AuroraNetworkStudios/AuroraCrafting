package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class BlueprintContext {
    public static final ItemPair AIR = new ItemPair(TypeId.from(Material.AIR), 0);

    private final Player player;
    private final ItemStack[] matrix;
    private final ItemPair[] idMatrix;
    private final String shapedLookupKey;
    private final String shapelessLookupKey;

    public BlueprintContext(Player player, ItemStack[] matrix) {
        this.player = player;
        this.matrix = matrix;

        ItemPair[] idMatrix = new ItemPair[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
                idMatrix[i] = AIR;
                continue;
            }
            idMatrix[i] = new ItemPair(AuroraAPI.getItemManager().resolveId(matrix[i]), matrix[i].getAmount());
        }

        this.idMatrix = idMatrix;
        this.shapedLookupKey = BlueprintLookupGenerator.toShapedKey(idMatrix);
        this.shapelessLookupKey = BlueprintLookupGenerator.toShapelessKey(idMatrix);
    }

    public static BlueprintContext blueprintContext(Player player, ItemStack[] matrix) {
        return new BlueprintContext(player, matrix);
    }
}
