package club.siwoo.siwooclub.ac;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class ViolationManager {
    public Permission admin = new Permission("admin");
    private final JavaPlugin plugin; // Reference to your plugin
    private FileConfiguration logsConfig;
    private File logsFile;

    public ViolationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        logsFile = new File(plugin.getDataFolder(), "aclogs.yml");
        logsConfig = YamlConfiguration.loadConfiguration(logsFile);
    }

    // Customize this based on your desired logging/punishment system
    public void flagPlayer(Player player, String violationType) {

        // Log the violation
        Bukkit.getLogger().warning(ChatColor.YELLOW + "[SiwooClub]" + ChatColor.RED + player.getName() + " failed: " + violationType);

        // Implement your punishment logic here
        // Examples:
        // - player.sendMessage("Warning: Suspicious activity detected.");
        // - player.teleport(player.getWorld().getSpawnLocation()); // Teleport to spawn
        // - Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + player.getName() + " Cheating"); // Ban
        // - ... (More sophisticated actions based on violation severity and history)
        if (player.getName().startsWith("bedrock_")) {
            player.sendMessage(ChatColor.BLUE + "[SiwooAC] Bedrock Checks are temporarily disabled. However, you flagged " + violationType + " in terms of java checks. This was logged and we will keep an eye on you.");
            logViolation(player, violationType);
        } else {
            Bukkit.broadcast(ChatColor.GOLD.toString() + ChatColor.BOLD + "[SiwooAC] " + ChatColor.RED + player.getName() + " failed " + violationType + ChatColor.GOLD + " (Client: " + "e" + ")", String.valueOf(admin));
            player.sendMessage(ChatColor.RED + "[SiwooAC] You have been flagged for: " + violationType + "." + ChatColor.GOLD + " This has been logged" + ChatColor.RED + " and if you think it is false, please contact an administrator.");
            logViolation(player, violationType);
        }
    }

    private void logViolation(Player player, String violationType) {
        String timestamp = LocalDateTime.now().toString(); // Get current timestamp
        String path = player.getUniqueId() + "." + timestamp;
        logsConfig.set(path + ".violation", violationType);
        logsConfig.set(path + ".timestamp", timestamp);

        try {
            logsConfig.save(logsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save violation log to aclogs.yml: " + e.getMessage());
        }
    }
}
