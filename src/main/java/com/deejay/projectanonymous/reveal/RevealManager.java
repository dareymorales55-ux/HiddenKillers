package com.deejay.projectanonymous.reveal;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.deejay.projectanonymous.ProjectAnonymous;

import xyz.haoshoku.nick.api.NickAPI;

public final class RevealManager {

    private static final Set<UUID> revealed = new HashSet<>();

    private RevealManager() {}

    /* =========================
       REVEAL
       ========================= */

    public static void reveal(Player player, ProjectAnonymous plugin, long durationTicks) {
        if (revealed.contains(player.getUniqueId())) return;

        revealed.add(player.getUniqueId());

        // Show real identity
        NickAPI.resetNick(player);
        NickAPI.resetSkin(player);
        NickAPI.resetUniqueId(player);
        NickAPI.resetProfileName(player);
        NickAPI.refreshPlayer(player);

        player.setDisplayName(ChatColor.DARK_RED + player.getName());
        player.setPlayerListName(ChatColor.DARK_RED + player.getName());
        player.setGlowing(true);

        for (Player online : Bukkit.getOnlinePlayers()) {
            Scoreboard board = online.getScoreboard();
            if (board == Bukkit.getScoreboardManager().getMainScoreboard()) {
                board = Bukkit.getScoreboardManager().getNewScoreboard();
                online.setScoreboard(board);
            }

            Team team = board.getTeam("revealed");
            if (team == null) {
                team = board.registerNewTeam("revealed");
                team.setColor(ChatColor.DARK_RED);
            }

            team.addEntry(player.getName());
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> hide(player), durationTicks);
    }

    /* =========================
       HIDE (Re-anonymize)
       ========================= */

    public static void hide(Player player) {
        if (!revealed.remove(player.getUniqueId())) return;

        NickAPI.setNick(player, "Morkovnica");
        NickAPI.setSkin(player, "Morkovnica");
        NickAPI.refreshPlayer(player);

        player.setDisplayName("Morkovnica");
        player.setPlayerListName("Morkovnica");
        player.setGlowing(false);

        for (Player online : Bukkit.getOnlinePlayers()) {
            Team team = online.getScoreboard().getTeam("revealed");
            if (team != null) {
                team.removeEntry(player.getName());
            }
        }
    }

    /* =========================
       CAUGHT DURING REVEAL
       ========================= */

    public static boolean handleDeath(Player victim, ProjectAnonymous plugin) {
        if (!isRevealed(victim)) return false;

        // Broadcast caught message
        Bukkit.broadcastMessage(
                ChatColor.RED + victim.getName() + " has been caught."
        );

        // Ban
        Bukkit.getBanList(BanList.Type.NAME).addBan(
                victim.getName(),
                ChatColor.DARK_RED + "Your cover was blown.",
                null,
                null
        );

        // Kick safely
        Bukkit.getScheduler().runTask(plugin, () ->
                victim.kickPlayer(ChatColor.DARK_RED + "Your cover was blown.")
        );

        revealed.remove(victim.getUniqueId());
        return true;
    }

    /* =========================
       STATE
       ========================= */

    public static boolean isRevealed(Player player) {
        return revealed.contains(player.getUniqueId());
    }
}
