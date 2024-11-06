package club.siwoo.siwooclub.motd;

import club.siwoo.siwooclub.Siwoo;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeMOTD implements Listener {

    private final Siwoo plugin;
    private String currentMOTD;
    private int fakeMaxPlayers = 0; // -1 means no fake max players
    private final LocalDateTime targetTime;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd'd' HH'h' mm'm' ss's'");

    public TimeMOTD(Siwoo plugin, LocalDateTime targetTime) {
        this.plugin = plugin;
        this.targetTime = targetTime;
        currentMOTD = "&c&lSIWOO.CLUB &f| SEASON 1 Private\n&6&l ▶ Season 1.5 Starts In: ";

        // Start timer update task
        new BukkitRunnable() {
            @Override
            public void run() {
                updateMotdWithTimer();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second
    }

    private void updateMotdWithTimer() {
        Duration remainingTime = Duration.between(LocalDateTime.now(), targetTime);
        if (remainingTime.isNegative()) {
            currentMOTD = "&c&lSIWOO.CLUB &f&l| SEASON 1 Private\n&f&l ▶ &6&lSeason 1.5 Will Be LIVE SOON!";
        } else {
            String formattedTime = String.format("%dd %dh %dm %ds",
                    remainingTime.toDays(),
                    remainingTime.toHoursPart(),
                    remainingTime.toMinutesPart(),
                    remainingTime.toSecondsPart());

            currentMOTD = "&c&lSIWOO.CLUB &f&l| SEASON 1 Private\n&f&l ▶ &6&lSeason 1.5 Starts In&f: " + formattedTime;
        }
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        event.setMotd(ChatColor.translateAlternateColorCodes('&', currentMOTD));

        if (fakeMaxPlayers > -1) {
            event.setMaxPlayers(fakeMaxPlayers);
        }
    }

    public void setMOTD(String motd) {
        currentMOTD = motd;
    }

    public void setFakeMaxPlayers(int maxPlayers) {
        fakeMaxPlayers = maxPlayers;
    }
}