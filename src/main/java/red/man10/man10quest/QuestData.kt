package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.lang.StringBuilder
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import java.util.TimerTask



class QuestData(private val plugin :Man10Quest) {

    val quest = mutableListOf<Data>()
    val type = mutableListOf<Data>()
    val hideType = mutableListOf<Data>()
    val name = HashMap<String,Data>()

    fun getName(stack:ItemStack):String{
        return stack.itemMeta.lore[stack.itemMeta.lore.size-2].replace("§","")
    }

    fun loadQuest(){

        quest.clear()
        type.clear()
        hideType.clear()
        name.clear()

        val folder = File(Bukkit.getServer().pluginManager.getPlugin("Man10Quest").dataFolder,File.separator)

        if (!folder.exists()){
            return
        }

        val file = folder.listFiles().toMutableList()

        for (f in file){
            if (!f.isFile || f.name.indexOf("config") >=0 || f.name.lastIndexOf(".yml") <0){
                continue
            }

            val yml = YamlConfiguration.loadConfiguration(f)

            val d = Data()

            d.name = yml.getString("name","quest")
            d.title = yml.getString("title","クエスト1")
            d.description = yml.getString("description","none")
            d.material = yml.getString("material","STONE")
            d.damage = yml.getInt("damage",0)
            d.type = yml.getString("type","none")
            d.recRank = yml.getString("rank","§e§lGuest")
            d.hide = yml.getBoolean("hide",false)
            d.finishMessage = yml.getString("finishMsg","none")
            d.replicaTitle = yml.getString("replicaTitle","§e証のレプリカ")
            d.msg = yml.getString("msg","none")
            d.cmd = yml.getString("cmd","none")
            d.once = yml.getBoolean("once",true)
            d.unlock = yml.getStringList("unlock")
            d.daily = yml.getBoolean("daily",false)

            val l = yml.getStringList("lore")
            val na = d.name.toCharArray()
            val buildName = StringBuilder()

            for (n in na){

                buildName.append("§$n")
            }

            l.add(buildName.toString())
            l.add("§a§l推奨ランク:${d.recRank}§a§l以上")
            d.lore = l


            if (d.type == "none"){
                when(d.hide){
                    false->type.add(d)
                    true->hideType.add(d)
                }
            }else{ quest.add(d) }

            name[d.name] = d

        }

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
    var msg = ""
    var cmd = ""
    var once = true
    var unlock = mutableListOf<String>()
    var daily = false
}
