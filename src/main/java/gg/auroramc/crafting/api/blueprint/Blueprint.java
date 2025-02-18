package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.aurora.api.util.TriConsumer;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.book.BookCategory;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class Blueprint {
    protected final String id;
    protected List<BookCategory> category = new ArrayList<>();
    protected String source;
    protected ItemPair result;
    protected ItemStack resultItem;
    protected String permission;
    protected final Workbench workbench;
    protected DisplayOptions displayOptions;
    protected Map<Integer, MergeOptions> mergeOptions;
    protected Integer resultIngredientIndex = null;
    protected final List<TriConsumer<Player, ItemStack, Integer>> craftActions = new ArrayList<>();
    protected final List<ItemPair> ingredients = new ArrayList<>();
    protected final List<ItemStack> ingredientItems = new ArrayList<>();
    protected final Map<TypeId, Integer> ingredientCount = new HashMap<>();

    public Blueprint(Workbench workbench, String id) {
        this.workbench = workbench;
        this.id = id;
    }

    /**
     * Get the result item for the blueprint. Will apply
     * merge options if they are present.
     *
     * @param context the context to get the result item for
     * @return the result item
     */
    public ItemStack getResultItem(BlueprintContext context) {
        if (context.getMatrix().length == 0 || mergeOptions == null) {
            return resultItem.clone();
        }

        var result = resultIngredientIndex != null ? context.getMatrix()[resultIngredientIndex].clone() : resultItem.clone();

        for (int i = 0; i < context.getMatrix().length; i++) {
            var ingredient = context.getMatrix()[i];
            if (ingredient == null) {
                continue;
            }
            var mergeOption = mergeOptions.get(String.valueOf(i));
            if (mergeOption == null) {
                continue;
            }
            result = mergeToResult(result, ingredient, mergeOption);
        }

        return result;
    }

    protected ItemStack mergeToResult(ItemStack result, ItemStack ingredient, MergeOptions options) {
        if (options.enchants) {
            for (var enchant : ingredient.getEnchantments().entrySet()) {
                if (result.getEnchantments().containsKey(enchant.getKey())) {
                    result.addEnchantment(enchant.getKey(), enchant.getValue() + result.getEnchantmentLevel(enchant.getKey()));
                } else {
                    result.addEnchantment(enchant.getKey(), enchant.getValue());
                }
            }
        }

        if (options.trim) {
            if (ingredient.getItemMeta() instanceof ArmorMeta armorMeta && armorMeta.hasTrim()) {
                if (result.getItemMeta() instanceof ArmorMeta resultArmorMeta) {
                    resultArmorMeta.setTrim(armorMeta.getTrim());
                }
            }
        }

        return result;
    }

    /**
     * Set the result of the blueprint
     *
     * @param result the result item pair
     * @return the blueprint
     */
    public Blueprint result(ItemPair result) {
        var itemStack = AuroraAPI.getItemManager().resolveItem(result.id());
        itemStack.setAmount(result.amount());
        if (itemStack.isEmpty()) {
            throw new IllegalArgumentException("Invalid item ID: " + result.id());
        }
        this.result = result;
        this.resultItem = itemStack;
        return this;
    }

    /**
     * Set the result of the blueprint based on an already
     * registered ingredient
     *
     * @param index index of the ingredient to use as the result
     * @return the blueprint
     */
    public Blueprint result(int index) {
        if (index < 0 || index >= ingredients.size()) {
            throw new IllegalArgumentException("Invalid ingredient index: " + index + " for blueprint: " + id + " with " + ingredients.size() + " ingredients");
        }
        if (ingredientItems.get(index).isEmpty()) {
            throw new IllegalArgumentException("Invalid ingredient index: " + index + " for blueprint: " + id + ". Ingredient is empty/air.");
        }
        this.result = ingredients.get(index);
        this.resultItem = ingredientItems.get(index).clone();
        this.resultIngredientIndex = index;
        return this;
    }

    /**
     * Set the permission required to craft the blueprint
     *
     * @param permission the permission
     * @return the blueprint
     */
    public Blueprint permission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Set the load source of the blueprint
     *
     * @param source source of the blueprint
     * @return the blueprint
     */
    public Blueprint source(String source) {
        this.source = source;
        return this;
    }

    /**
     * Set the book category which the blueprint belongs to
     *
     * @param category the category
     * @return the blueprint
     */
    public Blueprint category(BookCategory category) {
        this.category.add(category);
        return this;
    }

    /**
     * Set the display options for the blueprint.
     * This defines how the blueprint will be displayed in the recipe book.
     *
     * @param displayOptions the display options
     * @return the blueprint
     */
    public Blueprint displayOptions(DisplayOptions displayOptions) {
        this.displayOptions = displayOptions;
        return this;
    }

    /**
     * Merge options for the blueprint. This allows you to merge
     * enchantments and other data from the ingredients to the result.
     *
     * @param index        the index of the ingredient
     * @param mergeOptions the merge options
     * @return the blueprint
     */
    public Blueprint mergeOptions(int index, MergeOptions mergeOptions) {
        if (this.mergeOptions == null) {
            this.mergeOptions = new HashMap<>();
        }
        this.mergeOptions.put(index, mergeOptions);
        return this;
    }

    /**
     * Add an ingredient to the blueprint
     *
     * @param itemPair the item pair
     * @return the blueprint
     */
    public Blueprint addIngredient(ItemPair itemPair) {
        var item = AuroraAPI.getItemManager().resolveItem(itemPair.id());
        item.setAmount(itemPair.amount());
        this.ingredients.add(itemPair);
        this.ingredientItems.add(item);
        this.ingredientCount.merge(itemPair.id(), itemPair.amount(), Integer::sum);
        if (itemPair.id().equals(TypeId.from(Material.AIR))) {
            this.ingredientCount.remove(itemPair.id());
        }
        return this;
    }

    /**
     * Add an ingredient to the blueprint
     *
     * @param ingredients the ingredient pairs
     * @return the blueprint
     */
    public Blueprint ingredients(List<ItemPair> ingredients) {
        ingredients.forEach(this::addIngredient);
        return this;
    }

    /**
     * Register a craft action for the blueprint. This will be called
     * when the blueprint is crafted.
     *
     * @param handler the handler to call
     * @return the blueprint
     */
    public Blueprint onCraft(TriConsumer<Player, ItemStack, Integer> handler) {
        if (handler == null) return this;
        this.craftActions.add(handler);
        return this;
    }

    /**
     * Get the total result of the blueprint based on the number of times crafted.
     *
     * @param context      the context to get the result for
     * @param timesCrafted the number of times the blueprint was crafted
     * @return the total result
     */
    public ItemStack[] getTotalResult(BlueprintContext context, int timesCrafted) {
        var itemResult = getResultItem(context);
        return ItemUtils.createStacksFromAmount(itemResult, result.amount() * timesCrafted);
    }

    /**
     * Check if the player has access to the blueprint
     *
     * @param player the player
     * @return true if the player has access
     */
    public boolean hasAccess(Player player) {
        return permission == null || player.hasPermission(permission);
    }

    /**
     * Get the number of times the player can quick craft the blueprint
     * based on the items in their inventory.
     *
     * @param itemCount the item count map
     * @return the number of times the player can quick craft the recipe
     */
    public int getQuickCraftTimes(Map<TypeId, Integer> itemCount) {
        int maxCraftable = Integer.MAX_VALUE;
        var matches = true;

        for (var entry : ingredientCount.entrySet()) {
            var ingredient = entry.getKey();
            var ingredientAmount = entry.getValue();
            var itemAmount = itemCount.getOrDefault(ingredient, 0);

            if (itemAmount < ingredientAmount) {
                matches = false;
                break;
            } else if (ingredientAmount != 0) {
                maxCraftable = Math.min(maxCraftable, itemAmount / ingredientAmount);
            }
        }

        return matches ? maxCraftable : 0;
    }


    /**
     * Quick craft the blueprint for the player based on the number of times
     * and the items in their inventory.
     *
     * @param context           the context to quick craft the recipe for
     * @param times             the number of times to craft the recipe
     * @param addMinusOneResult if the result should be added times - 1 times
     */
    public void quickCraft(BlueprintContext context, int times, boolean addMinusOneResult) {
        var player = context.getPlayer();
        // Calculate the total ingredients required
        var totalIngredients = ingredientCount.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue() * times)) // Multiply by the number of times
                .toList();

        // Remove items based on their IDs
        var failedToRemove = false;
        for (var entry : totalIngredients) {
            TypeId itemId = entry.getKey();
            int requiredAmount = entry.getValue();

            // Iterate through the player's inventory to remove items
            var inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack itemStack = inventory.getItem(slot);
                if (itemStack == null) continue;

                // Resolve the item ID for the current stack
                TypeId currentItemId = AuroraAPI.getItemManager().resolveId(itemStack);
                if (currentItemId != null && currentItemId.equals(itemId)) {
                    int stackAmount = itemStack.getAmount();

                    if (stackAmount >= requiredAmount) {
                        // Reduce the stack size or remove the item
                        itemStack.setAmount(stackAmount - requiredAmount);
                        if (itemStack.getAmount() <= 0) {
                            inventory.setItem(slot, null); // Remove the item if the stack is empty
                        }
                        requiredAmount = 0; // All required items have been removed
                        break;
                    } else {
                        // Remove the entire stack and reduce the required amount
                        inventory.setItem(slot, null);
                        requiredAmount -= stackAmount;
                    }
                }
            }

            // If we couldn't remove all required items, mark it as failed
            if (requiredAmount > 0) {
                failedToRemove = true;
                break;
            }
        }

        if (!failedToRemove) {
            // Add the crafted result to the inventory
            player.getInventory().addItem(this.getTotalResult(context, addMinusOneResult ? times - 1 : times));
        } else {
            // Log a warning if the recipe couldn't be completed
            AuroraCrafting.logger().severe("Failed to quick craft recipe " + id + " for player " + player.getName() +
                    ", because ingredients couldn't be fully taken. THIS IS A DUPE!");
        }
    }

    /**
     * Get the number of times the blueprint can be crafted based on the items in the matrix
     *
     * @param context the context to get the times craftable for
     * @return the number of times the recipe can be crafted
     */
    public abstract int getTimesCraftable(BlueprintContext context);

    /**
     * Calculate the remaining ingredient matrix based on the number of times crafted.
     *
     * @param context      the context to calculate the remaining ingredient matrix for
     * @param timesCrafted the number of times the recipe was crafted
     * @return the remaining ingredient matrix
     */
    public abstract ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted);

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static final class DisplayOptions {
        private List<String> lockedLore;
        private Map<String, ItemConfig> items;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static final class MergeOptions {
        private boolean enchants;
        private boolean trim;
    }
}
