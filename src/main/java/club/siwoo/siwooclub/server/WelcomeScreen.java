package club.siwoo.siwooclub.server;

import club.siwoo.siwooclub.Siwoo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class WelcomeScreen implements Listener {

    private final Siwoo plugin;
    private final FileConfiguration tipsConfig;
    private final List<String> tips = new ArrayList<>();
    private final Random random = new Random();
    private BukkitTask loadingTipsTask;

    public WelcomeScreen(Siwoo plugin) {
        this.plugin = plugin;
        tipsConfig = loadTipsConfig();
        loadTips();
    }

    private FileConfiguration loadTipsConfig() {
        File tipsFile = new File(plugin.getDataFolder(), "tips.yml");
        if (!tipsFile.exists()) {
            try {
                tipsFile.createNewFile();
                // Add some default tips to the file
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(tipsFile);
                List<String> defaultTips = new ArrayList<>();
                defaultTips.add("Make Sure To Use /daily for daily rewards!");
                defaultTips.add("Use Lunar Client for The Best Experience!");
                defaultTips.add("Don't forget to join our Discord Server!");
                defaultTips.add("You can get Auras from staff members!");
                defaultTips.add("Auras can be used as credit score!");
                defaultTips.add("Sponsored By HorrorMC!");
                defaultTips.add("Ask Players For Financial Problems!");
                defaultTips.add("If You're In Debt, You prolly do in real life!");
                defaultTips.add("Also Try Minemen Club");
                defaultTips.add("Minecraft is the Best Game!");
                defaultTips.add("You are Immortal During Loading!");
                defaultTips.add("This was made for No Reason At All!");
                defaultTips.add("Good Luck!");
                defaultTips.add("Make Sure to Open the Voice Chat while playing!");
                defaultConfig.set("tips", defaultTips);
                defaultConfig.save(tipsFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create tips file.", e);
            }
        }
        return YamlConfiguration.loadConfiguration(tipsFile);
    }

    private void loadTips() {
        tips.addAll(tipsConfig.getStringList("tips"));
    }

    boolean isImmortal = Boolean.parseBoolean(null);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (loadingTipsTask != null && !loadingTipsTask.isCancelled()) {
            loadingTipsTask.cancel();
        }

        // Temporary blindness and slowness (5 seconds)
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 5, 255)); // Max level slowness
        player.setInvulnerable(true);
        AtomicBoolean isImmortal = new AtomicBoolean(true);

        // 5 secs later actions
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setInvulnerable(false);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            isImmortal.set(false);
        }, 20L * 5);

        // Select two random tips
        List<String> shuffledTips = new ArrayList<>(tips);
        Collections.shuffle(shuffledTips);
        String tip1 = shuffledTips.get(0); // First tip
        String tip2 = shuffledTips.get(1); // Second tip

        // welcome message
        loadingTipsTask = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count == 0) {
                    player.sendTitle(ChatColor.translateAlternateColorCodes('&', tip1), ChatColor.GRAY + "Loading...", 0, 20, 0);
                } else if (count == 1) {
                    player.sendTitle(ChatColor.translateAlternateColorCodes('&', tip2), ChatColor.GRAY + "Loading...", 0, 20, 0);
                } else {
                    cancel();
                }
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // Cancel movement while the welcome screen is active
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isImmortal) {
            event.setCancelled(true); // Cancel Movement
        }
    }

    // Cancel damage while the welcome screen is active
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
