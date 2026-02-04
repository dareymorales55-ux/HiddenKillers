package com.deejay.projectanonymous;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.deejay.projectanonymous.commands.RevealCommand;
import com.deejay.projectanonymous.listeners.DeathListener;
import com.deejay.projectanonymous.listeners.JoinListener;
import com.deejay.projectanonymous.reveal.HourlyReveal;

import xyz.haoshoku.nick.api.NickAPI;

public class ProjectAnonymous extends JavaPlugin {

    @Override
    public void onEnable() {

        // Ensure NickAPI is present (skins only)
        if (Bukkit.getPluginManager().getPlugin("NickAPI") == null) {
            getLogger().severe("NickAPI not found! Disabling ProjectAnonymous.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register listeners
        Bukkit.getPluginManager().registerEvents(
                new JoinListener(this),
                this
        );

        Bukkit.getPluginManager().registerEvents(
                new DeathListener(this),
                this
        );

        Bukkit.getPluginManager().registerEvents(
                new HourlyReveal(this),
                this
        );

        // Register commands
        getCommand("reveal").setExecutor(
                new RevealCommand(this)
        );

        getLogger().info("ProjectAnonymous enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProjectAnonymous disabled.");
    }

    /**
     * Utility method (optional, not required by logic)
     */
    public boolean isNicked(org.bukkit.entity.Player player) {
        return NickAPI.isNicked(player);
    }
}
