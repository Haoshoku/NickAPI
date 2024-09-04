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

package xyz.haoshoku.nick.version.v1_20_R3;

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
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import xyz.haoshoku.nick.user.UserHandler;
import xyz.haoshoku.nick.utils.ReflectionUtils;
import xyz.haoshoku.nick.version.VersionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

public class Handler_v1_20_R3 implements VersionHandler {

    @Override
    public void pluginOnEnable( Plugin plugin ) {
        Bukkit.getScheduler().runTask( plugin, () -> {
            for ( var player : Bukkit.getOnlinePlayers() ) {
                UserHandler.createUser( player.getUniqueId() );
                var user = UserHandler.getUser( player.getUniqueId() );
                this.setPlayerData( player );
                user.setInitialized( true );
                this.sendPacket( player, plugin );
            }
        } );
    }

    @Override
    public void pluginOnDisable( Plugin plugin ) {
        for ( var player : Bukkit.getOnlinePlayers() ) {
            var user = UserHandler.getUser( player.getUniqueId() );
            if ( user != null ) {
                var profile = ((CraftPlayer) player).getProfile();
                profile.getProperties().put( "textures", new Property( "textures",
                        user.getOriginalValue(), user.getOriginalSignature() ) );
                ReflectionUtils.setField( profile, "id", user.getOriginalUniqueId() );
                ReflectionUtils.setField( profile, "name", user.getOriginalName() );

                for ( Player online : Bukkit.getOnlinePlayers() ) {
                    if ( player != online )
                        this.removeCurrentUniqueId( player, online );
                }
                UserHandler.deleteUser( player.getUniqueId() );
            }
        }
    }

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

                if ( packet instanceof ClientboundBundlePacket bundlePacket ) {
                    for ( var subPacket : bundlePacket.subPackets() ) {
                        if ( subPacket instanceof ClientboundAddEntityPacket addEntityPacket ) {
                            var uuid = (UUID) ReflectionUtils.getField( addEntityPacket, "d" );
                            var receivedUser = UserHandler.getUser( uuid );
                            var playerUser = UserHandler.getUser( player.getUniqueId() );

                            if ( playerUser != null && receivedUser != null && receivedUser.getNickedUniqueId() != null
                                    && !playerUser.getBypassNickSet().contains( uuid ) )
                                ReflectionUtils.setField( addEntityPacket, "d", receivedUser.getNickedUniqueId() );
                        }
                    }
                }


                if ( packet instanceof ClientboundPlayerInfoUpdatePacket infoUpdatePacket ) {
                    var entries = infoUpdatePacket.entries();
                    var entriesListCopied = new ArrayList<>( entries );

                    for ( int i = 0; i < entriesListCopied.size(); i++ ) {
                        var entry = entriesListCopied.get( i );
                        if ( entry.profileId().equals( player.getUniqueId() ) ) continue;
                        var receivedUser = UserHandler.getUser( entry.profileId() );
                        var receivedPlayer = Bukkit.getPlayer( entry.profileId() );
                        if ( receivedUser == null || receivedPlayer == null || !receivedPlayer.isOnline() ) continue;
                        if ( receivedUser.getNickedUniqueId() == null || receivedUser.getNickedName() == null
                                || receivedUser.getNickedValue() == null || receivedUser.getNickedSignature() == null ) continue;
                        if ( UserHandler.getUser( player.getUniqueId() ) == null ) continue;


                        UUID packetUniqueId;
                        String packetName, packetValue, packetSignature;

                        var playerUser = UserHandler.getUser( player.getUniqueId() );

                        if ( playerUser.getBypassNickSet().contains( receivedUser.getOriginalUniqueId() ) ) {
                            packetUniqueId = receivedUser.getOriginalUniqueId();
                            packetName = receivedUser.getOriginalName();
                            packetValue = receivedUser.getOriginalValue();
                            packetSignature = receivedUser.getOriginalSignature();
                        } else {
                            packetUniqueId = receivedUser.getNickedUniqueId();
                            packetName = receivedUser.getNickedName();
                            packetValue = receivedUser.getNickedValue();
                            packetSignature = receivedUser.getNickedSignature();
                        }

                        var newGameProfile = new GameProfile( packetUniqueId, packetName );
                        newGameProfile.getProperties().put( "textures", new Property( "textures", packetValue,
                                packetSignature) );

                        var newEntry = new ClientboundPlayerInfoUpdatePacket.Entry( packetUniqueId, newGameProfile,
                                entry.listed(), entry.latency(), entry.gameMode(), entry.displayName(), entry.chatSession() );

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

        String name, value, signature;

        if ( user.getBypassNickSet().contains( player.getUniqueId() ) ) {
            name = user.getOriginalName();
            value = user.getOriginalValue();
            signature = user.getOriginalSignature();
        } else {
            name = user.getNickedName();
            value = user.getNickedValue();
            signature = user.getNickedSignature();
        }

        var newProfile = new GameProfile( user.getNickedUniqueId(), name );
        newProfile.getProperties().removeAll( "textures" );
        newProfile.getProperties().put( "textures", new Property( "textures", value, signature ) );

        var enumActions =
                EnumSet.of( ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME );

        var updateInfoPacket = new ClientboundPlayerInfoUpdatePacket( ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, serverPlayer ); // DECORATION
        ReflectionUtils.setField( updateInfoPacket, "a", enumActions );
        ReflectionUtils.setField( updateInfoPacket, "b", Collections.singletonList( new ClientboundPlayerInfoUpdatePacket.Entry( player.getUniqueId(), newProfile, true, craftPlayer.getPing(),
                serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.listName, Optionull.map( serverPlayer.getChatSession(), RemoteChatSession::asData ) ) ) );


        var playerList = ((CraftServer) Bukkit.getServer()).getServer().getPlayerList();
        var location = player.getLocation().clone();

        serverPlayer.connection.send( removeInfoPacket );
        serverPlayer.connection.send( updateInfoPacket );

        playerList.respawn( serverPlayer, serverPlayer.serverLevel(), true, location, true, PlayerRespawnEvent.RespawnReason.PLUGIN );

        for ( var online : Bukkit.getOnlinePlayers() ) {
            if ( online != player ) {
                if ( online.canSee( player ) ) {
                    online.hidePlayer( plugin, player );
                    online.showPlayer( plugin, player );
                }
            }
        }

        user.setCurrentNicking( false );
    }

    private ChannelPipeline pipeline( Player player ) {
        var gamePacketListener = ((CraftPlayer) player).getHandle().connection;

        try {
            var connectionField = gamePacketListener.getClass().getSuperclass().getDeclaredField( "c" );
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
        ((CraftPlayer) toPlayer).getHandle().connection.send( new ClientboundPlayerInfoRemovePacket(
                Collections.singletonList( playerUser.getNickedUniqueId() ) ) );
    }

    @Override
    public void setPlayerData( Player player ) {
        var user = UserHandler.getUser( player.getUniqueId() );
        var profile = ReflectionUtils.getProfile( player );

        user.setOriginalName( player.getName() );
        user.setOriginalUniqueId( player.getUniqueId() );
        user.setNickedName( player.getName() );
        user.setNickedUniqueId( player.getUniqueId() );

        for ( var property : profile.getProperties().get( "textures" ) ) {
            user.setOriginalValue( property.value() );
            user.setOriginalSignature( property.signature() );
            user.setNickedValue( property.value() );
            user.setNickedSignature( property.signature() );
        }
    }

}
