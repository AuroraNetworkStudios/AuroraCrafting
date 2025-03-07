package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.aurora.api.util.TriConsumer;
import gg.auroramc.aurora.api.util.Version;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.book.BookCategory;
import gg.auroramc.crafting.api.workbench.Workbench;
import gg.auroramc.crafting.util.PersistentDataUtils;
import lombok.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

@Getter
public abstract class Blueprint {
    protected final String id;
    protected BlueprintGroup group;
    protected List<BookCategory> category = new ArrayList<>();
    protected String source;
    protected ItemPair result;
    protected ItemStack resultItem;
    protected String permission;
    protected final Workbench workbench;
    protected DisplayOptions displayOptions;
    protected boolean mergeOptionsEnabled = false;
    protected boolean ingredientAsResult = false;
    protected final List<TriConsumer<Player, ItemStack, Integer>> craftActions = new ArrayList<>();
    protected final List<Ingredient> ingredients = new ArrayList<>();
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
        if (context.getMatrix().length == 0 || !mergeOptionsEnabled) {
            return resultItem.clone();
        }

        var matchedIngredients = getMatchedIngredientList(context);

        ItemStack result;

        if (ingredientAsResult) {
            int index = 0;
            for (int i = 0; i < matchedIngredients.size(); i++) {
                if (matchedIngredients.get(i).isResult()) {
                    index = i;
                    break;
                }
            }
            result = context.getMatrix()[index].clone();
        } else {
            result = resultItem.clone();
        }

        for (int i = 0; i < context.getMatrix().length; i++) {
            var ingredient = context.getMatrix()[i];
            if (ingredient == null) {
                continue;
            }
            var mergeOption = matchedIngredients.get(i).getMergeOptions();
            if (mergeOption == null) {
                continue;
            }
            result = mergeToResult(result, ingredient, mergeOption);
        }

