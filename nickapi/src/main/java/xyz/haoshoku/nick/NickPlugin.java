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

package xyz.haoshoku.nick;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.haoshoku.nick.api.NickConfig;
import xyz.haoshoku.nick.listener.*;
import xyz.haoshoku.nick.metrics.Metrics;
import xyz.haoshoku.nick.version.VersionHandler;
import xyz.haoshoku.nick.version.v1_21_R1.Handler_v1_21_R1;
import xyz.haoshoku.nick.version.v1_21_R2.Handler_v1_21_R2;
import xyz.haoshoku.nick.version.v1_21_R3.Handler_v1_21_R3;
import xyz.haoshoku.nick.version.v1_21_R4.Handler_v1_21_R4;
import xyz.haoshoku.nick.version.v1_21_R5.Handler_v1_21_R5;
import xyz.haoshoku.nick.version.v1_8_R3.Handler_v1_8_R3;

public class NickPlugin extends JavaPlugin {

    private static NickPlugin instance;

    private VersionHandler handler;
    private String nmsVersion;

    @Override
    public void onEnable() {
        NickPlugin.instance = this;
        this.setNmsVersion();
        this.initializeVersion();
        this.registerListener();

        this.handler.pluginOnEnable( this );

        this.getConfig().options().copyDefaults( true );
        this.saveDefaultConfig();

        NickConfig.setDefaultSkin( this.getConfig().getString( "nickapi.default_skin_name" ) );
        NickConfig.setCracked( this.getConfig().getBoolean( "nickapi.cracked" ) );

        int id = 9115;
        new Metrics( this, id );
    }

    @Override
    public void onDisable() {
        this.handler.pluginOnDisable( this );
    }

    private void initializeVersion() {
        this.getLogger().info( this.nmsVersion + " has been detected" );
        switch ( this.nmsVersion ) {

            case "v1_8_R3": {
                this.handler = new Handler_v1_8_R3();
                break;
            }

            case "v1_21_R1": {
                this.handler = new Handler_v1_21_R1();
                break;
            }

            case "v1_21_R2": {
                this.handler = new Handler_v1_21_R2();
                break;
            }

            case "v1_21_R3": {
                this.handler = new Handler_v1_21_R3();
                break;
            }

            case "v1_21_R4": {
                this.handler = new Handler_v1_21_R4();
                break;
            }

            case "v1_21_R5": {
                this.handler = new Handler_v1_21_R5();
                break;
            }

            default: {
                this.getLogger().warning( this.nmsVersion + " is not compatible to the NickAPI version you are using" );
                Bukkit.getPluginManager().disablePlugin( this );
                break;
            }
        }
    }

    private void setNmsVersion() {
        String versionTemp = "Error while reading the version..";

        try {
            versionTemp = Bukkit.getServer().getClass().getPackage().getName().split( "\\." )[3];
        } catch ( Exception ignore ) {
            String bukkitVersion = Bukkit.getBukkitVersion();

            if ( bukkitVersion.startsWith( "1.21.7" ) || bukkitVersion.startsWith( "1.21.6" ) )
                versionTemp = "v1_21_R5";
            if ( bukkitVersion.startsWith( "1.21.5" ) )
                versionTemp = "v1_21_R4";
            else if ( bukkitVersion.startsWith( "1.21.4" ) )
                versionTemp = "v1_21_R3";
            else if ( bukkitVersion.startsWith( "1.21.2" ) || bukkitVersion.startsWith( "1.21.3" ) )
                versionTemp = "v1_21_R2";
            else if ( bukkitVersion.startsWith( "1.21.1" ) || bukkitVersion.startsWith( "1.21-" ) )
                versionTemp = "v1_21_R1";

        }
        this.nmsVersion = versionTemp;
    }

    private void registerListener() {
        Listener[] listeners = new Listener[]{
                new AsyncPlayerPreLoginListener(), new PlayerCommandPreprocessListener(),
                new PlayerJoinListener(), new PlayerLoginListener(), new PlayerQuitListener() };

        for ( Listener listener : listeners )
            Bukkit.getPluginManager().registerEvents( listener, this );
    }

    public String getNMSVersion() {
        return this.nmsVersion;
    }

    public static NickPlugin instance() {
        return NickPlugin.instance;
    }

    public VersionHandler getHandler() {
        return this.handler;
    }

}
