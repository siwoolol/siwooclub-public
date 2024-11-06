package club.siwoo.siwooclub.moderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender has permission to use the command
        if (!sender.hasPermission("siwooclub.broadcast")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Check if any message was provided
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /broadcast <message>");
            return true;
        }

        // Combine all arguments into a single message
        String message = String.join(" ", args);

        // Format the message (you can customize this)
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', "&f[&4ANNOUNCEMENT&f] &f" + message);

        // Broadcast the message to all players
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(formattedMessage);
        Bukkit.broadcastMessage("");

        return true;
    }
}
