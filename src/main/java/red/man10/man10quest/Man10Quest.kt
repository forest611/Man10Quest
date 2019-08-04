package red.man10.man10quest

import org.bukkit.plugin.java.JavaPlugin

class Man10Quest : JavaPlugin() {

    lateinit var questInventory : QuestInventory
    lateinit var questData : QuestData
    lateinit var playerData : PlayerData
    lateinit var thread : Thread

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

        questData.dailyProcess()

        getCommand("mq").executor = QuestCommand(this)
        server.pluginManager.registerEvents(questInventory,this)
        questData.loadQuest()

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
