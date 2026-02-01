package com.deejay.projectanonymous;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.haoshoku.nick.api.NickAPI;

public class RevealManager {

    private static final Set<UUID> REVEALED = new HashSet<>();

    /**
     * Reveals a player temporarily
     */
    public static void reveal(Player player, ProjectAnonymous plugin, int durationTicks) {
        if (REVEALED.contains(player.getUniqueId())) return;

        REVEALED.add(player.getUniqueId());

        // Reset NickAPI to real identity
        NickAPI.resetNick(player);
        NickAPI.resetSkin(player);
        NickAPI.resetProfileName(player);
        NickAPI.resetUniqueId(player);
        NickAPI.refreshPlayer(player);

        String realName = player.getName();
        String darkRedName = ChatColor.DARK_RED + realName;

        // Paper-side name changes (for deaths, advancements, hover text, etc.)
        player.setDisplayName(darkRedName);
        player.setPlayerListName(darkRedName);
        player.setGlowing(true);

        // Re-anonymize later
        new BukkitRunnable() {
            @Override
            public void run() {
                REVEALED.remove(player.getUniqueId());
                player.setGlowing(false);
                plugin.applyAnonymity(player);
            }
        }.runTaskLater(plugin, durationTicks);
    }

    /**
     * Returns whether a player is currently revealed
     */
    public static boolean isRevealed(Player player) {
        return REVEALED.contains(player.getUniqueId());
    }

    /**
     * Called when a revealed player is killed
     */
    public static void handleRevealDeath(Player victim) {
        String realName = victim.getName();

        // Ban (dark red)
        Bukkit.getBanList(BanList.Type.NAME).addBan(
                realName,
                ChatColor.DARK_RED + "Your cover was blown.",
                null,
                null
        );

        victim.kickPlayer(ChatColor.DARK_RED + "Your cover was blown.");

        // Chat message (light red)
        Bukkit.broadcastMessage(ChatColor.RED + realName + " has been caught.");
    }
}
