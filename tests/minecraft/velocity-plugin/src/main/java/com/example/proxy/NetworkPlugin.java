package com.example.proxy;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import java.nio.file.Path;

@Plugin(id = "example-network", name = "ExampleNetwork", version = "1.0.0",
        description = "Velocity skeleton", authors = {"Example"})
public final class NetworkPlugin {
    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDir;

    @Inject
    public NetworkPlugin(ProxyServer proxy, Logger logger, @DataDirectory Path dataDir) {
        this.proxy = proxy;           // construction: store fields only
        this.logger = logger;
        this.dataDir = dataDir;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        BrigadierCommand cmd = new BrigadierCommand(
            BrigadierCommand.literalArgumentBuilder("online")
                .executes(ctx -> {
                    ctx.getSource().sendMessage(
                        Component.text("Players online: " + proxy.getPlayerCount()));
                    return Command.SINGLE_SUCCESS;
                })
                .build());
        CommandMeta meta = proxy.getCommandManager().metaBuilder("online").plugin(this).build();
        proxy.getCommandManager().register(meta, cmd);
        logger.info("ExampleNetwork ready, data dir {}", dataDir);
    }
}
