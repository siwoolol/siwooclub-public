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

public class PointsGive implements CommandExecutor {

    private final Siwoo plugin;

    public PointsGive(Siwoo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        UUID senderUUID = player.getUniqueId();
        int sendercurrentPoints = plugin.getPlayerPoints(senderUUID);

        if (args.length < 2) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Usage: /pay <player> <amount>"));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Player not found"));
            return true;
        }

        Player sameSender = Bukkit.getPlayer(sender.getName());
        if (targetPlayer == sameSender) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You cannot pay yourself"));
            return true;
        }

        int amount = Integer.parseInt(args[1]);
        if (amount <= 0) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Amount must be positive"));
            return true;
        }

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Invalid amount"));
            return true;
        }

        if (sendercurrentPoints < amount) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You don't have enough points"));
            return true;
        }

        UUID targetUUID = targetPlayer.getUniqueId();
        int currentPoints = plugin.getPlayerPoints(targetUUID);
        plugin.setPlayerPoints(targetUUID, currentPoints + amount);
        plugin.setPlayerPoints(senderUUID, sendercurrentPoints - amount);
        plugin.logPointActivity(targetPlayer.getName(), -amount, "received points from " + sender.getName());
        plugin.logPointActivity(sender.getName(), -amount, "paid player " + targetPlayer.getName());
        ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You paid " + amount + " points to " + targetPlayer.getName()));
        targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You received " + amount + " points from " + sender.getName()));

        plugin.updateScoreboard(targetPlayer); // Update the player's scoreboard
        plugin.updateScoreboard((Player) sender);
        return true;
    }
}
