package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap


class QuestData(private val plugin :Man10Quest) {

    val quest = ConcurrentHashMap<String,Quest>()
    var questType = ConcurrentHashMap<String,QuestType>()


    fun loadQuest(){
        quest.clear()
        questType.clear()

        Bukkit.getLogger().info("Loading files....")

         val folder = File(Bukkit.getServer().pluginManager.getPlugin("Man10Quest")!!.dataFolder,File.separator)

        if (!folder.exists()){
            return
        }

        val file = folder.listFiles()?.toMutableList()?:return

        for (f in file){
            if (f.isFile){
                continue
            }
            val files = f.listFiles()?:return

            val config = YamlConfiguration.loadConfiguration(files[f.list()?.indexOf("setting.yml")?:return])

            val type = QuestType()

            Bukkit.getLogger().info("Loading setting.yml")

            type.name = config.getString("name","quest")!!
            type.title = config.getString("title","クエスト1")!!
            type.material = Material.valueOf(config.getString("material","STONE")!!)
            type.customModelData = config.getInt("customModelData",0)
            type.recRank = config.getString("rank","§e§lGuest")!!
            type.number = config.getInt("number",-1)

            var lore = config.getStringList("lore")
            lore.add("§a§l推奨ランク:${type.recRank}§a§l以上")
            type.lore = lore

            val dataFile = YamlConfiguration.loadConfiguration(files[f.list()?.indexOf("quest.yml")?:return])

            val names = dataFile.getKeys(false)

            val quests = mutableListOf<Quest>()

            for (name in names){

                val quest1 = Quest()

                quest1.name = name
                quest1.title = dataFile.getString("$name.title","クエスト1")!!
                quest1.description = dataFile.getString("$name.description","none")!!
                quest1.material = Material.valueOf(dataFile.getString("$name.material","STONE")!!)
                quest1.customModelData = dataFile.getInt("$name.customModelData",0)
                quest1.recRank = dataFile.getString("$name.rank","§e§lGuest")!!
                quest1.finishMessage = dataFile.getString("$name.finishMsg","none")!!
                quest1.msg = dataFile.getStringList("$name.msg" )
                quest1.cmd = dataFile.getStringList("$name.cmd")
                quest1.once = dataFile.getBoolean("$name.once",true)

                lore = dataFile.getStringList("lore")

                //TODO:報酬が設定されていないときの処理

                lore.add("§a§l推奨ランク:${quest1.recRank}§a§l以上")
                quest1.lore = lore

                quests.add(quest1)
            }

            type.quests = quests

            questType[type.name] = type

            Bukkit.getLogger().info("Loaded setting ${type.name}")

        }
    }




}
class Quest{
    var name = ""
    var title = ""
    var lore = mutableListOf<String>()
    var recRank = ""    //推奨ランク
    var material = Material.STONE
    var customModelData = 0
    var description = ""
    var finishMessage = ""
    var msg = mutableListOf<String>() //クリアのトリガーとなるメッセージ
    var cmd = mutableListOf<String>() //クリアのトリガーとなるコマンド(指定コマンド実行でクリア)
    var once = true
}

//クエストの
class QuestType{

    var name = ""
    var title = ""
    var lore = mutableListOf<String>()
    var recRank = ""
    var material = Material.STONE
    var customModelData = 0
    var hide = false
    var number = 0//表示順

    var quests = mutableListOf<Quest>()
}