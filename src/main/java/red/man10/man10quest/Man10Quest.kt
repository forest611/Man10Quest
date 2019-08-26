package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Man10Quest : JavaPlugin() {

    lateinit var questInventory : QuestInventory
    lateinit var questData : QuestData
    lateinit var playerData : PlayerData
    lateinit var thread : Thread
    lateinit var event : QuestEvent

    var damage1 = 0
    var damage2 = 0

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()

        damage1 = config.getInt("damage1")
        damage2 = config.getInt("damage2")

        saveConfig()

        questInventory = QuestInventory(this)
        questData = QuestData(this)
        playerData = PlayerData(this)
        event = QuestEvent(this)

        questData.dailyProcess()

        getCommand("mq").executor = QuestCommand(this)
        server.pluginManager.registerEvents(event,this)
        questData.loadQuest()
        event.loadPrize()

        for (p in Bukkit.getOnlinePlayers()){
            playerData.getFinishQuest(p)
        }


    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
