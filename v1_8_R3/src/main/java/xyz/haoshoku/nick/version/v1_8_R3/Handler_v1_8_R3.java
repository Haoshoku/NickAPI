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

package xyz.haoshoku.nick.version.v1_8_R3;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.haoshoku.nick.user.User;
import xyz.haoshoku.nick.user.UserHandler;
import xyz.haoshoku.nick.utils.ReflectionUtils;
import xyz.haoshoku.nick.version.VersionHandler;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Handler_v1_8_R3 implements VersionHandler {

    public void inject( Player player ) {
        ChannelDuplexHandler duplexHandler = new ChannelDuplexHandler() {

            @Override
            public void write( ChannelHandlerContext ctx, Object packet, ChannelPromise promise ) throws Exception {

                if ( packet instanceof PacketPlayOutPlayerInfo ) {
                    PacketPlayOutPlayerInfo playerInfoPacket = (PacketPlayOutPlayerInfo) packet;
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction infoAction = (PacketPlayOutPlayerInfo.EnumPlayerInfoAction) ReflectionUtils.getField( playerInfoPacket, "a" );
                    List<PacketPlayOutPlayerInfo.PlayerInfoData> playerInfoDataList = (List<PacketPlayOutPlayerInfo.PlayerInfoData>)
                            ReflectionUtils.getField( playerInfoPacket, "b" );

                    if ( infoAction != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER ) {
                        for ( int i = 0; i < playerInfoDataList.size(); i++ ) {
                            PacketPlayOutPlayerInfo.PlayerInfoData infoData = playerInfoDataList.get( i );
                            UUID receivedUUID = infoData.a().getId();
                            if ( receivedUUID.equals( player.getUniqueId() ) ) continue;
                            User user = UserHandler.getUser( receivedUUID );
                            Player receivedPlayer = Bukkit.getPlayer( receivedUUID );
                            if ( user == null || receivedPlayer == null || !receivedPlayer.isOnline() ) continue;

                            UUID nickedUniqueId = user.getNickedUniqueId();

                            GameProfile newGameProfile = new GameProfile( nickedUniqueId, user.getNickedName() );
                            newGameProfile.getProperties().put( "textures", new Property( "textures", user.getNickedValue(), user.getNickedSignature() ) );

                            PacketPlayOutPlayerInfo.PlayerInfoData newInfoData =
                                    playerInfoPacket.new PlayerInfoData( newGameProfile, infoData.b(), infoData.c(), infoData.d() );
                            playerInfoDataList.set( i, newInfoData );
                        }
                    }
                }

                if ( packet instanceof PacketPlayOutNamedEntitySpawn ) {
                    PacketPlayOutNamedEntitySpawn entitySpawnPacket = (PacketPlayOutNamedEntitySpawn) packet;
                    UUID uuid = (UUID) ReflectionUtils.getField( entitySpawnPacket, "b" );
                    User user = UserHandler.getUser( uuid );

                    if ( user != null )
                        ReflectionUtils.setField( entitySpawnPacket, "b", user.getNickedUniqueId() );
                }

                super.write( ctx, packet, promise );
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore( "packet_handler", "nickapi", duplexHandler );
    }

    public void sendPacket( Player player, Plugin plugin ) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( !player.isOnline() || user == null ) return;

        PacketPlayOutPlayerInfo removeInfoPacket = new PacketPlayOutPlayerInfo
                ( PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer );

        GameProfile newProfile = new GameProfile( player.getUniqueId(), user.getNickedName() );
        newProfile.getProperties().removeAll( "textures" );
        newProfile.getProperties().put( "textures", new Property( "textures", user.getNickedValue(), user.getNickedSignature() ) );


        PacketPlayOutPlayerInfo addInfoPacket = new PacketPlayOutPlayerInfo( PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER ); // DECORATION
        ReflectionUtils.setField( addInfoPacket, "b", Collections.singletonList( addInfoPacket.new PlayerInfoData( newProfile, entityPlayer.ping,
                entityPlayer.playerInteractManager.getGameMode(), entityPlayer.listName ) ) );

        entityPlayer.u().getPlayerChunkMap().removePlayer( entityPlayer );
        Location location = player.getLocation().clone();
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();

        int actualDimension = worldServer.getWorld().getEnvironment().getId();

        entityPlayer.playerConnection.sendPacket( removeInfoPacket );
        entityPlayer.playerConnection.sendPacket( addInfoPacket );


        entityPlayer.playerConnection.sendPacket( new PacketPlayOutRespawn( actualDimension, worldServer.getDifficulty(),
                worldServer.getWorldData().getType(), entityPlayer.playerInteractManager.getGameMode() ) );
        entityPlayer.playerConnection.sendPacket( new PacketPlayOutRespawn( actualDimension, worldServer.getDifficulty(),
                worldServer.getWorldData().getType(), entityPlayer.playerInteractManager.getGameMode() ) );

        entityPlayer.u().getPlayerChunkMap().addPlayer( entityPlayer );

        player.teleport( location.clone().add( 75, 75, 75 ) );
        player.teleport( location );

        player.setFallDistance( 0F );

        for ( Player online : Bukkit.getOnlinePlayers() ) {
            online.hidePlayer( player );
            online.showPlayer( player );
        }

        PlayerList playerList = MinecraftServer.getServer().getPlayerList();
        entityPlayer.updateAbilities();
        playerList.updateClient( entityPlayer );
        entityPlayer.triggerHealthUpdate();

        user.setCurrentNicking( false );
    }

    @Override
    public void removeCurrentUniqueId( Player player, Player toPlayer ) {
        User playerUser = UserHandler.getUser( player.getUniqueId() );
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        if ( playerUser == null ) return;

        GameProfile profile = new GameProfile( playerUser.getNickedUniqueId(), playerUser.getNickedName() );
        PacketPlayOutPlayerInfo removeInfoPacket = new PacketPlayOutPlayerInfo( PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER );
        PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData = removeInfoPacket.new PlayerInfoData( profile, 0,
                entityPlayer.playerInteractManager.getGameMode(), entityPlayer.listName );
        ReflectionUtils.setField( removeInfoPacket, "b", Collections.singletonList( playerInfoData ) );;
        ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket( removeInfoPacket );
    }

}
