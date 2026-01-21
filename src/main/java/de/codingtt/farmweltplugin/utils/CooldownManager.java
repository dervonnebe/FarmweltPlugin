package de.codingtt.farmweltplugin.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class CooldownManager {
    private final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final long cooldownSeconds;
    private final boolean enabled;

    public CooldownManager(long cooldownSeconds, boolean enabled) {
        this.cooldownSeconds = cooldownSeconds;
        this.enabled = enabled;
    }

    public boolean hasCooldown(UUID uuid) {
        if (!enabled) return false;
        
        Long lastUseTime = cooldowns.get(uuid);
        if (lastUseTime == null) return false;
        
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - lastUseTime) / 1000;
        
        return elapsedSeconds < cooldownSeconds;
    }

    public long getRemainingCooldown(UUID uuid) {
        if (!enabled) return 0;
        
        Long lastUseTime = cooldowns.get(uuid);
        if (lastUseTime == null) return 0;
        
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - lastUseTime) / 1000;
        long remaining = cooldownSeconds - elapsedSeconds;
        
        return remaining > 0 ? remaining : 0;
    }

    public void setCooldown(UUID uuid) {
        if (!enabled) return;
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    public void removeCooldown(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }
}
