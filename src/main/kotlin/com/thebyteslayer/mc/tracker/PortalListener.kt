package com.thebyteslayer.mc.tracker

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent

class PortalListener(private val compassTracker: CompassTracker) : Listener {

    @EventHandler
    fun onPlayerPortal(event: PlayerPortalEvent) {
        val player = event.player
        val fromLocation = event.from
        val toLocation = event.to

        if (isPortalLocation(fromLocation)) {
            compassTracker.recordPortalEntry(player.uniqueId, fromLocation)

            if (toLocation != null && fromLocation.world != toLocation.world) {
                val targetWorld = toLocation.world ?: return
                compassTracker.recordPortalEntryForWorld(player.uniqueId, fromLocation, targetWorld)
            }
        }
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ||
            event.cause == PlayerTeleportEvent.TeleportCause.END_PORTAL) {

            val player = event.player
            val fromLocation = event.from
            val toLocation = event.to

            if (isPortalLocation(fromLocation)) {
                compassTracker.recordPortalEntry(player.uniqueId, fromLocation)

                if (toLocation != null && fromLocation.world != toLocation.world) {
                    val targetWorld = toLocation.world ?: return
                    compassTracker.recordPortalEntryForWorld(player.uniqueId, fromLocation, targetWorld)
                }
            }
        }
    }

    private fun isPortalLocation(location: Location): Boolean {
        val blockType = location.block.type

        if (blockType == Material.NETHER_PORTAL || blockType == Material.END_PORTAL) {
            return true
        }

        for (x in -2..2) {
            for (y in -1..2) {
                for (z in -2..2) {
                    val checkLoc = location.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                    val checkType = checkLoc.block.type

                    if (checkType == Material.NETHER_PORTAL || checkType == Material.END_PORTAL) {
                        return true
                    }
                }
            }
        }

        return false
    }
}
