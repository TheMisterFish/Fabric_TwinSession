package com.twinsession;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.twinsession.config.ModConfigs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

import static net.minecraft.world.level.block.Blocks.LAVA;

public class TwinSession implements ModInitializer {
    public static final String MOD_ID = "twinsession";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Map<UUID, Map<Integer, UUID>> twinMap = new HashMap<>();

    private static final RandomSource random = RandomSource.create();

    @Override
    public void onInitialize() {
        ModConfigs.registerConfigs();

        if (FabricLoader.getInstance().isModLoaded("luckperms"))
            LOGGER.info("LuckPerms API is available, TwinSession will copy permissions to duplicate users.");
    }

    public static GameProfile createNewGameProfile(ServerPlayer sourcePlayer) {
        GameProfile gameProfile = sourcePlayer.getGameProfile();
        UUID originalUUID = gameProfile.id();

        Map<Integer, UUID> uuidMap = twinMap.computeIfAbsent(originalUUID, key -> new HashMap<>());

        int nextPosition = 1;
        while (uuidMap.containsKey(nextPosition)) {
            nextPosition++;
        }

        String newName = StringUtils.left(nextPosition + "_" + gameProfile.name(), 16);
        UUID newUUID = generateUUIDFromName(newName + "$");

        uuidMap.put(nextPosition, newUUID);

        String nameToUse = ModConfigs.PREFIX_WITH_NUMBER ? newName : gameProfile.name();

        if (ModConfigs.COPY_TEXTURE) {
            Multimap<String, Property> map = ArrayListMultimap.create();

            sourcePlayer.getGameProfile().properties().get("textures")
                    .forEach(property -> map.put("textures", property));

            return new GameProfile(newUUID, nameToUse, new PropertyMap(map));
        }

        return new GameProfile(newUUID, nameToUse);
    }

    public static void playerJoined(@NotNull ServerPlayer joiningPlayer) {
        joiningPlayer.level().getServer();

        ServerPlayer sourcePlayer = getSourcePlayer(joiningPlayer);

        if (sourcePlayer != null) {
            copyPlayerOpStatus(sourcePlayer, joiningPlayer);
            copyPlayerGamemode(sourcePlayer, joiningPlayer);

            if (ModConfigs.SPAWN_NEAR_PLAYER && !playerDataExists(joiningPlayer.level().getServer(), joiningPlayer.getGameProfile().id())) {
                spawnPlayerNearby(joiningPlayer, sourcePlayer);
            }
        }
    }

    public static boolean canJoin(ServerPlayer joiningPlayer) {
        Map<Integer, UUID> mapEntry = twinMap.get(joiningPlayer.getUUID());
        if (mapEntry == null) {
            return true;
        }
        return mapEntry.size() + 1 < ModConfigs.MAX_PLAYERS;
    }

    public static void playerLeft(ServerPlayer serverPlayer) {
        UUID playerUUID = serverPlayer.getGameProfile().id();
        List<UUID> keysToRemove = new ArrayList<>();

        twinMap.forEach((originalUUID, uuidMap) -> {
            Integer positionToRemove = null;
            for (Map.Entry<Integer, UUID> entry : uuidMap.entrySet()) {
                if (entry.getValue().equals(playerUUID)) {
                    positionToRemove = entry.getKey();
                    break;
                }
            }

            if (positionToRemove != null) {
                uuidMap.remove(positionToRemove);

                if (uuidMap.isEmpty()) {
                    keysToRemove.add(originalUUID);
                }
            }
        });

        keysToRemove.forEach(twinMap::remove);
    }

    private static ServerPlayer getSourcePlayer(ServerPlayer joiningPlayer) {
        UUID searchUUID = joiningPlayer.getGameProfile().id();

        for (Map.Entry<UUID, Map<Integer, UUID>> entry : twinMap.entrySet()) {
            Map<Integer, UUID> uuidMap = entry.getValue();

            if (uuidMap.containsValue(searchUUID)) {
                UUID originalUUID = entry.getKey();
                return joiningPlayer.level().getServer().getPlayerList().getPlayer(originalUUID);
            }
        }
        return null;
    }

    private static UUID generateUUIDFromName(String name) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(name.getBytes(StandardCharsets.UTF_8));

            long mostSigBits = 0;
            long leastSigBits = 0;

            for (int i = 0; i < 8; i++) {
                mostSigBits = (mostSigBits << 8) | (hash[i] & 0xff);
            }
            for (int i = 8; i < 16; i++) {
                leastSigBits = (leastSigBits << 8) | (hash[i] & 0xff);
            }

