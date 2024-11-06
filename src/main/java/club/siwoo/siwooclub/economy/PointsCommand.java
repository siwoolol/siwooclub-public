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

import java.util.UUID;

public class PointsCommand implements CommandExecutor {

    private final Siwoo plugin;

    public PointsCommand(Siwoo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        if (!sender.hasPermission("siwoo.admin")) { // Requires permission "siwoo.admin"
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You can't execute this command"));
            return true;
        }

        if (args.length < 3) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Usage: /points <player> <add/set/remove> <amount>"));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Player not found"));
            return true;
        }

        String action = args[1].toLowerCase();
        int amount;

        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Invalid amount"));
            return true;
        }

        UUID targetUUID = targetPlayer.getUniqueId();
        int currentPoints = plugin.getPlayerPoints(targetUUID);

        switch (action) {
            case "add":
                plugin.setPlayerPoints(targetUUID, currentPoints + amount);

                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Added " + amount + " points to " + targetPlayer.getName()));

                targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You received " + amount + " points from Admin"));
                break;
            case "set":
                plugin.setPlayerPoints(targetUUID, amount);

                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Set " + targetPlayer.getName() + "'s points to " + amount));

                targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Your points have been set to " + amount + " by Admin"));
                break;
            case "remove":
                plugin.setPlayerPoints(targetUUID, currentPoints - amount);

                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Removed " + amount + " points from " + targetPlayer.getName()));

                targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You lost " + amount + " points from Admin"));
                break;
            default:
                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Invalid action. Use 'add', 'set', or 'remove'"));
        }

        plugin.updateScoreboard(targetPlayer); // Update the player's scoreboard
        return true;
    }
}
