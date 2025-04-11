package me.willkroboth.modifyvelocity;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin {

  @Override
  public void onEnable() {
    new CommandAPICommand("modifyvelocity")
            .withPermission("modifyvelocity.command")
            .withArguments(
                    new MultiLiteralArgument("modifyMode", "add", "multiply", "set"),
                    new MultiLiteralArgument("positionMode", "relative", "absolute"),
                    new LocationArgument("velocity")
            )
            .executesNative((sender, args) -> {
              CommandSender target = sender.getCallee();
              if (!(target instanceof Entity entity))
                throw CommandAPI.failWithString("This command must be run for an entity");

              String modifyMode = args.getUnchecked("modifyMode");
              String positionMode = args.getUnchecked("positionMode");
              Location location = args.getUnchecked("velocity");

              assert modifyMode != null;
              assert positionMode != null;
              assert location != null;

              if (positionMode.equalsIgnoreCase("relative")) location.subtract(sender.getLocation());

              Vector change = location.toVector();
              Vector velocity = entity.getVelocity();

              getLogger().info("Old velocity: " + velocity);
              getLogger().info("Change: " + change);

              switch (modifyMode) {
                case "add" -> velocity.add(change);
                case "multiply" -> velocity.multiply(change);
                case "set" -> velocity = change;
              }

              getLogger().info("New velocity: " + velocity);

              entity.setVelocity(velocity);
            })
            .register();
  }
}
