package com.twinsession.mixin;

import com.twinsession.TwinSession;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerList.class)
public class PlayerList_TwinSessionMixin {
    @Inject(method = "disconnectAllPlayersWithProfile", at = @At("HEAD"), cancellable = true)
    private void onDisconnectAllPlayersWithProfile(UUID uUID, CallbackInfoReturnable<Boolean> cir) {
        // Prevent disconnection of existing players
        cir.setReturnValue(false);
    }

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void afterPlaceNewPlayer(Connection clientConnection, ServerPlayer playerIn, CommonListenerCookie cookie, CallbackInfo ci) {
        TwinSession.playerJoined(playerIn);
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void afterPlayerDisconnect(ServerPlayer playerOut, CallbackInfo ci) {
        TwinSession.playerLeft(playerOut);
    }
}
