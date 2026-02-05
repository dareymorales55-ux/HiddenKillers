package com.deejay.projectanonymous.listeners;

import com.deejay.projectanonymous.ProjectAnonymous;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import xyz.haoshoku.nick.api.NickAPI;

public class JoinListener implements Listener {

    private final ProjectAnonymous plugin;

    private static final String ANON_NAME = "Player";
    private static final String ANON_SKIN = "Morkovnica";

    public JoinListener(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Custom join message
        event.setJoinMessage(ChatColor.YELLOW + "Player has joined");

        // Delay avoids race conditions
        Bukkit.getScheduler().runTaskLater(plugin, () -> anonymize(player), 5L);
    }

    private void anonymize(Player player) {
        if (!player.isOnline()) return;

        /* =========================
           SKIN (NickAPI ONLY)
           ========================= */
        NickAPI.setSkin(player, ANON_SKIN);
        NickAPI.refreshPlayer(player);

        /* =========================
           NAME / TAGS (Paper ONLY)
           ========================= */
        player.setDisplayName(ANON_NAME);
        player.setPlayerListName(ANON_NAME);

        // Ensure nametag is clean
        player.customName(null);
    }
}
