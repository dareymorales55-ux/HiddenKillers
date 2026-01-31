package com.deejay.projectanonymous;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.deejay.projectanonymous.listeners.OnJoin;
import com.deejay.projectanonymous.listeners.PlayerDeathEvent;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.haoshoku.nick.api.NickAPI;

public class Anonymous extends JavaPlugin {

    private final Map<UUID, String> realNames = new HashMap<>();

    @Override
    public void onEnable() {

        if (getServer().getPluginManager().getPlugin("NickAPI") == null) {
            getLogger().severe("NickAPI is required!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(new OnJoin(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathEvent(this), this);

        // Reload safety
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                realNames.put(player.getUniqueId(), player.getName());
                applyAnonymity(player);
            }
        }, 20L);
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (NickAPI.isNicked(player)) {
                NickAPI.resetNick(player);
                NickAPI.resetSkin(player);
                NickAPI.resetProfileName(player);
                NickAPI.refreshPlayer(player);

                player.displayName(Component.text(player.getName()));
                player.playerListName(Component.text(player.getName()));
            }
        }
        realNames.clear();
    }

    // =====================
    // Core API
    // =====================
    public void applyAnonymity(Player player) {
        String fakeName = "Player";

        realNames.put(player.getUniqueId(), player.getName());

        NickAPI.setNick(player, fakeName);
        NickAPI.setSkin(player, fakeName);
        NickAPI.refreshPlayer(player);

        player.displayName(Component.text(fakeName));
        player.playerListName(Component.text(fakeName));
    }

    public void reveal(Player player) {
        String realName = realNames.get(player.getUniqueId());

        NickAPI.resetNick(player);
        NickAPI.resetSkin(player);
        NickAPI.resetProfileName(player);
        NickAPI.refreshPlayer(player);

        player.displayName(Component.text(realName));
        player.playerListName(Component.text(realName));
    }

    public String getRealName(UUID uuid) {
        return realNames.get(uuid);
    }
}
