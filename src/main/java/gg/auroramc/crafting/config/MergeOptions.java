package gg.auroramc.crafting.config;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MergeOptions {
    private Boolean enchants = false;
    private Boolean trim = false;
    private List<String> pdc = new ArrayList<>();
    private Integer restoreDurability = null;
    private Boolean mergeDurability = false;
    private Boolean copyDurability = false;
}
