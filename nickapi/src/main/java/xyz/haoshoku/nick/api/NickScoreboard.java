package xyz.haoshoku.nick.api;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class NickScoreboard {

    private static final Map<String, Object[]> SCOREBOARD_MAP = new HashMap<>();

    /**
     * If you want a nicked name has a prefix and suffix, you should work with this method.
     *
     * @param nickedName The nicked player name
     * @param teamName   The team name the nicked player should be in
     * @param prefix     The prefix the team should have
     * @param suffix     The suffix the team should have
     */
    public static void write( String nickedName, String teamName, String prefix, String suffix ) {
        NickScoreboard.write( nickedName, teamName, prefix, suffix, true, ChatColor.BLUE );
    }

    /**
     * If you want a nicked name has a prefix and suffix, you should work with this method.
     *
     * @param nickedName    The nicked player name
     * @param teamName      The team name the nicked player should be in
     * @param prefix        The prefix the team should have
     * @param suffix        The suffix the team should have
     * @param newScoreboard If you don't want to use the main scoreboard, set it to true
     * @param color         The color (needed only for 1.13+ servers)
     */
    public static void write( String nickedName, String teamName, String prefix, String suffix, boolean newScoreboard, ChatColor color ) {
        NickScoreboard.SCOREBOARD_MAP.put( nickedName, new Object[]{ teamName, prefix, suffix, newScoreboard, color } );
    }

    /**
     * If you want a nicked name has a prefix and suffix, you should work with this method.
     *
     * @param nickedName The nicked player name
     * @param teamName   The team name the nicked player should be in
     * @param prefix     The prefix the team should have
     * @param suffix     The suffix the team should have
     * @param color      The color (needed only for 1.13+ servers)
     */
    public static void write( String nickedName, String teamName, String prefix, String suffix, ChatColor color ) {
        NickScoreboard.SCOREBOARD_MAP.put( nickedName, new Object[]{ teamName, prefix, suffix, true, color } );
    }

    /**
     * This method is here if you want to remove the nicked name from team
     *
     * @param nickedName The nicked name that should be removed from the team
     */
    public static void delete( String nickedName ) {
        NickScoreboard.SCOREBOARD_MAP.remove( nickedName );
    }

    /**
     * Updates the scoreboard of all the players
     */
    public static void updateAllScoreboard() {
        for ( Map.Entry<String, Object[]> entry : NickScoreboard.SCOREBOARD_MAP.entrySet() ) {
            String teamName = (String) entry.getValue()[0];
            String prefix = (String) entry.getValue()[1];
            String suffix = (String) entry.getValue()[2];
            boolean newScoreboard = (boolean) entry.getValue()[3];
            ChatColor color = (ChatColor) entry.getValue()[4];

            for ( Player player : Bukkit.getOnlinePlayers() ) {
                Scoreboard scoreboard;

                if ( newScoreboard ) {
                    if ( player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard() )
                        player.setScoreboard( Bukkit.getScoreboardManager().getNewScoreboard() );
                }
                scoreboard = player.getScoreboard();

                Team team = scoreboard.getTeam( teamName ) != null ? scoreboard.getTeam( teamName ) : scoreboard.registerNewTeam( teamName );
                team.setPrefix( prefix );
                team.setSuffix( suffix );

                try {
                    if ( color != null )
                        team.setColor( color );
                } catch ( NoSuchMethodError ignore ) {
                }

                team.addEntry( entry.getKey() );
            }
        }
    }

    /**
     * If you want to update the scoreboard only for the specific nicked name, you should use this method
     *
     * @param nickedName The nicked name that should've been updated through all players
     */
    public static void updateScoreboard( String nickedName ) {
        if ( !NickScoreboard.SCOREBOARD_MAP.containsKey( nickedName ) ) return;
        Object[] values = NickScoreboard.SCOREBOARD_MAP.get( nickedName );

        String teamName = (String) values[0];
        String prefix = (String) values[1];
        String suffix = (String) values[2];
        boolean newScoreboard = (boolean) values[3];
        ChatColor color = (ChatColor) values[4];
        for ( Player player : Bukkit.getOnlinePlayers() ) {
            Scoreboard scoreboard;

            if ( newScoreboard ) {
                if ( player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard() )
                    player.setScoreboard( Bukkit.getScoreboardManager().getNewScoreboard() );
            }
            scoreboard = player.getScoreboard();

            Team team = scoreboard.getTeam( teamName ) != null ? scoreboard.getTeam( teamName ) : scoreboard.registerNewTeam( teamName );
            team.setPrefix( prefix );
            team.setSuffix( suffix );

            try {
                team.setColor( color );
            } catch ( NoSuchMethodError ignore ) {
            }

            team.addEntry( nickedName );
        }
    }
}
