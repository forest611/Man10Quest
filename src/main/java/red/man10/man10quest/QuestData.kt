package red.man10.man10quest

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
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
            if (f.isFile){
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
            type.number = config.getInt("setting.number",-1)
            type.file = f.name

            val lore = config.getStringList("setting.lore")
            lore.add("§a§l推奨ランク:${type.recRank}§a§l以上")
            type.lore = lore

            for (name in names){

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

                quest.prize = plugin.itemStackArrayFromBase64(config.getString("$name.prize")!!)
                quest.delivery = plugin.itemStackArrayFromBase64(config.getString("$name.delivery")!!)

                val lore2 = config.getStringList("lore")

                lore2.add("§a§l推奨ランク:${quest.recRank}§a§l以上")
                quest.lore = lore2

                quests.add(quest.name)
                questMap[quest.name] = quest
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
            plugin.itemStackArrayToBase64(items.toTypedArray())

            val yml = YamlConfiguration.loadConfiguration(File("${plugin.dataFolder}/${questMap[quest]!!.file}"))
            yml.set("quest.prize",plugin.itemStackArrayToBase64(items.toTypedArray()))

        }

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

        GlobalScope.launch {
            plugin.itemStackArrayToBase64(items.toTypedArray())

            val yml = YamlConfiguration.loadConfiguration(File("${plugin.dataFolder}/${questMap[quest]!!.file}"))
            yml.set("quest.deliver",plugin.itemStackArrayToBase64(items.toTypedArray()))
        }

        questMap[quest]!!.delivery = items

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

    var quests = mutableListOf<String>()
}