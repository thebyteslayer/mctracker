package com.thebyteslayer.mc.tracker

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack

class CompassListener(private val compassTracker: CompassTracker) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        checkPlayerInventory(player)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        val item = event.currentItem
        if (item != null && item.type == Material.COMPASS) {
            scheduleCompassCheck(player)
        }
    }

    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val item = player.inventory.getItem(event.newSlot)

        if (item != null && item.type == Material.COMPASS) {
            scheduleCompassCheck(player)
        }
    }

    private fun scheduleCompassCheck(player: Player) {
        compassTracker.plugin.server.scheduler.runTaskLater(
            compassTracker.plugin,
            Runnable { checkPlayerInventory(player) },
            1L
        )
    }

    private fun checkPlayerInventory(player: Player) {
        for (item in player.inventory.contents) {
            if (item != null && item.type == Material.COMPASS) {
                checkCompass(player, item)
            }
        }

        val mainHand = player.inventory.itemInMainHand
        if (mainHand.type == Material.COMPASS) {
            checkCompass(player, mainHand)
        }

        val offHand = player.inventory.itemInOffHand
        if (offHand.type == Material.COMPASS) {
            checkCompass(player, offHand)
        }
    }

    private fun checkCompass(holder: Player, compass: ItemStack) {
        val meta = compass.itemMeta ?: return
        if (!meta.hasDisplayName()) {
            return
        }

        val displayName = meta.displayName ?: return
        val targetName = ChatColor.stripColor(displayName)!!

        val target = compassTracker.plugin.server.getPlayer(targetName)
        if (target != null && target.isOnline) {
            compassTracker.addCompassTarget(holder.uniqueId, targetName)

            val currentLore = meta.lore
            val trackingLore = ChatColor.translateAlternateColorCodes('&', "&7Tracking: &a${targetName}")
            if (currentLore == null || !currentLore.contains(trackingLore)) {
                meta.setLore(listOf(
                    trackingLore,
                    ChatColor.translateAlternateColorCodes('&', "&8Tracker Compass")
                ))
                compass.itemMeta = meta
            }
        }
    }
}
