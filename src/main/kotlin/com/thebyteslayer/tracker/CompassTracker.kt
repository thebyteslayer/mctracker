package com.thebyteslayer.tracker

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

class CompassTracker(val plugin: TrackerPlugin) {

    private val compassTargets: MutableMap<UUID, String> = HashMap()
    private val playerPortalLocations: MutableMap<UUID, MutableMap<String, Location>> = HashMap()
    private var updateTask: BukkitTask? = null

    init {
        startUpdateTask()
    }

    private fun startUpdateTask() {
        updateTask = object : BukkitRunnable() {
            override fun run() {
                updateAllCompasses()
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    private fun updateAllCompasses() {
        for (player in Bukkit.getOnlinePlayers()) {
            updatePlayerCompasses(player)
        }
    }

    private fun updatePlayerCompasses(player: Player) {
        for (item in player.inventory.contents) {
            if (item != null && item.type == Material.COMPASS) {
                updateCompass(player, item)
            }
        }
    }

    private fun updateCompass(holder: Player, compass: ItemStack) {
        val meta = compass.itemMeta ?: return
        if (!meta.hasDisplayName()) {
            return
        }

        val displayName = meta.displayName ?: return
        val targetName = ChatColor.stripColor(displayName)!!
        val target = Bukkit.getPlayer(targetName)

        if (target != null && target == holder) {
            return
        }

        var targetLocation: Location? = null

        if (target != null && target.isOnline) {
            if (target.world != holder.world) {
                targetLocation = getPortalLocationForPlayer(target.uniqueId, holder.world)
            } else {
                targetLocation = target.location
            }
        } else {
            targetLocation = getPortalLocationForPlayer(getOfflinePlayerUUID(targetName), holder.world)
        }

        if (targetLocation != null) {
            // In 1.21+, compasses always use lodestones
            val meta = compass.itemMeta as CompassMeta

            // Clear any existing lodestone tracking first
            if (meta.hasLodestone()) {
                // Create a new compass without lodestone tracking
                val freshCompass = ItemStack(Material.COMPASS)
                val freshMeta = freshCompass.itemMeta

                if (meta.hasDisplayName()) {
                    freshMeta?.setDisplayName(meta.displayName)
                }
                if (meta.hasLore()) {
                    freshMeta?.setLore(meta.lore)
                }

                compass.itemMeta = freshMeta
            }

            // Set the lodestone to the target location
            meta.setLodestone(targetLocation)
            // Ensure lodestone tracking is disabled so it points to the lodestone location
            meta.isLodestoneTracked = false
            compass.itemMeta = meta

            compassTargets[holder.uniqueId] = targetName!!
        }
    }

    private fun getPortalLocationForPlayer(playerUUID: UUID?, trackerWorld: World): Location? {
        if (playerUUID == null) return getDefaultPortalLocation(trackerWorld)

        val playerPortals = playerPortalLocations[playerUUID]
        val portalLocation = playerPortals?.get(trackerWorld.name)

        return portalLocation ?: getDefaultPortalLocation(trackerWorld)
    }

    private fun getDefaultPortalLocation(world: World): Location {
        if (world.environment == World.Environment.NORMAL) {
            // For now, just return spawn location as default
            // Structure location finding API has changed in newer versions
            return Location(world, 0.0, 64.0, 0.0)
        }

        return world.spawnLocation
    }

    fun recordPortalEntry(playerUUID: UUID, portalLocation: Location) {
        val world = portalLocation.world ?: return
        val playerPortals = playerPortalLocations.computeIfAbsent(playerUUID) { HashMap() }
        playerPortals[world.name] = portalLocation.clone()
    }

    fun recordPortalEntryForWorld(playerUUID: UUID, portalLocation: Location, targetWorld: World) {
        val playerPortals = playerPortalLocations.computeIfAbsent(playerUUID) { HashMap() }
        playerPortals[targetWorld.name] = portalLocation.clone()
    }

    fun clearPortalLocation(playerUUID: UUID) {
        playerPortalLocations.remove(playerUUID)
    }

    fun clearPortalLocationForWorld(playerUUID: UUID, worldName: String) {
        val playerPortals = playerPortalLocations[playerUUID]
        playerPortals?.remove(worldName)
        if (playerPortals?.isEmpty() == true) {
            playerPortalLocations.remove(playerUUID)
        }
    }

    private fun getOfflinePlayerUUID(playerName: String?): UUID? {
        if (playerName == null) return null

        val onlinePlayer = Bukkit.getPlayer(playerName)
        if (onlinePlayer != null) {
            return onlinePlayer.uniqueId
        }

        try {
            val offlinePlayer = Bukkit.getOfflinePlayer(playerName)
            if (offlinePlayer.hasPlayedBefore()) {
                return offlinePlayer.uniqueId
            }
        } catch (e: Exception) {
            // Fallback if offline player lookup fails
        }

        return null
    }

    fun addCompassTarget(compassHolder: UUID, targetName: String) {
        compassTargets[compassHolder] = targetName
    }

    fun removeCompassTarget(compassHolder: UUID) {
        compassTargets.remove(compassHolder)
    }

    fun getCompassTarget(compassHolder: UUID): String? {
        return compassTargets[compassHolder]
    }

    fun cleanup() {
        updateTask?.cancel()
        compassTargets.clear()
        playerPortalLocations.clear()
    }
}
