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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeathListener implements Listener {

    private final ProjectAnonymous plugin;
    private final Set<UUID> renameCaught = new HashSet<>();

    public DeathListener(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null || !weapon.hasItemMeta()) return;

        ItemMeta meta = weapon.getItemMeta();
        if (!meta.hasDisplayName()) return;

        String realName = plugin.getRealName(victim.getUniqueId());
        if (realName == null) return;

        String weaponName = ChatColor.stripColor(meta.getDisplayName());

        // RENAME-CATCH CHECK
        if (!weaponName.equalsIgnoreCase(realName)) return;

        // Mark so we can override quit message
        renameCaught.add(victim.getUniqueId());

        // Broadcast caught message
        Bukkit.broadcastMessage(ChatColor.RED + realName + " has been caught.");

        // Ban player
        Bukkit.getBanList(BanList.Type.NAME).addBan(
                victim.getName(),
                ChatColor.DARK_RED + "Your cover was blown.",
                null,
                null
        );

        victim.kickPlayer(ChatColor.DARK_RED + "Your cover was blown.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!renameCaught.remove(player.getUniqueId())) return;

        // Suppress vanilla quit message
        event.setQuitMessage(
                ChatColor.YELLOW + plugin.getRealName(player.getUniqueId()) + " left the game"
        );
    }
}
