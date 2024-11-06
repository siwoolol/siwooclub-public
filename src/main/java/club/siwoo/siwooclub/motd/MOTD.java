package club.siwoo.siwooclub.motd;

import club.siwoo.siwooclub.Siwoo;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MOTD implements Listener {

    private String motd;

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        motd = "&c&lSIWOO.CLUB &f&l| SEASON 1.5 Private &f&l ▶ &eSeason 1.5 Just Started!";

        event.setMotd(ChatColor.translateAlternateColorCodes('&', motd));
        event.setMaxPlayers(0);
    }
}
