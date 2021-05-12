package me.willkroboth.customteleports;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.List;

public class CommandAddVector implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equals("addvector")) return true;
        // usage: /addvector <entities> <value/from> (<x> <y> <z>)/(<entity>)
        if(args.length < 2 || (args[1].equals("value") && args.length != 5) || (args[1].equals("from") && args.length != 3)){
            sender.sendMessage(ChatColor.RED + "Wrong number of arguments");
            return false;
        }

        // target entities
        List<Entity> targets;
        try {
            targets = Bukkit.selectEntities(sender, args[0]);
        } catch (IllegalArgumentException e){
            sender.sendMessage(ChatColor.RED + "Invalid target selector: " + args[0]);
            return false;
        }
        if(targets.size() == 0){
            sender.sendMessage(ChatColor.RED + "No entities found: " + args[0]);
            return false;
        }

        // vector
        Vector vector = new Vector();
        if(args[1].equals("value")){
            //x
            try {
                vector.setX(Double.parseDouble(args[2]));
            } catch (NumberFormatException e){
                sender.sendMessage(ChatColor.RED + "Expected double for argument x: " + args[2]);
                return false;
            }
            //y
            try {
                vector.setY(Double.parseDouble(args[3]));
            } catch (NumberFormatException e){
                sender.sendMessage(ChatColor.RED + "Expected double for argument x: " + args[3]);
                return false;
            }
            //z
            try {
                vector.setZ(Double.parseDouble(args[4]));
            } catch (NumberFormatException e){
                sender.sendMessage(ChatColor.RED + "Expected double for argument x: " + args[4]);
                return false;
            }
        }
        else if(args[1].equals("from")){
            // get vector entity
            List<Entity> vectorEntity;
            try {
                vectorEntity = Bukkit.selectEntities(sender, args[2]);
            } catch (IllegalArgumentException e){
                sender.sendMessage(ChatColor.RED + "Invalid target selector: " + args[2]);
                return false;
            }

            if(vectorEntity.size() == 0){
                sender.sendMessage(ChatColor.RED + "No entities found: " + args[2]);
                return false;
            } else if(vectorEntity.size() == 1) {
                vector = vectorEntity.get(0).getLocation().toVector();
            } else {
                sender.sendMessage(ChatColor.RED + "Selector for vector entity should only give 1 entity: " + args[2]);
                return false;
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "Expected value or from: " + args[5]);
            return false;
        }

        for (Entity e:targets){
            Location old = e.getLocation();
            e.teleport(old.add(vector));
        }
        sender.sendMessage("Added " + vector + " to " + targets.size() + " target(s) position");

        return true;
    }
}
