package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import red.man10.man10drugplugin.MySQLManager

class Man10Quest : JavaPlugin() {


    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()

        saveConfig()

        getCommand("mq")!!.setExecutor(QuestCommand(this))

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}
