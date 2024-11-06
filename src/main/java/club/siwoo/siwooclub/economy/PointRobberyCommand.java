package club.siwoo.siwooclub.economy;

import club.siwoo.siwooclub.Siwoo;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PointRobberyCommand implements CommandExecutor {

    private final Map<UUID, Integer> playerPoints;
    private final Map<UUID, Long> robberyCooldowns; // Map to store cooldowns
    private final Random random = new Random();

    public PointRobberyCommand(Map<UUID, Integer> playerPoints) {
        this.playerPoints = playerPoints;
        robberyCooldowns = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player robber = (Player) sender;

        if (args.length != 1) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            robber.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Usage: /rob <player>"));
            return true;
        }

        // Get victim later so that it doesn't define the length
        Player victim = Bukkit.getPlayer(args[0]);

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }

        if (victim == null) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            robber.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Player not found"));
            return true;
        }

        if (victim == robber) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            robber.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You can't rob yourself"));
            return true;
        }

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        Long lastRobberyTime = robberyCooldowns.get(robber.getUniqueId());

        if (lastRobberyTime == null) {
            lastRobberyTime = currentTime - 600000;
            robber.spigot().sendMessage(ChatMessageType.SYSTEM, new TextComponent(ChatColor.RED + "Last Robbery Time was null so it was automatically set to rollback. If this issue persists, please contact manager or create issue on github."));
        }

        long remainingTime = TimeUnit.MILLISECONDS.toMinutes(TimeUnit.MINUTES.toMillis(10) - (currentTime - lastRobberyTime));
        if (currentTime - lastRobberyTime < 600000) { // 10 minutes = 600000 milliseconds
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            robber.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Please wait " + remainingTime + " minutes before robbing again!"));
            return true;
        }

        // Check if robber has enough points (at least 20% of victim's points to ensure safety)
        int robberPoints = playerPoints.getOrDefault(robber.getUniqueId(), 0);
        int victimPoints = playerPoints.getOrDefault(victim.getUniqueId(), 0);
        if (robberPoints < victimPoints * 0.2) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            robber.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You must at least have 20% of the player"));
            return true;
        }

        attemptRobbery(victim, robber);
        robberyCooldowns.put(robber.getUniqueId(), currentTime); // Set cooldown after robbery attempt
        return true;
    }

    private void attemptRobbery(Player victim, Player robber) {

        if (random.nextInt(100) < 30) { // 30% chance of successful robbery
            int stolenPoints = calculateStolenPoints(victim);
            playerPoints.put(victim.getUniqueId(), Math.max(0, playerPoints.get(victim.getUniqueId()) - stolenPoints)); // Ensure points don't go negative
            playerPoints.put(robber.getUniqueId(), playerPoints.get(robber.getUniqueId()) + stolenPoints);
            Siwoo plugin = new Siwoo();
            plugin.updateScoreboard(robber);
            plugin.updateScoreboard(victim);

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatColor.GREEN + "✔ Hall Of Fame!");
            Bukkit.broadcastMessage(robber.getName() + " Stole " + stolenPoints + " Points from " + victim.getName() + "!");
            Bukkit.broadcastMessage("");
        } else {
            // Robbery failed - robber loses points, victim gains points
            int lostPoints = calculateStolenPoints(robber); // Calculate points for the robber to lose

            playerPoints.put(robber.getUniqueId(), Math.max(0, playerPoints.get(robber.getUniqueId()) - lostPoints)); // Robber loses points (can't go negative)
            playerPoints.put(victim.getUniqueId(), playerPoints.get(victim.getUniqueId()) + lostPoints); // Victim gains the lost points

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatColor.RED + "\uD83E\uDD21 Hall Of Shame!");
            Bukkit.broadcastMessage(robber.getName() + " tried to steal from " + victim.getName() + " but failed and lost " + lostPoints + " points!");
            Bukkit.broadcastMessage("");
        }
    }

    private int calculateStolenPoints(Player victim) {
        int victimPoints = playerPoints.getOrDefault(victim.getUniqueId(), 0);
        // You can adjust this calculation based on your desired game logic
        return Math.max(1, victimPoints / 5); // Steal up to 20% of the victim's points (minimum 1)
    }
}