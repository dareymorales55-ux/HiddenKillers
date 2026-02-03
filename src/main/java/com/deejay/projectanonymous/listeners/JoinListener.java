package com.deejay.projectanonymous.listeners;

import com.deejay.projectanonymous.ProjectAnonymous;
import org.bukkit.Bukkit;
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

        // Remove vanilla join message
        event.setJoinMessage(null);

        // Delay to avoid skin/name race conditions
        Bukkit.getScheduler().runTaskLater(plugin, () -> anonymize(player), 10L);
    }

    private void anonymize(Player player) {
        if (!player.isOnline()) return;

        // Prevent double-nicking
        if (NickAPI.isNicked(player)) return;

        String fakeName = "Player";
        String skinOwner = "Morkovnica"; // slim Steve skin

        // Set BOTH name and skin
        NickAPI.setNick(player, fakeName);
        NickAPI.setSkin(player, skinOwner);

        // Apply changes
        NickAPI.refreshPlayer(player);

        // Ensure consistency
        player.setDisplayName(fakeName);
        player.setPlayerListName(fakeName);
    }
}
