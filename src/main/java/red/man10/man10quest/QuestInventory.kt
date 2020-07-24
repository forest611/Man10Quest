package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import red.man10.man10quest.Man10Quest.Companion.playerData
import red.man10.man10quest.Man10Quest.Companion.quest
import javax.naming.Name

class QuestInventory(private val plugin:Man10Quest) {


    fun openPrize(p: Player, name: String){

        val quest = quest.questMap[name]?:return
        val inv = Bukkit.createInventory(null,27,"§a§l報酬")

        if (quest.prize.isEmpty()){
            return
        }

        for (item in quest.prize){
            inv.addItem(item)
        }

        p.openInventory(inv)
    }

    //確認用
    fun openDelivery(p: Player, name: String){

        val quest = quest.questMap[name]?:return
        val inv = Bukkit.createInventory(null,27,"§a§l納品アイテム")

        if (quest.delivery.isEmpty()){
            return
        }

        for (item in quest.delivery){
            inv.addItem(item)
        }

        p.openInventory(inv)
    }

    fun setPrize(p:Player,name: String){

        val inv = Bukkit.createInventory(null,27,"クエスト報酬$name")

        p.openInventory(inv)
    }

    fun setDelivery(p:Player,name: String){

        val inv = Bukkit.createInventory(null,27,"納品アイテム$name")

        p.openInventory(inv)

    }

    fun openMainMenu(p:Player,page:Int){

        val inv = Bukkit.createInventory(null,27,"§e§lクエストタイプを選ぶ")

        val quests = playerData.getUnlockQuestTypes(p)

        if (quests.size > (page+1)*3){

            val next = ItemStack(Material.PAPER)
            val nMeta = next.itemMeta

            nMeta.setDisplayName("§6§l次のページ")

            nMeta.persistentDataContainer.set(NamespacedKey(plugin,"page"), PersistentDataType.INTEGER,page+1)

            next.itemMeta = nMeta

            inv.setItem(8,next)
            inv.setItem(17,next)
            inv.setItem(26,next)

        }

        if (page!=0){
            val previous = ItemStack(Material.PAPER)
            val pMeta = previous.itemMeta

            pMeta.setDisplayName("§6§l前のページ")

            pMeta.persistentDataContainer.set(NamespacedKey(plugin,"page"), PersistentDataType.INTEGER,page-1)

            previous.itemMeta = pMeta

            inv.setItem(0,previous)
            inv.setItem(9,previous)
            inv.setItem(18,previous)
        }

        val slots = mutableListOf(11,13,15)
        var slot = 0

        for (i in page*3 until  (page*3)+3){

            if (i >= quests.size)break

            val data = quest.questType[quests[i]]?:continue

            val item = ItemStack(data.material)
            val meta = item.itemMeta
            meta.setDisplayName(data.title)
            meta.lore = data.lore
            meta.setCustomModelData(data.customModelData)
            meta.persistentDataContainer.set(NamespacedKey(plugin,"name"), PersistentDataType.STRING,quests[i])

            item.itemMeta = meta

            inv.setItem(slots[i],item)
            slot ++
        }

        p.openInventory(inv)

    }

    fun openQuestMenu(p:Player,type:String){

        val inv = Bukkit.createInventory(null,54,"§6§lクエスト一覧")

        val quests = quest.questType[type]!!.quests

        for (q in quests){
            if (playerData.getStatus(p,q) != QuestStatus.UNLOCK)continue

            val data = quest.questMap[q]?:continue

            val item = ItemStack(data.material)
            val meta = item.itemMeta
            meta.setDisplayName(data.title)
            meta.lore = data.lore
            meta.persistentDataContainer.set(NamespacedKey(plugin,"name"), PersistentDataType.STRING,q)
            meta.setCustomModelData(data.customModelData)

            item.itemMeta = meta

            inv.addItem(item)
        }

        p.openInventory(inv)

    }

    fun openPlayingQuest(p:Player){

        val quest = quest.questMap[Man10Quest.playerData.getPlayingQuest(p)?:return]!!
        val inv = Bukkit.createInventory(null,9,"§a§lクエスト：${quest.title}")

        val icon1 = ItemStack(Material.PAPER)
        val meta1 = icon1.itemMeta
        meta1.setDisplayName(quest.description)
        meta1.lore = quest.lore
        icon1.itemMeta = meta1

        val icon2 = ItemStack(Material.OAK_DOOR)
        val meta2 = icon2.itemMeta
        meta2.setDisplayName("§c§lクエストを中断する")
        meta2.lore = mutableListOf("§4§lクエストを中断した場合","§4§lもう一度はじめから","§4§lクエストを始める必要があります")
        icon2.itemMeta = meta2

        inv.setItem(2,icon1)
        inv.setItem(6,icon2)

        p.openInventory(inv)

    }

}