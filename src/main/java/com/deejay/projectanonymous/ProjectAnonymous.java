package com.deejay.projectunknown;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.haoshoku.nick.api.NickAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProjectUnknown extends JavaPlugin implements Listener {

    // Stores real names so we can restore them later (reveal, etc.)
    private final Map<UUID, String> realNames = new HashMap<>();

    @Override
    public void onEnable() {
        // Make sure NickAPI exists
        if (getServer().getPluginManager().getPlugin("NickAPI") == null) {
            getLogger().severe("NickAPI is required! Plugin disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register join/quit listener
        getServer().getPluginManager().registerEvents(this, this);

        // Anonymize anyone already online (reload safety)
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                realNames.put(player.getUniqueId(), player.getName());
                applyAnonymity(player);
            }
        }, 1L);

        getLogger().info("ProjectUnknown enabled");
    }

    @Override
    public void onDisable() {
        // Restore everyone on shutdown
        for (Player player : Bukkit.getOnlinePlayers()) {
            restoreIdentity(player);
        }
        realNames.clear();
        getLogger().info("ProjectUnknown disabled");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Save real name
        realNames.put(player.getUniqueId(), player.getName());

        // Apply anonymity shortly after join (prevents most flashes)
        Bukkit.getScheduler().runTaskLater(this, () -> {
            applyAnonymity(player);
        }, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        realNames.remove(event.getPlayer().getUniqueId());
    }

    /* =========================
       ANONYMITY LOGIC
       ========================= */

    public void applyAnonymity(Player player) {
        String anonymousName = "Player";
        String anonymousSkin = "Morkovnica"; // ✅ climate / slim skin

        // NickAPI handles tablist, chat, nameplates, skin
        NickAPI.setNick(player, anonymousName);
        NickAPI.setSkin(player, anonymousSkin);
        NickAPI.refreshPlayer(player);

        // Paper handles deaths, kills, advancements, hover text
        player.setDisplayName(anonymousName);
    }

    public void restoreIdentity(Player player) {
        if (!NickAPI.isNicked(player)) return;

        NickAPI.resetNick(player);
        NickAPI.resetSkin(player);
        NickAPI.resetUniqueId(player);
        NickAPI.resetProfileName(player);
        NickAPI.refreshPlayer(player);

        String realName = realNames.getOrDefault(player.getUniqueId(), player.getName());
        player.setDisplayName(realName);
    }

    public String getRealName(UUID uuid) {
        return realNames.get(uuid);
    }
}
