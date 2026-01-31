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

    public JoinListener(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Suppress vanilla join message
        event.setJoinMessage(null);

        // Delay to avoid skin race conditions
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            anonymize(player);
        }, 10L); // ~0.5 seconds
    }

    private void anonymize(Player player) {
        if (!player.isOnline()) return;

        // Already nicked? Don't reapply
        if (NickAPI.isNicked(player)) return;

        String fakeName = "Morkovnica";
        String skinOwner = "Morkovnica"; // slim Steve skin

        NickAPI.setNick(player, fakeName);
        NickAPI.setSkin(player, skinOwner);

        // IMPORTANT: refresh after both
        NickAPI.refreshPlayer(player);

        // Display name consistency
        player.setDisplayName(fakeName);
        player.setPlayerListName(fakeName);
    }
}
