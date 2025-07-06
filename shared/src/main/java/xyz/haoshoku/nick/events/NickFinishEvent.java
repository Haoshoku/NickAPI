package xyz.haoshoku.nick.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Set;
import java.util.UUID;

public class NickFinishEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;

    private final UUID originalUniqueId;
    private final String originalName,originalValue, originalSignature;

    private final UUID uniqueId;
    private final String name, value, signature;

    private final Set<UUID> bypassList;

    public NickFinishEvent( Player player, UUID originalUniqueId, String originalName, String originalValue, String originalSignature, UUID uniqueId, String name, String value, String signature, Set<UUID> bypassList ) {
        this.player = player;
        this.originalUniqueId = originalUniqueId;
        this.originalName = originalName;
        this.originalValue = originalValue;
        this.originalSignature = originalSignature;
        this.uniqueId = uniqueId;
        this.name = name;
        this.value = value;
        this.signature = signature;
        this.bypassList = bypassList;
    }

    public String getSignature() {
        return signature;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getOriginalUniqueId() {
        return originalUniqueId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getOriginalSignature() {
        return originalSignature;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Set<UUID> getBypassList() {
        return bypassList;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }




}
