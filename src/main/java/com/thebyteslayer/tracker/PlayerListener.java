package com.thebyteslayer.tracker;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    
    private final CompassTracker compassTracker;
    
    public PlayerListener(CompassTracker compassTracker) {
        this.compassTracker = compassTracker;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        for (Player onlinePlayer : compassTracker.plugin.getServer().getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                String targetName = compassTracker.getCompassTarget(onlinePlayer.getUniqueId());
                if (targetName != null && targetName.equalsIgnoreCase(player.getName())) {
                    onlinePlayer.sendMessage(ChatColor.GREEN + player.getName() + " has joined! Your compass will now track them.");
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        compassTracker.removeCompassTarget(player.getUniqueId());
        // Note: We DON'T clear portal locations when players quit so they persist
        
        for (Player onlinePlayer : compassTracker.plugin.getServer().getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                String targetName = compassTracker.getCompassTarget(onlinePlayer.getUniqueId());
                if (targetName != null && targetName.equalsIgnoreCase(player.getName())) {
                    onlinePlayer.sendMessage(ChatColor.RED + player.getName() + " has left! Your compass will continue tracking their last known portal location.");
                }
            }
        }
    }
} 