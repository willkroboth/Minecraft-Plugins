package me.willkroboth.customteleports;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class customteleports extends JavaPlugin {
    @Override
    public void onEnable() {
        //Fired when the server enables the plugin
        this.getCommand("puttoground").setExecutor(new CommandPutToGround());
        this.getCommand("spreadsphere").setExecutor(new CommandSpreadSphere());
        this.getCommand("addvector").setExecutor(new CommandAddVector());
    }
    @Override
    public void onDisable(){
        //Fired when the server stops and disables all plugins
    }
}