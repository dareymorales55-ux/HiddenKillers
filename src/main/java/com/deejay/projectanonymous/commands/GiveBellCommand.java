package com.deejay.projectanonymous.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.deejay.projectanonymous.ProjectAnonymous;
import com.deejay.projectanonymous.items.BellOfTruth;

public class GiveBellCommand implements CommandExecutor {

    private final ProjectAnonymous plugin;

    public GiveBellCommand(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /givebell <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        BellOfTruth bell = new BellOfTruth(plugin);
        target.getInventory().addItem(bell.createBell());

        sender.sendMessage(ChatColor.GREEN + "Gave Bell of Truth to " + target.getName());
        target.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "You received the Bell of Truth");

        return true;
    }
}
