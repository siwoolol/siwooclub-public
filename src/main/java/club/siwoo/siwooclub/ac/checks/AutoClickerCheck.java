package club.siwoo.siwooclub.ac.checks;

import club.siwoo.siwooclub.ac.ViolationManager;
import club.siwoo.siwooclub.ac.util.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class AutoClickerCheck implements Listener {

    private final ViolationManager violationManager;
    private final PlayerData playerData;

    private static final int MAX_CPS = 18;            // Max clicks per second
    private static final int MAX_RIGHT_CLICK_CPS = 5;  // Max right clicks per second
    private static final int MAX_CLICK_VARIATION = 2;   // Allowed variation in click intervals

    public AutoClickerCheck(ViolationManager violationManager) {
        this.violationManager = violationManager;
        playerData = new PlayerData();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        long currentTime = System.currentTimeMillis();

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Left Click Handling
            checkClickPattern(player, currentTime, false);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Right Click Handling
            checkClickPattern(player, currentTime, true);
        }
    }

    private void checkClickPattern(Player player, long currentTime, boolean isRightClick) {
        long lastClickTime = playerData.getLastClickTime(player, isRightClick);
        if (lastClickTime != 0) {
            int clickInterval = (int) (currentTime - lastClickTime);
            int expectedInterval = 1000 / (isRightClick ? MAX_RIGHT_CLICK_CPS : MAX_CPS); // Calculate expected interval

            playerData.addClickInterval(player, clickInterval, isRightClick);
            int violationLevel = playerData.analyzeClickIntervals(player, expectedInterval, MAX_CLICK_VARIATION, isRightClick);

            if (violationLevel > 10) { // Adjust the threshold as needed
                violationManager.flagPlayer(player, "Suspicious Clicking Pattern");
            }
        }
        playerData.setLastClickTime(player, currentTime, isRightClick);
    }
}