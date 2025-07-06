package xyz.haoshoku.nick.version;

public class RespawnHandler {

    private static boolean disabledSpigotRespawn1_8 = false;

    public static boolean hasDisabledSpigotRespawn1_8() {
        return RespawnHandler.disabledSpigotRespawn1_8;
    }

    public static void disableSpigotRespawn1_8( boolean disableSpigotRespawn1_8 ) {
        RespawnHandler.disabledSpigotRespawn1_8 = disableSpigotRespawn1_8;
    }
}
