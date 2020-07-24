package red.man10.man10quest

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.concurrent.ConcurrentHashMap


class QuestData(private val plugin :Man10Quest) {

    val questMap = ConcurrentHashMap<String,Quest>()
    var questType = ConcurrentHashMap<String,QuestType>()


    fun loadQuest(){
        questMap.clear()
        questType.clear()

        Bukkit.getLogger().info("Loading files....")

         val folder = File(plugin.dataFolder,File.separator)

        if (!folder.exists()){
            return
        }

        val files = folder.listFiles()?.toMutableList()?:return

        for (f in files){
            if (!f.isFile|| !f.path.endsWith(".yml") || f.name == "config.yml"){
                continue
            }

            val config = YamlConfiguration.loadConfiguration(f)

            val names = config.getKeys(false)

            val quests = mutableListOf<String>()

            val type = QuestType()

            type.name = config.getString("setting.name","quest")!!
            type.title = config.getString("setting.title","クエスト1")!!
            type.material = Material.valueOf(config.getString("setting.material","STONE")!!)
            type.customModelData = config.getInt("setting.customModelData",0)
            type.recRank = config.getString("setting.rank","§e§lGuest")!!
            type.lock = config.getBoolean("setting.lock",false)
            type.file = f.name

            val lore = config.getStringList("setting.lore")
            lore.add("§a§l推奨ランク:${type.recRank}§a§l以上")
            type.lore = lore

            Bukkit.getLogger().info("Loaded quest type ${type.name}")

            for (name in names){

                if (name == "setting")continue

                val quest = Quest()

                quest.name = name
                quest.file = f.name
                quest.title = config.getString("$name.title","クエスト1")!!
                quest.description = config.getString("$name.description","none")!!
                quest.material = Material.valueOf(config.getString("$name.material","STONE")!!)
                quest.customModelData = config.getInt("$name.customModelData",0)
                quest.recRank = config.getString("$name.rank","§e§lGuest")!!
                quest.finishMessage = config.getString("$name.finishMsg","none")!!
                quest.msg = config.getStringList("$name.msg" )
                quest.cmd = config.getStringList("$name.cmd")
                quest.once = config.getBoolean("$name.once",true)
                quest.lock = config.getBoolean("$name.lock",false)

                if (config.getString("$name.prize")!=null){
                    quest.prize = plugin.itemStackArrayFromBase64(config.getString("$name.prize")!!)
                }

                if (config.getString("$name.delivery")!=null){
                    quest.delivery = plugin.itemStackArrayFromBase64(config.getString("$name.delivery")!!)
                }

                val lore2 = config.getStringList("lore")

                lore2.add("§a§l推奨ランク:${quest.recRank}§a§l以上")
                quest.lore = lore2

                quests.add(quest.name)
                questMap[quest.name] = quest

                Bukkit.getLogger().info("Loaded quest ${quest.name}")

            }

            type.quests = quests

            questType[type.name] = type

            Bukkit.getLogger().info("Loaded setting ${type.name}")

        }
    }

    fun setPrize(quest:String,inv: Inventory){

        val items = mutableListOf<ItemStack>()

        for (i in 0..26){
            val item = inv.getItem(i)
            if (item == null || item.type == Material.AIR){
                items.add(ItemStack(Material.AIR))
                continue
            }
            items.add(item)
        }

        GlobalScope.launch {

        }

        Thread(Runnable {
            plugin.itemStackArrayToBase64(items.toTypedArray())

            val file = File("${plugin.dataFolder}/${questMap[quest]!!.file}")

            val yml = YamlConfiguration.loadConfiguration(file)
            yml.set("$quest.prize",plugin.itemStackArrayToBase64(items.toTypedArray()))

            yml.save(file)

        }).start()

        questMap[quest]!!.prize = items

    }

    //納品アイテム
    fun setDelivery(quest:String,inv: Inventory){

        val items = mutableListOf<ItemStack>()

        for (i in 0..26){
            val item = inv.getItem(i)
            if (item == null || item.type == Material.AIR){
                items.add(ItemStack(Material.AIR))
                continue
            }
            items.add(item)
        }

        Thread(Runnable {
            plugin.itemStackArrayToBase64(items.toTypedArray())

            val file = File("${plugin.dataFolder}/${questMap[quest]!!.file}")

            val yml = YamlConfiguration.loadConfiguration(file)
            yml.set("$quest.deliver",plugin.itemStackArrayToBase64(items.toTypedArray()))

            yml.save(file)

        }).start()

        questMap[quest]!!.delivery = items

    }

    fun checkDelivery(p:Player,name:String,take:Boolean):Boolean{
        val quest=  questMap[name]?:return false

        if (Man10Quest.playerData.getPlayingQuest(p) != name)return false

        var bool = false

        val inv = p.inventory

        for (item in quest.delivery){

            if (item.type == Material.AIR)continue

            for (i in inv){

                if (i == null || i.type == Material.AIR)continue

                if (item.toString() == i.toString()){

                    if (take){
                        inv.removeItem(i)
                    }

                    bool = true
                    break
                }
                bool = false
            }

        }

        return bool
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
    var file = ""
    var prize = mutableListOf<ItemStack>()
    var delivery = mutableListOf<ItemStack>()

    var lock = false
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
    var file = ""

    var lock = false//デフォルトでロックをかけるか

    var quests = mutableListOf<String>()
}