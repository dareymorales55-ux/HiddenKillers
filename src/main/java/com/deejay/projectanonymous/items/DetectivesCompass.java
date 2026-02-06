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
            ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Detective's Compass";

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, TrackingData> tracking = new HashMap<>();

    public DetectivesCompass(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.COMPASS) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return;

        if (!meta.getDisplayName().equals(COMPASS_NAME)) return;

        event.setCancelled(true);

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(ChatColor.RED + "You must be in the overworld.");
            return;
        }

        // Cooldown
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage(ChatColor.RED + "Cooldown: " + formatTime((int) timeLeft));
                return;
            }
            cooldowns.remove(player.getUniqueId());
        }

        List<Player> targets = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player) && p.getWorld().getEnvironment() == World.Environment.NORMAL) {
                targets.add(p);
            }
        }

        if (targets.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No players to track.");
            return;
        }

        Player target = targets.get(new Random().nextInt(targets.size()));

        player.sendMessage(ChatColor.YELLOW + "Tracking " + target.getName());
        target.sendMessage(ChatColor.RED + "You are being tracked.");

        playParticles(target);

        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 60_000);
        player.setCooldown(Material.COMPASS, 20 * 60);

        tracking.put(player.getUniqueId(), new TrackingData(target.getUniqueId(), 30));
        startTracking(player);
    }

    private void startTracking(Player hunter) {
        new BukkitRunnable() {
            int seconds = 30;

            public void run() {
                if (!hunter.isOnline() || !tracking.containsKey(hunter.getUniqueId())) {
                    cancel();
                    return;
                }

                TrackingData data = tracking.get(hunter.getUniqueId());
                Player target = Bukkit.getPlayer(data.target);

                if (target == null || !target.isOnline()) {
                    hunter.sendMessage(ChatColor.RED + "Target offline.");
                    tracking.remove(hunter.getUniqueId());
                    cancel();
                    return;
                }

                double distance = hunter.getLocation().distance(target.getLocation());
                String arrow = getArrow(hunter, target);

                hunter.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.RED + formatTime(seconds) +
                                ChatColor.GRAY + " | " +
                                (int) distance + "m " + arrow)
                );

                seconds--;
                if (seconds <= 0) {
                    hunter.sendMessage(ChatColor.GRAY + "Tracking ended.");
                    target.sendMessage(ChatColor.GRAY + "Tracking ended.");
                    tracking.remove(hunter.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void playParticles(Player target) {
        Location loc = target.getLocation();
        for (int i = 0; i < 20; i++) {
            double angle = 2 * Math.PI * i / 20;
            loc.getWorld().spawnParticle(
                    Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                    loc.clone().add(Math.cos(angle), 1, Math.sin(angle)),
                    1
            );
        }
    }

    private String getArrow(Player from, Player to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double angle = Math.toDegrees(Math.atan2(dz, dx)) - from.getYaw();
        angle = (angle + 360) % 360;

        if (angle < 45 || angle >= 315) return "⬆";
        if (angle < 135) return "➡";
        if (angle < 225) return "⬇";
        return "⬅";
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private static class TrackingData {
        UUID target;
        int duration;

        TrackingData(UUID target, int duration) {
            this.target = target;
            this.duration = duration;
        }
    }
}
