package com.deejay.projectanonymous.items;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import com.deejay.projectanonymous.ProjectAnonymous;
import com.deejay.projectanonymous.reveal.RevealManager;

public class BellOfTruth implements Listener {

    private final ProjectAnonymous plugin;
    private final NamespacedKey bellKey;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    // ===== CONSTANTS =====
    private static final int COOLDOWN_SECONDS = 300;
    private static final int REVEAL_DURATION_TICKS = 200;
    private static final double RADIUS = 20.0;

    public BellOfTruth(ProjectAnonymous plugin) {
        this.plugin = plugin;
        this.bellKey = new NamespacedKey(plugin, "bell_of_truth");
    }

    /* =========================
       PLACE / BREAK
       ========================= */

    @EventHandler
    public void onBellPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.BELL) return;
        if (!item.hasItemMeta()) return;

        if (!item.getItemMeta().getPersistentDataContainer()
                .has(bellKey, PersistentDataType.BOOLEAN)) return;

        Block block = event.getBlockPlaced();
        block.getChunk().getPersistentDataContainer()
                .set(getBlockKey(block), PersistentDataType.BOOLEAN, true);
    }

    @EventHandler
    public void onBellBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.BELL) return;

        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block);

        if (!pdc.has(blockKey, PersistentDataType.BOOLEAN)) return;

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), createBell());
        pdc.remove(blockKey);
    }

    /* =========================
       RING
       ========================= */

    @EventHandler
    public void onBellRing(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BELL) return;

        if (!block.getChunk().getPersistentDataContainer()
                .has(getBlockKey(block), PersistentDataType.BOOLEAN)) return;

        Player player = event.getPlayer();

        long remaining = getCooldownRemaining(player.getUniqueId());
        if (remaining > 0) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Bell on cooldown: " + formatTime((int) remaining));
            return;
        }

        cooldowns.put(
                player.getUniqueId(),
                System.currentTimeMillis() + COOLDOWN_SECONDS * 1000L
        );

        Location bellLoc = block.getLocation().add(0.5, 0.5, 0.5);
        spawnBellCircle(bellLoc);

        // 🔥 Delegate reveal ONLY to RevealManager
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.getWorld().equals(bellLoc.getWorld())) continue;
            if (target.getLocation().distance(bellLoc) > RADIUS) continue;

            RevealManager.reveal(target, plugin, REVEAL_DURATION_TICKS);
        }
    }

    /* =========================
       VISUALS
       ========================= */

    private void spawnBellCircle(Location center) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= REVEAL_DURATION_TICKS) {
                    cancel();
                    return;
                }

                int points = 40;
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = center.getX() + RADIUS * Math.cos(angle);
                    double z = center.getZ() + RADIUS * Math.sin(angle);

                    center.getWorld().spawnParticle(
                            Particle.TRIAL_SPAWNER_DETECTION,
                            new Location(center.getWorld(), x, center.getY(), z),
                            1
                    );
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /* =========================
       ITEM
       ========================= */

    public ItemStack createBell() {
        ItemStack bell = new ItemStack(Material.BELL);
        ItemMeta meta = bell.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Bell of Truth");
        meta.setLore(List.of(
                ChatColor.GRAY + "Reveals nearby players"
        ));

        // ✅ SAFE GLINT ENCHANT
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        meta.getPersistentDataContainer()
                .set(bellKey, PersistentDataType.BOOLEAN, true);

        bell.setItemMeta(meta);
        return bell;
    }

    /* ========================= */

    private NamespacedKey getBlockKey(Block block) {
        return new NamespacedKey(
                plugin,
                "bell_" + block.getX() + "_" + block.getY() + "_" + block.getZ()
        );
    }

    private long getCooldownRemaining(UUID uuid) {
        Long expiry = cooldowns.get(uuid);
        if (expiry == null) return 0;

        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        if (remaining <= 0) {
            cooldowns.remove(uuid);
            return 0;
        }
        return remaining;
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
