package club.siwoo.siwooclub.server;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathItemControl implements Listener {

    private final Map<UUID, Boolean> keepInventoryOnDeath = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();


        // Check if the player was killed by another player
        boolean killedByPlayer = killer != null;

        // Keep inventory only if not killed by a player
        event.setKeepInventory(!killedByPlayer);
        event.setKeepLevel(!killedByPlayer); // Keep experience levels as well

        if (killedByPlayer) {
            Location deathLocation = player.getLocation();

            // Drop each item individually if killed by a player
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && !item.getType().isAir()) {
                    dropItem(deathLocation, item); // Use the dropItem method
                }
            }

            // Drop experience (if not kept)
            event.setDroppedExp(player.getTotalExperience());

            // Clear inventory after items are dropped
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
        }

        // Store the death cause for respawn handling
        keepInventoryOnDeath.put(player.getUniqueId(), !killedByPlayer);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Boolean shouldKeepInventory = keepInventoryOnDeath.remove(player.getUniqueId()); // Retrieve and remove the flag

        // Only restore inventory if shouldKeepInventory is true (not killed by a player)
        if (shouldKeepInventory != null && shouldKeepInventory) {
            player.getInventory().setContents(event.getPlayer().getInventory().getContents());
            player.getInventory().setArmorContents(event.getPlayer().getInventory().getArmorContents());
            player.setLevel(event.getPlayer().getLevel());
            player.setExp(event.getPlayer().getExp());
            player.setTotalExperience(event.getPlayer().getTotalExperience());
        } // Otherwise, the inventory is already empty, as the items were dropped in onPlayerDeath
    }

    // Drop item method (you need to implement this based on your server's version)
    private void dropItem(Location location, ItemStack item) {
        // Implement item dropping logic here
        // This might vary depending on your server version or if you are using an external plugin
        location.getWorld().dropItem(location, item);
    }
}
