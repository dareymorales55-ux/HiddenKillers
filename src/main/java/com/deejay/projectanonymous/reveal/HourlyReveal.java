package com.deejay.projectanonymous.reveal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.deejay.projectanonymous.ProjectAnonymous;

public final class HourlyReveal implements Listener {

    private final ProjectAnonymous plugin;
    private boolean started = false;
    private final Random random = new Random();

    public HourlyReveal(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    /* =========================
       START ON FIRST JOIN
       ========================= */

    @EventHandler
    public void onFirstJoin(PlayerJoinEvent event) {
        if (started) return;

        started = true;
        startCycle();
    }

    /* =========================
       MAIN LOOP
       ========================= */

    private void startCycle() {
        new BukkitRunnable() {
            @Override
            public void run() {

                // Step 1: wait 10 minutes
                Bukkit.getScheduler().runTaskLater(plugin, () -> {

                    Bukkit.broadcastMessage(
                            ChatColor.RED + "" + ChatColor.BOLD +
                            "Player(s) will be revealed promptly…"
                    );

                    // Step 2: wait 5 seconds
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {

                        revealRandomPlayers();

                    }, 20L * 5);

                }, 20L * 60 * 10);

            }
        }.runTask(plugin);
    }

    /* =========================
       REVEAL LOGIC
       ========================= */

    private void revealRandomPlayers() {

        // ONLY players NOT already revealed
        List<Player> eligible = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!RevealManager.isRevealed(player)) {
                eligible.add(player);
            }
        }

        if (eligible.isEmpty()) return;

        Collections.shuffle(eligible);

        int revealCount = Math.min(
                eligible.size(),
                random.nextBoolean() ? 1 : 2
        );

        for (int i = 0; i < revealCount; i++) {
            Player target = eligible.get(i);

            RevealManager.reveal(
                    target,
                    plugin,
                    20L * 60 * 10 // 10 minutes
            );
        }

        // Reveal start sound
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(
                    p.getLocation(),
                    Sound.BLOCK_RESPAWN_ANCHOR_CHARGE,
                    1.0f,
                    1.0f
            );
        }

        // End reveal + schedule next cycle
        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(
                        p.getLocation(),
                        Sound.BLOCK_BREWING_STAND_BREW,
                        1.0f,
                        1.0f
                );
            }

            // Wait 1 hour before restarting cycle
            Bukkit.getScheduler().runTaskLater(
                    plugin,
                    this::startCycle,
                    20L * 60 * 60
            );

        }, 20L * 60 * 10);
    }
}
