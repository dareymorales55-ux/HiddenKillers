package com.deejay.projectanonymous.items;

import java.util.*;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.deejay.projectanonymous.ProjectAnonymous;
import com.deejay.projectanonymous.reveal.RevealManager;

public class DragonEgg implements Listener {

    private final ProjectAnonymous plugin;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> heartMap = new HashMap<>();

    private static final int COOLDOWN_SECONDS = 25;
    private static final int COUNTDOWN_SECONDS = 5;
    private static final int REVEAL_DURATION_TICKS = 200;
    private static final double RING_RADIUS = 8.0;
    private static final double DISPLAY_RADIUS = 15.0;
    private static final int MAX_HEARTS = 20;

    public DragonEgg(ProjectAnonymous plugin) {
        this.plugin = plugin;
        startParticleTask();
    }

    /* =========================
       EGG CHECK / CONVERT
       ========================= */

    private boolean isEgg(ItemStack item) {
        return item != null && item.getType() == Material.DRAGON_EGG;
    }

    private void convertEgg(ItemStack egg) {
        ItemMeta meta = egg.getItemMeta();
        if (meta == null) return;

        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Dragon Egg");
        meta.setLore(List.of(
                ChatColor.GRAY + "Gain heart on kill",
                ChatColor.GRAY + "Max of 20 hearts",
                ChatColor.GRAY + "Right-click to reveal all players",
                ChatColor.GRAY + "within an 8 block radius for 10 seconds"
        ));

        Enchantment glint = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
        if (glint != null && !meta.hasEnchant(glint)) {
            meta.addEnchant(glint, 1, true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        egg.setItemMeta(meta);
    }

    private boolean hasEgg(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isEgg(item)) return true;
        }
        return false;
    }

    /* =========================
       RIGHT CLICK ABILITY
       ========================= */

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (!isEgg(event.getItem())) return;

        convertEgg(event.getItem());
        Player player = event.getPlayer();

        long remaining = getCooldown(player);
        if (remaining > 0) {
            player.sendMessage(ChatColor.RED + "Dragon Egg cooldown: " + remaining + "s");
            return;
        }

        cooldowns.put(player.getUniqueId(),
                System.currentTimeMillis() + COOLDOWN_SECONDS * 1000L);

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_GROWL,
                1f, 1f
        );

        startCountdown(player);
    }

    private void startCountdown(Player holder) {
        new BukkitRunnable() {
            int time = COUNTDOWN_SECONDS;

            @Override
            public void run() {
                if (!holder.isOnline()) {
                    cancel();
                    return;
                }

                Location center = holder.getLocation();
                spawnRing(center);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getWorld().equals(center.getWorld())) continue;
                    if (p.getLocation().distance(center) > DISPLAY_RADIUS) continue;

                    ChatColor color = time == 1 ? ChatColor.RED : ChatColor.LIGHT_PURPLE;
                    p.sendTitle(color + "" + ChatColor.BOLD + time, "", 0, 20, 0);
                }

                if (time == 0) {
                    revealPlayers(holder);
                    cancel();
                    return;
                }
                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void revealPlayers(Player holder) {
        Location center = holder.getLocation();

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(holder)) continue;
            if (!target.getWorld().equals(center.getWorld())) continue;
            if (target.getLocation().distance(center) > RING_RADIUS) continue;

            RevealManager.reveal(target, plugin, REVEAL_DURATION_TICKS);
        }
    }

    private void spawnRing(Location center) {
        for (int i = 0; i < 40; i++) {
            double angle = 2 * Math.PI * i / 40;
            double x = center.getX() + RING_RADIUS * Math.cos(angle);
            double z = center.getZ() + RING_RADIUS * Math.sin(angle);

            center.getWorld().spawnParticle(
                    Particle.PORTAL,
                    x, center.getY() + 0.1, z,
                    1, 0, 0, 0, 0
            );
        }
    }

    /* =========================
       HEART SYSTEM
       ========================= */

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || !hasEgg(killer)) return;

        int hearts = heartMap.getOrDefault(killer.getUniqueId(), 0);
        if (hearts >= MAX_HEARTS) return;

        hearts++;
        heartMap.put(killer.getUniqueId(), hearts);
        updateHealth(killer);
    }

    private void updateHealth(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        int hearts = heartMap.getOrDefault(player.getUniqueId(), 0);
        attr.setBaseValue(20 + hearts * 2);
    }

    /* =========================
       INVENTORY / PARTICLES
       ========================= */

    private void startParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!hasEgg(player)) continue;

                    player.getWorld().spawnParticle(
                            Particle.PORTAL,
                            player.getLocation().add(0, 1, 0),
                            5, 0.3, 0.5, 0.3, 0
                    );
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!hasEgg(player)) {
                heartMap.remove(player.getUniqueId());
                AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
                if (attr != null) attr.setBaseValue(20);
            }
        }, 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!hasEgg(player)) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> updateHealth(player), 1L);
    }

    /* ========================= */

    private long getCooldown(Player player) {
        Long expiry = cooldowns.get(player.getUniqueId());
        if (expiry == null) return 0;

        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        if (remaining <= 0) {
            cooldowns.remove(player.getUniqueId());
            return 0;
        }
        return remaining;
    }
}
