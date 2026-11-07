package com.example.plugin;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import java.util.List;

public final class ExampleBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
            event.registrar().register(
                Commands.literal("visits")
                    .then(Commands.literal("reset")
                        .requires(src -> src.getSender().hasPermission("example.visits.reset"))
                        .then(Commands.argument("target", ArgumentTypes.player())
                            .executes(ctx -> {
                                PlayerSelectorArgumentResolver r =
                                    ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                Player target = r.resolve(ctx.getSource()).getFirst();
                                VisitCounter.reset(target);
                                ctx.getSource().getSender().sendMessage(
                                    Component.text("Reset visits for " + target.getName()));
                                return Command.SINGLE_SUCCESS;
                            })))
                    .build(),
                "Show or reset visit counters",
                List.of()));
    }
}
