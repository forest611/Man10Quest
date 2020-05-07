package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class QuestInventory(private val plugin:Man10Quest) {


    /////////////////
    //クエストタイプ
    //////////////////
    fun openQuestType(page:Int,p:Player,isHide:Boolean){

        val inv = if (isHide){
            Bukkit.createInventory(null,27,"§0§l裏クエスト§m§a§n§1§0§Q§u§e§s§t")
        }else{
            Bukkit.createInventory(null,27,"§e§lクエストタイプを選択§m§a§n§1§0§Q§u§e§s§t")
        }

        val next = ItemStack(Material.PAPER)
        val nMeta = next.itemMeta

        if (isHide){ nMeta.setDisplayName( "§0§l次のページ") }else{
            nMeta.setDisplayName("§6§l次のページ") }

        nMeta.lore = mutableListOf((page+1).toString())
        next.itemMeta = nMeta

        val previous = ItemStack(Material.PAPER)
        val pMeta = previous.itemMeta

        if (isHide){ pMeta.setDisplayName("§0§l前のページ") }else{
            pMeta.setDisplayName("§6§l前のページ") }

        pMeta.lore = mutableListOf((page-1).toString())
        previous.itemMeta = pMeta

        if (isHide&&(page*3)+1 <=plugin.questData.hideType.size){
            inv.setItem(17,next)
        }

        if (!isHide&&(page*3)+1 <=plugin.questData.type.size){
            inv.setItem(17,next)
        }


        if (page != 1){
            inv.setItem(9,previous)
        }

        try {
            if (isHide){
                inv.setItem(11,makeItem(plugin.questData.hideType[(page*3)-3]))
                inv.setItem(13,makeItem(plugin.questData.hideType[(page*3)-2]))
                inv.setItem(15,makeItem(plugin.questData.hideType[(page*3)-1]))

            }else{
                inv.setItem(11,makeItem(plugin.questData.type[(page*3)-3]))
                inv.setItem(13,makeItem(plugin.questData.type[(page*3)-2]))
                inv.setItem(15,makeItem(plugin.questData.type[(page*3)-1]))
            }

        }catch (e:IndexOutOfBoundsException){
        }


        p.openInventory(inv)
    }
    ////////////////
    //クエスト
    //////////////////
    fun openQuestMenu(type:String,player:Player){
        val inv = Bukkit.createInventory(null,54,"§e§lクエストを選択§m§a§n§1§0§Q§u§e§s§t")

        Bukkit.getScheduler().runTask(plugin, Runnable {

            val quest =plugin.questData.quest

            for (q in quest){
                if (q.type != type){ continue }
                if (plugin.playerData.finishQuest[player]!=null
                        && plugin.playerData.finishQuest[player]!!.contains(q.name))continue
                if (!plugin.playerData.isUnlock(player,q)){ continue }

                inv.addItem(makeItem(q))
            }

            val cancel = ItemStack(Material.RED_STAINED_GLASS_PANE,1)
            val cMeta = cancel.itemMeta
            cMeta.setDisplayName("§c§lクエストタイプ選択画面に戻る")
            cancel.itemMeta = cMeta

            for (i in 45..53){
                inv.setItem(i,cancel)
            }
            player.openInventory(inv)

        })
    }

    ///////////////////
    //タイプ選択アイテム
    ////////////////////
    fun makeItem(data:Data): ItemStack {
        val item = ItemStack(Material.valueOf(data.material),1)
        val meta = item.itemMeta
        meta.setDisplayName(data.title)

        meta.setCustomModelData(data.damage)

        meta.lore = data.lore

        meta.lore = data.lore
        meta.isUnbreakable = true
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS)
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON)
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)

        meta.persistentDataContainer.set(NamespacedKey(plugin,"name"), PersistentDataType.STRING,data.name)

        item.itemMeta = meta
        return item

    }

    ///////////////
    //クエストが進行中の場合
    //////////////
    fun openQuest(player: Player){

        val data= plugin.playerData.playerQuest[player]!!

        val inv = Bukkit.createInventory(null,27,data.title+"§m§a§n§1§0§Q§u§e§s§t")

        val ex = ItemStack(Material.PAPER)
        val meta1 = ex.itemMeta
        meta1.setDisplayName("§6§lクエスト内容を確認")
        meta1.lore = data.lore
        ex.itemMeta = meta1

        val retire = ItemStack(Material.OAK_DOOR)
        val meta2 = retire.itemMeta
        meta2.setDisplayName("§6§lクエストを中断する")
        meta2.lore = mutableListOf("§4§lクエストを中断した場合","§4§lもう一度はじめから","§4§lクエストを始める必要があります")
        retire.itemMeta = meta2

        inv.setItem(11,ex)
        inv.setItem(15,retire)

        player.openInventory(inv)

    }
}