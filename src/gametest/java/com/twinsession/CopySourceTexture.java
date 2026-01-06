package com.twinsession;

import com.google.common.collect.ImmutableListMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.twinsession.config.ModConfigs;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class CopySourceTexture {
    @GameTest
    public void withTextureTest(GameTestHelper context) {
        GameProfile sourceProfile = new GameProfile(UUID.randomUUID(), "withTexture");

        FakePlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);
        sourcePlayer.getGameProfile().getProperties().put("textures", new Property("test", "test"));
        sourcePlayer.getGameProfile().getProperties().put("junk", new Property("test", "test"));

        context.getLevel().getServer().getPlayerList().respawn(sourcePlayer, false, Entity.RemovalReason.DISCARDED);

        GameProfile joiningProfile = TwinSession.createNewGameProfile(sourcePlayer);
        FakePlayer joiningPlayer = FakePlayer.get(context.getLevel(), joiningProfile);
        TwinSession.copySourceTexture(joiningPlayer);

        PropertyMap expectedProperties = new PropertyMap();
        expectedProperties.put("textures", new Property("test", "test"));

        context.assertValueEqual("1_withTexture", joiningPlayer.getGameProfile().getName(), Component.literal("Checking new player name"));
        context.assertValueEqual(expectedProperties, joiningPlayer.getGameProfile().getProperties(), Component.literal("Checking new player texture property"));

        TwinSession.getTwinMap().clear();
        context.succeed();
    }


    @GameTest
    public void withTexturePropertyDisabledTest(GameTestHelper context) {
        GameProfile sourceProfile = new GameProfile(UUID.randomUUID(), "withoutTexture");

        FakePlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);
        sourcePlayer.getGameProfile().getProperties().put("textures", new Property("test", "test"));
        sourcePlayer.getGameProfile().getProperties().put("junk", new Property("test", "test"));

        context.getLevel().getServer().getPlayerList().respawn(sourcePlayer, false, Entity.RemovalReason.DISCARDED);

        GameProfile joiningProfile = TwinSession.createNewGameProfile(sourcePlayer);
        FakePlayer joiningPlayer = FakePlayer.get(context.getLevel(), joiningProfile);

        ModConfigs.COPY_TEXTURE = false;
        TwinSession.copySourceTexture(joiningPlayer);

        context.assertValueEqual("1_withoutTexture", joiningPlayer.getGameProfile().getName(), Component.literal("Checking new player name"));
        context.assertValueEqual(ImmutableListMultimap.of(), joiningPlayer.getGameProfile().getProperties(), Component.literal("Checking new player texture property"));

        TwinSession.getTwinMap().clear();
        ModConfigs.COPY_TEXTURE = true;
        context.succeed();
    }
}
