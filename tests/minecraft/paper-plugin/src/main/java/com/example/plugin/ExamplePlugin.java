package com.example.plugin;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExamplePlugin extends JavaPlugin implements Listener {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        int visits = VisitCounter.increment(event.getPlayer());
        // player name inserted unparsed: no tag injection
        event.getPlayer().sendMessage(MM.deserialize("<green>Welcome <name>, visit #<count>",
            Placeholder.unparsed("name", event.getPlayer().getName()),
            Placeholder.unparsed("count", Integer.toString(visits))));
    }
}
