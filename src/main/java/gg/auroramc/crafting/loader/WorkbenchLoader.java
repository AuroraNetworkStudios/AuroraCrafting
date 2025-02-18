package gg.auroramc.crafting.loader;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.parser.WorkbenchParser;

public class WorkbenchLoader {
    public static void loadWorkbenches(AuroraCrafting plugin) {
        for (var config : plugin.getConfigManager().getWorkbenchConfig()) {
            try {
                plugin.getWorkbenchRegistry().registerWorkbench(WorkbenchParser.from(config).parse());
            } catch (Exception e) {
                AuroraCrafting.logger().severe("Failed to load workbench " + config.getId() + ": " + e.getMessage());
            }

        }
    }
}
