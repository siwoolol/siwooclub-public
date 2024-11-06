package club.siwoo.siwooclub.ac.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class AimAssistUtil {

    private static final int MAX_ACCURACY_SAMPLES = 20;
    private static final Map<UUID, Queue<Double>> accuracyHistory = new HashMap<>();
    private static final Map<UUID, Vector> lastLookDirection = new HashMap<>();
    private static final Map<UUID, Long> lastAttackTime = new HashMap<>();

    public static boolean isSuspiciouslyAccurate(Player attacker, Player victim) {
        UUID attackerUUID = attacker.getUniqueId();
        Location attackerEyes = attacker.getEyeLocation();
        Location victimHead = victim.getEyeLocation().add(0, -0.5, 0);
        Vector victimDirection = victimHead.toVector().subtract(attackerEyes.toVector());

        // Calculate accuracy
        double accuracy = attackerEyes.getDirection().dot(victimDirection.normalize());

        // Maintain accuracy history
        Queue<Double> playerAccuracyHistory = accuracyHistory.computeIfAbsent(attackerUUID, k -> new LinkedList<>());
        playerAccuracyHistory.offer(accuracy);
        if (playerAccuracyHistory.size() > MAX_ACCURACY_SAMPLES) {
            playerAccuracyHistory.poll();
        }

        // Check if the attacker is actively moving their crosshair
        long lastAttack = lastAttackTime.getOrDefault(attackerUUID, 0L);
        long currentTime = System.currentTimeMillis();
        boolean isMovingCrosshair = currentTime - lastAttack < 500; // 500ms threshold for active aiming

        // Refined accuracy check
        double averageAccuracy = playerAccuracyHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        if (accuracy > 0.9 && averageAccuracy > 0.9 && isMovingCrosshair) { // Check for active aiming
            return true;
        }

        // Refined sudden snapping check (more lenient)
        Vector lastLook = lastLookDirection.getOrDefault(attackerUUID, victimDirection);
        double lookAngleChange = lastLook.angle(victimDirection);
        if (lookAngleChange > 155 && accuracy > 0.9 && isMovingCrosshair) {
            return true;
        }

        // Update last attack time and look direction
        lastAttackTime.put(attackerUUID, currentTime);
        lastLookDirection.put(attackerUUID, attackerEyes.getDirection());
        return false;
    }
}
