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

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import xyz.haoshoku.nick.user.User;
import xyz.haoshoku.nick.user.UserHandler;

public class PlayerCommandPreprocessListener implements Listener {

    @EventHandler
    public void onCommand( PlayerCommandPreprocessEvent event ) {
        String message = event.getMessage();
        String[] args = message.split( " " ).clone();

        if ( args.length == 2 ) {
            for ( User user : UserHandler.getUsers() ) {
                if ( user != null ) {
                    if ( args[1].equalsIgnoreCase( user.getNickedName() ) ) {
                        message = message.replaceAll( "(?i)" + user.getNickedName(), user.getOriginalName() );
                        event.setMessage( message );
                        break;
                    }
                }
            }
        }

        if ( args.length >= 3 ) {
            if ( message.startsWith( "/msg" ) || message.startsWith( "/tell" ) || message.startsWith( "/w" ) ) {

                for ( User user : UserHandler.getUsers() ) {
                    if ( user != null ) {
                        if ( args[1].equalsIgnoreCase( user.getNickedName() ) ) {
                            args[1] = args[1].replaceAll( "(?i)" + user.getNickedName(), user.getOriginalName() );

                            StringBuilder command = new StringBuilder();
                            for ( int i = 0; i < args.length; i++ )
                                command.append( args[i] ).append( " " );

                            event.setMessage( command.substring( 0, command.toString().length() - 1 ) );
                            break;
                        }
                    }
                }
            } else {
                for ( int i = 1; i < args.length; i++ ) {
                    for ( User user : UserHandler.getUsers() ) {
                        if ( user != null ) {
                            if ( args[i].equalsIgnoreCase( user.getNickedName() ) )
                                args[i] = args[i].replaceAll( "(?i)" + user.getNickedName(), user.getOriginalName() );
                        }
                    }
                }

                StringBuilder command = new StringBuilder();
                for ( int i = 0; i < args.length; i++ )
                    command.append( args[i] ).append( " " );

                event.setMessage( command.substring( 0, command.toString().length() - 1 ) );
            }
        }
    }

}
