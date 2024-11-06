package club.siwoo.siwooclub.server;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldToggleCommand implements CommandExecutor {

    // Declare the set as a field of the class
    private Map<String, Location> lastOverworldLocations = new HashMap<>(); // Store last overworld locations
    private Set<String> disabledWorlds = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("worldtoggle.use")) { // Replace with your desired permission
            sender.sendMessage(ChatColor.RED + "You don't have permission for this command.");
            return true;
        }

        if (args.length != 1 || (!args[0].equalsIgnoreCase("end") && !args[0].equalsIgnoreCase("nether"))) {
            sender.sendMessage(ChatColor.RED + "Usage: /toggle <end|nether>");
            return true;
        }

        String worldName = args[0].toUpperCase();

        if (disabledWorlds.contains(worldName)) {  // 'disabledWorlds' can be accessed here now
            disabledWorlds.remove(worldName);
            Bukkit.broadcastMessage(ChatColor.GREEN + "Access to the " + worldName + " has been enabled!");
        } else {
            disabledWorlds.add(worldName);
            Bukkit.broadcastMessage(ChatColor.RED + "Access to the " + worldName + " has been disabled!");
        }

        return true;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom().getWorld();
        World toWorld = event.getTo().getWorld();
        String toWorldName = toWorld.getName().toUpperCase();

        if (disabledWorlds.contains(toWorldName)) {
            event.setCancelled(true);

            // Teleport the player back to their last overworld location
            Location lastLocation = lastOverworldLocations.get(player.getName());
            if (lastLocation != null) {
                player.teleport(lastLocation);
            } else {
                // If no last location, teleport to the main world's spawn
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }

            player.sendMessage(ChatColor.RED + "Access to the " + toWorldName + " is currently disabled.");
        } else {
            // Store the player's last location in the overworld
            if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
                lastOverworldLocations.put(player.getName(), player.getLocation());
            }
        }
    }
}