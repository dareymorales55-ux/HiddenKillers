package com.deejay.projectanonymous.listeners;

import com.deejay.projectanonymous.ProjectAnonymous;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DeathListener implements Listener {

    private final ProjectAnonymous plugin;

    public DeathListener(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    /**
     * Caught logic:
     * If killer uses an item renamed to the victim's REAL name → victim is caught
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null) return;

        ItemMeta meta = weapon.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        String realName = victim.getName();

        if (!displayName.equalsIgnoreCase(realName)) return;

        // ✅ DO NOT cancel vanilla death message

        // Caught message (light red, NOT bold)
        Bukkit.broadcastMessage(
                ChatColor.RED + realName + " has been caught."
        );

        // Ban victim
        Bukkit.getBanList(BanList.Type.NAME).addBan(
                realName,
                ChatColor.DARK_RED + "Your cover was blown.",
                null,
                null
        );

        // Kick safely
        Bukkit.getScheduler().runTask(plugin, () ->
                victim.kickPlayer(ChatColor.DARK_RED + "Your cover was blown.")
        );
    }

    /**
     * Replace vanilla quit message
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(
                ChatColor.YELLOW + event.getPlayer().getName() + " left the game"
        );
    }
}
