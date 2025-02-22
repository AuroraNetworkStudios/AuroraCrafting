package gg.auroramc.crafting.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Square {
    public static boolean isSquareCraftingArea(List<Integer> matrixSlots, int gridSize) {
        if (matrixSlots.isEmpty()) return false;

        // Find min/max row & col to determine bounding box
        int minRow = gridSize, maxRow = 0, minCol = gridSize, maxCol = 0;

        for (int slot : matrixSlots) {
            int row = slot / gridSize;
            int col = slot % gridSize;

            minRow = Math.min(minRow, row);
            maxRow = Math.max(maxRow, row);
            minCol = Math.min(minCol, col);
            maxCol = Math.max(maxCol, col);
        }

        int width = maxCol - minCol + 1;
        int height = maxRow - minRow + 1;

        // Must be a perfect square
        if (width != height) return false;

        // Ensure all expected slots exist
        Set<Integer> slotSet = new HashSet<>(matrixSlots);
        for (int r = minRow; r < minRow + height; r++) {
            for (int c = minCol; c < minCol + width; c++) {
                if (!slotSet.contains(r * gridSize + c)) {
                    return false;
                }
            }
        }
        return true;
    }
}
