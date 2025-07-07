package com.thebyteslayer.tracker;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompassListener implements Listener {
    
    private final CompassTracker compassTracker;
    
    public CompassListener(CompassTracker compassTracker) {
        this.compassTracker = compassTracker;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkPlayerInventory(player);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        ItemStack item = event.getCurrentItem();
        if (item != null && item.getType() == Material.COMPASS) {
            Player player = (Player) event.getWhoClicked();
            scheduleCompassCheck(player);
        }
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        
        if (item != null && item.getType() == Material.COMPASS) {
            scheduleCompassCheck(player);
        }
    }
    
    private void scheduleCompassCheck(Player player) {
        compassTracker.plugin.getServer().getScheduler().runTaskLater(
            compassTracker.plugin, 
            () -> checkPlayerInventory(player), 
            1L
        );
    }
    
    private void checkPlayerInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS) {
                checkCompass(player, item);
            }
        }
        
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.COMPASS) {
            checkCompass(player, mainHand);
        }
        
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.COMPASS) {
            checkCompass(player, offHand);
        }
    }
    
    private void checkCompass(Player holder, ItemStack compass) {
        if (!compass.hasItemMeta() || !compass.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String displayName = compass.getItemMeta().getDisplayName();
        String targetName = ChatColor.stripColor(displayName);
        
        Player target = compassTracker.plugin.getServer().getPlayer(targetName);
        if (target != null && target.isOnline()) {
            compassTracker.addCompassTarget(holder.getUniqueId(), targetName);
            
            ItemMeta meta = compass.getItemMeta();
            if (meta.getLore() == null || !meta.getLore().contains(ChatColor.GRAY + "Tracking: " + targetName)) {
                meta.setLore(java.util.Arrays.asList(
                    ChatColor.GRAY + "Tracking: " + targetName,
                    ChatColor.DARK_GRAY + "Tracker Compass"
                ));
                compass.setItemMeta(meta);
            }
        }
    }
} 