            mostSigBits &= ~0x000000000000F000L;
            mostSigBits |= 0x0000000000003000L;

            leastSigBits &= ~(0xc000000000000000L);
            leastSigBits |= 0x8000000000000000L;

            return new UUID(mostSigBits, leastSigBits);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return UUID.randomUUID();
        }
    }

    private static void copyPlayerOpStatus(ServerPlayer source, ServerPlayer target) {
        if (!ModConfigs.AUTO_OP) {
            return;
        }

        PlayerList playerList = target.level().getServer().getPlayerList();
        NameAndId sourceNameAndId = new NameAndId(source.getGameProfile());
        NameAndId targetNameAndId = new NameAndId(target.getGameProfile());
        if (playerList.isOp(sourceNameAndId)) {
            playerList.op(targetNameAndId);
        }
    }

    private static void copyPlayerGamemode(ServerPlayer source, ServerPlayer target) {
        target.setGameMode(source.gameMode.getGameModeForPlayer());
    }

    private static boolean playerDataExists(MinecraftServer server, UUID playerUUID) {
        Path playerDataDir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);

        Path playerDataFile = playerDataDir.resolve(playerUUID.toString() + ".dat");

        return Files.exists(playerDataFile);
    }

    private static void spawnPlayerNearby(ServerPlayer joiningPlayer, Player targetPlayer) {
        if (!(targetPlayer.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetPos = targetPlayer.position();
        boolean isCreativeFlying = targetPlayer.isCreative() && targetPlayer.getAbilities().flying;
        boolean isOnNetherRoof = isPlayerOnNetherRoof(serverLevel, targetPos);

        double newX = targetPos.x;
        double newY = targetPos.y;
        double newZ = targetPos.z;
        boolean validPosition = false;
        int distanceThreshold = 3;
        int spawn_diameter = ModConfigs.SPAWN_NEAR_PLAYER_RADIUS * 2;

        while (!validPosition && distanceThreshold >= 1) {
            for (int attempt = 0; attempt < 10; attempt++) {
                double xOffset, zOffset, distance;

                do {
                    xOffset = (random.nextDouble() - 0.5) * spawn_diameter;
                    zOffset = (random.nextDouble() - 0.5) * spawn_diameter;
                    distance = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
                } while (distance < distanceThreshold);

                newX = targetPos.x + xOffset;
                newZ = targetPos.z + zOffset;

                if (isCreativeFlying) {
                    double yOffset = (random.nextDouble() - 0.5) * 6;
                    newY = targetPos.y + yOffset;
                    validPosition = true;
                    break;
                } else {
                    int potentialY = findSafeYWithinRange(serverLevel, new BlockPos((int) newX, (int) targetPos.y, (int) newZ), 3);
                    if (potentialY != -1) {
                        if (!isOnNetherRoof && potentialY > serverLevel.getLogicalHeight()) {
                            continue;
                        }
                        newY = potentialY;
                        validPosition = true;
                        break;
                    }
                }
            }

            if (!validPosition) {
                distanceThreshold--;
            }
        }

        if (!validPosition) {
            newX = targetPos.x;
            newY = targetPos.y;
            newZ = targetPos.z;
        }

        joiningPlayer.teleportTo(serverLevel, newX, newY, newZ, Set.of(), joiningPlayer.getYRot(), joiningPlayer.getXRot(), true);

        if (isCreativeFlying) {
            Abilities abilities = joiningPlayer.getAbilities();
            abilities.mayfly = true;
            abilities.flying = true;
            joiningPlayer.onUpdateAbilities();
        }
    }

    private static int findSafeYWithinRange(ServerLevel world, BlockPos pos, int range) {
        int minY = Math.max(world.getMinY(), pos.getY() - range);
        int maxY = Math.min(world.getMaxY() - 1, pos.getY() + range);

        for (int y = maxY; y >= minY; y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (isSafeSpawn(world, checkPos)) {
                return y;
            }
        }

        return -1;
    }

    private static boolean isSafeSpawn(ServerLevel world, BlockPos pos) {
        return world.getBlockState(pos).isAir() &&
                world.getBlockState(pos.above()).isAir() &&
                !world.getBlockState(pos.below()).isAir() &&
                !world.getBlockState(pos.below()).is(LAVA);
    }

    private static boolean isPlayerOnNetherRoof(ServerLevel world, Vec3 pos) {
        return world.dimensionType().hasCeiling() && pos.y >= world.getLogicalHeight();
    }

    @VisibleForTesting
    public static Map<UUID, Map<Integer, UUID>> getTwinMap(){
        return twinMap;
    }
}