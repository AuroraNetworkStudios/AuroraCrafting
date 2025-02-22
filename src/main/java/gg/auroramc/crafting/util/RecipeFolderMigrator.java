package gg.auroramc.crafting.util;

import gg.auroramc.crafting.AuroraCrafting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class RecipeFolderMigrator {
    public static void tryMigrate(AuroraCrafting plugin) {
        Path oldBasePath = plugin.getDataFolder().toPath();
        Path recipesPath = oldBasePath.resolve("recipes");
        Path newBlueprintsPath = oldBasePath.resolve("blueprints/aurora");
        Path newVanillaPath = oldBasePath.resolve("blueprints/vanilla");

        if (Files.exists(recipesPath) && Files.isDirectory(recipesPath)) {
            try {
                moveDirectoryIfExists(oldBasePath.resolve("blasting_recipes"), newVanillaPath.resolve("blast_furnace"));
                moveDirectoryIfExists(oldBasePath.resolve("campfire_recipes"), newVanillaPath.resolve("campfire"));
                moveDirectoryIfExists(oldBasePath.resolve("furnace_recipes"), newVanillaPath.resolve("furnace"));
                moveDirectoryIfExists(oldBasePath.resolve("smithing_recipes"), newVanillaPath.resolve("smithing_table"));
                moveDirectoryIfExists(oldBasePath.resolve("smoking_recipes"), newVanillaPath.resolve("smoker"));

                moveDirectory(recipesPath, newBlueprintsPath);

                plugin.getLogger().info("Folder migration completed successfully.");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to migrate recipe folders to their new places!");
                e.printStackTrace();
            }
        }
    }

    private static void moveDirectoryIfExists(Path source, Path target) throws IOException {
        if (Files.exists(source) && Files.isDirectory(source)) {
            moveDirectory(source, target);
        }
    }

    private static void moveDirectory(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
