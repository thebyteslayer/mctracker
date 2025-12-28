package com.thebyteslayer.mc.tracker

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(private val compassTracker: CompassTracker) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        for (onlinePlayer in compassTracker.plugin.server.onlinePlayers) {
            if (onlinePlayer != player) {
                val targetName = compassTracker.getCompassTarget(onlinePlayer.uniqueId)
                if (targetName != null && targetName.equals(player.name, ignoreCase = true)) {
                    onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a${player.name} &7has &ajoined. &7Your compass will now &atrack &7them."))
                }
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        compassTracker.removeCompassTarget(player.uniqueId)
        // Note: We DON'T clear portal locations when players quit so they persist

        for (onlinePlayer in compassTracker.plugin.server.onlinePlayers) {
            if (onlinePlayer != player) {
                val targetName = compassTracker.getCompassTarget(onlinePlayer.uniqueId)
                if (targetName != null && targetName.equals(player.name, ignoreCase = true)) {
                    onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c${player.name} &7has &cleft. &7Your compass will continue &ctracking &7their last known &cportal location."))
                }
            }
        }
    }
}
