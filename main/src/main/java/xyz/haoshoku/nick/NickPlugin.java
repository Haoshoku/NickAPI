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
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.haoshoku.nick.api.NickAPI;
import xyz.haoshoku.nick.listener.AsyncPlayerPreLoginListener;
import xyz.haoshoku.nick.listener.PlayerJoinListener;
import xyz.haoshoku.nick.listener.PlayerLoginListener;
import xyz.haoshoku.nick.listener.PlayerQuitListener;
import xyz.haoshoku.nick.version.VersionHandler;
import xyz.haoshoku.nick.version.v1_20_R2.Handler_v1_20_R2;
import xyz.haoshoku.nick.version.v1_8_R3.Handler_v1_8_R3;

public class NickPlugin extends JavaPlugin {

    private static NickPlugin instance;

    private VersionHandler handler;

    @Override
    public void onEnable() {
        NickPlugin.instance = this;
        this.initializeVersion();
        this.registerListener();
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelAllTasks();

        for ( Player player : Bukkit.getOnlinePlayers() ) {
            NickAPI.resetNick( player );
            NickAPI.resetSkin( player );
        }
    }

    private void initializeVersion() {
        String version = Bukkit.getServer().getClass().getName().split( "\\." )[3];
        this.getLogger().info( version + " has been detected" );
        switch ( version ) {

            case "v1_8_R3": {
                this.handler = new Handler_v1_8_R3();
                break;
            }

            case "v1_20_R2": {
                this.handler = new Handler_v1_20_R2();
                break;
            }

            default: {
                this.getLogger().warning( version + " is not compatible to the NickAPI version you are using" );
                Bukkit.getPluginManager().disablePlugin( this );
                break;
            }
        }
    }

    private void registerListener() {
        Listener[] listeners = new Listener[] {
                new AsyncPlayerPreLoginListener(),
                new PlayerJoinListener(), new PlayerLoginListener(), new PlayerQuitListener() };

        for ( Listener listener : listeners )
            Bukkit.getPluginManager().registerEvents( listener, this );
    }

    public static NickPlugin instance() {
        return NickPlugin.instance;
    }

    public VersionHandler getHandler() {
        return this.handler;
    }

}
