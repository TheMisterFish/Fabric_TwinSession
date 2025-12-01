package com.twinsession.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.twinsession.TwinSession;
import com.twinsession.config.ModConfigs;
import com.twinsession.patch.LuckPermsPatch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteListEntry;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImpl_TwinSessionMixin {
    @Shadow
    @Final
    MinecraftServer server;
    @Shadow
    GameProfile authenticatedProfile;

    @Shadow
    abstract void finishLoginAndWaitForClient(GameProfile profile);

    @Shadow
    static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "verifyLoginAndFinishConnectionSetup", at = @At("HEAD"), cancellable = true)
    private void onVerifyLoginAndFinishConnectionSetup(GameProfile gameProfile, CallbackInfo ci) {
        PlayerList playerList = this.server.getPlayerList();
        ServerPlayer serverPlayer = playerList.getPlayer(gameProfile.id());

        if (serverPlayer != null) {
            if (TwinSession.canJoin(serverPlayer)) {

                // Modify the existing profile to allow the duplicate login
                GameProfile modifiedProfile = TwinSession.createNewGameProfile(gameProfile, serverPlayer);

                this.authenticatedProfile = modifiedProfile;
                LOGGER.info("Modified profile for duplicate login of {}: {} (New UUID: {})",
                        gameProfile.name(), modifiedProfile.name(), modifiedProfile.id());

                // Add whitelist
                NameAndId nameAndId = new NameAndId(gameProfile.id(), gameProfile.name());
                NameAndId modifiedProfileNameAndId = new NameAndId(modifiedProfile.id(), modifiedProfile.name());
                if (ModConfigs.AUTO_WHITELIST && playerList.isUsingWhitelist() && playerList.isWhiteListed(nameAndId)) {
                    UserWhiteListEntry whitelistEntry = new UserWhiteListEntry(modifiedProfileNameAndId);
                    playerList.getWhiteList().add(whitelistEntry);
                }

                // LuckPerms patch
                LuckPermsPatch.playerJoined(gameProfile.id(), modifiedProfile.id());

                this.finishLoginAndWaitForClient(modifiedProfile);
                ci.cancel();
            } else {
                LOGGER.info("Could not connect {} because of too many connections already", gameProfile.name());
                try {
                    Objects.requireNonNull(playerList.getPlayer(gameProfile.id())).disconnect();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}