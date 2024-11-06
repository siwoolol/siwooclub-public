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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.logging.Level;

public class ShopGUI implements Listener, CommandExecutor {

    private final Siwoo plugin;

    public ShopGUI(Siwoo plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin); // Register this as an event listener
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player!");
            return true;
        }

//        openShop(player); // open shop gui
        Player player = (Player) sender;
        ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Shop is currently under Maintenance."));
        return true;
    }

    public void openShop(Player player) {
        Inventory shopInventory = Bukkit.createInventory(null, 54, ChatColor.BOLD.toString() + ChatColor.YELLOW + "Points Shop");
        shopInventory.setItem(19, createItem(Material.ENDER_PEARL, "&9Ender Pearl", 30));
        shopInventory.setItem(20, createItem(Material.DIAMOND, "&bDiamond", 70));
        shopInventory.setItem(21, createItem(Material.IRON_INGOT, "&fIron Ingot", 10));
        shopInventory.setItem(22, createItem(Material.OAK_LOG, "&6OAK LOG", 10));
        shopInventory.setItem(23, createItem(Material.ANVIL, "&8ANVIL", 300));

        player.openInventory(shopInventory);
    }

    private static ItemStack createItem(Material material, String name, int price) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Price: " + price + " points"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private long lastClickTime = 0;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.getView().getTitle().equals(ChatColor.BOLD.toString() + ChatColor.YELLOW + "Points Shop")) {

            // 2. Get current time and check if enough time has passed since the last click
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 500) { // Adjust the delay (500ms = 0.5 seconds) as needed
                return; // Ignore clicks within the delay period
            }
            lastClickTime = currentTime; // Update the last click time

            event.setCancelled(true); // Prevent items from being moved/taken by default

            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();

            if (event.getView().getTitle().equals(ChatColor.BOLD.toString() + ChatColor.YELLOW + "Points Shop")) {
                event.setCancelled(true);

                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.hasItemMeta()) {
                    int price = getPriceFromLore(clickedItem);

                    if (price > 0 && plugin.getPlayerPoints(player.getUniqueId()) >= price) {
                        player.getInventory().addItem(clickedItem.clone());
                        plugin.setPlayerPoints(player.getUniqueId(), plugin.getPlayerPoints(player.getUniqueId()) - price);
                        plugin.updateScoreboard(player);
                        plugin.logPointActivity(player.getName(), -price, "purchase: " + clickedItem.getItemMeta().getDisplayName());
                    } else {
                        player.sendMessage(ChatColor.RED + "Insufficient points.");
                    }
                }
            }
        }
    }

    private int getPriceFromLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line.startsWith(ChatColor.GRAY + "Price: ")) {
                    try {
                        String priceString = ChatColor.stripColor(line).replace("Price: ", "");
                        String[] parts = priceString.split(" ");
                        return Integer.parseInt(parts[0]);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.WARNING, "Invalid price format in lore: " + line);
                        return -1; // Move this return statement inside the catch block
                    }
                }
            }
        }
        return -1; // If no valid price is found, return -1
    }
}