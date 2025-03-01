package gg.auroramc.crafting.api.blueprint;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
public class BlueprintGroup {
    private final List<Blueprint> blueprints = new ArrayList<>();

    private Blueprint first;

    public void addBlueprint(Blueprint blueprint) {
        blueprints.add(blueprint);
        if (first == null) {
            first = blueprint;
        }
    }
}
