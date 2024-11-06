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

public class AuraCommand implements CommandExecutor {

    private final Siwoo plugin;

    public AuraCommand(Siwoo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        if (args.length < 2 || args.length > 3) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Usage: /aura <player> <set/add/remove> [amount]"));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);

        if (!sender.hasPermission(Siwoo.PERMISSION_ADMIN_POINTS)) { // Use the defined permission
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You can't execute this command"));
            return true;
        }

        if (targetPlayer == null) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Player not found"));
            return true;
        }

        String action = args[1].toLowerCase();
        int amount = 0;

        if (action.equals("set") || action.equals("add") || action.equals("remove")) {
            if (args.length != 3) {
                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Usage: /aura <player> <set/add/remove> [amount]"));
                return true;
            }

            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Invalid amount"));
                return true;
            }
        }

        int currentAura = plugin.getAuraManager().getPlayerAura(targetPlayer.getUniqueId()); // Get current aura

        switch (action) {
            case "add":
                plugin.getAuraManager().setPlayerAura(targetPlayer, currentAura + amount);
                sender.sendMessage(ChatColor.GREEN + "Added " + amount + " to " + targetPlayer.getName() + "'s aura.");
                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Added " + amount + " auras from " + targetPlayer.getName()));

                targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You were granted " + amount + " auras from Admin"));
                plugin.updateScoreboard(targetPlayer);
                break;
            case "set":
                plugin.getAuraManager().setPlayerAura(targetPlayer, amount);
                sender.sendMessage(ChatColor.GREEN + "Set " + targetPlayer.getName() + "'s aura to " + amount);
                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Set " + targetPlayer.getName() + "'s aura to " + amount));

                targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Your auras have been set to " + amount + " by Admin"));
                plugin.updateScoreboard(targetPlayer);
                break;
            case "remove":
                plugin.getAuraManager().setPlayerAura(targetPlayer, Math.max(0, currentAura - amount));
                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Removed " + amount + " auras from " + targetPlayer.getName()));

                targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You lost " + amount + " auras from Admin"));
                plugin.updateScoreboard(targetPlayer);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid action. Use 'set', 'add', or 'remove'.");
                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Invalid action. Use 'set', 'add', or 'remove'"));
        }

        plugin.updateScoreboard(targetPlayer); // Update the scoreboard after changing the aura
        return true;
    }
}