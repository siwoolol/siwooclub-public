package club.siwoo.siwooclub.ac.checks;

import club.siwoo.siwooclub.ac.ViolationManager;
import club.siwoo.siwooclub.ac.util.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CriticalsCheck implements Listener {

    private final ViolationManager violationManager;
    private PlayerData playerData = null;
    private static final double CRITICAL_HIT_RATE_THRESHOLD = 0.8; // 80%
    private static final int CRITICAL_HIT_COUNT_THRESHOLD = 10; // Minimum hits to analyze

    public CriticalsCheck(ViolationManager violationManager) {
        this.violationManager = violationManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {


        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return; // Not a PvP scenario
        }

        Player attacker = (Player) event.getDamager();

        // Exempt creative mode
        if (attacker.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check if hit is a critical
        boolean isCritical = attacker.getFallDistance() > 0.0F && !attacker.isOnGround()
                && !attacker.isInsideVehicle() && !event.getEntity().isOnGround();

        // Update hit counters
        if (isCritical) {
            playerData.incrementCriticalHits(attacker);
        } else {
            playerData.incrementNormalHits(attacker);
        }

        // Analyze critical hit rate
        int totalHits = playerData.getCriticalHits(attacker) + playerData.getNormalHits(attacker);
        if (totalHits >= CRITICAL_HIT_COUNT_THRESHOLD) {
            double criticalHitRate = playerData.getCriticalHitRate(attacker);

            if (criticalHitRate > CRITICAL_HIT_RATE_THRESHOLD) {
                violationManager.flagPlayer(attacker, "Suspicious Criticals (Rate: " + String.format("%.2f", criticalHitRate * 100) + "%)");
                // (Optional) Cancel the critical hit
                event.setDamage(event.getDamage() - 2);
            }
        }
    }
}
