package com.deejay.projectanonymous.commands;

import org.bukkit.Bukkit;
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

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /givebell <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        target.getInventory().addItem(bell.createBell());
        sender.sendMessage("§aGave Bell of Truth to §e" + target.getName());
        return true;
    }
}
