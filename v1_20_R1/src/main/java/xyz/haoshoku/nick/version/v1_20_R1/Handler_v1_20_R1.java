/*
 * MIT License
 *
 * Copyright (c) 2023 Haoshoku
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.haoshoku.nick.version.v1_20_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.Optionull;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.BiomeManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.haoshoku.nick.user.UserHandler;
import xyz.haoshoku.nick.utils.ReflectionUtils;
import xyz.haoshoku.nick.version.VersionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

public class Handler_v1_20_R1 implements VersionHandler {

    public void inject( Player player ) {
        ChannelDuplexHandler duplexHandler = new ChannelDuplexHandler() {

            @Override
            public void write( ChannelHandlerContext ctx, Object packet, ChannelPromise promise ) throws Exception {

                if ( packet instanceof ClientboundPlayerChatPacket chatPacket ) {
                    var content = chatPacket.unsignedContent();
                    if ( content == null )
                        content = Component.literal( chatPacket.body().content() );
                    var chatType = chatPacket.chatType().resolve(
                            ((CraftPlayer) player).getHandle().level().registryAccess() );

                    ((CraftPlayer) player).getHandle().connection.send(
                            new ClientboundSystemChatPacket( chatType.orElseThrow().decorate( content ), false ) );
                    return;
                }

                if ( packet instanceof ClientboundPlayerInfoUpdatePacket infoUpdatePacket ) {
                    var entries = infoUpdatePacket.entries();
                    var entriesListCopied = new ArrayList<>( entries );

                    for ( int i = 0; i < entriesListCopied.size(); i++ ) {
                        var entry = entriesListCopied.get( i );
                        if ( entry.profileId().equals( player.getUniqueId() ) ) continue;
                        var user = UserHandler.getUser( entry.profileId() );
                        var receivedPlayer = Bukkit.getPlayer( entry.profileId() );
                        if ( user == null || receivedPlayer == null || !receivedPlayer.isOnline() ) continue;

                        var nickedUniqueId = user.getNickedUniqueId();

                        var newGameProfile = new GameProfile( nickedUniqueId, user.getNickedName() );
                        newGameProfile.getProperties().put( "textures", new Property( "textures", user.getNickedValue(), user.getNickedSignature() ) );

                        var newEntry = new ClientboundPlayerInfoUpdatePacket.Entry( nickedUniqueId, newGameProfile, entry.listed(), entry.latency(),
                                entry.gameMode(), entry.displayName(), entry.chatSession() );

                        entriesListCopied.set( i, newEntry );
                    }

                    ReflectionUtils.setField( infoUpdatePacket, "b", entriesListCopied );
                }


                super.write( ctx, packet, promise );
            }
        };

        var pipeline = this.pipeline( player );
        pipeline.addBefore( "packet_handler", "nickapi", duplexHandler );
    }

    public void sendPacket( Player player, Plugin plugin ) {
        var craftPlayer = (CraftPlayer) player;
        var serverPlayer = craftPlayer.getHandle();

        var user = UserHandler.getUser( player.getUniqueId() );
        if ( !player.isOnline() || user == null ) return;

        var removeInfoPacket = new ClientboundPlayerInfoRemovePacket(
                Collections.singletonList( player.getUniqueId() ) );

        var profile = ((CraftPlayer) player).getProfile();

        ReflectionUtils.setField( profile, "id", user.getNickedUniqueId() );

        var newProfile = new GameProfile( user.getNickedUniqueId(), user.getNickedName() );
        newProfile.getProperties().removeAll( "textures" );
        newProfile.getProperties().put( "textures", new Property( "textures", user.getNickedValue(), user.getNickedSignature() ) );

        var enumActions =
                EnumSet.of( ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME );

        var updateInfoPacket = new ClientboundPlayerInfoUpdatePacket( ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, serverPlayer ); // DECORATION
        ReflectionUtils.setField( updateInfoPacket, "a", enumActions );
        ReflectionUtils.setField( updateInfoPacket, "b", Collections.singletonList( new ClientboundPlayerInfoUpdatePacket.Entry( player.getUniqueId(), newProfile, true, craftPlayer.getPing(),
                serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.listName, Optionull.map( serverPlayer.getChatSession(), RemoteChatSession::asData ) ) ) );


        var playerList = MinecraftServer.getServer().getPlayerList();
        var location = player.getLocation().clone();
        var serverLevel = serverPlayer.serverLevel();

        var respawnPacket = new ClientboundRespawnPacket( serverLevel.dimensionTypeId(), serverLevel.dimension(),
                BiomeManager.obfuscateSeed( serverLevel.getSeed() ), serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.gameMode.getPreviousGameModeForPlayer(),
                serverLevel.isDebug(), serverLevel.isFlat(), (byte) 0, serverPlayer.getLastDeathLocation(), serverPlayer.getPortalCooldown() );


        serverPlayer.connection.send( removeInfoPacket );
        serverPlayer.connection.send( updateInfoPacket );
        serverPlayer.connection.send( respawnPacket );

        serverPlayer.onUpdateAbilities();
        player.teleport( location );
        playerList.sendAllPlayerInfo( serverPlayer );
        player.setLevel( player.getLevel() );

        for ( var online : Bukkit.getOnlinePlayers() ) {
            online.hidePlayer( plugin, player );
            online.showPlayer( plugin, player );
        }

        user.setCurrentNicking( false );
    }

    private ChannelPipeline pipeline( Player player ) {
        var gamePacketListener = ((CraftPlayer) player).getHandle().connection;

        try {
            var connectionField = gamePacketListener.getClass().getDeclaredField( "h" );
            connectionField.setAccessible( true );
            var connection = (Connection) connectionField.get( gamePacketListener );
            return connection.channel.pipeline();
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void removeCurrentUniqueId( Player player, Player toPlayer ) {
        var playerUser = UserHandler.getUser( player.getUniqueId() );
        if ( playerUser == null ) return;
        ( (CraftPlayer) toPlayer ).getHandle().connection.send( new ClientboundPlayerInfoRemovePacket(
                Collections.singletonList( playerUser.getNickedUniqueId() ) ) );
    }

}
