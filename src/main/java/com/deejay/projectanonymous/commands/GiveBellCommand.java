package com.deejay.projectanonymous.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.deejay.projectanonymous.ProjectAnonymous;
import com.deejay.projectanonymous.items.BellOfTruth;

public class GiveBellCommand implements CommandExecutor {

    private final ProjectAnonymous plugin;
    private final BellOfTruth bell;

    public GiveBellCommand(ProjectAnonymous plugin) {
        this.plugin = plugin;
        this.bell = new BellOfTruth(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("projectanonymous.givebell")) {
            player.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        player.getInventory().addItem(bell.createBell());
        player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "You have been given the Bell of Truth.");

        return true;
    }
}
