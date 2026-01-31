package com.deejay.projectanonymous;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.haoshoku.nick.api.NickAPI;

public class ProjectAnonymous extends JavaPlugin {

    @Override
    public void onEnable() {
        // Ensure NickAPI is present
        if (Bukkit.getPluginManager().getPlugin("NickAPI") == null) {
            getLogger().severe("NickAPI not found! Disabling ProjectAnonymous.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register listeners
        Bukkit.getPluginManager().registerEvents(
                new com.deejay.projectanonymous.listeners.JoinListener(this),
                this
        );

        Bukkit.getPluginManager().registerEvents(
                new com.deejay.projectanonymous.listeners.DeathListener(this),
                this
        );

        getLogger().info("ProjectAnonymous enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProjectAnonymous disabled.");
    }

    /**
     * Utility method if other classes ever need to check nick state
     */
    public boolean isNicked(org.bukkit.entity.Player player) {
        return NickAPI.isNicked(player);
    }
}
