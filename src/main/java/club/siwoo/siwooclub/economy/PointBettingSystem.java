package club.siwoo.siwooclub.economy;

import club.siwoo.siwooclub.Siwoo;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PointBettingSystem implements CommandExecutor, Listener {

    private final Siwoo plugin;
    private final Map<UUID, Integer> activeBets = new HashMap<>();
    private final Random random = new Random();
    private final Material[] possibleBlocks = {
            Material.DIAMOND_BLOCK,
            Material.GOLD_BLOCK,
            Material.EMERALD_BLOCK,
            Material.LAPIS_BLOCK,
            Material.REDSTONE_BLOCK
    };

    public PointBettingSystem(Siwoo plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin); // Register this as an event listener
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.playSound((player.getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Usage: /bet <amount>"));
            return true;
        }

        try {
            int amount = Integer.parseInt(args[0]);
            if (amount <= 0) {
                player.playSound((player.getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Bet amount must be positive"));
                return true;
            }
            if (plugin.getPlayerPoints(player.getUniqueId()) < amount) {
                player.playSound((player.getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You don't have enough points"));
                return true;
            }

            // Check if the player has already placed a bet
            if (activeBets.containsKey(player.getUniqueId())) {
                player.playSound((player.getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You already have an active bet"));
                player.sendMessage(ChatColor.YELLOW + "If you are experiencing this issue, you probably closed an active bet GUI. We are working on making a code to retrieve their points but for now, there is no way to retrieve your points. If you want to bet again," + ChatColor.LIGHT_PURPLE + " wait and try again");
                return true;
            }

            activeBets.put(player.getUniqueId(), amount);
            openBettingInventory(player); // Open the betting inventory immediately
            return true;

        } catch (NumberFormatException e) {
            player.playSound((player.getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Invalid bet amount"));
            return true;
        }
    }

    private void openBettingInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Choose a Block");

        // Shuffle the possible blocks for randomness
        List<Material> shuffledBlocks = Arrays.asList(possibleBlocks);
        Collections.shuffle(shuffledBlocks);

        // Add the first 3 shuffled blocks to the inventory
        for (int i = 0; i < 3; i++) {
            ItemStack blockItem = new ItemStack(shuffledBlocks.get(i));
            inv.setItem(i * 3 + 1, blockItem); // Place in slots 1, 4, and 7
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Choose a Block")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                // Generate the winning block at the time of the click
                Material winningBlock = possibleBlocks[random.nextInt(possibleBlocks.length)];
                resolveBet(player, clickedItem.getType() == winningBlock, winningBlock);
                player.closeInventory();
            }
        }
    }

    public void resolveBet(Player player, boolean won, Material winningBlock) {
        if (activeBets.containsKey(player.getUniqueId())) {
            int betAmount = activeBets.remove(player.getUniqueId());
            if (won) {
                plugin.setPlayerPoints(player.getUniqueId(), plugin.getPlayerPoints(player.getUniqueId()) + (betAmount * 3));
                player.playSound((player.getLocation()), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You won " + (betAmount * 3) + " points"));
                Bukkit.broadcastMessage(ChatColor.BOLD + " ");
                Bukkit.broadcastMessage(ChatColor.BOLD + "! WINNER ANNOUNCEMENT !");
                Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + " Has Just Won " + (betAmount * 3) + " Points From Betting! " + ChatColor.GOLD + "(/bet)");
                Bukkit.broadcastMessage(ChatColor.BOLD + " ");
            } else {
                plugin.setPlayerPoints(player.getUniqueId(), plugin.getPlayerPoints(player.getUniqueId()) - betAmount);
                player.playSound((player.getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You lost " + betAmount + " points"));
            }
            plugin.updateScoreboard(player);
            plugin.logPointActivity(player.getName(), won ? (betAmount * 3) : -betAmount, "bet on " + winningBlock.name());
        }
    }
}