        return result;
    }

    protected List<Ingredient> getMatchedIngredientList(BlueprintContext context) {
        return ingredients;
    }

    protected ItemStack mergeToResult(ItemStack result, ItemStack ingredient, MergeOptions options) {
        var ingredientMeta = ingredient.getItemMeta();
        var resultMeta = result.getItemMeta();

        if (options.enchants) {
            var ingredientEnchants = ingredientMeta.getEnchants();
            if (ingredientMeta instanceof EnchantmentStorageMeta ingredientStorageMeta) {
                ingredientEnchants = ingredientStorageMeta.getStoredEnchants();
            }

            for (var enchant : ingredientEnchants.entrySet()) {
                if (resultMeta.getEnchants().containsKey(enchant.getKey())) {
                    var currentLevel = resultMeta.getEnchantLevel(enchant.getKey());
                    var otherLevel = enchant.getValue();
                    resultMeta.removeEnchant(enchant.getKey());
                    if (currentLevel == otherLevel) {
                        resultMeta.addEnchant(enchant.getKey(), Math.min(currentLevel + 1, enchant.getKey().getMaxLevel()), true);
                    } else {
                        resultMeta.addEnchant(enchant.getKey(), Math.max(currentLevel, otherLevel), true);
                    }
                } else {
                    boolean conflict = false;
                    for (var otherEnchant : resultMeta.getEnchants().keySet()) {
                        if (enchant.getKey().conflictsWith(otherEnchant)) {
                            conflict = true;
                            break;
                        }
                    }
                    if (!conflict) {
                        resultMeta.addEnchant(enchant.getKey(), enchant.getValue(), true);
                    }
                }
            }
        }

        if (options.trim) {
            if (ingredientMeta instanceof ArmorMeta ingredientArmorMeta && ingredientArmorMeta.hasTrim()) {
                if (resultMeta instanceof ArmorMeta) {
                    ((ArmorMeta) resultMeta).setTrim(ingredientArmorMeta.getTrim());
                }
            }
        }

        if (!options.pdc.isEmpty()) {
            PersistentDataUtils.mergePaths(ingredient, resultMeta, options.pdc);
        }

        if (options.copyDurability) {
            if (ingredientMeta instanceof Damageable i && i.hasDamage() && resultMeta instanceof Damageable) {
                ((Damageable) resultMeta).setDamage(i.getDamage());
            }
        }

        if (options.mergeDurability || options.restoreDurability != null) {
            if (options.restoreDurability != null) {
                if (result.getItemMeta() instanceof Damageable d && d.hasDamage()) {
                    var damageable = (Damageable) resultMeta;
                    damageable.setDamage(Math.max(damageable.getDamage() - options.getRestoreDurability(), 0));
                    if (!damageable.hasDamage() && Version.isAtLeastVersion(21)) {
                        damageable.resetDamage();
                    }
                }
            } else {
                if (result.getItemMeta() instanceof Damageable r && r.hasDamage()) {
                    var resultDamageable = (Damageable) resultMeta;
                    if (ingredient.getItemMeta() instanceof Damageable ingredientDamageable) {
                        var restoreDurability = 0;

                        if (Version.isAtLeastVersion(20, 5) && ingredientDamageable.hasMaxDamage()) {
                            restoreDurability = ingredientDamageable.hasDamage()
                                    ? ingredientDamageable.getMaxDamage() - ingredientDamageable.getDamage()
                                    : ingredientDamageable.getMaxDamage();
                        } else {
                            restoreDurability = ingredientDamageable.hasDamage()
                                    ? ingredient.getType().getMaxDurability() - ingredientDamageable.getDamage()
                                    : ingredient.getType().getMaxDurability();
                        }

                        resultDamageable.setDamage(Math.max(resultDamageable.getDamage() - restoreDurability, 0));
                        if (!resultDamageable.hasDamage() && Version.isAtLeastVersion(21)) {
                            resultDamageable.resetDamage();
                        }
                    }
                }
            }
        }

        result.setItemMeta(resultMeta);

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
        if (ingredientAsResult) {
            throw new IllegalArgumentException("Another ingredient is already set as result." + " for blueprint: " + id);
        }
        ingredients.get(index).setResult(true);
        this.result = ingredients.get(index).getItemPair();
        this.resultItem = ingredientItems.get(index).clone();
        this.ingredientAsResult = true;
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

    public Blueprint group(BlueprintGroup group) {
        this.group = group;
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
        if (this.ingredients.size() <= index) {
            throw new IllegalArgumentException("Invalid ingredient index: " + index + " for blueprint merge options: " + id + " with " + ingredients.size() + " ingredients");
        }
        this.ingredients.get(index).setMergeOptions(mergeOptions);
        this.mergeOptionsEnabled = true;
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
        this.ingredients.add(new Ingredient(itemPair));
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

    public boolean isStacked() {
        for (var ingredient : ingredients) {
            if (ingredient.getItemPair().amount() > 1) {
                return true;
            }
        }
        return false;
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

    /**
     * Should be called when the blueprint is fully completed
     */
    public Blueprint complete() {
        if (ingredients.size() > workbench.getMatrixSlots().size()) {
            throw new IllegalArgumentException("Too many ingredients in blueprint: " + id + ", in file: " + source + ". Max ingredients: " + workbench.getMatrixSlots().size() + ", found: " + ingredients.size());
        }

        if (ingredients.size() < workbench.getMatrixSlots().size()) {
            // Append air ingredients to fill the matrix
            for (int i = ingredients.size(); i < workbench.getMatrixSlots().size(); i++) {
                addIngredient(new ItemPair(TypeId.from(Material.AIR), 0));
            }
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Blueprint blueprint = (Blueprint) o;
        return Objects.equals(id, blueprint.id) && Objects.equals(source, blueprint.source) && Objects.equals(workbench, blueprint.workbench);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source, workbench);
    }

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
    @ToString
    @Builder
    public static final class MergeOptions {
        private boolean enchants;
        private boolean trim;
        private List<String> pdc;
        private Integer restoreDurability;
        private boolean mergeDurability;
        private boolean copyDurability;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    @ToString
    public static class Ingredient {
        private final ItemPair itemPair;
        private MergeOptions mergeOptions;
        private boolean result;
    }
}
