package com.twinsession.mixin;

import net.minecraft.gametest.framework.GameTestServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GameTestServer.class)
public abstract class GameTestServerMixin {

        @ModifyArgs(
                method = "initServer",
                at = @At(
                        value = "INVOKE",
                        target = "net.minecraft.gametest.framework.GameTestServer$1.<init>(Lnet/minecraft/gametest/framework/GameTestServer;Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/world/level/storage/PlayerDataStorage;I)V"
                )
        )
        private void modifyAnonymousPlayerListArgs(Args args) {
            args.set(4, 20); // Index 4 is the int maxPlayers
        }
}
