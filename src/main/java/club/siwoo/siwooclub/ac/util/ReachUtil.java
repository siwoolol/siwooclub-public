package club.siwoo.siwooclub.ac.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ReachUtil {

    public static double getActualAttackDistance(Player attacker, Entity victim) {
        Location attackerEye = attacker.getEyeLocation();
        Location victimLocation = victim.getLocation();
        Vector direction = victimLocation.toVector().subtract(attackerEye.toVector()).normalize();

        // Adjust victim's location based on hitbox size
        victimLocation.add(direction.clone().multiply(victim.getWidth() / 3 + 0.1)); // 0.1 for a bit of extra reach

        // Check if attacker is looking at victim
        double dotProduct = attackerEye.getDirection().dot(direction);
        if (dotProduct <= 0) {
            return Double.MAX_VALUE; // Attacker not looking at the victim
        }

        return attackerEye.distance(victimLocation);
    }
}
