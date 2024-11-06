package club.siwoo.siwooclub.economy;

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
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DailyReward implements CommandExecutor {
    private final Siwoo plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> lastDailyClaim = new HashMap<>();

    public DailyReward(Siwoo plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        long currentTime = System.currentTimeMillis();
        long lastClaim = lastDailyClaim.getOrDefault(playerUUID, 0L);
        long timeSinceLastClaim = currentTime - lastClaim;

        if (TimeUnit.MILLISECONDS.toHours(timeSinceLastClaim) >= 24) {
            player.playSound((player.getLocation()), Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + "Your daily reward is ready! Use /daily to claim"));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        long currentTime = System.currentTimeMillis();
        long lastClaim = lastDailyClaim.getOrDefault(playerUUID, 0L);
        long timeSinceLastClaim = currentTime - lastClaim;

        if (TimeUnit.MILLISECONDS.toHours(timeSinceLastClaim) >= 24) {
            int randomPoints = random.nextInt(131) + 70; // Random number between 70 and 200
            plugin.setPlayerPoints(playerUUID, plugin.getPlayerPoints(playerUUID) + randomPoints);
            plugin.updateScoreboard(player);
            player.playSound((player.getLocation()), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You have received your daily reward of " + randomPoints + " points"));
            lastDailyClaim.put(playerUUID, currentTime); // Update the last claim time
        } else {
            long remainingTime = TimeUnit.MILLISECONDS.toHours(TimeUnit.HOURS.toMillis(24) - timeSinceLastClaim);
            player.playSound((player.getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Please wait " + remainingTime + " hours more to claim"));
        }
        return true;
    }
}
