package com.example.plugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

final class VisitCounter {
    static final NamespacedKey KEY = new NamespacedKey("exampleplugin", "visits");
    private VisitCounter() {}

    static int increment(Player p) {
        int next = p.getPersistentDataContainer().getOrDefault(KEY, PersistentDataType.INTEGER, 0) + 1;
        p.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, next);
        return next;
    }

    static void reset(Player p) { p.getPersistentDataContainer().remove(KEY); }
}
