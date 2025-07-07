package com.thebyteslayer.tracker;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PortalListener implements Listener {
    
    private final CompassTracker compassTracker;
    
    public PortalListener(CompassTracker compassTracker) {
        this.compassTracker = compassTracker;
    }
    
    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        Location fromLocation = event.getFrom();
        Location toLocation = event.getTo();
        
        // Record the portal location in the dimension they're leaving
        if (isPortalLocation(fromLocation)) {
            // Store portal location for the world they're coming FROM
            compassTracker.recordPortalEntry(player.getUniqueId(), fromLocation);
            
            // If they're going to a different world, also store the destination portal
            if (toLocation != null && !fromLocation.getWorld().equals(toLocation.getWorld())) {
                // Store the FROM location for people in the TO world to find
                compassTracker.recordPortalEntryForWorld(player.getUniqueId(), fromLocation, toLocation.getWorld());
            }
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Also catch teleports through portals
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ||
            event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            
            Player player = event.getPlayer();
            Location fromLocation = event.getFrom();
            Location toLocation = event.getTo();
            
            // Record the portal location in the dimension they're leaving
            if (isPortalLocation(fromLocation)) {
                // Store portal location for the world they're coming FROM
                compassTracker.recordPortalEntry(player.getUniqueId(), fromLocation);
                
                // If they're going to a different world, also store for cross-dimensional tracking
                if (toLocation != null && !fromLocation.getWorld().equals(toLocation.getWorld())) {
                    // Store the FROM location for people in the TO world to find
                    compassTracker.recordPortalEntryForWorld(player.getUniqueId(), fromLocation, toLocation.getWorld());
                }
            }
        }
    }
    
    private boolean isPortalLocation(Location location) {
        // Check if the location is near a portal
        Material blockType = location.getBlock().getType();
        
        if (blockType == Material.NETHER_PORTAL || blockType == Material.END_PORTAL) {
            return true;
        }
        
        // Check surrounding blocks for portals (player might be standing next to it)
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Location checkLoc = location.clone().add(x, y, z);
                    Material checkType = checkLoc.getBlock().getType();
                    
                    if (checkType == Material.NETHER_PORTAL || checkType == Material.END_PORTAL) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
} 