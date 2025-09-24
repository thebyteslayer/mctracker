package com.thebyteslayer.tracker

import org.bukkit.plugin.java.JavaPlugin

class TrackerPlugin : JavaPlugin() {

    private var compassTracker: CompassTracker? = null

    override fun onEnable() {
        compassTracker = CompassTracker(this)

        server.pluginManager.registerEvents(CompassListener(compassTracker!!), this)
        server.pluginManager.registerEvents(PlayerListener(compassTracker!!), this)
        server.pluginManager.registerEvents(PortalListener(compassTracker!!), this)


        logger.info("Tracker plugin has been enabled!")
    }

    override fun onDisable() {
        compassTracker?.cleanup()
        logger.info("Tracker plugin has been disabled!")
    }
}
