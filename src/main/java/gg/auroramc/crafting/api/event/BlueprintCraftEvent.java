package gg.auroramc.crafting.api.event;

import gg.auroramc.crafting.api.blueprint.Blueprint;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
public class BlueprintCraftEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final ItemStack item;
    private final int amount;
    private final Blueprint blueprint;

    public BlueprintCraftEvent(@NotNull Player who, @NotNull ItemStack item, @NotNull Blueprint blueprint, int amount) {
        super(who);
        this.item = item;
        this.amount = amount;
        this.blueprint = blueprint;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
