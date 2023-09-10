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

package xyz.haoshoku.nick.listener;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import xyz.haoshoku.nick.user.User;
import xyz.haoshoku.nick.user.UserHandler;
import xyz.haoshoku.nick.utils.ReflectionUtils;

public class PlayerLoginListener implements Listener {

    @EventHandler( priority = EventPriority.LOWEST )
    public void onLogin( PlayerLoginEvent event ) {
        Player player = event.getPlayer();
        UserHandler.createUser( player.getUniqueId() );
        User user = UserHandler.getUser( player.getUniqueId() ); // TODO NULL

        user.setOriginalName( player.getName() );
        user.setNickedName( player.getName() );

        GameProfile profile = ReflectionUtils.getProfile( player );

        for ( Property property : profile.getProperties().get( "textures" ) ) {
            user.setOriginalValue( property.getValue() );
            user.setOriginalSignature( property.getSignature() );
            user.setNickedValue( property.getValue() );
            user.setNickedSignature( property.getSignature() );
        }

        user.setNickedUniqueId( player.getUniqueId() );
        user.setOriginalUniqueId(  player.getUniqueId() );
    }


}
