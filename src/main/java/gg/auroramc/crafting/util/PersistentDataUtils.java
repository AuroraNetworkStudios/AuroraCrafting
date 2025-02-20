package gg.auroramc.crafting.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PersistentDataUtils {

    public static void mergePaths(ItemStack ingredient, ItemMeta resultMeta, List<String> paths) {
        var ingredientPdc = ingredient.getPersistentDataContainer();
        var resultPdc = resultMeta.getPersistentDataContainer();

        for (var path : paths) {
            String[] keys = path.split("\\.");

            if (keys.length == 0) continue;

            var currentContainer = ingredientPdc;
            var targetContainer = resultPdc;
            String lastKey = keys[keys.length - 1];

            // Traverse nested containers
            for (int i = 0; i < keys.length - 1; i++) {
                String key = keys[i];
                var keyNamespace = NamespacedKey.fromString(key); // Convert to NamespacedKey

                if (!currentContainer.has(keyNamespace, PersistentDataType.TAG_CONTAINER)) break;
                currentContainer = currentContainer.get(keyNamespace, PersistentDataType.TAG_CONTAINER);

                if (!targetContainer.has(keyNamespace, PersistentDataType.TAG_CONTAINER)) {
                    targetContainer.set(keyNamespace, PersistentDataType.TAG_CONTAINER, targetContainer.getAdapterContext().newPersistentDataContainer());
                }
                targetContainer = targetContainer.get(keyNamespace, PersistentDataType.TAG_CONTAINER);
            }

            var finalKey = NamespacedKey.fromString(lastKey);
            if (currentContainer.has(finalKey, PersistentDataType.STRING)) {
                String value = currentContainer.get(finalKey, PersistentDataType.STRING);
                targetContainer.set(finalKey, PersistentDataType.STRING, value);
            } else if (currentContainer.has(finalKey, PersistentDataType.INTEGER)) {
                int value = currentContainer.get(finalKey, PersistentDataType.INTEGER);
                targetContainer.set(finalKey, PersistentDataType.INTEGER, value);
            } else if (currentContainer.has(finalKey, PersistentDataType.DOUBLE)) {
                double value = currentContainer.get(finalKey, PersistentDataType.DOUBLE);
                targetContainer.set(finalKey, PersistentDataType.DOUBLE, value);
            } else if (currentContainer.has(finalKey, PersistentDataType.FLOAT)) {
                float value = currentContainer.get(finalKey, PersistentDataType.FLOAT);
                targetContainer.set(finalKey, PersistentDataType.FLOAT, value);
            } else if (currentContainer.has(finalKey, PersistentDataType.LONG)) {
                long value = currentContainer.get(finalKey, PersistentDataType.LONG);
                targetContainer.set(finalKey, PersistentDataType.LONG, value);
            } else if (currentContainer.has(finalKey, PersistentDataType.BYTE)) {
                byte value = currentContainer.get(finalKey, PersistentDataType.BYTE);
                targetContainer.set(finalKey, PersistentDataType.BYTE, value);
            } else if (currentContainer.has(finalKey, PersistentDataType.SHORT)) {
                short value = currentContainer.get(finalKey, PersistentDataType.SHORT);
                targetContainer.set(finalKey, PersistentDataType.SHORT, value);
            } else if (currentContainer.has(finalKey, PersistentDataType.BYTE_ARRAY)) {
                byte[] value = currentContainer.get(finalKey, PersistentDataType.BYTE_ARRAY);
                targetContainer.set(finalKey, PersistentDataType.BYTE_ARRAY, value);
            } else if (currentContainer.has(finalKey, PersistentDataType.INTEGER_ARRAY)) {
                int[] value = currentContainer.get(finalKey, PersistentDataType.INTEGER_ARRAY);
                targetContainer.set(finalKey, PersistentDataType.INTEGER_ARRAY, value);
            } else if (currentContainer.has(finalKey, PersistentDataType.LONG_ARRAY)) {
                long[] value = currentContainer.get(finalKey, PersistentDataType.LONG_ARRAY);
                targetContainer.set(finalKey, PersistentDataType.LONG_ARRAY, value);
            } else if (currentContainer.has(finalKey, PersistentDataType.TAG_CONTAINER_ARRAY)) {
                PersistentDataContainer[] array = currentContainer.get(finalKey, PersistentDataType.TAG_CONTAINER_ARRAY);
                targetContainer.set(finalKey, PersistentDataType.TAG_CONTAINER_ARRAY, array);
            } else if (currentContainer.has(finalKey, PersistentDataType.TAG_CONTAINER)) {
                targetContainer.set(finalKey, PersistentDataType.TAG_CONTAINER, currentContainer.get(finalKey, PersistentDataType.TAG_CONTAINER));
            }
        }
    }
}
