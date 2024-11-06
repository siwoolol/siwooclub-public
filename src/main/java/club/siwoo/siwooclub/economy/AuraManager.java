package club.siwoo.siwooclub.economy;

import club.siwoo.siwooclub.Siwoo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class AuraManager {

    private final Siwoo plugin;
    private final FileConfiguration aurasConfig;
    private final File aurasFile;
    private final Map<UUID, Integer> activeAuras = new HashMap<>();

    public AuraManager(Siwoo plugin) {
        this.plugin = plugin;
        aurasFile = new File(plugin.getDataFolder(), "auras.yml");
        if (!aurasFile.exists()) {
            try {
                aurasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create auras file.", e);
            }
        }
        aurasConfig = YamlConfiguration.loadConfiguration(aurasFile);
    }

    public void giveAura(Player player, int auraScore) {
        activeAuras.put(player.getUniqueId(), auraScore);
        saveAuras();
    }

    // Public method for external use (e.g., from commands)
    public void setPlayerAura(Player player, int auraScore) {
        giveAura(player, auraScore); // Call the internal giveAura method
        plugin.updateScoreboard(player); // Update the scoreboard immediately
    }

    private void saveAuras() {
        for (UUID uuid : activeAuras.keySet()) {
            aurasConfig.set(uuid.toString(), activeAuras.get(uuid));
        }
        try {
            aurasConfig.save(aurasFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save auras to file.", e);
        }
    }

    public void loadAuras() {
        for (String key : aurasConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                activeAuras.put(uuid, aurasConfig.getInt(key));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "Invalid UUID in auras file: " + key);
            }
        }
    }

    public int getPlayerAura(UUID playerUUID) {
        return activeAuras.getOrDefault(playerUUID, 0);
    }

    // Make activeAuras accessible from Siwoo
    public Map<UUID, Integer> getActiveAuras() {
        return activeAuras;
    }
}