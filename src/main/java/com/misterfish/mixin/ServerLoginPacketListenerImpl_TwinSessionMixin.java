package com.misterfish.mixin;

import com.misterfish.TwinSession;
import com.misterfish.config.ModConfigs;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteListEntry;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

        if (playerList.getPlayer(gameProfile.getId()) != null) {
            if (TwinSession.canJoin(playerList.getPlayer(gameProfile.getId()))) {

                // Modify the existing profile to allow the duplicate login
                GameProfile modifiedProfile = TwinSession.createNewGameProfile(gameProfile);

                this.authenticatedProfile = modifiedProfile;
                LOGGER.info("Modified profile for duplicate login of {}: {} (New UUID: {})",
                        gameProfile.getName(), modifiedProfile.getName(), modifiedProfile.getId());

                // Add whitelist
                if (ModConfigs.AUTO_WHITELIST && playerList.isUsingWhitelist() && playerList.isWhiteListed(gameProfile)) {
                    UserWhiteListEntry whitelistEntry = new UserWhiteListEntry(modifiedProfile);
                    playerList.getWhiteList().add(whitelistEntry);
                }

                this.finishLoginAndWaitForClient(modifiedProfile);
                ci.cancel();
            } else {
                LOGGER.info("Could not connect {} because of too many connections already", gameProfile.getName());
                try {
                    playerList.getPlayer(gameProfile.getId()).disconnect();
                } catch (Exception e){
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}