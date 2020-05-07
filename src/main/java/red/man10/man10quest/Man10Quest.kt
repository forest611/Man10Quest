package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import red.man10.man10drugplugin.MySQLManager

class Man10Quest : JavaPlugin() {

    lateinit var questInventory : QuestInventory
    lateinit var questData : QuestData
    lateinit var playerData : PlayerData
    lateinit var event : QuestEvent

    var damage1 = 0
    var damage2 = 0

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()

        damage1 = config.getInt("damage1")
        damage2 = config.getInt("damage2")

        saveConfig()

        createTable()

        questInventory = QuestInventory(this)
        questData = QuestData(this)
        playerData = PlayerData(this)
        event = QuestEvent(this)

        questData.dailyProcess()

        getCommand("mq")!!.setExecutor(QuestCommand(this))
        server.pluginManager.registerEvents(event,this)
        event.loadPrize()
        questData.loadQuest()


        for (p in Bukkit.getOnlinePlayers()){
            playerData.getFinishQuest(p)
        }


    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun createTable(){

        val mysql = MySQLManager(this,"quest")

        mysql.execute("CREATE TABLE if not exists `finish_player` (\n" +
                "\t`player` VARCHAR(20) NULL DEFAULT NULL,\n" +
                "\t`uuid` VARCHAR(40) NULL DEFAULT NULL,\n" +
                "\t`quest` TEXT NULL DEFAULT NULL,\n" +
                "\t`date` DATE NULL\n" +
                ");");
        mysql.execute("CREATE TABLE if not exists `prize` (" +
                "`quest` TEXT NULL DEFAULT NULL," +
                "`prize` TEXT NULL DEFAULT NULL);");
    }
}
