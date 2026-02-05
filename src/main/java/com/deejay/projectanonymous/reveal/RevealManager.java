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

    private static final String ANON_NAME = "Player";
    private static final String ANON_SKIN = "Morkovnica";

    private RevealManager() {}

    /* =========================
       REVEAL
       ========================= */

    public static void reveal(Player player, ProjectAnonymous plugin, long durationTicks) {
        if (revealed.contains(player.getUniqueId())) return;

        revealed.add(player.getUniqueId());

        // Restore REAL skin
        NickAPI.resetSkin(player);
        NickAPI.refreshPlayer(player);

        // Show REAL name (Paper only)
        player.setDisplayName(ChatColor.DARK_RED + player.getName());
        player.setPlayerListName(ChatColor.DARK_RED + player.getName());

        // Glow
        player.setGlowing(true);

        // Dark red glow team
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

        // Auto hide
        Bukkit.getScheduler().runTaskLater(plugin, () -> hide(player), durationTicks);
    }

    /* =========================
       HIDE
       ========================= */

    public static void hide(Player player) {
        if (!revealed.remove(player.getUniqueId())) return;

        // Anonymous skin
        NickAPI.setSkin(player, ANON_SKIN);
        NickAPI.refreshPlayer(player);

        // Anonymous name
        player.setDisplayName(ANON_NAME);
        player.setPlayerListName(ANON_NAME);

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

    public static void handleCaught(Player victim, ProjectAnonymous plugin) {

        if (!revealed.contains(victim.getUniqueId())) return;

        // Light red message (NOT bold)
        Bukkit.broadcastMessage(
                ChatColor.RED + victim.getName() + " has been caught."
        );

        Bukkit.getBanList(BanList.Type.NAME).addBan(
                victim.getName(),
                ChatColor.DARK_RED + "Your cover was blown.",
                null,
                null
        );

        Bukkit.getScheduler().runTask(plugin, () ->
                victim.kickPlayer(ChatColor.DARK_RED + "Your cover was blown.")
        );

        revealed.remove(victim.getUniqueId());
    }

    /* =========================
       STATE
       ========================= */

    public static boolean isRevealed(Player player) {
        return revealed.contains(player.getUniqueId());
    }
}
