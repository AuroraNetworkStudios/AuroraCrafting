package gg.auroramc.crafting.util;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PersistentDataUtils {

    public static PersistentDataContainer mergePersistentDataContainers(PersistentDataContainer base, PersistentDataContainer result) {
        for (NamespacedKey key : base.getKeys()) {
            if (base.has(key, PersistentDataType.STRING)) {
                mergeValue(base, result, key, PersistentDataType.STRING);
            } else if (base.has(key, PersistentDataType.INTEGER)) {
                mergeNumericValue(base, result, key, PersistentDataType.INTEGER);
            } else if (base.has(key, PersistentDataType.LONG)) {
                mergeNumericValue(base, result, key, PersistentDataType.LONG);
            } else if (base.has(key, PersistentDataType.DOUBLE)) {
                mergeNumericValue(base, result, key, PersistentDataType.DOUBLE);
            } else if (base.has(key, PersistentDataType.FLOAT)) {
                mergeNumericValue(base, result, key, PersistentDataType.FLOAT);
            } else if (base.has(key, PersistentDataType.SHORT)) {
                mergeNumericValue(base, result, key, PersistentDataType.SHORT);
            } else if (base.has(key, PersistentDataType.BYTE)) {
                mergeNumericValue(base, result, key, PersistentDataType.BYTE);
            } else if (base.has(key, PersistentDataType.TAG_CONTAINER)) {
                mergeNestedContainer(base, result, key);
            }
        }

        return result;
    }

    private static <T, Z> void mergeValue(PersistentDataContainer base, PersistentDataContainer result, NamespacedKey key, PersistentDataType<T, Z> type) {
        if (!result.has(key, type)) {
            result.set(key, type, base.get(key, type));
        }
    }

    private static <T extends Number> void mergeNumericValue(
            PersistentDataContainer base,
            PersistentDataContainer result,
            NamespacedKey key,
            PersistentDataType<T, T> type) {
        if (!result.has(key, type)) {
            result.set(key, type, base.get(key, type));
        } else {
            T resultValue = result.get(key, type);
            T baseValue = base.get(key, type);
            if (resultValue.doubleValue() == 0) {
                result.set(key, type, baseValue);
            }
        }
    }

    private static void mergeNestedContainer(PersistentDataContainer base, PersistentDataContainer result, NamespacedKey key) {
        PersistentDataContainer baseValue = base.get(key, PersistentDataType.TAG_CONTAINER);
        PersistentDataContainer resultValue = result.get(key, PersistentDataType.TAG_CONTAINER);

        if (baseValue != null && resultValue != null) {
            PersistentDataContainer mergedContainer = mergePersistentDataContainers(baseValue, resultValue);
            result.set(key, PersistentDataType.TAG_CONTAINER, mergedContainer);
        } else if (baseValue != null) {
            result.set(key, PersistentDataType.TAG_CONTAINER, baseValue);
        }
    }
}
