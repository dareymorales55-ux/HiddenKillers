package com.deejay.projectanonymous.items;

import java.util.*;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.deejay.projectanonymous.ProjectAnonymous;

public class DetectivesCompass implements Listener {

    private final ProjectAnonymous plugin;

    private static final String COMPASS_NAME =
            ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Detectives Compass";

    private static final List<String> COMPASS_LORE = List.of(
            ChatColor.GRAY + "Right-click to hunt the closest player",
            ChatColor.GRAY + "Tracks target for 5 minutes",
            ChatColor.RED + "Overworld only"
    );

    private static final int TRACK_DURATION_SECONDS = 300; // 5 minutes
    private static final int COOLDOWN_SECONDS = 900;       // 15 minutes
    private static final double MIN_TRACK_DISTANCE = 8.0;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, TrackingData> tracking = new HashMap<>();

    public DetectivesCompass(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    /* =========================
       ITEM CREATION
       ========================= */

    public static ItemStack createCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        meta.setDisplayName(COMPASS_NAME);
        meta.setLore(COMPASS_LORE);

        compass.setItemMeta(meta);
        return compass;
    }

    /* =========================
       USE LOGIC
       ========================= */

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.COMPASS) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!COMPASS_NAME.equals(meta.getDisplayName())) return;

        event.setCancelled(true);

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(ChatColor.RED + "You must be in the overworld.");
            return;
        }

        // ===== COOLDOWN =====
        if (cooldowns.containsKey(player.getUniqueId())) {
            long remaining =
                    (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;

            if (remaining > 0) {
                player.sendMessage(ChatColor.RED + "Cooldown: " + formatTime((int) remaining));
                return;
            }
            cooldowns.remove(player.getUniqueId());
        }

        // ===== FIND CLOSEST VALID TARGET (EXCLUDE <= 8 BLOCKS) =====
        Player closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(player)) continue;
            if (target.getWorld().getEnvironment() != World.Environment.NORMAL) continue;

            double distance = player.getLocation().distance(target.getLocation());
            if (distance <= MIN_TRACK_DISTANCE) continue;

            if (distance < closestDistance) {
                closestDistance = distance;
                closestTarget = target;
            }
        }

        if (closestTarget == null) {
            player.sendMessage(ChatColor.RED + "No players to track.");
            return;
        }

        // ===== START HUNT =====
        player.sendMessage(ChatColor.YELLOW + "Tracking " + closestTarget.getName());
        closestTarget.sendMessage(ChatColor.RED + "You are being tracked.");

        player.playSound(player.getLocation(),
                Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);
        closestTarget.playSound(closestTarget.getLocation(),
                Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);

        playParticles(closestTarget);

        cooldowns.put(
                player.getUniqueId(),
                System.currentTimeMillis() + COOLDOWN_SECONDS * 1000L
        );
        player.setCooldown(Material.COMPASS, COOLDOWN_SECONDS * 20);

        tracking.put(
                player.getUniqueId(),
                new TrackingData(closestTarget.getUniqueId())
        );

        startTracking(player);
    }

    /* =========================
       TRACKING LOOP
       ========================= */

    private void startTracking(Player hunter) {
        new BukkitRunnable() {
            int timeLeft = TRACK_DURATION_SECONDS;

            @Override
            public void run() {
                if (!hunter.isOnline() || !tracking.containsKey(hunter.getUniqueId())) {
                    cancel();
                    return;
                }

                Player target = Bukkit.getPlayer(tracking.get(hunter.getUniqueId()).target);
                if (target == null || !target.isOnline()) {
                    hunter.sendMessage(ChatColor.RED + "Target offline.");
                    tracking.remove(hunter.getUniqueId());
                    cancel();
                    return;
                }

                if (!hunter.getWorld().equals(target.getWorld())) {
                    hunter.sendMessage(ChatColor.RED + "Target left dimension.");
                    tracking.remove(hunter.getUniqueId());
                    cancel();
                    return;
                }

                double distance = hunter.getLocation().distance(target.getLocation());
                String arrow = getDirectionArrow(hunter, target);

                hunter.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        new TextComponent(
                                ChatColor.RED + "" + ChatColor.BOLD + formatTime(timeLeft) +
                                ChatColor.GRAY + " | " +
                                ChatColor.WHITE + (int) distance + "m " + arrow
                        )
                );

                timeLeft--;
                if (timeLeft <= 0) {
                    hunter.sendMessage(ChatColor.GRAY + "Tracking ended.");
                    target.sendMessage(ChatColor.GRAY + "Tracking ended.");

                    hunter.playSound(hunter.getLocation(),
                            Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1f, 1f);
                    target.playSound(target.getLocation(),
                            Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1f, 1f);

                    tracking.remove(hunter.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /* =========================
       PARTICLES (OLD STYLE)
       ========================= */

    private void playParticles(Player target) {
        Location loc = target.getLocation();
        double radius = 1.5;
        int points = 24;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            loc.getWorld().spawnParticle(
                    Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                    loc.clone().add(x, 1, z),
                    1, 0, 0, 0, 0
            );
        }
    }

    /* =========================
       DIRECTION (8 ARROWS)
       ========================= */

    private String getDirectionArrow(Player hunter, Player target) {
        double dx = target.getX() - hunter.getX();
        double dz = target.getZ() - hunter.getZ();

        double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        double yaw = hunter.getLocation().getYaw();
        double relative = (angle - yaw + 360) % 360;

        if (relative < 22.5 || relative >= 337.5) return "⬆";
        if (relative < 67.5) return "⬈";
        if (relative < 112.5) return "➡";
        if (relative < 157.5) return "⬊";
        if (relative < 202.5) return "⬇";
        if (relative < 247.5) return "⬋";
        if (relative < 292.5) return "⬅";
        return "⬉";
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private static class TrackingData {
        UUID target;

        TrackingData(UUID target) {
            this.target = target;
        }
    }
}
