package club.siwoo.siwooclub.ac.checks;

import club.siwoo.siwooclub.ac.ViolationManager;
import club.siwoo.siwooclub.ac.util.AimAssistUtil;
import club.siwoo.siwooclub.ac.util.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AimAssistCheck implements Listener {

    private final ViolationManager violationManager;
    private final PlayerData playerData;

    public AimAssistCheck(ViolationManager violationManager) {
        this.violationManager = violationManager;
        playerData = new PlayerData();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            // Check for suspicious aim patterns
            if (AimAssistUtil.isSuspiciouslyAccurate(attacker, victim)) {

                // Check if the player was recently flagged
                if (playerData.wasRecentlyFlagged(attacker)) {
                    violationManager.flagPlayer(attacker, "Aim Assist");
                    // Optionally, cancel the attack:
                    // event.setCancelled(true);
                } else {
                    playerData.setFlagged(attacker, true); // Mark as recently flagged
                }
            } else {
                playerData.setFlagged(attacker, false); // Reset flag if no suspicious aim
            }
        }
    }
}
