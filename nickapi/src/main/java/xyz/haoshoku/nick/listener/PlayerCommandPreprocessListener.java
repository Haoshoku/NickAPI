package xyz.haoshoku.nick.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import xyz.haoshoku.nick.NickPlugin;
import xyz.haoshoku.nick.api.NickAPI;
import xyz.haoshoku.nick.api.NickConfig;
import xyz.haoshoku.nick.user.NickUser;
import xyz.haoshoku.nick.user.UserHandler;

public class PlayerCommandPreprocessListener implements Listener {

    @EventHandler( ignoreCancelled = true )
    public void onCommand( PlayerCommandPreprocessEvent event ) {
        String message = event.getMessage();
        String replacedMessage = null;

        if ( !NickConfig.isCommandSupport() || !NickPlugin.instance().getNMSVersion().equalsIgnoreCase( "v1_8_R3" ) ) return;

        // TODO 1.20+ => Problem: Signature, ClientboundDisguisePacket is making problems => Analysing it

        for ( NickUser user : UserHandler.getUsers() ) {
            if ( user != null ) {
                Player target = Bukkit.getPlayer( user.getOriginalUniqueId() );
                String name = user.getNickedName();

                if ( target != null && name != null && !user.getOriginalName().equalsIgnoreCase( user.getNickedName() ) ) {
                    if ( !message.startsWith( "/msg" ) && !message.startsWith( "/tell" ) && !message.startsWith( "/w" ) && !message.startsWith( "/whisper" ) ) {
                        if ( message.toLowerCase().contains( name.toLowerCase() ) )
                            message = message.replaceAll( "(?i)" + name, NickAPI.getOriginalName( target ) );
                    } else {
                        String[] splitter = message.split( " " );
                        int length = splitter.length;

                        if ( length >= 3 ) {
                            String targetAsString = message.split( " " )[1];
                            targetAsString = targetAsString.replaceAll( "(?i)" + name, NickAPI.getOriginalName( target ) );

                            StringBuilder newMessage = new StringBuilder();
                            for ( int i = 0; i < splitter.length; i++ ) {
                                if ( i == 1 ) {
                                    newMessage.append( targetAsString ).append( " " );
                                    continue;
                                }
                                newMessage.append( splitter[i] ).append( " " );
                            }

                            replacedMessage = newMessage.toString();
                            replacedMessage = replacedMessage.substring( 0, replacedMessage.length() - 1 );
                        }
                    }
                }
            }
        }

        if ( replacedMessage != null )
            event.setMessage( replacedMessage );
        else
            event.setMessage( message );}

}
