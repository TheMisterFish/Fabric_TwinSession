package com.twinsession;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJoinedTest {
    private final UUID ORIGINAL_UUID = UUID.fromString("1c9ac288-7555-4d07-9280-126a773aeb70");
    private final UUID EXPECTED_UUID = UUID.fromString("154b443e-554d-3301-850d-637ccc5e91ef");
    private final GameProfile sourceProfile = new GameProfile(ORIGINAL_UUID, "MisterFish_");
    private final GameProfile joiningProfile = new GameProfile(EXPECTED_UUID, "1_MisterFish_");

    @GameTest
    public void playerJoinedSurvivalNoOpTest(GameTestHelper context) {
        ServerPlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);
        sourcePlayer.setGameMode(GameType.SURVIVAL);
        TwinSession.getTwinMap().put(ORIGINAL_UUID, new HashMap<>());
        context.getLevel().getServer().getPlayerList().respawn(sourcePlayer, false, Entity.RemovalReason.CHANGED_DIMENSION);

        Map<Integer, UUID> twins = new HashMap<>();
        twins.put(0, EXPECTED_UUID);
        TwinSession.getTwinMap().put(ORIGINAL_UUID, twins);

        ServerPlayer joiningPlayer = FakePlayer.get(context.getLevel(), joiningProfile);
        TwinSession.playerJoined(joiningPlayer);

        context.assertValueEqual(
                sourcePlayer.gameMode.getGameModeForPlayer(),
                joiningPlayer.gameMode.getGameModeForPlayer(),
                Component.literal("Checking gamemode copy")
        );

        NameAndId joiningNameAndId = new NameAndId(joiningPlayer.getGameProfile());
        context.getLevel().getServer().getPlayerList().isOp(joiningNameAndId);
        context.assertFalse(context.getLevel().getServer().getPlayerList().isOp(joiningNameAndId),
                Component.literal("Checking op status copy"));

        TwinSession.getTwinMap().clear();
        context.getLevel().getServer().getPlayerList().disconnectAllPlayersWithProfile(ORIGINAL_UUID);
        context.succeed();
    }

    @GameTest
    public void playerJoinedCreativeWithOpTest(GameTestHelper context) {
        ServerPlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);
        sourcePlayer.setGameMode(GameType.CREATIVE);
        TwinSession.getTwinMap().put(ORIGINAL_UUID, new HashMap<>());
        context.getLevel().getServer().getPlayerList().respawn(sourcePlayer, false, Entity.RemovalReason.CHANGED_DIMENSION);

        NameAndId sourceNameAndId = new NameAndId(sourcePlayer.getGameProfile());
        context.getLevel().getServer().getPlayerList().op(sourceNameAndId);

        Map<Integer, UUID> twins = new HashMap<>();
        twins.put(0, EXPECTED_UUID);
        TwinSession.getTwinMap().put(ORIGINAL_UUID, twins);

        ServerPlayer joiningPlayer = FakePlayer.get(context.getLevel(), joiningProfile);
        TwinSession.playerJoined(joiningPlayer);

        context.assertValueEqual(
                sourcePlayer.gameMode.getGameModeForPlayer(),
                joiningPlayer.gameMode.getGameModeForPlayer(),
                Component.literal("Checking gamemode copy")
        );

        NameAndId joiningNameAndId = new NameAndId(joiningPlayer.getGameProfile());
        context.assertTrue(context.getLevel().getServer().getPlayerList().isOp(joiningNameAndId),
                Component.literal("Checking op status copy"));

        TwinSession.getTwinMap().clear();
        context.getLevel().getServer().getPlayerList().disconnectAllPlayersWithProfile(ORIGINAL_UUID);
        context.getLevel().getServer().getPlayerList().deop(sourceNameAndId);
        context.getLevel().getServer().getPlayerList().deop(joiningNameAndId);
        context.succeed();
    }
}
