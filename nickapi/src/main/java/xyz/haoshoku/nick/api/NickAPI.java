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
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.data.Skin;
import org.mineskin.data.Texture;
import xyz.haoshoku.nick.NickPlugin;
import xyz.haoshoku.nick.user.NickUser;
import xyz.haoshoku.nick.user.UserHandler;
import xyz.haoshoku.nick.utils.ReflectionUtils;
import xyz.haoshoku.nick.website.SkinGetter;
import xyz.haoshoku.nick.website.UUIDGetter;

import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class NickAPI {

    public static final SkinGetter SKIN_GETTER = new SkinGetter();
    public static final UUIDGetter UUID_GETTER = new UUIDGetter();

    private static MineSkinClient client = MineSkinClient.builder()
            .requestHandler( JsoupRequestHandler::new )
            .userAgent( "NickAPI/v7.0" )
            .build();

    /**
     * Sets the default skin, if nicked name does not belong to a Minecraft account.
     *
     * @param minecraftName
     */

    public static void setDefaultSkin( String minecraftName ) {
        Bukkit.getScheduler().runTaskAsynchronously( NickPlugin.instance(), () -> {
            String[] textures = NickAPI.SKIN_GETTER.uuidToSkinData( NickAPI.UUID_GETTER.minecraftNameToUniqueId( minecraftName ) );
            NickAPI.SKIN_GETTER.setDefaultValue( textures[0] );
            NickAPI.SKIN_GETTER.setDefaultSignature( textures[1] );
        } );
    }

    /**
     * Sets the default skin, if nicked name does not belong to a Minecraft account.
     *
     * @param value     Textures: Value
     * @param signature Textures: Signature
     */

    public static void setDefaultSkin( String value, String signature ) {
        NickAPI.SKIN_GETTER.setDefaultValue( value );
        NickAPI.SKIN_GETTER.setDefaultSignature( signature );
    }

    /**
     * Changes the nick of a player
     *
     * @param player - Player who should be nicked
     * @param name   Minecraft name the player should have
     */

    public static void setNick( Player player, String name ) {
        if ( name.length() > 16 )
            name = name.substring( 0, 16 );
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setNickedName( name );
    }

    /**
     * Writes the player name serverside
     *
     * @param player
     * @param name
     */

    public static void setProfileName( Player player, String name ) {
        if ( name.length() > 16 )
            name = name.substring( 0, 16 );
        ReflectionUtils.setField( ReflectionUtils.getProfile( player ), "name", name );
    }

    /**
     * Changes the player skin to the same of the premium minecraft name
     *
     * @param player        Player whose skin is going to be changed
     * @param minecraftName The minecraft name
     */

    public static void setSkin( Player player, String minecraftName ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setRequestedSkinFromMinecraftName( minecraftName );
    }

    /**
     * Changes the player skin from skin data
     *
     * @param player        Player whose skin is going to be changed
     * @param skinValue     Textures: Value
     * @param skinSignature Textures: Signature
     */

    public static void setSkin( Player player, String skinValue, String skinSignature ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null ) {
            user.setNickedValue( skinValue );
            user.setNickedSignature( skinSignature );
        }
    }

    /**
     * Sets the mineskin API key and user agent to get further access
     *
     * @param apiKey
     */

    public static void setMineSkinAPIData( String userAgent, String apiKey ) {
        NickAPI.client = MineSkinClient.builder()
                .requestHandler( JsoupRequestHandler::new )
                .userAgent( userAgent ).apiKey( apiKey )
                .build();
    }

    /**
     * Changes the player skin from a file
     * You may need your own MineSkin API key in order to work with it
     *
     * @param player
     * @param file
     */

    public static void setSkin( Player player, File file ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setRequestedSkinFromFile( file );
    }

    /**
     * Changes the player skin from an url
     * You may need your own MineSkin API key in order to work with it
     *
     * @param player
     * @param url
     * @param urlUse
     */

    public static void setSkin( Player player, String url, boolean urlUse ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setRequestedSkinFromURL( url );
    }

    /**
     * Changes the uuid of the player clientsidely
     *
     * @param player Player whose uuid should be changed
     * @param uuid   the uuid
     */

    public static void setUniqueId( Player player, UUID uuid ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setRequestedUUIDFromUUID( uuid );
    }

    /**
     * Changes the uuid of the player clientsidely
     *
     * @param player        Player whose uuid should be changed
     * @param minecraftName The minecraft name whose uuid should be used for player
     */

    public static void setUniqueId( Player player, String minecraftName ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setRequestedUUIDFromMinecraftName( minecraftName );
    }

    /**
     * Check whether player is nicked
     *
     * @param player
     * @return
     */
    public static boolean isNicked( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return !Objects.equals( user.getOriginalName(), user.getNickedName() );
        return false;
    }

    /**
     * Check whether players skin has been changed
     *
     * @param player
     * @return
     */

    public static boolean isSkinChanged( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return !Objects.equals( user.getOriginalValue(), user.getNickedValue() );
        return false;
    }

    /**
     * Checks whether profile name has been changed
     *
     * @param player
     * @return
     */
    public static boolean isProfileNameChanged( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return !Objects.equals( user.getOriginalValue(), user.getNickedValue() );
        return false;
    }

    /**
     * Fully resets everything from NickAPI that has been applied to the player
     * @param player
     */

    public static void resetAll( Player player ) {
        NickAPI.resetNick( player );
        NickAPI.resetSkin( player );
        NickAPI.resetProfileName( player );
        NickAPI.resetUniqueId( player );
    }

    /**
     * Reset players nick
     *
     * @param player
     */

    public static void resetNick( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setNickedName( user.getOriginalName() );
    }

    /**
     * Reset players skin
     *
     * @param player
     */
    public static void resetSkin( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null ) {
            user.setNickedValue( user.getOriginalValue() );
            user.setNickedSignature( user.getOriginalSignature() );
        }
    }

    /**
     * Resets players profile name
     *
     * @param player
     */

    public static void resetProfileName( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            ReflectionUtils.setField( ReflectionUtils.getProfile( player ), "name", user.getOriginalName() );
    }

    /**
     * Resets players uuid (the uuid that is gonna be changed clientside)
     *
     * @param player
     */

    public static void resetUniqueId( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            user.setRequestedUUIDFromUUID( user.getOriginalUniqueId() );
    }


    /**
     * Gets player original name
     *
     * @param player
     * @return
     */

    public static String getOriginalName( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getOriginalName();
        return null;
    }

    /**
     * Gets player original skin value
     *
     * @param player
     * @return
     */

    public static String getOriginalValue( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getOriginalValue();
        return null;
    }

    /**
     * Gets player original skin signature
     *
     * @param player
     * @return
     */

    public static String getOriginalSignature( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getOriginalSignature();
        return null;
    }

    /**
     * Gets player name. If nicked -> nicked name, if unnicked -> original name
     *
     * @param player
     * @return
     */

    public static String getName( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getNickedName();
        return null;
    }

    /**
     * Gets player skin value. If changed -> changed value, otherwise original value
     *
     * @param player
     * @return
     */

    public static String getValue( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getNickedValue();
        return null;
    }

    /**
     * Gets player skin signature. If changed -> changed signature, otherwise original signature
     *
     * @param player
     * @return
     */

    public static String getSignature( Player player ) {
        NickUser user = UserHandler.getUser( player.getUniqueId() );
        if ( user != null )
            return user.getNickedSignature();
        return null;
    }

    /**
     * Checks between online players whether the parameter name is already used by an unnicked / nicked player
     *
     * @param name
     * @return
     */

    public static boolean nameExists( String name ) {
        for ( NickUser user : UserHandler.getUsers() ) {
            if ( user != null ) {
                if ( user.getOriginalName().equalsIgnoreCase( name ) || user.getNickedName().equalsIgnoreCase( name ) )
                    return true;
            }
        }
        return false;
    }

    /**
     * Checks between online players whether the parameter name is already used by a nicked player
     *
     * @param name
     * @return
     */

    public static boolean nickExists( String name ) {
        for ( NickUser user : UserHandler.getUsers() ) {
            if ( user != null ) {
                if ( user.getNickedName().equalsIgnoreCase( name ) )
                    return true;
            }
        }
        return false;
    }

    /**
     * Hide player from other players
     *
     * @param player   Player that should be hidden
     * @param hiddenTo Hidden to which player?
     */

    public static void hidePlayer( Player player, Player hiddenTo ) {
        NickPlugin.instance().getHandler().removeCurrentUniqueId( player, hiddenTo );
        hiddenTo.hidePlayer( player );
    }

    /**
     * Show player again to other players
     *
     * @param player  Player that is going to be shown
     * @param shownTo To which players the player is going to be shown?
     */

    public static void showPlayer( Player player, Player shownTo ) {
        shownTo.showPlayer( player );
    }

    /**
     * Hide player from ALL online players
     *
     * @param player
     */

    public static void hidePlayer( Player player ) {
        for ( Player online : Bukkit.getOnlinePlayers() )
            NickAPI.hidePlayer( player, online );
    }

    /**
     * Show player for ALL online players
     *
     * @param player
     */
    public static void showPlayer( Player player ) {
        for ( Player online : Bukkit.getOnlinePlayers() )
            NickAPI.showPlayer( player, online );
    }

    /**
     * Allows player to bypass nicked players nick state.
     * So he will see him unnicked
     *
     * @param bypassingPlayer The player who should see unnicked state of nicked player
     * @param nickedPlayer    The nicked player
     */

    public static void addBypass( Player bypassingPlayer, Player nickedPlayer ) {
        NickAPI.getBypassedUUIDs( bypassingPlayer ).add( nickedPlayer.getUniqueId() );
    }

    /**
     * Removes player to bypass nicked players nick state.
     * So he will the nicked player nicked again
     *
     * @param bypassingPlayer
     * @param nickedPlayer
     */

    public static void removeBypass( Player bypassingPlayer, Player nickedPlayer ) {
        NickAPI.getBypassedUUIDs( bypassingPlayer ).remove( nickedPlayer.getUniqueId() );
    }

    /**
     * Gets the uuids that has been added to the bypass set to allow players to see nicked players unnicked
     *
     * @param bypassingPlayer
     * @return
     */

    public static Set<UUID> getBypassedUUIDs( Player bypassingPlayer ) {
        NickUser user = UserHandler.getUser( bypassingPlayer.getUniqueId() );
        if ( user != null && user.getBypassNickSet() != null ) return user.getBypassNickSet();
        return null;
    }

    /**
     * The same as the addBypass method but with uuids
     *
     * @param bypassingPlayer
     * @param nickedUUID
     */

    public static void addBypassUUID( Player bypassingPlayer, UUID nickedUUID ) {
        NickAPI.getBypassedUUIDs( bypassingPlayer ).add( nickedUUID );
    }

    /**
     * The same as the removeBypass method but with uuids
     *
     * @param bypassingPlayer
     * @param nickedUUID
     */

    public static void removeBypassUUID( Player bypassingPlayer, UUID nickedUUID ) {
        NickAPI.getBypassedUUIDs( bypassingPlayer ).remove( nickedUUID );
    }


    /**
     * This method is important.
     * It is the "trigger signal" to nick a player. If you nick / unnick / change skin / etc. call this method.
     *
     * @param player
     */
    public static void refreshPlayer( Player player ) {
        Bukkit.getScheduler().runTaskAsynchronously( NickPlugin.instance(), () -> {
            if ( !player.isOnline() ) return;
            NickUser user = UserHandler.getUser( player.getUniqueId() );
            if ( user == null ) return;

            if ( user.isCurrentNicking() )
                return;

            user.setCurrentNicking( true );
            user.setInitializedNickTime( System.currentTimeMillis() + 3000L );

            while ( !user.isInitialized() && user.getInitializedNickTime() >= System.currentTimeMillis() ) {
            }

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

            String skinByURL = user.getRequestedSkinFromURL();
            if ( skinByURL != null ) {
                try {
                    Skin skin = NickAPI.client.generateUrl( skinByURL ).get().getSkin();
                    Texture texture = skin.data().texture();
                    user.setNickedValue( texture.value() );
                    user.setNickedSignature( texture.signature() );
                } catch ( Exception e ) {
                    throw new RuntimeException( e );
                }
            }

            File skinByFile = user.getRequestedSkinFromFile();
            if ( skinByFile != null ) {
                try {
                    Skin skin = NickAPI.client.generateUpload( skinByFile ).get().getSkin();
                    Texture texture = skin.data().texture();
                    user.setNickedValue( texture.value() );
                    user.setNickedSignature( texture.signature() );
                } catch ( Exception e ) {
                    throw new RuntimeException( e );
                }
            }

            user.setRequestedSkinFromFile( null );
            user.setRequestedUUIDFromUUID( null );
            user.setRequestedUUIDFromMinecraftName( null );
            user.setRequestedSkinFromMinecraftName( null );

            Bukkit.getScheduler().runTask( NickPlugin.instance(), () -> NickPlugin.instance().getHandler().sendPacket( player, NickPlugin.instance() ) );
        } );
    }

}
