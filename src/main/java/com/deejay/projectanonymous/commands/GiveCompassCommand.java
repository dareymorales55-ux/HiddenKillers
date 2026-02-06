package com.deejay.projectanonymous.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GiveCompassCommand implements CommandExecutor {

    private static final String COMPASS_NAME =
            ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Detective's Compass";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        meta.setDisplayName(COMPASS_NAME);
        meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        compass.setItemMeta(meta);
        player.getInventory().addItem(compass);

        player.sendMessage(ChatColor.GREEN + "You received a Detective's Compass.");
        return true;
    }
}
