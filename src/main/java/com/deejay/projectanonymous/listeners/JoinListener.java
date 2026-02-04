package com.deejay.projectanonymous.listeners;

import com.deejay.projectanonymous.ProjectAnonymous;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import xyz.haoshoku.nick.api.NickAPI;

public class JoinListener implements Listener {

    private final ProjectAnonymous plugin;

    public JoinListener(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Cancel vanilla join message
        event.setJoinMessage(null);

        // Custom join message
        Bukkit.broadcastMessage(
                ChatColor.YELLOW + "Player has joined"
        );

        // Delay to avoid skin race conditions
        Bukkit.getScheduler().runTaskLater(plugin, () -> anonymize(player), 10L);
    }

    private void anonymize(Player player) {
        if (!player.isOnline()) return;

        // --- SKIN ONLY ---
        NickAPI.setSkin(player, "Morkovnica");
        NickAPI.refreshPlayer(player);

        // --- NAME ONLY (no NickAPI) ---
        player.setDisplayName("Player");
        player.setPlayerListName("Player");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Cancel vanilla quit message
        event.setQuitMessage(null);

        // Custom quit message
        Bukkit.broadcastMessage(
                ChatColor.YELLOW + "Player has left"
        );
    }
}
