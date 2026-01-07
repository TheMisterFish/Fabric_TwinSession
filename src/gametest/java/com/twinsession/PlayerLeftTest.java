package com.twinsession;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerLeftTest {
    private final UUID ORIGINAL_UUID = UUID.fromString("1c9ac288-7555-4d07-9280-126a773aeb70");
    private final String ORIGINAL_NAME = "MisterFish_";

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void removeSingleTwinTest(GameTestHelper context) {
        UUID twinUUID = UUID.randomUUID();
        Map<Integer, UUID> map = new HashMap<>();
        map.put(1, twinUUID);
        map.put(2, UUID.randomUUID());
        TwinSession.getTwinMap().put(ORIGINAL_UUID, map);

        GameProfile twinProfile = new GameProfile(twinUUID, ORIGINAL_NAME);
        ServerPlayer twinPlayer = FakePlayer.get(context.getLevel(), twinProfile);

        TwinSession.playerLeft(twinPlayer);

        context.assertTrue(
                TwinSession.getTwinMap().containsKey(ORIGINAL_UUID),
                "Original key should remain"
        );
        context.assertFalse(
                TwinSession.getTwinMap().get(ORIGINAL_UUID).containsValue(twinUUID),
                "Twin UUID should be removed"
        );

        TwinSession.getTwinMap().clear();
        context.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void removeLastFromTwinMapTest(GameTestHelper context) {
        UUID twinUUID = UUID.randomUUID();
        Map<Integer, UUID> map = new HashMap<>();
        map.put(1, twinUUID);
        TwinSession.getTwinMap().put(ORIGINAL_UUID, map);

        GameProfile twinProfile = new GameProfile(twinUUID, ORIGINAL_NAME);
        ServerPlayer twinPlayer = FakePlayer.get(context.getLevel(), twinProfile);

        TwinSession.playerLeft(twinPlayer);

        context.assertFalse(
                TwinSession.getTwinMap().containsKey(ORIGINAL_UUID),
                "Original key should be removed when no twins remain"
        );

        TwinSession.getTwinMap().clear();
        context.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void playerNotInMapTest(GameTestHelper context) {
        TwinSession.getTwinMap().clear();

        GameProfile profile = new GameProfile(UUID.randomUUID(), "Random");
        ServerPlayer randomPlayer = FakePlayer.get(context.getLevel(), profile);

        TwinSession.playerLeft(randomPlayer);

        context.assertTrue(
                TwinSession.getTwinMap().isEmpty(),
                "Twin map should remain empty when player not found"
        );

        context.succeed();
    }
}
