package club.siwoo.siwooclub.ac.util;

import org.bukkit.entity.Player;

import java.util.*;

public class PlayerData {

    private final Map<UUID, Integer> flightTicks = new HashMap<>();
    private final Map<UUID, Integer> flightSpeedViolationTicks = new HashMap<>();
    private final Map<UUID, Double> lastVerticalVelocity = new HashMap<>();
    private final Map<UUID, Boolean> recentCollision = new HashMap<>();
    private final Map<UUID, Integer> ticksSinceJump = new HashMap<>();
    private final Map<UUID, Long> lastLeftClickTime = new HashMap<>();
    private final Map<UUID, Long> lastRightClickTime = new HashMap<>();
    private final Map<UUID, Boolean> recentlyFlagged = new HashMap<>();
    private final Map<UUID, Queue<Integer>> leftClickIntervals = new HashMap<>();
    private final Map<UUID, Queue<Integer>> rightClickIntervals = new HashMap<>();
    private final Map<UUID, Integer> criticalHits = new HashMap<>();
    private final Map<UUID, Integer> normalHits = new HashMap<>();

    public void incrementFlightTicks(Player player) {
        flightTicks.put(player.getUniqueId(), getFlightTicks(player) + 1);
    }

    public void resetFlightTicks(Player player) {
        flightTicks.put(player.getUniqueId(), 0);
    }

    public int getFlightTicks(Player player) {
        return flightTicks.getOrDefault(player.getUniqueId(), 0);
    }

    public void incrementFlightSpeedViolationTicks(Player player) {
        flightSpeedViolationTicks.put(player.getUniqueId(), getFlightSpeedViolationTicks(player) + 1);
    }

    public void resetFlightSpeedViolationTicks(Player player) {
        flightSpeedViolationTicks.put(player.getUniqueId(), 0);
    }

    public int getFlightSpeedViolationTicks(Player player) {
        return flightSpeedViolationTicks.getOrDefault(player.getUniqueId(), 0);
    }

    public void setLastVerticalVelocity(Player player, double velocity) {
        lastVerticalVelocity.put(player.getUniqueId(), velocity);
    }

    public double getLastVerticalVelocity(Player player) {
        return lastVerticalVelocity.getOrDefault(player.getUniqueId(), 0.0);
    }

    public void setHasCollided(Player player, boolean collided) {
        recentCollision.put(player.getUniqueId(), collided);
    }

    public boolean hasCollidedRecently(Player player) {
        return recentCollision.getOrDefault(player.getUniqueId(), false);
    }

    public void setTicksSinceJump(Player player, int ticks) {
        ticksSinceJump.put(player.getUniqueId(), ticks);
    }

    public int getTicksSinceJump(Player player) {
        return ticksSinceJump.getOrDefault(player.getUniqueId(), 0);
    }

    public void setFlagged(Player player, boolean flagged) {
        recentlyFlagged.put(player.getUniqueId(), flagged);
    }

    public boolean wasRecentlyFlagged(Player player) {
        return recentlyFlagged.getOrDefault(player.getUniqueId(), false);
    }

    public long getLastClickTime(Player player, boolean isRightClick) {
        return isRightClick ? lastRightClickTime.getOrDefault(player.getUniqueId(), 0L)
                : lastLeftClickTime.getOrDefault(player.getUniqueId(), 0L);
    }

    public void setLastClickTime(Player player, long time, boolean isRightClick) {
        if (isRightClick) {
            lastRightClickTime.put(player.getUniqueId(), time);
        } else {
            lastLeftClickTime.put(player.getUniqueId(), time);
        }
    }

    public void addClickInterval(Player player, int interval, boolean isRightClick) {
        Queue<Integer> intervals = isRightClick ? rightClickIntervals.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>())
                : leftClickIntervals.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
        intervals.add(interval);
        if (intervals.size() > 10) {  // Keep a maximum of 10 intervals for analysis
            intervals.poll();
        }
    }

    public int analyzeClickIntervals(Player player, int expectedInterval, int allowedVariation, boolean isRightClick) {
        Queue<Integer> intervals = isRightClick ? rightClickIntervals.get(player.getUniqueId())
                : leftClickIntervals.get(player.getUniqueId());
        if (intervals == null || intervals.size() < 10) {
            return 0;
        }

        int violations = 0;
        for (int interval : intervals) {
            if (Math.abs(interval - expectedInterval) > allowedVariation) {
                violations++;
            }
        }
        return violations;
    }

    public double getCriticalHitRate(Player player) {
        int criticals = criticalHits.getOrDefault(player.getUniqueId(), 0);
        int normals = normalHits.getOrDefault(player.getUniqueId(), 0);
        int totalHits = criticals + normals;
        return totalHits == 0 ? 0.0 : (double) criticals / totalHits;
    }

    // Critical Hits
    public void incrementCriticalHits(Player player) {
        criticalHits.put(player.getUniqueId(), getCriticalHits(player) + 1);
    }

    public void incrementNormalHits(Player player) {
        normalHits.put(player.getUniqueId(), getNormalHits(player) + 1);
    }

    public int getCriticalHits(Player player) {
        return criticalHits.getOrDefault(player.getUniqueId(), 0);
    }

    public int getNormalHits(Player player) {
        return normalHits.getOrDefault(player.getUniqueId(), 0);
    }

    // You can add more fields and methods to track other player data as needed.
}
