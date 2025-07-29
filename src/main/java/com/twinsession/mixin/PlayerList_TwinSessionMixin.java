package com.twinsession.mixin;

import com.twinsession.TwinSession;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerList_TwinSessionMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void afterPlaceNewPlayer(Connection clientConnection, ServerPlayer playerIn, CallbackInfo ci) {
        TwinSession.playerJoined(playerIn);
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0))
    private void injectBeforePlayerInfoUpdate(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
        // Have to set before joining, otherwise it will not render correctly.
        TwinSession.copySourceTexture(serverPlayer);
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void afterPlayerDisconnect(ServerPlayer playerOut, CallbackInfo ci) {
        TwinSession.playerLeft(playerOut);
    }
}
