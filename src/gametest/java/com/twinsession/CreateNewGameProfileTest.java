package com.twinsession;

import com.mojang.authlib.GameProfile;
import com.twinsession.config.ModConfigs;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class CreateNewGameProfileTest {
    private final UUID ORIGINAL_UUID = UUID.fromString("1c9ac288-7555-4d07-9280-126a773aeb70");
    private final UUID EXPECTED_UUID = UUID.fromString("154b443e-554d-3301-850d-637ccc5e91ef");
    private final String ORIGINAL_NAME = "MisterFish_";
    private final String EXPECTED_NAME = "1_MisterFish_";
    private final String SECOND_EXPECTED_NAME = "2_MisterFish_";


    @GameTest
    public void withPrefix(GameTestHelper context) {
        GameProfile sourceProfile = new GameProfile(ORIGINAL_UUID, ORIGINAL_NAME);
        FakePlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);

        GameProfile joiningProfile = TwinSession.createNewGameProfile(sourcePlayer);

        context.assertValueEqual(EXPECTED_NAME, joiningProfile.getName(), Component.nullToEmpty("Checking new player name"));
        context.assertValueEqual(EXPECTED_UUID, joiningProfile.getId(), Component.nullToEmpty("Checking new player uuid"));

        TwinSession.getTwinMap().clear();
        context.succeed();
    }

    @GameTest
    public void withPrefixSecond(GameTestHelper context) {
        GameProfile sourceProfile = new GameProfile(ORIGINAL_UUID, ORIGINAL_NAME);
        FakePlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);

        GameProfile firstProfile = TwinSession.createNewGameProfile(sourcePlayer);
        GameProfile secondProfile = TwinSession.createNewGameProfile(sourcePlayer);

        context.assertValueEqual(EXPECTED_NAME, firstProfile.getName(), Component.nullToEmpty("Checking new player name"));
        context.assertValueEqual(SECOND_EXPECTED_NAME, secondProfile.getName(), Component.nullToEmpty("Checking new player name"));

        TwinSession.getTwinMap().clear();
        context.succeed();
    }

    @GameTest
    public void withoutPrefix(GameTestHelper context) {
        ModConfigs.PREFIX_WITH_NUMBER = false;
        GameProfile sourceProfile = new GameProfile(ORIGINAL_UUID, ORIGINAL_NAME);
        FakePlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);

        GameProfile joiningProfile = TwinSession.createNewGameProfile(sourcePlayer);

        context.assertValueEqual(ORIGINAL_NAME, joiningProfile.getName(), Component.nullToEmpty("Checking new player name"));
        context.assertValueEqual(EXPECTED_UUID, joiningProfile.getId(), Component.nullToEmpty("Checking new player uuid"));

        TwinSession.getTwinMap().clear();
        ModConfigs.PREFIX_WITH_NUMBER = true;
        context.succeed();
    }

}
