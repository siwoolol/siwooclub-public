package club.siwoo.siwooclub.minigames;

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

public class FightCommand implements CommandExecutor {

    private final Siwoo plugin;

    public FightCommand(Siwoo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /fight <player>");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        Player player = sender instanceof Player ? (Player) sender : null;

        if (targetPlayer == sender) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You cannot fight yourself"));
            return true;
        }

        return false;
    }
}
