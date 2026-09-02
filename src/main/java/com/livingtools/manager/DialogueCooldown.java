package com.livingtools.manager;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Evita saturar el chat con líneas de personalidad / armadura.
 */
public final class DialogueCooldown {

    public static final long TOOL_CHAT_MS = 180_000L;
    public static final long ARMOR_CHAT_MS = 180_000L;

    private static final Map<UUID, Long> toolChat = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> armorChat = new ConcurrentHashMap<>();

    private DialogueCooldown() {
    }

    public static boolean tryToolChat(Player player) {
        return tryAcquire(toolChat, player.getUniqueId(), TOOL_CHAT_MS);
    }

    public static boolean tryArmorChat(Player player) {
        return tryAcquire(armorChat, player.getUniqueId(), ARMOR_CHAT_MS);
    }

    private static boolean tryAcquire(Map<UUID, Long> map, UUID id, long cooldownMs) {
        long now = System.currentTimeMillis();
        Long last = map.get(id);
        if (last != null && now - last < cooldownMs) {
            return false;
        }
        map.put(id, now);
        return true;
    }
}
