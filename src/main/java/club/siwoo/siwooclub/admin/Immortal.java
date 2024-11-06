package club.siwoo.siwooclub.admin;

import club.siwoo.siwooclub.Siwoo;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class Immortal implements CommandExecutor {
    private final Siwoo plugin;

    public Immortal(Siwoo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (!sender.hasPermission("siwoo.admin")) { // Replace with your desired permission
            player.playSound((player.getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You don't have permission for this command"));
            return true;
        }

        if (args.length < 0) {
            player.playSound((player.getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Usage: /immortal"));
            return true;
        }

        if (args.length == 0) {
            if (!player.isInvulnerable()) {
                player.setInvulnerable(true);
                player.playSound((player.getLocation()), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Your Immortal Status Was Set to True"));
            } else if (player.isInvulnerable()) {
                player.setInvulnerable(false);
                player.playSound((player.getLocation()), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Your Immortal Status Was Set to False"));
            }
        }
        return true;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.isInvulnerable()) {
                event.setCancelled(true);
            }
        }
    }
}
