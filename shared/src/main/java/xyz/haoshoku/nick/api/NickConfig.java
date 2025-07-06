package xyz.haoshoku.nick.api;

import xyz.haoshoku.nick.version.RespawnHandler;
import xyz.haoshoku.nick.website.SkinGetter;
import xyz.haoshoku.nick.website.UUIDGetter;

import java.util.concurrent.CompletableFuture;

public class NickConfig {

    private static boolean cracked = false;

    private static String defaultSkinValue = "", defaultSkinSignature = "";
    private static String mineSkinUserAgent = "NickAPI/vX.X", mineSkinAPIKey = "";
    private static boolean commandSupport = false;

    /**
     * Returns the MineSkin user agent you set before
     * @return
     */

    public static String getMineSkinUserAgent() {
        return mineSkinUserAgent;
    }

    /**
     * Returns the API key of MineSkin you set before
     * @return
     */

    public static String getMineSkinAPIKey() {
        return mineSkinAPIKey;
    }

    /**
     * Checks whether command preprocess support is activated
     * @return
     */

    public static boolean isCommandSupport() {
        return commandSupport;
    }

    /**
     * Actives/deactivates NickAPI's built in command support.
     * If a command does contain a nicked name, it will be translated to the original name internally.
     * In order to use this feature, you need to set the value to true.
     * @param value Activate/deactivate
     */

    public static void setCommandSupport( boolean value ) {
        NickConfig.commandSupport = value;
    }

    /**
     * Sets the MineSkin API user agent and api key
     * @param userAgent
     * @param apiKey
     */

    public static void setMineSkinAPIData( String userAgent, String apiKey ) {
        NickConfig.mineSkinUserAgent = userAgent;
        NickConfig.mineSkinAPIKey = apiKey;
    }

    /**
     * Checks if the value of NickAPI has been set to cracked
     * @return
     */

    public static boolean isCracked() {
        return NickConfig.cracked;
    }

    /**
     * If you set this to true, the NickAPI will request the uuid
     * @param value
     */

    public static void setCracked( boolean value ) {
        NickConfig.cracked = value;
    }

    public static String[] getDefaultSkin() {
        return new String[] { NickConfig.defaultSkinValue, NickConfig.defaultSkinSignature };
    }

    /**
     * Sets the default skin, if nicked name does not belong to a Minecraft account.
     *
     * @param minecraftName
     */

    public static void setDefaultSkin( String minecraftName ) {
        CompletableFuture.runAsync( () -> {
            String[] textures = SkinGetter.uuidToSkinTexture( UUIDGetter.minecraftNameToUniqueId( minecraftName ) );
            NickConfig.defaultSkinValue = textures[0];
            NickConfig.defaultSkinSignature = textures[1];
        });
    }

    /**
     * Sets the default skin, if nicked name does not belong to a Minecraft account.
     *
     * @param value     Textures: Value
     * @param signature Textures: Signature
     */

    public static void setDefaultSkin( String value, String signature ) {
        NickConfig.defaultSkinValue = value;
        NickConfig.defaultSkinSignature = signature;
    }

    /**
     * This feature disables the spigot respawn while nicking on 1.8.8 and uses the workaround from the NickAPI.
     * Advantages => Better compatibility with some NPC plugins or other entity based plugins
     * Disadvantage => May not support plugins like Geyser
     * @param value
     */

    public static void disableSpigotRespawn1_8( boolean value ) {
        RespawnHandler.disableSpigotRespawn1_8( value );
    }

}
