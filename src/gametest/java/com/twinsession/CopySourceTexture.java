package com.twinsession;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.twinsession.config.ModConfigs;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.UUID;

public class CopySourceTexture {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void withTextureTest(GameTestHelper context) {
        GameProfile sourceProfile = new GameProfile(UUID.randomUUID(), "withTexture");

        FakePlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);
        sourcePlayer.getGameProfile().getProperties().put("textures", new Property("test", "test"));
        sourcePlayer.getGameProfile().getProperties().put("junk", new Property("test", "test"));

        context.getLevel().getServer().getPlayerList().respawn(sourcePlayer, false, Entity.RemovalReason.DISCARDED);

        GameProfile joiningProfile = TwinSession.createNewGameProfile(sourcePlayer.getGameProfile());
        FakePlayer joiningPlayer = FakePlayer.get(context.getLevel(), joiningProfile);
        TwinSession.copySourceTexture(joiningPlayer);

        context.assertTrue("1_withTexture".equals(joiningPlayer.getGameProfile().getName()), "Checking new player name");
        context.assertTrue(joiningPlayer.getGameProfile().getProperties().size() == 1, "Checking new player properties size");

        Collection<Property> textureProperty = joiningPlayer.getGameProfile().getProperties().get("textures");
        context.assertTrue(sourcePlayer.getGameProfile().getProperties().get("textures").equals(textureProperty), "Checking new player textures property");

        TwinSession.getTwinMap().clear();
        context.succeed();
    }


    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void withTexturePropertyDisabledTest(GameTestHelper context) {
        GameProfile sourceProfile = new GameProfile(UUID.randomUUID(), "withoutTexture");

        FakePlayer sourcePlayer = FakePlayer.get(context.getLevel(), sourceProfile);
        sourcePlayer.getGameProfile().getProperties().put("textures", new Property("test", "test"));
        sourcePlayer.getGameProfile().getProperties().put("junk", new Property("test", "test"));

        context.getLevel().getServer().getPlayerList().respawn(sourcePlayer, false, Entity.RemovalReason.DISCARDED);

        GameProfile joiningProfile = TwinSession.createNewGameProfile(sourcePlayer.getGameProfile());
        FakePlayer joiningPlayer = FakePlayer.get(context.getLevel(), joiningProfile);

        ModConfigs.COPY_TEXTURE = false;
        TwinSession.copySourceTexture(joiningPlayer);

        context.assertTrue("1_withoutTexture".equals(joiningPlayer.getGameProfile().getName()), "Checking new player name");
        context.assertTrue(joiningPlayer.getGameProfile().getProperties().isEmpty(), "Checking new player texture property");

        TwinSession.getTwinMap().clear();
        ModConfigs.COPY_TEXTURE = true;
        context.succeed();
    }
}
