package gg.auroramc.crafting.listener;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.BlueprintContext;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.blueprint.CauldronBlueprint;
import gg.auroramc.crafting.api.workbench.Workbench;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class CauldronListener implements Listener {

    private final AuroraCrafting plugin;
    private final List<NamespacedKey> cauldronUseSoundkeys = List.of(
            NamespacedKey.minecraft("item.bucket.empty_powder_snow"),
            NamespacedKey.minecraft("item.bucket.empty_lava"),
            NamespacedKey.minecraft("item.bucket.empty")
    );

    // vanilla cauldron is not in the list since that is the empty variant
    private final Set<Material> cauldronItems = Set.of(
        Material.WATER_CAULDRON,
        Material.LAVA_CAULDRON,
        Material.POWDER_SNOW_CAULDRON
    );

    private final Set<Action> acceptedActions = Set.of(Action.RIGHT_CLICK_BLOCK);

    public CauldronListener(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCauldronInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        // only trigger on right click block, we dont need anything else
        if(!acceptedActions.contains(action)) return;

        // to avoid duplicate crafting / too little delay we ignore offhand interacts
        if(EquipmentSlot.OFF_HAND == event.getHand()) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = player.getInventory().getItemInMainHand();

        // if the player has no items or there is no clicked block we ignore
        if(block == null || item.isEmpty()) return;
        Material blockType = block.getType();

        // if the clicked block is not a cauldron, we ignore
        if(!cauldronItems.contains(blockType)) return;

        Workbench workbench = plugin.getWorkbenchRegistry().getCauldron();

        BlueprintContext context = workbench.createContext(player, item);
        CauldronBlueprint blueprint = (CauldronBlueprint) workbench.lookupBlueprint(context, BlueprintType.CAULDRON);

        // if there is no blueprint found, we ignore
        if (blueprint == null) {
            return;
        }

        if(!blockType.equals(blueprint.getVanillaOptions().getFluid())) {
            return;
        }

        // if we can craft 0 or less items, we ignore
        var timesCraftable = blueprint.getTimesCraftable(context);
        if (timesCraftable <= 0) return;

        // if the cauldron level cannot be decremented (not enough fluid, we ignore)
        // remove required amount of fluid from the block
        boolean success = decrementCauldronLevel(block, blueprint.getVanillaOptions().getFluidLevel(), blueprint.getSource());
        if(!success) return;

        // ok so at this point, we know the user has all the items and have taken them, and we have removed the fluid from the cauldron
        // now all that's left is to give the player the result

        this.playSound(player, blockType);
        removeItem(player, blueprint.getIngredients().getFirst().getItemPair().amount());

        // remove every ingredient from the player's inventory
        // add the result (if there is space, if not drop to the ground) to the player inventory
        ItemStack result = blueprint.getResultItem(context);
        HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(result);
        remaining.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));


    }

    private void removeItem(Player player, int number) {
        int handItemCound = player.getInventory().getItemInMainHand().getAmount();
        if(handItemCound < number) return;

        if(handItemCound == number) {
            player.getInventory().setItemInMainHand(null);
            return;
        }

        player.getInventory().getItemInMainHand().setAmount(handItemCound - number);
    }

    // todo play sound
    private boolean decrementCauldronLevel(Block b, int level, String blueprintPath) {
        Material blockMaterial = b.getType();

        // powdered snow and water cauldron fall under this category
        // lava only has one "level"
        if(b.getBlockData() instanceof Levelled lb) {
            if(lb.getLevel() < level) return false;

            // we cant actually set level to 0, if its 0 we need to set the type to CAULDRON
            int newLevel = lb.getLevel() - level;
            if(newLevel == 0) {
                b.setType(Material.CAULDRON);
                return true;
            }
            else lb.setLevel(lb.getLevel() - level);

            b.setBlockData(lb);
            return true;
        }

        // lava cauldron has only one leve, either full or "empty" (regular cauldron)
        if(blockMaterial == Material.LAVA_CAULDRON) {
            b.setType(Material.CAULDRON);
            return true;
        }

        return false;
    }

    private void playSound(Player p, Material m) {
        int index = 2;
        if(m == Material.POWDER_SNOW_CAULDRON) index = 0;
        else if(m == Material.LAVA_CAULDRON) index = 1;

        final Sound sound = Registry.SOUNDS.get(cauldronUseSoundkeys.get(index));
        if(sound != null) {
            p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
        }
    }
}
