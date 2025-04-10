package me.willkroboth.delayedcommands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.CommandArgument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.wrappers.CommandResult;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class DelayedCommands extends JavaPlugin {
    @Override
    public void onLoad() {
        new CommandAPICommand("delay")
                .withPermission("delayedcommands.command")
                .withArguments(
                        new FloatArgument("time"),
                        new CommandArgument("command")
                )
                .executes((sender, args) -> {
                    float time = args.getUnchecked("time");
                    CommandResult command = args.getUnchecked("command");

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            command.execute(sender);
                        }
                    }.runTaskLater(this, (long) (time * 20));
                })
                .register();
    }
}
