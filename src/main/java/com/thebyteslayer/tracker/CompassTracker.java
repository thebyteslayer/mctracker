package com.thebyteslayer.tracker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CompassTracker {
    
    public final TrackerPlugin plugin;
    private final Map<UUID, String> compassTargets;
    private final Map<UUID, Map<String, Location>> playerPortalLocations; // Track where players entered portals by world
    private BukkitTask updateTask;
    
    public CompassTracker(TrackerPlugin plugin) {
        this.plugin = plugin;
        this.compassTargets = new HashMap<>();
        this.playerPortalLocations = new HashMap<>();
        startUpdateTask();
    }
    
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllCompasses();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second
    }
    
    private void updateAllCompasses() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerCompasses(player);
        }
    }
    
    private void updatePlayerCompasses(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS) {
                updateCompass(player, item);
            }
        }
    }
    
    private void updateCompass(Player holder, ItemStack compass) {
        if (!compass.hasItemMeta() || !compass.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String displayName = compass.getItemMeta().getDisplayName();
        String targetName = ChatColor.stripColor(displayName);
        Player target = Bukkit.getPlayer(targetName);
        
        if (target != null && target.equals(holder)) {
            return; // Don't track yourself
        }
        
        Location targetLocation = null;
        
        if (target != null && target.isOnline()) {
            // Target is online
            if (!target.getWorld().equals(holder.getWorld())) {
                // Target is in different dimension - point to the portal they used
                targetLocation = getPortalLocationForPlayer(target.getUniqueId(), holder.getWorld());
            } else {
                // Target is in same dimension - point directly to them
                targetLocation = target.getLocation();
            }
        } else {
            // Target is offline - try to point to their last known portal location
            targetLocation = getPortalLocationForPlayer(getOfflinePlayerUUID(targetName), holder.getWorld());
        }
        
        // Always check if we need to clear lodestone data when in Overworld
        if (holder.getWorld().getEnvironment() == World.Environment.NORMAL) {
            CompassMeta meta = (CompassMeta) compass.getItemMeta();
            if (meta.hasLodestone() || meta.isLodestoneTracked()) {
                // Create a completely fresh compass meta to clear all lodestone data
                ItemStack freshCompass = new ItemStack(Material.COMPASS);
                CompassMeta freshMeta = (CompassMeta) freshCompass.getItemMeta();
                
                // Copy over the display name and lore from the original
                if (meta.hasDisplayName()) {
                    freshMeta.setDisplayName(meta.getDisplayName());
                }
                if (meta.hasLore()) {
                    freshMeta.setLore(meta.getLore());
                }
                
                // Apply the fresh meta to completely reset compass behavior
                compass.setItemMeta(freshMeta);
            }
        }
        
        if (targetLocation != null) {
            // Check if we're in the Nether or End - compasses don't work normally there
            if (holder.getWorld().getEnvironment() == World.Environment.NETHER || 
                holder.getWorld().getEnvironment() == World.Environment.THE_END) {
                
                // Use lodestone compass mechanics for Nether/End
                CompassMeta meta = (CompassMeta) compass.getItemMeta();
                meta.setLodestone(targetLocation);
                meta.setLodestoneTracked(false); // Don't require actual lodestone block
                compass.setItemMeta(meta);
            } else {
                // Use traditional setCompassTarget for Overworld (lodestone already cleared above)
                holder.setCompassTarget(targetLocation);
            }
            
            compassTargets.put(holder.getUniqueId(), targetName);
        }
    }
    
    private Location getPortalLocationForPlayer(UUID playerUUID, World trackerWorld) {
        // Get the stored portal locations for this player
        Map<String, Location> playerPortals = playerPortalLocations.get(playerUUID);
        
        if (playerPortals != null) {
            // Look for a portal location in the tracker's world
            Location portalLocation = playerPortals.get(trackerWorld.getName());
            if (portalLocation != null) {
                return portalLocation;
            }
        }
        
        // Fallback to default locations if no portal recorded
        return getDefaultPortalLocation(trackerWorld);
    }
    
    private Location getDefaultPortalLocation(World world) {
        if (world.getEnvironment() == World.Environment.NORMAL) {
            // In overworld, try to find stronghold for end portal or use spawn for nether portal
            try {
                org.bukkit.util.StructureSearchResult result = world.locateNearestStructure(
                    world.getSpawnLocation(), 
                    org.bukkit.generator.structure.StructureType.STRONGHOLD, 
                    1000,
                    false
                );
                if (result != null) {
                    return result.getLocation();
                }
            } catch (Exception e) {
                // Fallback if structure search fails
            }
            
            // Default to spawn area where portals are commonly built
            return new Location(world, 0, 64, 0);
        }
        
        return world.getSpawnLocation();
    }
    
    public void recordPortalEntry(UUID playerUUID, Location portalLocation) {
        // Store where the player entered a portal for their current world
        Map<String, Location> playerPortals = playerPortalLocations.computeIfAbsent(playerUUID, k -> new HashMap<>());
        playerPortals.put(portalLocation.getWorld().getName(), portalLocation.clone());
    }
    
    public void recordPortalEntryForWorld(UUID playerUUID, Location portalLocation, World targetWorld) {
        // Store portal location for a specific target world (for cross-dimensional tracking)
        Map<String, Location> playerPortals = playerPortalLocations.computeIfAbsent(playerUUID, k -> new HashMap<>());
        playerPortals.put(targetWorld.getName(), portalLocation.clone());
    }
    
    public void clearPortalLocation(UUID playerUUID) {
        playerPortalLocations.remove(playerUUID);
    }
    
    public void clearPortalLocationForWorld(UUID playerUUID, String worldName) {
        Map<String, Location> playerPortals = playerPortalLocations.get(playerUUID);
        if (playerPortals != null) {
            playerPortals.remove(worldName);
            if (playerPortals.isEmpty()) {
                playerPortalLocations.remove(playerUUID);
            }
        }
    }
    
    private UUID getOfflinePlayerUUID(String playerName) {
        // Try to get UUID from online player first
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }
        
        // Try to get from offline player
        try {
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer.hasPlayedBefore()) {
                return offlinePlayer.getUniqueId();
            }
        } catch (Exception e) {
            // Fallback if offline player lookup fails
        }
        
        return null;
    }
    

    
    public void addCompassTarget(UUID compassHolder, String targetName) {
        compassTargets.put(compassHolder, targetName);
    }
    
    public void removeCompassTarget(UUID compassHolder) {
        compassTargets.remove(compassHolder);
    }
    
    public String getCompassTarget(UUID compassHolder) {
        return compassTargets.get(compassHolder);
    }
    
    public void cleanup() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        compassTargets.clear();
        playerPortalLocations.clear();
    }
} 