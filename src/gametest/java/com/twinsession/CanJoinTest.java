package com.twinsession;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CanJoinTest {
    private final UUID ORIGINAL_UUID = UUID.fromString("1c9ac288-7555-4d07-9280-126a773aeb70");
    private final String ORIGINAL_NAME = "MisterFish_";

    @GameTest
    public void firstJoinTest(GameTestHelper context) {
        GameProfile sourceProfile = new GameProfile(ORIGINAL_UUID, ORIGINAL_NAME);
        FakePlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);

        context.assertTrue(TwinSession.canJoin(sourcePlayer), Component.literal("Checking if player can join"));
        context.succeed();
    }

    @GameTest
    public void secondJoinTest(GameTestHelper context) {
        GameProfile sourceProfile = new GameProfile(ORIGINAL_UUID, ORIGINAL_NAME);
        FakePlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);
        TwinSession.getTwinMap().computeIfAbsent(ORIGINAL_UUID, key -> new HashMap<>());

        context.assertTrue(TwinSession.canJoin(sourcePlayer), Component.literal("Checking if second player can join"));

        TwinSession.getTwinMap().clear();
        context.succeed();
    }


    @GameTest
    public void tooManyJoinsTest(GameTestHelper context) {
        Map<Integer, UUID> map =
                java.util.stream.IntStream.rangeClosed(1, 8)
                        .boxed()
                        .collect(java.util.stream.Collectors.toMap(i -> i, i -> UUID.randomUUID()));
        TwinSession.getTwinMap().putIfAbsent(ORIGINAL_UUID, map);
        GameProfile sourceProfile = new GameProfile(ORIGINAL_UUID, ORIGINAL_NAME);
        FakePlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);

        context.assertFalse(TwinSession.canJoin(sourcePlayer), Component.literal("Checking if 9th player cannot join"));

        TwinSession.getTwinMap().clear();
        context.succeed();
    }

}
