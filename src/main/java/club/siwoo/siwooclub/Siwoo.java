package club.siwoo.siwooclub;

import club.siwoo.siwooclub.ac.SiwooAC;
import club.siwoo.siwooclub.ac.ViolationManager;
import club.siwoo.siwooclub.ac.checks.*;
import club.siwoo.siwooclub.ac.util.PlayerData;
import club.siwoo.siwooclub.admin.Immortal;
import club.siwoo.siwooclub.economy.*;
import club.siwoo.siwooclub.minigames.FightCommand;
import club.siwoo.siwooclub.moderation.ACLogsCommand;
import club.siwoo.siwooclub.moderation.BroadcastCommand;
import club.siwoo.siwooclub.moderation.PlayerTracker;
import club.siwoo.siwooclub.motd.MOTD;
import club.siwoo.siwooclub.server.DeathItemControl;
import club.siwoo.siwooclub.motd.TimeMOTD;
import club.siwoo.siwooclub.server.TimeRestartCommand;
import club.siwoo.siwooclub.server.WorldToggleCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Siwoo extends JavaPlugin implements Listener {

    public static final String PERMISSION_ADMIN_POINTS = "siwoo.points";
    private AuraManager auraManager;
    private static final String SCOREBOARD_TITLE = "&c&lsiwoo.club &7PRIVATE";
    private static final String POINTS_FILE_NAME = "points.yml";
    private static final String POINTS_LOG_FILE_NAME = "pointslog.yml";
    private static final int POINTS_PER_KILL = 50;
    private static final int POINTS_PER_DEATH = -50;
    private static final int POINTS_PER_ADVANCEMENT = 20;
    private TimeMOTD motdChanger;
    private static final String LUNARLINK = "https://www.lunarclient.com/download";

    private FileConfiguration pointsConfig;
    private File pointsFile;
    private FileConfiguration pointsLogConfig;
    private File pointsLogFile;
    public Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    public Map<UUID, Integer> playerPoints = new HashMap<>();
    private Map<UUID, String> playerBrands = new HashMap<>();

    private ViolationManager violationManager;
    private PlayerData playerData = null;

    public void onEnable() {

        // Auras Should be Loaded first and seperately (solved from pull request-e9ece695b46218aeba101142b7950b567766c689)
        auraManager = new AuraManager(this);
        auraManager.loadAuras();
        getCommand("aura").setExecutor(new AuraCommand(this));

        pointsFile = new File(getDataFolder(), POINTS_FILE_NAME);
        if (!pointsFile.exists()) {
            saveResource(POINTS_FILE_NAME, false);
        }
        pointsConfig = YamlConfiguration.loadConfiguration(pointsFile);

        setupPointsLog();
        loadPoints();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ShopGUI(this), this);
//        getServer().getPluginManager().registerEvents(new WelcomeScreen(this), this); // Loading Screens are now Deprecated
        violationManager = new ViolationManager(this); // Initialize AC
        registerCommands(); // Register commands after initializing motdChanger
        EnableChecks(); // The hits are not from the ACs
//        timeMotd(); // Timed MOTD
        normalMotd(); // Normal MOTD
        startScoreboardUpdater();
    }

    private void registerCommands() { // Command registration
        getCommand("immortal").setExecutor(new Immortal(this));
        getCommand("shop").setExecutor(new ShopGUI(this));
        getCommand("points").setExecutor(new PointsCommand(this));
        getCommand("bet").setExecutor(new PointBettingSystem(this));
        getCommand("pay").setExecutor(new PointsGive(this));
        getCommand("daily").setExecutor(new DailyReward(this));
        getCommand("rob").setExecutor(new PointRobberyCommand(playerPoints));
        getCommand("timerestart").setExecutor(new TimeRestartCommand(this));
        getCommand("broadcast").setExecutor(new BroadcastCommand());
        getCommand("toggle").setExecutor(new WorldToggleCommand());
        getCommand("track").setExecutor(new PlayerTracker(this));
        getCommand("trackdisable").setExecutor(new PlayerTracker(this));

        // Minigames
        getCommand("fight").setExecutor(new FightCommand(this));

        // Crucial
        getServer().getPluginManager().registerEvents(new DeathItemControl(), this);

        // AC Commands
        getCommand("siwooac").setExecutor(new SiwooAC(this));
        getCommand("aclogs").setExecutor(new ACLogsCommand(this));
    }

    private void EnableChecks() {
        violationManager = new ViolationManager(this); // You might want to pass 'this' to it if it needs the plugin instance
        playerData = new PlayerData();
        new ReachCheck(violationManager, playerData);
        getServer().getPluginManager().registerEvents(new FlightCheck(violationManager), this);
        getServer().getPluginManager().registerEvents(new AutoClickerCheck(violationManager), this);
        getServer().getPluginManager().registerEvents(new AimAssistCheck(violationManager), this);
        getServer().getPluginManager().registerEvents(new CriticalsCheck(violationManager), this);
    }

    private void timeMotd() {
        LocalDateTime targetTime = LocalDateTime.of(2024, 8, 14, 12, 0);
        getServer().getPluginManager().registerEvents(new TimeMOTD(this, targetTime), this);
    }

    private void normalMotd() {
        getServer().getPluginManager().registerEvents(new MOTD(), this);
    }

    public AuraManager getAuraManager() {
        if (auraManager == null) {
            auraManager = new AuraManager(this);
            auraManager.loadAuras();
        }
        return auraManager;
    }

    private void setupPointsLog() {
        pointsLogFile = new File(getDataFolder(), POINTS_LOG_FILE_NAME);
        if (!pointsLogFile.exists()) {
            saveResource(POINTS_LOG_FILE_NAME, false); // Create a new pointslog.yml if it doesn't exist
        }
        pointsLogConfig = YamlConfiguration.loadConfiguration(pointsLogFile);
    }

    private void loadPoints() {
        for (String key : pointsConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                playerPoints.put(uuid, pointsConfig.getInt(key));
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.WARNING, "Invalid UUID in points file: " + key);
            }
        }
    }

    private void savePoints() {
        try {
            pointsConfig.save(pointsFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save points to file", e);
        }
    }

    // Scoreboard Methods
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        createScoreboardForPlayer(player);

        ClearChat(player);
        LunarEnforce(player);
        sendDiscordInvite(player);
        Announcement(player);
    }

    private void Announcement(Player player) {
        player.playSound((player.getLocation()), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "The Last Reboot Maintenance was Successful. Thank You For Your Co-operation.");
        player.sendMessage("");
    }

    private void LunarEnforce(Player player) {
        String userAgent = player.getAddress().getHostName().toLowerCase();
        playerBrands.put(player.getUniqueId(), userAgent);

        if (!userAgent.contains("lunar client")) {
            TextComponent message = new TextComponent(ChatColor.YELLOW + " ");
            TextComponent message2 = new TextComponent(ChatColor.RED + "Looks Like You Aren't Using Lunar Client!\n");
            TextComponent link = new TextComponent(ChatColor.YELLOW + "We Recommend Using" + ChatColor.AQUA + " Lunar Client " + ChatColor.YELLOW + "for the best experience!");
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, LUNARLINK));
            link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Download Lunar Client").create()));
            message2.addExtra(link);
            player.spigot().sendMessage(message);
            player.spigot().sendMessage(message2);
        }
    }

    private void ClearChat(Player player) {
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.ITALIC + "*From This, The Chats Are From siwoo.club*");
    }

    private void sendDiscordInvite(Player player) {
        player.sendMessage(ChatColor.RED + " ");
        player.sendMessage(ChatColor.BOLD + "Make Sure To Join Our Discord server:");
        player.sendMessage(ChatColor.AQUA + "https://discord.gg/QQ2FDSk8hG");
        player.sendMessage(ChatColor.RED + " ");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerScoreboards.remove(event.getPlayer().getUniqueId());
    }

    public void updateScoreboard(Player player) {
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            return;
        }

        Objective objective = scoreboard.getObjective("points");
        if (objective == null) {
            return;
        }

        // Reset Scores for All "Points:" Entries
        for (String entry : scoreboard.getEntries()) {
            if (entry.startsWith(ChatColor.GOLD + "⛀ Points:")) {
                objective.getScoreboard().resetScores(entry);
            }
        }

        // Reset Scores for All "Aura:" Entries
        for (String entry : scoreboard.getEntries()) {
            if (entry.startsWith(ChatColor.LIGHT_PURPLE + "\uD83D\uDC8E Aura:")) {
                objective.getScoreboard().resetScores(entry);
            }
        }

        // Set/Update Static Text (Only do this once when the player joins, not every update)
        if (!playerScoreboards.containsKey(player.getUniqueId())) {
            objective.getScore(ChatColor.BOLD + "SEASON " + ChatColor.RED + "1 " + ChatColor.WHITE + "Stats: ").setScore(6); // Change later
            objective.getScore("  ").setScore(7);
            objective.getScore("  ").setScore(3);
            objective.getScore("  ").setScore(2);
            objective.getScore("  ").setScore(1);
            objective.getScore(ChatColor.GRAY + "sᴘᴏɴsᴏʀᴇᴅ ʙʏ sɪᴡᴏᴏ.ᴄʟᴜʙ").setScore(0);
        }

        // Update Dynamic Scores
        objective.getScore(ChatColor.GOLD + "⛀ Points: " + ChatColor.WHITE + playerPoints.getOrDefault(player.getUniqueId(), 0)).setScore(5);
        objective.getScore(ChatColor.AQUA + "⌚ Playtime: " + ChatColor.WHITE + "Coming Soon").setScore(4);
        int auraScore = auraManager.getPlayerAura(player.getUniqueId());
        objective.getScore(ChatColor.LIGHT_PURPLE + "\uD83D\uDC8E Aura: " + ChatColor.WHITE + auraScore).setScore(3);
        objective.getScore(ChatColor.RED + "\uD83D\uDDE1 Status: " + ChatColor.GREEN + "✅ Operational").setScore(2);
    }

    private void startScoreboardUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    private void createScoreboardForPlayer(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("points", "dummy", translateColorCodes(SCOREBOARD_TITLE));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        playerScoreboards.put(player.getUniqueId(), scoreboard);

        // Initialize scoreboard with entries and scores
        objective.getScore(ChatColor.BOLD + "SEASON " + ChatColor.RED + "1 " + ChatColor.WHITE + "STATS: ").setScore(6);
        objective.getScore("  ").setScore(4);
        objective.getScore(ChatColor.GOLD + "⛀ Points:").setScore(3);
        objective.getScore("  ").setScore(1);
        objective.getScore(ChatColor.GRAY + "sᴘᴏɴsᴏʀᴇᴅ ʙʏ sɪᴡᴏᴏ.ᴄʟᴜʙ").setScore(0);

        updateScoreboard(player); // Update the scoreboard with initial values
        player.setScoreboard(scoreboard);
    }

    // Event Handling
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID killerUUID = killer.getUniqueId();
            setPlayerPoints(killerUUID, getPlayerPoints(killerUUID) + POINTS_PER_KILL);
            updateScoreboard(killer);
            logPointActivity(killer.getName(), POINTS_PER_KILL, "kill");
            killer.sendMessage(ChatColor.YELLOW + "You Have Killed a Player and Received 20 Points!");
        }

        if (victim != killer) {
            UUID victimUUID = victim.getUniqueId();
            setPlayerPoints(victimUUID, getPlayerPoints(victimUUID) + POINTS_PER_DEATH);
            updateScoreboard(victim);
            logPointActivity(victim.getName(), POINTS_PER_DEATH, "death");
            victim.sendMessage(ChatColor.RED + "You Have Been Killed and Lost 20 Points!");
        }
    }

    @EventHandler // Add this event handler back in
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();

        if (isMajorAdvancement(player, advancement)) {
            UUID playerUUID = player.getUniqueId();

            setPlayerPoints(playerUUID, getPlayerPoints(playerUUID) + POINTS_PER_ADVANCEMENT);
            updateScoreboard(player);
            logPointActivity(player.getName(), POINTS_PER_ADVANCEMENT, "advancement");
            player.sendMessage(ChatColor.YELLOW + "You Have Completed a Major Advancement and Received 20 Points!");
        }
    }

    private boolean isMajorAdvancement(Player player, Advancement advancement) {
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        return progress.isDone() && advancement.getDisplay() != null;
    }

    // Getter and Setter for playerPoints
    public int getPlayerPoints(UUID playerUUID) {
        return playerPoints.getOrDefault(playerUUID, 0);
    }

    public void setPlayerPoints(UUID playerUUID, int newPoints) {
        int oldPoints = playerPoints.getOrDefault(playerUUID, 0);
        playerPoints.put(playerUUID, newPoints);
        pointsConfig.set(playerUUID.toString(), newPoints);
        savePoints();

        if (oldPoints != newPoints) { // Only log if points actually changed
            Player player = Bukkit.getPlayer(playerUUID);
            String playerName = player != null ? player.getName() : playerUUID.toString();
            logPointActivity(playerName, newPoints - oldPoints, "point adjustment");
        }
    }

    public void logPointActivity(String playerName, int points, String reason) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = timestamp + ": " + playerName + " " + (points > 0 ? "gained" : "lost") + " "
                + Math.abs(points) + " points (" + reason + ")";

        // Find the next available log entry index
        int index = 0;
        while (pointsLogConfig.contains("log_entries." + index)) {
            index++;
        }

        pointsLogConfig.set("log_entries." + index, logEntry);
        try {
            pointsLogConfig.save(pointsLogFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save to points log file.", e);
        }
    }

    // Color Code Translation Method (Replaces colorize)
    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
