package com.misterfish.patch;

import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.misterfish.TwinSession.MOD_ID;

public class LuckPermsPatch {
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static boolean isLuckPermsLoaded = false;
    private static LuckPerms luckPermsApi = null;

    static {
        checkLuckPermsLoaded();
    }

    private static void checkLuckPermsLoaded() {
        isLuckPermsLoaded = FabricLoader.getInstance().isModLoaded("luckperms");
        if (isLuckPermsLoaded) {
            try {
                LOGGER.debug("Loading LuckPerms API");
                luckPermsApi = LuckPermsProvider.get();
            } catch (IllegalStateException e) {
                isLuckPermsLoaded = false;
                LOGGER.error("LuckPerms API is not available", e);
            }
        }
    }

    public static void playerJoined(UUID sourceUUID, UUID newUUID) {
        if (!isLuckPermsLoaded || luckPermsApi == null) {
            LOGGER.error("hoii?");
            return;
        }

        CompletableFuture.runAsync(() -> {
            User sourceUser = luckPermsApi.getUserManager().loadUser(sourceUUID).join();
            User newUser = luckPermsApi.getUserManager().loadUser(newUUID).join();

            if (sourceUser == null) {
                LOGGER.error("Failed to load LuckPerms user data for original player with UUID {}", sourceUUID);
                return;
            }

            if (newUser == null) {
                LOGGER.error("Failed to load LuckPerms user data for duplicate player with UUID {}", newUUID);
                return;
            }

            newUser.data().clear();
            for (Node node : sourceUser.getNodes()) {
                newUser.data().add(node);
            }

            newUser.setPrimaryGroup(sourceUser.getPrimaryGroup());

            luckPermsApi.getUserManager().saveUser(newUser);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while copying LuckPerms permissions", throwable);
            return null;
        });
    }
}