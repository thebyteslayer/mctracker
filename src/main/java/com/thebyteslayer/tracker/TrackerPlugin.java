package com.thebyteslayer.tracker;

import org.bukkit.plugin.java.JavaPlugin;

public class TrackerPlugin extends JavaPlugin {
    
    private CompassTracker compassTracker;
    
    @Override
    public void onEnable() {
        this.compassTracker = new CompassTracker(this);
        
        getServer().getPluginManager().registerEvents(new CompassListener(compassTracker), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(compassTracker), this);
        getServer().getPluginManager().registerEvents(new PortalListener(compassTracker), this);
        

        
        getLogger().info("Tracker plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (compassTracker != null) {
            compassTracker.cleanup();
        }
        getLogger().info("Tracker plugin has been disabled!");
    }
} 