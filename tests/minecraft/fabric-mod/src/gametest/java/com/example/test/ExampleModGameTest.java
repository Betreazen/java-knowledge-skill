package com.example.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

public class ExampleModGameTest {
    @GameTest
    public void placesDiamondBlock(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, Blocks.DIAMOND_BLOCK);
        helper.succeedWhen(() -> helper.assertBlock(new BlockPos(0, 1, 0),
                block -> block == Blocks.DIAMOND_BLOCK,
                block -> Component.literal("Expected a diamond block")));
    }
}
