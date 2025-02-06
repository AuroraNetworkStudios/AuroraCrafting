package gg.auroramc.crafting.util;

import java.util.List;

public class Square {
    public static boolean isSquareCraftingArea(List<Integer> matrixSlots) {
        int size = matrixSlots.size();

        // Condition 1: Must be a perfect square
        int gridSize = (int) Math.sqrt(size);
        if (gridSize * gridSize != size) {
            return false;
        }

        // Condition 2: Slots must form a contiguous NxN square
        return isContiguousSquare(matrixSlots, gridSize);
    }

    private static boolean isContiguousSquare(List<Integer> slots, int gridSize) {
        // First slot defines the "top-left" of the area
        int firstRow = slots.get(0) / 9;  // Get row in a 9-wide chest grid
        int firstCol = slots.get(0) % 9;  // Get column

        for (int i = 0; i < gridSize; i++) {  // Iterate rows
            for (int j = 0; j < gridSize; j++) {  // Iterate columns
                int expectedSlot = (firstRow + i) * 9 + (firstCol + j);
                int index = i * gridSize + j;

                // Ensure the slot matches the expected position
                if (index >= slots.size() || !slots.get(index).equals(expectedSlot)) {
                    return false;
                }
            }
        }
        return true;
    }

}
