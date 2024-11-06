package club.siwoo.siwooclub.ac.checks;

import club.siwoo.siwooclub.ac.ViolationManager;
import club.siwoo.siwooclub.ac.util.PlayerData;
import club.siwoo.siwooclub.ac.util.ReachUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ReachCheck implements Listener {

    private final ViolationManager violationManager;
    private final PlayerData playerData;

    public ReachCheck(ViolationManager violationManager, PlayerData playerData) { // Add PlayerData dependency
        this.violationManager = violationManager;
        this.playerData = playerData;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) {
            return; // Not a player hitting a living entity
        }

        Player attacker = (Player) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();

        // Exempt creative mode
        if (attacker.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        double distance = ReachUtil.getActualAttackDistance(attacker, victim);

        // Account for various reach modifiers
        double maxReach = 3.0; // Base reach distance

        // Ping compensation and recent collisions
        double pingAdjustment = attacker.getPing() / 100;
        double collisionAdjustment = playerData.hasCollidedRecently(attacker) ? 0.4 : 0; // Adjust for recent collisions
        maxReach += pingAdjustment + collisionAdjustment;

        // Check for other factors like items, abilities, etc.
        // ... (Add your custom checks here) ...

        System.out.println(attacker.getName() + " reach check: distance=" + distance + ", maxReach=" + maxReach);

        // Check for violation
        if (distance > maxReach) {
            violationManager.flagPlayer(attacker, "Reach Violation (Distance: " + distance + ", MaxReach: " + maxReach + ")");
            event.setCancelled(true); // Cancel the attack
        }
    }
}