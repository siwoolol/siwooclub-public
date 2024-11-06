package club.siwoo.siwooclub.moderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

// TODO finalize ACLogs

public class ACLogsCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ACLogsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        try {
            File dataFolder = Bukkit.getPluginManager().getPlugin("SiwooClub").getDataFolder();
            if (dataFolder == null) {
                throw new IllegalStateException("SiwooClub data folder not found!");
            }

            FileConfiguration logsConfig = YamlConfiguration.loadConfiguration(new File(dataFolder, "aclogs.yml"));
            Map<UUID, List<String>> playerLogs = new HashMap<>();

            for (String playerUUIDString : logsConfig.getKeys(false)) {
                UUID playerUUID = UUID.fromString(playerUUIDString);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);

                if (offlinePlayer == null) {
                    continue; // Skip if player not found
                }

                List<String> logs = new ArrayList<>();
                for (String timestampKey : logsConfig.getConfigurationSection(playerUUIDString).getKeys(false)) {
                    String violation = logsConfig.getString(playerUUIDString + "." + timestampKey + ".violation");
                    String timestamp = logsConfig.getString(playerUUIDString + "." + timestampKey + ".timestamp");
                    logs.add(timestamp + ": " + violation);
                }

                playerLogs.put(playerUUID, logs);
            }

            // Display logs in chat (without a GUI)
            if (args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                UUID targetUUID = target.getUniqueId();
                if (playerLogs.containsKey(targetUUID)) {
                    sender.sendMessage(ChatColor.GOLD + "Logs for " + target.getName() + ":");
                    for (String log : playerLogs.get(targetUUID)) {
                        sender.sendMessage(ChatColor.GRAY + log);
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "No logs found for " + target.getName() + ".");
                }
                return true; // Exit after displaying logs in chat
            }

            // Create a basic inventory to display player heads
            Inventory logsInventory = Bukkit.createInventory(null, 54, "Anti-Cheat Logs");

            for (UUID playerUUID : playerLogs.keySet()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);

                SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                skullMeta.setOwningPlayer(offlinePlayer);
                skullMeta.setDisplayName(offlinePlayer.getName());
                playerHead.setItemMeta(skullMeta);

                logsInventory.addItem(playerHead);
            }

            // Register inventory click listener
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onInventoryClick(InventoryClickEvent event) {
                    if (event.getClickedInventory() == logsInventory) {
                        event.setCancelled(true);

                        ItemStack clickedItem = event.getCurrentItem();
                        if (clickedItem != null && clickedItem.getType() == Material.PLAYER_HEAD) {
                            SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
                            String playerName = skullMeta.getDisplayName();

                            UUID playerUUID = playerLogs.keySet().stream()
                                    .filter(uuid -> Bukkit.getOfflinePlayer(uuid).getName().equals(playerName))
                                    .findFirst()
                                    .orElse(null);

                            if (playerUUID != null) {
                                // Show logs for the clicked player in chat
                                List<String> logs = playerLogs.get(playerUUID);
                                event.getWhoClicked().sendMessage(ChatColor.GOLD + "Logs for " + playerName + ":");
                                for (String log : logs) {
                                    event.getWhoClicked().sendMessage(ChatColor.GRAY + log);
                                }
                            } else {
                                event.getWhoClicked().sendMessage(ChatColor.RED + "No logs found for " + playerName + ".");
                            }
                        }
                    }
                }
            }, Bukkit.getPluginManager().getPlugin("SiwooAC"));  // Register with the plugin instance

            player.openInventory(logsInventory); // Open the inventory

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error loading anti-cheat logs: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}