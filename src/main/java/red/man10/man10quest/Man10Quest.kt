package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Man10Quest : JavaPlugin() {

    companion object{
        lateinit var quest : QuestData
        lateinit var playerData : PlayerData
    }


    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()

        saveConfig()

        getCommand("mq")!!.setExecutor(QuestCommand(this))

        MySQLManager.setupBlockingQueue(this,"Man10Quest")

        quest = QuestData(this)
        playerData = PlayerData(this)

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}
