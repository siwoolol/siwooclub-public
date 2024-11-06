package club.siwoo.siwooclub.ac.checks;

import club.siwoo.siwooclub.ac.ViolationManager;
import club.siwoo.siwooclub.ac.util.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class FlightCheck implements Listener {
    private final ViolationManager violationManager;
    private final PlayerData playerData; // Store player data for analysis

    public FlightCheck(ViolationManager violationManager) {
        this.violationManager = violationManager;
        playerData = new PlayerData();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // Exempt Creative Players
        if (player.getGameMode() == GameMode.CREATIVE // Exclude creative mode
                || (player.getGameMode() == GameMode.SPECTATOR) // Exclude spectator mode too lol
                || player.getAllowFlight()
                || !player.isFlying()
                || player.isInWater()
                || player.isGliding()
                || player.isInsideVehicle()) {
            return;
        }

        // Basic Flight Check (with GameMode and Vehicle Check)
        if (player.getGameMode() != GameMode.CREATIVE // Exclude creative mode
                && (player.getGameMode() == GameMode.SPECTATOR) // Exclude spectator mode too lol
                && !player.getAllowFlight()
                && player.isFlying()
                && !player.isInWater()
                && !player.isGliding()
                && !player.isInsideVehicle()) {  // Exclude vehicles
            violationManager.flagPlayer(player, "Flight (Basic)");
            event.setCancelled(true);
            return;
        }

        // Vertical Velocity Check (with sprint and fall adjustments)
        double verticalVelocity = to.getY() - from.getY();
        if (verticalVelocity > 0.42 && !player.isOnGround()) { // Don't check for sprinting here
            playerData.incrementFlightTicks(player);
            if (playerData.getFlightTicks(player) > 8) {
                violationManager.flagPlayer(player, "Flight (Velocity)");
            }
        } else if (verticalVelocity < -0.5) { // Allow faster falling
            playerData.resetFlightTicks(player);
        }

        // Ground Spoofing Check (with Slime Block and Distance Thresholds)
        if (!player.isOnGround() && to.getY() < from.getY()) {
            Block blockBelow = to.clone().subtract(0, 1, 0).getBlock();
            double distanceFallen = from.getY() - to.getY();

            if (blockBelow.getType() == Material.AIR
                    && distanceFallen > 1.5 // Increased minimum fall distance
                    && verticalVelocity < -0.5
                    && !blockBelow.isPassable()  // Check if block below is solid
                    && !player.getLocation().add(0, -2, 0).getBlock().getType().equals(Material.SLIME_BLOCK)) { // Allow slime block jumps
                violationManager.flagPlayer(player, "Flight (Ground Spoof)");
                event.setCancelled(true);
            }
        }

        // Check for Flight Speed Modifications (Experimental, Refined)
        if (player.getFlySpeed() > 0 && verticalVelocity >= -0.5) {
            double expectedDistance = player.getFlySpeed() * 0.1;
            double actualDistance = from.distance(to);
            double speedDifference = actualDistance - expectedDistance;

            // Additional Factors:
            boolean onIce = to.getBlock().getType() == Material.ICE || to.getBlock().getType() == Material.PACKED_ICE; // Consider ice movement
            boolean isNearEdge = to.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR; // Check if near edge of block

            // Adjust threshold based on factors
            double maxAllowedDistance = expectedDistance + (player.isSprinting() ? 0.5 : 0.2);
            maxAllowedDistance += onIce ? 0.1 : 0;  // Add allowance for ice
            maxAllowedDistance += isNearEdge ? 0.1 : 0; // Add allowance for edge of block

            if (speedDifference > maxAllowedDistance + 0.2) { // Slightly tighter threshold
                playerData.incrementFlightSpeedViolationTicks(player);
                if (playerData.getFlightSpeedViolationTicks(player) > 5) { // Back to 5 ticks
                    violationManager.flagPlayer(player, "Flight (Speed Modification)");
                    event.setCancelled(true);
                }
            } else {
                playerData.resetFlightSpeedViolationTicks(player);
            }
        }

        // Movement Pattern Analysis (with Horizontal Speed, Jump, Sprint, Momentum, Collision, and Ground Check Adjustments)
        Vector velocity = player.getVelocity();
        double horizontalSpeed = Math.hypot(velocity.getX(), velocity.getZ());
        boolean isJumping = to.getY() > from.getY() && player.isOnGround();
        boolean isSprinting = player.isSprinting();

        // Ground Check Refinement
        boolean nearGround = false;
        for (int y = 0; y < 3; y++) {
            if (to.getBlock().getRelative(BlockFace.DOWN, y).getType().isSolid()) {
                nearGround = true;
                break;
            }
        }

        // Calculate momentum change
        double momentumChange = Math.abs(velocity.getY() - playerData.getLastVerticalVelocity(player));
        double maxVerticalVelocity = isSprinting ? 0.6 + momentumChange * 0.6 : 0.3 + momentumChange * 0.3;
        double minHorizontalSpeed = isSprinting ? 0.3 : 0.2;

        // Check for recent collisions
        boolean recentCollision = playerData.hasCollidedRecently(player);

        // Check for blocks obstructing movement
        boolean blockedAbove = to.getBlock().getRelative(BlockFace.UP).getType().isSolid();

        // Dynamic thresholds
        boolean exemptDueToJump = isJumping && playerData.getTicksSinceJump(player) < 5;

        if (recentCollision || blockedAbove || exemptDueToJump || nearGround) {
            maxVerticalVelocity *= 1.6; // Increase threshold for collisions, obstructions, recent jumps, or being near ground
        }

        // Violation trigger
        if (horizontalSpeed > minHorizontalSpeed && verticalVelocity > maxVerticalVelocity && !player.isOnGround() && !isJumping) {
            if (!player.isGliding()) {
                violationManager.flagPlayer(player, "Flight (Pattern)");
            }
        }
        // Store current vertical velocity and collision and jump tick status for the next tick
        playerData.setLastVerticalVelocity(player, velocity.getY());
        playerData.setHasCollided(player, player.getLocation().getBlock().getType().isSolid());
        playerData.setTicksSinceJump(player, isJumping ? 0 : playerData.getTicksSinceJump(player) + 1); // Increment if not jumping
    }
}
