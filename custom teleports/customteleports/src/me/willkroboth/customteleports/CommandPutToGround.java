package me.willkroboth.customteleports;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.List;

public class CommandPutToGround implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equals("puttoground")) return true;
        //usage: /puttoground <entities>
        if(args.length != 1){
            sender.sendMessage(ChatColor.RED + "Wrong number of arguments given");
            return false;
        }
        List<Entity> targets;
        try{
            targets = Bukkit.selectEntities(sender, args[0]);
        } catch (IllegalArgumentException e){
            sender.sendMessage(ChatColor.RED + "Invalid target selector: " + args[0]);
            return false;
        }

        if(targets.size() == 0) {
            sender.sendMessage(ChatColor.RED + "No entities found: " + args[0]);
            return false;
        }

        for(Entity e:targets) {
            Location newLocation = e.getLocation();
            newLocation.setY(e.getWorld().getMaxHeight());
            while (newLocation.getBlock().isPassable()) {
                newLocation.setY(newLocation.getY() - 1);
            }
            newLocation.setY(newLocation.getY() + 1);
            e.teleport(newLocation);
        }
        sender.sendMessage("Put " + targets.size() + " target(s) to the ground");

        return true;
    }
}
