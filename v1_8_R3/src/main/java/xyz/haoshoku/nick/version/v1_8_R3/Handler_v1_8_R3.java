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
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.haoshoku.nick.events.NickFinishEvent;
import xyz.haoshoku.nick.user.NickUser;
import xyz.haoshoku.nick.user.UserHandler;
import xyz.haoshoku.nick.utils.ReflectionUtils;
import xyz.haoshoku.nick.version.RespawnHandler;
import xyz.haoshoku.nick.version.VersionHandler;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Handler_v1_8_R3 implements VersionHandler {

    private Plugin plugin;

    @Override
    public void pluginOnEnable( Plugin plugin ) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTask( plugin, () -> {
            for ( Player player : Bukkit.getOnlinePlayers() ) {
                UserHandler.createUser( player.getUniqueId() );
                NickUser user = UserHandler.getUser( player.getUniqueId() );
                this.setPlayerData( player );
                user.setInitialized( true );
                this.sendPacket( player );

            }
        } );
    }

    @Override
    public void pluginOnDisable( Plugin plugin ) {
        for ( Player player : Bukkit.getOnlinePlayers() ) {
            NickUser user = UserHandler.getUser( player.getUniqueId() );
            if ( user != null ) {
                GameProfile profile = ((CraftPlayer) player).getProfile();
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
                            NickUser receivedUser = UserHandler.getUser( receivedUUID );
                            if ( receivedUser == null ) continue;
                            if ( receivedUser.getNickedUniqueId() == null || receivedUser.getNickedName() == null
                                    || receivedUser.getNickedValue() == null || receivedUser.getNickedSignature() == null ) continue;

                            if ( UserHandler.getUser( player.getUniqueId() ) == null ) continue;

                            UUID packetUniqueId;
                            String packetName, packetValue, packetSignature;

                            NickUser playerUser = UserHandler.getUser( player.getUniqueId() );

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

                            GameProfile newGameProfile = new GameProfile( packetUniqueId, packetName );
                            newGameProfile.getProperties().put( "textures", new Property( "textures", packetValue, packetSignature ) );
                            PacketPlayOutPlayerInfo.PlayerInfoData newInfoData =
                                    playerInfoPacket.new PlayerInfoData( newGameProfile, infoData.b(), infoData.c(), infoData.d() );
                            playerInfoDataList.set( i, newInfoData );
                        }
                    }
                }

                if ( packet instanceof PacketPlayOutNamedEntitySpawn ) {
                    PacketPlayOutNamedEntitySpawn entitySpawnPacket = (PacketPlayOutNamedEntitySpawn) packet;
                    UUID uuid = (UUID) ReflectionUtils.getField( entitySpawnPacket, "b" );
                    NickUser user = UserHandler.getUser( uuid );
                    NickUser playerUser = UserHandler.getUser( player.getUniqueId() );

                    if ( user != null && playerUser != null && !playerUser.getBypassNickSet().contains( user.getOriginalUniqueId() ) )
                        ReflectionUtils.setField( entitySpawnPacket, "b", user.getNickedUniqueId() );
                }

                super.write( ctx, packet, promise );
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore( "packet_handler", "nickapi", duplexHandler );
    }

    public void sendPacket( Player player ) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( !player.isOnline() || user == null ) return;

        PacketPlayOutPlayerInfo removeInfoPacket = new PacketPlayOutPlayerInfo
                ( PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer );

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

        GameProfile newProfile = new GameProfile( player.getUniqueId(), name );
        newProfile.getProperties().removeAll( "textures" );
        newProfile.getProperties().put( "textures", new Property( "textures", value, signature ) );

        PacketPlayOutPlayerInfo addInfoPacket = new PacketPlayOutPlayerInfo( PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER ); // DECORATION
        ReflectionUtils.setField( addInfoPacket, "b", Collections.singletonList( addInfoPacket.new PlayerInfoData( newProfile, entityPlayer.ping,
                entityPlayer.playerInteractManager.getGameMode(), entityPlayer.listName ) ) );

        entityPlayer.playerConnection.sendPacket( removeInfoPacket );
        entityPlayer.playerConnection.sendPacket( addInfoPacket );

        Location location = player.getLocation().clone();

        PlayerList playerList = MinecraftServer.getServer().getPlayerList();

        if ( RespawnHandler.hasDisabledSpigotRespawn1_8() ) {
            /*
            Inspired by paper
             */
            entityPlayer.playerConnection.sendPacket( new PacketPlayOutRespawn( player.getWorld().getEnvironment().getId(), entityPlayer.world.getDifficulty(),
                    entityPlayer.world.worldData.getType(), entityPlayer.playerInteractManager.getGameMode() ) );
            entityPlayer.playerConnection.sendPacket( new PacketPlayOutRespawn( player.getWorld().getEnvironment().getId(), entityPlayer.world.getDifficulty(),
                    entityPlayer.world.worldData.getType(), entityPlayer.playerInteractManager.getGameMode() ) );
            entityPlayer.updateAbilities();

            try {
                Method declaredMethod = entityPlayer.playerConnection.getClass().getDeclaredMethod( "internalTeleport", double.class, double.class,
                        double.class, float.class, float.class, Class.forName( "java.util.Set" ) );
                declaredMethod.setAccessible( true );
                declaredMethod.invoke( entityPlayer.playerConnection,
                        location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), Collections.emptySet() );
            } catch ( Exception e ) {
                throw new RuntimeException( e );
            }

            entityPlayer.playerConnection.sendPacket( new PacketPlayOutExperience( entityPlayer.exp, entityPlayer.newLevel, entityPlayer.expTotal ) );

            for ( MobEffect mobEffect : entityPlayer.getEffects() )
                entityPlayer.playerConnection.sendPacket( new PacketPlayOutEntityEffect( entityPlayer.getId(), mobEffect ) );
            if ( player.isOp() ) {
                player.setOp( false );
                player.setOp( true );
            }

        } else {
            WorldServer worldServer = entityPlayer.server.getWorldServer( 0 );
            ReflectionUtils.setFieldSuperClass( worldServer, "world", player.getWorld() );
            playerList.moveToWorld( entityPlayer, 0, false, location, true );
            ReflectionUtils.setFieldSuperClass( worldServer, "world", Bukkit.getWorlds().get( 0 ) );
            playerList.a( entityPlayer, worldServer );
        }

        player.teleport( location );
        player.setFallDistance( 0F );

        entityPlayer.updateAbilities();
        player.recalculatePermissions();
        playerList.b( entityPlayer, entityPlayer.u() );
        playerList.updateClient( entityPlayer );
        entityPlayer.triggerHealthUpdate();

        for ( Player online : Bukkit.getOnlinePlayers() ) {
            if ( player != online && online.canSee( player ) ) {
                online.hidePlayer( player );
                online.showPlayer( player );
            }
        }

        user.setCurrentNicking( false );

        Bukkit.getPluginManager().callEvent( new NickFinishEvent( player, user.getOriginalUniqueId(),
                user.getOriginalName(), user.getOriginalValue(), user.getOriginalSignature(), user.getNickedUniqueId(),
                user.getNickedName(), user.getNickedValue(), user.getNickedSignature(), user.getBypassNickSet() ) );
    }

    @Override
    public void setPlayerData( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        GameProfile profile = ReflectionUtils.getProfile( player );

        user.setOriginalName( player.getName() );
        user.setOriginalUniqueId( player.getUniqueId() );
        user.setNickedName( player.getName() );
        user.setNickedUniqueId( player.getUniqueId() );

        for ( Property property : profile.getProperties().get( "textures" ) ) {
            user.setOriginalValue( property.getValue() );
            user.setOriginalSignature( property.getSignature() );
            user.setNickedValue( property.getValue() );
            user.setNickedSignature( property.getSignature() );
        }

        if ( user.getOriginalValue() == null ) {
            user.setOriginalValue( "" );
            user.setOriginalSignature( "" );
            user.setNickedValue( "" );
            user.setNickedSignature( "" );
        }
    }

    @Override
    public void removeCurrentUniqueId( Player player, Player toPlayer ) {
        NickUser playerUser = UserHandler.getUser( player.getUniqueId() );
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        if ( playerUser == null ) return;

        GameProfile nickedProfile = new GameProfile( playerUser.getNickedUniqueId(), playerUser.getNickedName() );
        PacketPlayOutPlayerInfo removeInfoPacketNicked = new PacketPlayOutPlayerInfo( PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER );
        PacketPlayOutPlayerInfo.PlayerInfoData playerInfoDataNicked = removeInfoPacketNicked.new PlayerInfoData( nickedProfile, 0,
                entityPlayer.playerInteractManager.getGameMode(), entityPlayer.listName );
        ReflectionUtils.setField( removeInfoPacketNicked, "b", Collections.singletonList( playerInfoDataNicked ) );
        ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket( removeInfoPacketNicked );

    }

}
