package com.twinsession;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Connection;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.debugchart.LocalSampleLogger;

import java.util.Objects;
import java.util.UUID;

public class TwinSessionGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void testDoubleLogin(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "TestPlayer");

        //first login
        handleClientLogin(server, profile);

        //second login
        handleClientLogin(server, profile);

        boolean playerJoined = server.getPlayerList().getPlayers().stream()
                .anyMatch(serverPlayer -> Objects.equals(serverPlayer.getGameProfile().getName(), "TestPlayer"));
        boolean twinPlayerJoined = server.getPlayerList().getPlayers().stream()
                .anyMatch(serverPlayer -> Objects.equals(serverPlayer.getGameProfile().getName(), "1_TestPlayer"));

        helper.assertTrue(playerJoined, String.format("Player with name %s was not found in the playerlist", profile.getId().toString()));
        helper.assertTrue(twinPlayerJoined, String.format("TwinSession player with name %s was not found in the playerlist", profile.getId().toString()));

        helper.succeed();
    }

    private static void handleClientLogin(MinecraftServer server, GameProfile profile) {
        GameTestServer gameTestServer = (GameTestServer) server;

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        Channel channel = new EmbeddedChannel(connection);

        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("splitter", new Varint21FrameDecoder(new BandwidthDebugMonitor((LocalSampleLogger) gameTestServer.getTickTimeLogger())));
        pipeline.addLast("prepender", new Varint21LengthFieldPrepender());

        ServerLoginPacketListenerImpl loginListener = new ServerLoginPacketListenerImpl(server, connection, true);
        connection.setListenerForServerboundHandshake(new ServerHandshakePacketListenerImpl(server, connection));

        ServerboundHelloPacket helloPacket = new ServerboundHelloPacket(profile.getName(), profile.getId());
        loginListener.handleHello(helloPacket);

        loginListener.tick();

        ServerboundLoginAcknowledgedPacket ackPacket = ServerboundLoginAcknowledgedPacket.INSTANCE;
        loginListener.handleLoginAcknowledgement(ackPacket);

        ServerConfigurationPacketListenerImpl configListener =
                (ServerConfigurationPacketListenerImpl) connection.getPacketListener();

        if (configListener == null) {
            throw new IllegalStateException("Configuration listener is null during test login");
        }

        configListener.returnToWorld();
        configListener.handleConfigurationFinished(ServerboundFinishConfigurationPacket.INSTANCE);
    }
}