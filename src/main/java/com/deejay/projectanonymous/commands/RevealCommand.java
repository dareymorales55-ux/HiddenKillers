package com.deejay.projectanonymous.commands;

import com.deejay.projectanonymous.ProjectAnonymous;
import com.deejay.projectanonymous.reveal.RevealManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RevealCommand implements CommandExecutor {

    private final ProjectAnonymous plugin;

    public RevealCommand(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("projectanonymous.reveal")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /reveal <player> <durationSeconds>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
            if (seconds <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Duration must be a positive number.");
            return true;
        }

        long ticks = seconds * 20L;

        RevealManager.reveal(target, plugin, ticks);

        sender.sendMessage(
                ChatColor.GREEN + "Revealed " +
                ChatColor.RED + target.getName() +
                ChatColor.GREEN + " for " + seconds + " seconds."
        );

        return true;
    }
}
