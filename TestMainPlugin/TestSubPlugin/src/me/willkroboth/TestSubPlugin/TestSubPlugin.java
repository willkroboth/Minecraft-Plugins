package me.willkroboth.TestSubPlugin;

import me.willkroboth.TestMainPlugin.TestMainPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class TestSubPlugin extends JavaPlugin {
    public void onEnable() {
        getLogger().info(TestMainPlugin.getMessage());
        getLogger().info("TestSubPlugin loaded");
    }
}
