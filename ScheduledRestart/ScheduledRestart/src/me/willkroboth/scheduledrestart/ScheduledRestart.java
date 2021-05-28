package me.willkroboth.scheduledrestart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ScheduledRestart extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Logger logger = getLogger();

        List<Map<?, ?>> restarts = getConfig().getMapList("restarts");
        if (restarts.size() == 0){
            logger.info(ChatColor.RED + "No restarts given. Disabling Plugin!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        logger.info("Now: " + now.toString());

        boolean hard_reset = false;
        LocalDateTime nextHardResetTime = now.plusDays(2);
        Restart nextHardReset = null;

        boolean soft_reset = false;
        LocalDateTime nextSoftResetTime = now.plusDays(2);
        Restart nextSoftReset = null;

        for(Map<?, ?> data: restarts){
            String rawTime = (String) data.get("time");
            if(rawTime == null){
                logger.info(ChatColor.YELLOW + "No time given: " + data.toString() + ". Skipping.");
                continue;
            }

            String[] parts = rawTime.split(":");
            if(parts.length != 2){
                logger.info(ChatColor.YELLOW + "Invalid time format: " + data.toString() + " (Should be HH:mm). Skipping.");
                continue;
            }
            int hour;
            int minute;
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e){
                logger.info(ChatColor.YELLOW + "Invalid time format: " + data.toString() + " (Should be HH:mm). Skipping.");
                continue;
            }
            LocalDateTime formattedTime = now.withHour(hour).withMinute(minute).withSecond(0);

            if(formattedTime.isBefore(now)){
                formattedTime = formattedTime.plusDays(1);
            }

            boolean waitForPlayers = Boolean.parseBoolean((String) data.get("waitForPlayers"));
            boolean warnPlayers = Boolean.parseBoolean((String) data.get("warnPlayers"));
            int[] intervals = new int[0];
            if(warnPlayers && !waitForPlayers){
                String rawIntervals = (String) data.get("warnIntervals");
                ArrayList<Integer> intervals_build = new ArrayList<>();

                if(rawIntervals != null){
                    String[] rawIntervalsSplit = rawIntervals.split(",");
                    for(String i:rawIntervalsSplit){
                        i = i.replace(" ", "");
                        try{
                            intervals_build.add(Integer.parseInt(i));
                        } catch (NumberFormatException e){
                            logger.info(ChatColor.RED +
                                    "An invalid number showed up in the warnInterval parameter: \"" +
                                    Arrays.toString(rawIntervalsSplit) + "\" (" + i + "). No intervals will be used.");
                            break;
                        }
                    }
                    if(intervals_build.size() == rawIntervalsSplit.length){
                        intervals = intervals_build.stream().mapToInt(i -> i).toArray();
                    }
                }
            }

            logger.info("Found " + (waitForPlayers ? "soft":"hard") + " reset at time: " + formattedTime.toString());
            if(waitForPlayers){
                if(formattedTime.isBefore(nextHardResetTime) && formattedTime.isBefore(nextSoftResetTime)){
                    nextSoftResetTime = formattedTime;
                    nextSoftReset = new Restart(true, warnPlayers, intervals);
                    soft_reset = true;
                }
            } else {
                if(formattedTime.isBefore(nextHardResetTime)){
                    nextHardResetTime = formattedTime;
                    nextHardReset = new Restart(false, warnPlayers, intervals);
                    hard_reset = true;

                    if(formattedTime.isBefore(nextSoftResetTime)){
                        soft_reset = false;
                    }
                }
            }
        }

        if(!(soft_reset || hard_reset)){
            logger.info(ChatColor.RED + "No valid restarts found. Disabling plugin!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        if (soft_reset){
            logger.info("Soft reset will occur at " + nextSoftResetTime.toString());
            long delay = now.until(nextSoftResetTime, ChronoUnit.SECONDS);
            scheduler.schedule(nextSoftReset, delay, SECONDS);
            nextSoftReset.scheduleIntervals(scheduler, delay);
        }
        if (hard_reset){
            logger.info("Hard reset will occur at " + nextHardResetTime.toString());
            long delay = now.until(nextHardResetTime, ChronoUnit.SECONDS);
            scheduler.schedule(nextHardReset, delay, SECONDS);
            nextHardReset.scheduleIntervals(scheduler, delay);
        }
    }

    @Override
    public void onDisable() {

    }
}

class Restart extends TimerTask implements Listener {
    private final boolean waitForPlayers;
    private final boolean warnPlayers;
    private final int[] intervals;

    public Restart(boolean waitForPlayers, boolean warnPlayers, int[] intervals){
        this.waitForPlayers = waitForPlayers;
        this.warnPlayers = warnPlayers;
        this.intervals = intervals;
    }

    public void scheduleIntervals(ScheduledExecutorService scheduler, long secondsUntilReset){
        if(!warnPlayers || waitForPlayers) return;
        Bukkit.getLogger().info("Warn intervals: " + Arrays.toString(intervals));
        for(int interval:intervals){
            long newDelay = secondsUntilReset - interval * 60L;
            if(newDelay <= 0) continue;
            scheduler.schedule(() -> {
                if(interval == 1) Bukkit.broadcastMessage("Server will automatically restart in 1 minute");
                else Bukkit.broadcastMessage("Server will automatically restart in " + interval + " minutes");
            }, newDelay, SECONDS);
        }
    }

    @Override
    public void run() {
        if (waitForPlayers && Bukkit.getOnlinePlayers().size() != 0) {
            PluginManager pluginManager = Bukkit.getServer().getPluginManager();
            Plugin plugin = pluginManager.getPlugin("ScheduledRestart");
            assert plugin != null:"This plugin was not found";
            pluginManager.registerEvents(this, plugin);

            if(warnPlayers){
                Bukkit.broadcastMessage("Server will automatically restart once all players leave");
            }
            Bukkit.getLogger().info("ScheduledRestart will restart the server once all players have left");
        } else {
            if(warnPlayers){
                Bukkit.broadcastMessage("Server is automatically restarting");
            }
            Bukkit.getLogger().info("ScheduledRestart is restarting the server");
            restart();
        }
    }

    @EventHandler
    public void OnPlayerLeave(PlayerQuitEvent ignored){
        if(Bukkit.getOnlinePlayers().size() <= 1){
            Bukkit.getLogger().info("All players have left so ScheduledRestart is restarting the sever.");
            restart();
        }
    }

    private void restart(){
        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        Plugin plugin = pluginManager.getPlugin("ScheduledRestart");
        assert plugin != null:"This plugin was not found";

        new BukkitRunnable() {
            @Override
            public void run() {
                Server s = Bukkit.getServer();
                s.dispatchCommand(s.getConsoleSender(), "restart");
            }
        }.runTaskLater(plugin, 20);
    }
}
