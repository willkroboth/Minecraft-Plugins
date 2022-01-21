package me.willkroboth.TestMainPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class TestMainPlugin extends JavaPlugin {
    public static String getMessage(){
        return "You have successfully accessed this plugin jar!";
    }

    public void onEnable() {
        getLogger().info("TestMainPlugin loaded");
    }
}
