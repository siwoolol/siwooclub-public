package club.siwoo.siwooclub.server;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class TimeRestartCommand implements CommandExecutor {

    private JavaPlugin plugin;
    private BukkitRunnable restartTask; // Track the task (no need for ID now)

    public TimeRestartCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("server.timerestart")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /timerestart <minutes>");
            return true;
        }

        try {
            int minutes = Integer.parseInt(args[0]);
            if (minutes <= 0) {
                sender.sendMessage(ChatColor.RED + "Please enter a valid number of minutes.");
                return true;
            }

            long delayMillis = TimeUnit.MINUTES.toMillis(minutes); // Use milliseconds directly

            // Cancel any existing restart task
            if (restartTask != null && !restartTask.isCancelled()) {
                restartTask.cancel();
                sender.sendMessage(ChatColor.GREEN + "Previous restart task cancelled.");
            }

            // Schedule the restart task
            restartTask = new BukkitRunnable() {
                @Override
                public void run() {
                    // Broadcast notifications before restarting
                    for (int i = 5; i > 0; i--) {
                        Bukkit.broadcastMessage(ChatColor.RED + "Server restarting in " + i + " minute(s)! Please log out.");
                        try {
                            Thread.sleep(60000); // 1 minute delay
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // Actual server restart logic
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                }
            };

            restartTask.runTaskLater(plugin, delayMillis / 50); // Schedule using the plugin reference
            sender.sendMessage(ChatColor.GREEN + "Server restart scheduled in " + minutes + " minutes.");

            return true;
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}