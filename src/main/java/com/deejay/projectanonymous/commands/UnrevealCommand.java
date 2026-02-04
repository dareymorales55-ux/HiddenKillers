package com.deejay.projectanonymous.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.deejay.projectanonymous.ProjectAnonymous;
import com.deejay.projectanonymous.reveal.RevealManager;

public class UnrevealCommand implements CommandExecutor {

    private final ProjectAnonymous plugin;

    public UnrevealCommand(ProjectAnonymous plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("projectanonymous.unreveal")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unreveal <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (!RevealManager.isRevealed(target)) {
            sender.sendMessage(ChatColor.YELLOW + target.getName() + " is not revealed.");
            return true;
        }

        RevealManager.hide(target);

        sender.sendMessage(
                ChatColor.GREEN + "Unrevealed " + target.getName() + "."
        );

        return true;
    }
}
