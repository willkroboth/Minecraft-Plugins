package me.willkroboth.customcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ConfigCommandExecutor extends BukkitCommand {
    String[] usage_keys;
    HashMap<String, String> usage_variables = new HashMap<>();

    List<String[]> commands;

    protected ConfigCommandExecutor(String name, String description, String usageMessage, List<String> aliases,
                                    String permission, List<String> commands) {
        super(name, description, usageMessage, aliases);
        this.setPermission(permission);
        parse_usage(usageMessage);
        parse_commands(commands);
    }

    private void parse_usage(String usage){
        String[] split_usage = usage.split(" ");
        usage_keys = Arrays.copyOfRange(split_usage, 1, split_usage.length);
        Bukkit.getLogger().info("usage: " + usage);
        Bukkit.getLogger().info("usage_keys: " + Arrays.toString(usage_keys));

        for (String key: usage_keys) {
            usage_variables.put(key, key);
        }
    }

    private void parse_commands(List<String> commands){
        ArrayList<String[]> parsed_commands = new ArrayList<>();
        StringBuilder debug_string = new StringBuilder();
        for(String command:commands){
            ArrayList<String> command_sections = new ArrayList<>();

            int previous_index = 0;
            intPair pair = get_first_index_and_length(command, usage_keys);
            int index = pair.first;
            int length = pair.last;

            while (index != -1){
                command_sections.add(command.substring(previous_index, index));
                command_sections.add(command.substring(index, index + length));

                previous_index = index + length;
                pair = get_first_index_and_length(command, usage_keys, previous_index);
                index = pair.first;
                length = pair.last;
            }
            if(previous_index != command.length()){
                command_sections.add(command.substring(previous_index));
            }
            parsed_commands.add(command_sections.toArray(new String[0]));
            debug_string.append(command_sections.toString());
        }
        this.commands = parsed_commands;
        Bukkit.getLogger().info("Parsed commands: " + debug_string.toString());
    }

    private intPair get_first_index_and_length(String string, String[] keys) {
        return get_first_index_and_length(string, keys, 0);
    }

    private intPair get_first_index_and_length(String string, String[] keys, int from_index){
        int least = string.length();
        int length = -1;

        for(String key:keys){
            int index = string.indexOf(key, from_index);
            if(index != -1 && index < least){
                least = index;
                length = key.length();
            }
        }

        if(least == string.length()) return new intPair(-1, -1);
        return new intPair(least, length);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(!this.testPermission(sender)) return false;

        if(args.length != usage_keys.length){
            sender.sendMessage(ChatColor.RED + "Wrong number of arguments");
            return false;
        }

        // setup variable hashmap
        for(int i = 0; i < usage_keys.length; i++){
            usage_variables.replace(usage_keys[i], args[i]);
        }

        //run commands
        Server server = Bukkit.getServer();
        CommandSender opSender = new OpSender(sender);
        for(String[] rawCommand:this.commands){
            StringBuilder command = new StringBuilder();
            boolean variable_section = false;
            for(String piece:rawCommand){
                if (variable_section){
                    command.append(usage_variables.get(piece));
                } else {
                    command.append(piece);
                }
                variable_section = !variable_section;
            }
            server.dispatchCommand(opSender, command.toString());
        }

        return true;
    }
}

class intPair{
    int first;
    int last;
    public intPair(int first, int last){
        this.first = first;
        this.last = last;
    }
}

class OpSender implements CommandSender {
    // sends any feedback to the sender passed in
    // makes them operator so they can run any command
    CommandSender sender;
    public OpSender(CommandSender sender){
        this.sender = sender;
    }

    @Override
    public void sendMessage(String s) {
        sender.sendMessage(s);
    }

    @Override
    public void sendMessage(String[] strings) {
        sender.sendMessage(strings);
    }

    @Override
    public void sendMessage(UUID uuid, String s) {
        sender.sendMessage(uuid, s);
    }

    @Override
    public void sendMessage(UUID uuid, String[] strings) {
        sender.sendMessage(uuid, strings);
    }

    @Override
    public Server getServer() {
        return sender.getServer();
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public Spigot spigot() {
        return sender.spigot();
    }

    @Override
    public boolean isPermissionSet(String s) {
        return sender.isPermissionSet(s);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return sender.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String s) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        return sender.addAttachment(plugin, s, b);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return sender.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        return sender.addAttachment(plugin, s, b, i);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return sender.addAttachment(plugin, i);
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) {
        sender.removeAttachment(permissionAttachment);
    }

    @Override
    public void recalculatePermissions() {
        sender.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return sender.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean b) {

    }
}
