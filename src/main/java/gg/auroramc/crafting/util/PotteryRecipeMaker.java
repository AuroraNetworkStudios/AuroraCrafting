package gg.auroramc.crafting.util;

import org.bukkit.Material;
import org.bukkit.block.DecoratedPot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PotteryRecipeMaker {
    public static ItemStack create(ItemStack[] matrix) {

        // split items into groups of 3
        List<ItemStack[]> groups = splitIntoGroups(matrix, 3).reversed();

        ItemStack pot = new ItemStack(Material.DECORATED_POT);
        BlockStateMeta meta = (BlockStateMeta) pot.getItemMeta();
        DecoratedPot state = (DecoratedPot) meta.getBlockState();

        state.setSherd(DecoratedPot.Side.FRONT, Material.BLADE_POTTERY_SHERD);

        DecoratedPot.Side[] sides = new DecoratedPot.Side[] {
                DecoratedPot.Side.FRONT,
                DecoratedPot.Side.BACK,
                DecoratedPot.Side.LEFT,
                DecoratedPot.Side.RIGHT,
        };

        int i = 0;
        for(ItemStack[] group : groups) {
            for(ItemStack item : group) {
                if(item.isEmpty()) continue;

                if(i >= sides.length) break;

                state.setSherd(sides[i], item.getType());
                i++;
            }
        }

        meta.setBlockState(state);
        pot.setItemMeta(meta);


        return pot;
    }


    private static List<ItemStack[]> splitIntoGroups(ItemStack[] matrix, int groupSize) {
        List<ItemStack[]> groups = new ArrayList<>();
        for (int i = 0; i < matrix.length; i += groupSize) {
            ItemStack[] group = Arrays.copyOfRange(matrix, i, Math.min(matrix.length, i + groupSize));
            groups.add(group);
        }
        return groups;
    }

}

