package club.siwoo.siwooclub.ac;

import club.siwoo.siwooclub.Siwoo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SiwooAC implements CommandExecutor {

    private final Siwoo plugin;

    public SiwooAC(Siwoo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "SiwooClub Integrated SiwooAC");
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "SiwooAC Commands " + ChatColor.YELLOW + "-" + ChatColor.RED + " 1 Out Of 1 Pages");
            sender.sendMessage(ChatColor.AQUA + "/ban " + ChatColor.GOLD + "-" + ChatColor.AQUA + " Bans a player");
            return true;
        }
        return false;
    }
}
