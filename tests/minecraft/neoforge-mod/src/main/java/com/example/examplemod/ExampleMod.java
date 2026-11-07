package com.example.examplemod;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ExampleMod.MOD_ID)
public final class ExampleMod {
    public static final String MOD_ID = "examplemod";

    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, MOD_ID);

    static {
        TEST_FUNCTIONS.register("smoke", () -> ExampleMod::smoke);
    }

    public ExampleMod(IEventBus modBus) {
        TEST_FUNCTIONS.register(modBus);
    }

    private static void smoke(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, Blocks.DIAMOND_BLOCK);
        helper.succeedWhen(() -> helper.assertBlock(new BlockPos(0, 1, 0),
                block -> block == Blocks.DIAMOND_BLOCK,
                block -> Component.literal("Expected a diamond block")));
    }
}
