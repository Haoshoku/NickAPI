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

package xyz.haoshoku.nick.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.haoshoku.nick.NickPlugin;
import xyz.haoshoku.nick.user.User;
import xyz.haoshoku.nick.user.UserHandler;
import xyz.haoshoku.nick.utils.ReflectionUtils;
import xyz.haoshoku.nick.website.SkinGetter;
import xyz.haoshoku.nick.website.UUIDGetter;

import java.util.Objects;
import java.util.UUID;

public class NickAPI {

    public static final SkinGetter SKIN_GETTER = new SkinGetter();
    public static final UUIDGetter UUID_GETTER = new UUIDGetter();

    public static void setNick( Player player, String minecraftName ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setNickedName( minecraftName );
    }

    public static void setProfileName( Player player, String minecraftName ) {
        ReflectionUtils.setField( ReflectionUtils.getProfile( player ), "name", minecraftName );
    }

    public static void setSkin( Player player, String minecraftName ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setRequestedSkinFromMinecraftName( minecraftName );
    }

    public static void setSkin( Player player, String skinValue, String skinSignature ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null ) {
            user.setNickedValue( skinValue );
            user.setNickedSignature( skinSignature );
        }
    }

    public static void setUniqueId( Player player, UUID uuid ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setRequestedUUIDFromUUID( uuid );
    }

    public static void setUniqueId( Player player, String minecraftName ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setRequestedUUIDFromMinecraftName( minecraftName );
    }

    public static boolean isNicked( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return !Objects.equals( user.getOriginalName(), user.getNickedName() );
        return false;
    }

    public static boolean isSkinChanged( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return !Objects.equals( user.getOriginalValue(), user.getNickedValue() );
        return false;
    }

    public static boolean isProfileNameChanged( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return !Objects.equals( user.getOriginalValue(), user.getNickedValue() );
        return false;
    }

    public static void resetNick( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setNickedName( user.getOriginalName() );
    }

    public static void resetSkin( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null ) {
            user.setNickedValue( user.getOriginalValue() );
            user.setNickedSignature( user.getOriginalSignature() );
        }
    }

    public static void resetProfileName( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            ReflectionUtils.setField( ReflectionUtils.getProfile( player ), "name", user.getOriginalName() );
    }

    public static void resetUniqueId( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setRequestedUUIDFromUUID( user.getOriginalUniqueId() );
    }


    public static String getOriginalName( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getOriginalName();
        return null;
    }

    public static String getOriginalValue( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getOriginalValue();
        return null;
    }

    public static String getOriginalSignature( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getOriginalSignature();
        return null;
    }

    public static String getName( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getNickedName();
        return null;
    }

    public static String getValue( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getNickedValue();
        return null;
    }

    public static String getSignature( Player player ) {
        User user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getNickedSignature();
        return null;
    }

    public static boolean nameExists( String name ) {
        for ( User user : UserHandler.getUsers() ) {
            if ( user != null ) {
                if ( user.getOriginalName().equalsIgnoreCase( name ) || user.getNickedName().equalsIgnoreCase( name ) )
                    return true;
            }
        }
        return false;
    }

    public static boolean nickExists( String name ) {
        for ( User user : UserHandler.getUsers() ) {
            if ( user != null ) {
                if ( user.getNickedName().equalsIgnoreCase( name ) )
                    return true;
            }
        }
        return false;
    }

    public static void refreshPlayer( Player player ) {
        Bukkit.getScheduler().runTaskAsynchronously( NickPlugin.instance(), () -> {
            if ( !player.isOnline() ) return;
            User user = UserHandler.getUser( player.getUniqueId() );
            if ( user == null ) return;

            if ( user.isCurrentNicking() )
                return;

            user.setCurrentNicking( true );
            user.setInitializedNickTime( System.currentTimeMillis() + 3000L );

            while ( !user.isInitialized() && user.getInitializedNickTime() >= System.currentTimeMillis() ) {}

            if ( !user.isInitialized() ) {
                NickPlugin.instance().getLogger().warning( "Player could not be nicked after trying for 3 seconds, abandoning.." );
                user.setCurrentNicking( false );
                return;
            }

            // That's why async
            String minecraftNameSkin = user.getRequestedSkinFromMinecraftName();
            if ( minecraftNameSkin != null ) {
                String[] skinData = NickAPI.SKIN_GETTER.uuidToSkinData( NickAPI.UUID_GETTER.minecraftNameToUniqueId( minecraftNameSkin ) );

                user.setNickedValue( skinData[0] );
                user.setNickedSignature( skinData[1] );
                user.setRequestedSkinFromMinecraftName( null );
            }

            for ( Player online : Bukkit.getOnlinePlayers() ) {
                if ( player != online )
                    NickPlugin.instance().getHandler().removeCurrentUniqueId( player, online );
            }

            // That's why async
            String minecraftNameUUID = user.getRequestedUUIDFromMinecraftName();
            if ( minecraftNameUUID != null ) {
                user.setNickedUniqueId( NickAPI.UUID_GETTER.minecraftNameToUniqueId( minecraftNameUUID ) );
                user.setRequestedUUIDFromMinecraftName( null );
            }

            UUID uuidFromUUID = user.getRequestedUUIDFromUUID();
            if ( uuidFromUUID != null ) {
                user.setNickedUniqueId( uuidFromUUID );
                user.setRequestedUUIDFromUUID( null );
            }

            Bukkit.getScheduler().runTask( NickPlugin.instance(), () -> NickPlugin.instance().getHandler().sendPacket( player, NickPlugin.instance() ) );
        } );
    }

}
