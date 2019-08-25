package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class QuestData(private val plugin :Man10Quest) {

    val quest = mutableListOf<Data>()
    var type = mutableListOf<Data>()
    var hideType = mutableListOf<Data>()

    val name = HashMap<String,Data>()

    fun getName(stack:ItemStack):String{
        return stack.itemMeta.lore[stack.itemMeta.lore.size-2].replace("§","")
    }

    fun loadQuest(){
        quest.clear()
        type.clear()
        hideType.clear()
        name.clear()

        Bukkit.getLogger().info("Loading files....")

        val quest_sort = mutableListOf<Data>()
        val questHide_sort = mutableListOf<Data>()

        val folder = File(Bukkit.getServer().pluginManager.getPlugin("Man10Quest").dataFolder,File.separator)

        if (!folder.exists()){
            return
        }

        val file = folder.listFiles().toMutableList()

        for (f in file){
            if (f.isFile){
                continue
            }
            val files = f.listFiles()

            val config = YamlConfiguration.loadConfiguration(files[f.list().indexOf("setting.yml")])

            val t = Data()

            Bukkit.getLogger().info("Loading setting.yml")

            t.name = config.getString("name","quest")
            t.title = config.getString("title","クエスト1")
            t.material = config.getString("material","STONE")
            t.damage = config.getInt("damage",0)
            t.recRank = config.getString("rank","§e§lGuest")
            t.hide = config.getBoolean("hide",false)
            t.unlock = config.getStringList("unlock")
            t.daily = config.getBoolean("daily",false)
            t.number = config.getInt("number",-1)

            var l = config.getStringList("lore")
            var na = t.name.toCharArray()
            var buildName = StringBuilder()

            for (n in na){

                buildName.append("§$n")
            }

            l.add(buildName.toString())
            l.add("§a§l推奨ランク:${t.recRank}§a§l以上")
            t.lore = l


            if (!t.hide){
                quest_sort.add(t)
            }else{
                questHide_sort.add(t)
            }

            for (data in files){
                if (data.name == "config.yml"){
                    continue
                }
                val yml = YamlConfiguration.loadConfiguration(data)
                val d = Data()

                Bukkit.getLogger().info("Loading yml ${data.name}")

                d.name = yml.getString("name","quest")
                d.title = yml.getString("title","クエスト1")
                d.description = yml.getString("description","none")
                d.material = yml.getString("material","STONE")
                d.damage = yml.getInt("damage",0)
                d.recRank = yml.getString("rank","§e§lGuest")
                d.hide = yml.getBoolean("hide",false)
                d.finishMessage = yml.getString("finishMsg","none")
                d.replicaTitle = yml.getString("replicaTitle","§e証のレプリカ")
                d.msg = yml.getStringList("msg" )
                d.cmd = yml.getStringList("cmd")
                d.once = yml.getBoolean("once",true)
                d.unlock = yml.getStringList("unlock")
                d.daily = yml.getBoolean("daily",false)
                d.number = yml.getInt("number",-1)
                d.type = t.name

                l = yml.getStringList("lore")
                na = d.name.toCharArray()
                buildName = StringBuilder()

                for (n in na){

                    buildName.append("§$n")
                }

                l.add(buildName.toString())
                l.add("§a§l推奨ランク:${d.recRank}§a§l以上")
                d.lore = l

                quest.add(d)
                name[d.name] = d
            }
            name[t.name] = t
            Bukkit.getLogger().info("Loaded setting ${t.name}")
            type = sortingTypes(quest_sort)
            hideType = sortingTypes(questHide_sort)
        }
    }


    fun sortingTypes(data:MutableList<Data>):MutableList<Data>{
        val sortedList = HashMap<Int,Data>()

        val list = mutableListOf<Data>()

        for (d in data){
            if (d.number == -1){
                list.add(d)
                continue
            }
            sortedList[d.number] = d
        }

        return (sortedList.values + list).toMutableList()
    }

    fun dailyProcess(){
        plugin.thread = Thread(Runnable {

            val w = true
            while (w){
                plugin.reloadConfig()
                val dif = (Date().time - plugin.config.getLong("daily"))/(1000*60*60)

                if (dif>=24){
                    val mysql = MySQLManagerV2(plugin,"quest")

                    for (q in quest){
                        if (!q.daily)continue

                        mysql.execute("DELETE FROM finish_player WHERE quest='$name';")
                    }

                    plugin.config.set("daily",Date().time)
                    plugin.saveConfig()
                    Bukkit.getServer().broadcastMessage("§eデイリークエストがリセットされました")

                }
                Thread.sleep(100000)
            }

        })
        plugin.thread.start()

    }

    fun get(quest:String):Data{
        return name[quest]?:return Data()
    }

}
class Data{
    var name = ""
    var title = ""
    var lore = mutableListOf<String>()
    var recRank = ""
    var type = ""
    var material = "STONE"
    var damage = 0
    var hide = false
    var description = ""
    var finishMessage = ""
    var start = true
    var replicaTitle = ""
    var msg = mutableListOf<String>()
    var cmd = mutableListOf<String>()
    var once = true
    var unlock = mutableListOf<String>()
    var daily = false
    var number = 0
    var dispatchCmd = mutableListOf<String>()
}
