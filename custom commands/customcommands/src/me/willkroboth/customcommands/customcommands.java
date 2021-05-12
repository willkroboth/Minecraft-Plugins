package me.willkroboth.customcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;

public class customcommands extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        Logger logger = getLogger();
        ConfigurationSection commands = getConfig().getConfigurationSection("commands");

        if(commands == null || commands.getKeys(false).size() == 0) {
            logger.info("No commands given. Skipping");
            return;
        }

        // register commands without plugin.yml
        // https://www.spigotmc.org/threads/small-easy-register-command-without-plugin-yml.38036/
        CommandMap commandMap;
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
            return;
        }

        for(String key:commands.getKeys(false)){
            logger.info("Loading command " + key);

            // vital data needed for command to work
            ConfigurationSection command = commands.getConfigurationSection(key);
            if(command == null) {
                logger.info(ChatColor.RED + key + " has no data. Skipping.");
                continue;
            }

            String usage = (String) command.get("usage");
            if(usage == null) {
                logger.info(ChatColor.RED + key + " has no usage. Skipping.");
                continue;
            }

            List<String> commandsToRun = command.getStringList("commands");
            if(commandsToRun.size() == 0) {
                logger.info(ChatColor.RED + key + " has no commands. Skipping.");
                continue;
            }

            // less important, but should warn user
            String description = command.getString("description");
            if(description == null) logger.info(ChatColor.YELLOW + key + " has no description.");

            List<String> aliases = command.getStringList("aliases");
            if(aliases.size() == 0) logger.info(ChatColor.YELLOW + key + " has no aliases.");

            String permission = command.getString("permission");
            if(permission == null) logger.info(ChatColor.YELLOW + key + " has no permission. Anyone can use the command.");

            String name = usage.split(" ", 2)[0].substring(1);
            logger.info("Loading " + key + " with name: " + name);

            commandMap.register(name, new ConfigCommandExecutor(name, description, usage, aliases, permission, commandsToRun));

        }
    }

    @Override
    public void onDisable() {

    }
}