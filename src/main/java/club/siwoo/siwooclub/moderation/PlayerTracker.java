package club.siwoo.siwooclub.moderation;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTracker implements CommandExecutor, Listener {

    private Map<UUID, BukkitRunnable> trackingTasks = new HashMap<>();
    private final JavaPlugin plugin;

    public PlayerTracker(JavaPlugin plugin) { // Constructor takes plugin instance
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player tracker = (Player) sender;

        if (label.equalsIgnoreCase("track")) {
            String targetName = args[0];
            Player target = Bukkit.getPlayer(args[0]);

            if (target == sender) {
                tracker.sendMessage(ChatColor.RED + "You can't track yourself.");
                return true;
            }

            if (args.length == 1) {

                if (args.length != 1) {
                    sender.sendMessage(ChatColor.RED + "Usage: /track <player>");
                    return true;
                }

                if (target != null) {
                    if (trackingTasks.containsKey(tracker.getUniqueId())) { // Check if already tracking
                        tracker.sendMessage(ChatColor.RED + "You are already tracking someone.");
                        return true;
                    }
                    startTracking(tracker, target);
                    tracker.sendMessage(ChatColor.GREEN + "You are now tracking " + targetName + ".");
                } else {
                    tracker.sendMessage(ChatColor.RED + "Player not found.");
                }
            } else {
                tracker.sendMessage(ChatColor.RED + "Usage: /track <player>");
            }
        } else if (label.equalsIgnoreCase("trackdisable")) {
            if (stopTracking(tracker)) {
                tracker.sendMessage(ChatColor.GREEN + "Tracking disabled.");
            } else {
                tracker.sendMessage(ChatColor.RED + "You are not currently tracking anyone.");
            }
        }

        return true;
    }

    private void startTracking(Player tracker, Player target) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!tracker.isOnline() || !target.isOnline()) {
                    stopTracking(tracker);
                    return;
                }

                Location targetLocation = target.getLocation();
                String message = ChatColor.GREEN + target.getName() + ": " +
                        ChatColor.GOLD + "X: " + targetLocation.getBlockX() + " " +
                        ChatColor.GOLD + "Y: " + targetLocation.getBlockY() + " " +
                        ChatColor.GOLD + "Z: " + targetLocation.getBlockZ();
                tracker.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            }
        };
        task.runTaskTimer(plugin, 0L, 20L); // Update every second
        trackingTasks.put(tracker.getUniqueId(), task); // Store the task
    }

    private boolean stopTracking(Player tracker) {
        BukkitRunnable task = trackingTasks.remove(tracker.getUniqueId());
        if (task != null) {
            task.cancel();
            return true; // Indicate that a task was found and cancelled
        } else {
            return false; // Indicate that no task was found for the player
        }
    }
}
