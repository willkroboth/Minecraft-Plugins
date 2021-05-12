package me.willkroboth.customteleports;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.Collections;
import java.util.List;

public class CommandSpreadSphere implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equals("spreadsphere")) return true;

        //usage: /spreadsphere <entities> <x> <y> <z> <world> <value/from> <radius>/(<entity> <scoreboard>)
        if (args.length < 6 || (args[5].equals("value") && args.length != 7) || (args[5].equals("from") && args.length != 8)) {
            sender.sendMessage(ChatColor.RED + "Wrong number of arguments");
            return false;
        }

        // target entities
        List<Entity> targets;
        try {
            targets = Bukkit.selectEntities(sender, args[0]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid target selector: " + args[0]);
            return false;
        }
        if (targets.size() == 0) {
            sender.sendMessage(ChatColor.RED + "No entities found: " + args[0]);
            return false;
        }
        // x
        double x;
        try {
            x = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Expected double for argument x: " + args[1]);
            return false;
        }
        // y
        double y;
        try {
            y = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Expected double for argument y: " + args[2]);
            return false;
        }
        // z
        double z;
        try {
            z = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Expected double for argument z: " + args[3]);
            return false;
        }
        // world
        World world = Bukkit.getWorld(args[4]);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "No world found: " + args[4]);
            return false;
        }
        // radius
        int radius = 1;
        if (args[5].equals("value")) {
            try {
                radius = Integer.parseInt(args[6]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Expected int for argument radius: " + args[6]);
                return false;
            }
        } else if (args[5].equals("from")) {
            List<Entity> entities = Collections.emptyList();
            try {
                entities = Bukkit.selectEntities(sender, args[6]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid target selector: " + args[6]);
            }
            if (entities.size() == 0) {
                sender.sendMessage(ChatColor.RED + "No entity found: " + args[6]);
                return false;
            } else if (entities.size() == 1) {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                if (manager == null) {
                    return false;
                }
                Objective objective = manager.getMainScoreboard().getObjective(args[7]);
                if (objective == null) {
                    sender.sendMessage(ChatColor.RED + "No scoreboard found with name: " + args[7]);
                    return false;
                }
                Score score = objective.getScore(entities.get(0).getName());
                radius = score.getScore();

            } else {
                sender.sendMessage(ChatColor.RED + "Selector for scoreboard entity should give one entity: " + args[6]);
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Expected value or from: " + args[5]);
            return false;
        }

        int i = 0;
        int n = targets.size();
        double goldenRatio = (1 + Math.pow(5,0.5))/2;

        for(Entity e:targets){
            double theta = 2 * Math.PI * i / goldenRatio;
            double phi = Math.acos(1 - 2*(i+0.5)/n);

            double xo = Math.cos(theta) * Math.sin(phi) * radius;
            double yo = Math.sin(theta) * Math.sin(phi) * radius;
            double zo = Math.cos(phi) * radius;

            Location newLocation = new Location(world, x + xo, y + yo, z + zo);
            e.teleport(newLocation);

            i ++;
        }
        sender.sendMessage("Spread " + targets.size() +
                " target(s) around (" + x + "," + y + "," + z + ") in " + world.getName() +
                "with radius " + radius);

        return true;
    }
}
