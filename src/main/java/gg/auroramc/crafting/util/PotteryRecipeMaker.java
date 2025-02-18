package gg.auroramc.crafting.util;

import org.bukkit.Material;
import org.bukkit.block.DecoratedPot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PotteryRecipeMaker {

    private static final DecoratedPot.Side[] SHARD_SIDES = new DecoratedPot.Side[]{
            DecoratedPot.Side.FRONT,
            DecoratedPot.Side.BACK,
            DecoratedPot.Side.LEFT,
            DecoratedPot.Side.RIGHT,
    };

    public static ItemStack create(ItemStack[] matrix) {
        ItemStack pot = new ItemStack(Material.DECORATED_POT);
        BlockStateMeta meta = (BlockStateMeta) pot.getItemMeta();
        DecoratedPot state = (DecoratedPot) meta.getBlockState();


        List<ItemStack[]> groups = splitIntoGroups(matrix);
        int groupCount = groups.size();
        int sideIndex = 0;

        for(int i = groupCount - 1; i >= 0 && sideIndex < SHARD_SIDES.length; i--) {
            for (ItemStack item : groups.get(i)) {
                if(item.isEmpty()) continue;

                state.setSherd(SHARD_SIDES[sideIndex++], item.getType());
                if(sideIndex >= SHARD_SIDES.length) break;
            }

        }

        meta.setBlockState(state);
        pot.setItemMeta(meta);


        return pot;
    }


    private static List<ItemStack[]> splitIntoGroups(ItemStack[] matrix) {
        int groupCount = (int) Math.ceil((double) matrix.length / 3);
        List<ItemStack[]> groups = new ArrayList<>(groupCount);

        for (int i = 0; i < matrix.length; i += 3) {
            groups.add(Arrays.copyOfRange(matrix, i, Math.min(matrix.length, i + 3)));
        }
        return groups;
    }

}
