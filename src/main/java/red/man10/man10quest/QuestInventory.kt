package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class QuestInventory(private val plugin:Man10Quest) : Listener{


    /*

    inventoryとeventを扱うclass


     */

    /////////////////
    //クエストタイプ
    //////////////////
    fun openQuestType(page:Int,p:Player){
        val inv = Bukkit.createInventory(null,27,"§e§lクエストタイプを選択§m§a§n§1§0§Q§u§e§s§t")

        val next = ItemStack(Material.PAPER)
        val nMeta = next.itemMeta
        nMeta.displayName = "§6§l次のページ"
        nMeta.lore = mutableListOf((page+1).toString())
        next.itemMeta = nMeta

        val previous = ItemStack(Material.PAPER)
        val pMeta = previous.itemMeta
        pMeta.displayName = "§6§l前のページ"
        pMeta.lore = mutableListOf((page-1).toString())
        previous.itemMeta = pMeta

        if ((page*3)+1 <=plugin.questData.type.size){
            inv.setItem(17,next)

        }
        if (page != 1){
            inv.setItem(9,previous)
        }

        try {
            inv.setItem(11,makeItem(plugin.questData.type[(page*3)-3]))
            inv.setItem(13,makeItem(plugin.questData.type[(page*3)-2]))
            inv.setItem(15,makeItem(plugin.questData.type[(page*3)-1]))

        }catch (e:IndexOutOfBoundsException){
        }


        p.openInventory(inv)
    }
    ////////////////////
    //裏クエスト
    /////////////////////
    fun openHideQuest(page: Int,p:Player){

        val inv = Bukkit.createInventory(null,27,"§0§l裏クエスト§m§a§n§1§0§Q§u§e§s§t")

        val next = ItemStack(Material.PAPER)
        val nMeta = next.itemMeta
        nMeta.displayName = "§0§l次のページ"
        nMeta.lore = mutableListOf((page+1).toString())
        next.itemMeta = nMeta

        val previous = ItemStack(Material.PAPER)
        val pMeta = previous.itemMeta
        pMeta.displayName = "§0§l前のページ"
        pMeta.lore = mutableListOf((page-1).toString())
        previous.itemMeta = pMeta

        if ((page*3)+1 <=plugin.questData.hideType.size){
            inv.setItem(17,next)

        }
        if (page != 1){
            inv.setItem(9,previous)
        }


        try {
            inv.setItem(11,makeItem(plugin.questData.hideType[(page*3)-3]))
            inv.setItem(13,makeItem(plugin.questData.hideType[(page*3)-2]))
            inv.setItem(15,makeItem(plugin.questData.hideType[(page*3)-1]))

        }catch (e:IndexOutOfBoundsException){

        }


        p.openInventory(inv)
    }
    ////////////////
    //クエスト
    //////////////////
    fun openQuestMenu(type:String,player:Player){
        val inv = Bukkit.createInventory(null,54,"§e§lクエストを選択§m§a§n§1§0§Q§u§e§s§t")

        Bukkit.getScheduler().runTask(plugin) {

            val quest = plugin.playerData.getNotFinishQuest(player)

            for (q in quest){
                if (q.type != type)continue
                if (!plugin.playerData.isUnlock(player,q))continue
                inv.addItem(makeItem(q))
            }

        }
        player.openInventory(inv)
    }

    ///////////////////
    //タイプ選択画面
    ////////////////////
    fun makeItem(data:Data): ItemStack {
        val item = ItemStack(Material.valueOf(data.material),1,data.damage.toShort())
        val meta = item.itemMeta
        meta.displayName = data.title

        meta.lore = data.lore
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
        meta1.displayName = "§6§lクエスト内容を確認"
        meta1.lore = data.lore
        ex.itemMeta = meta1

        val retire = ItemStack(Material.WOOD_DOOR)
        val meta2 = retire.itemMeta
        meta2.displayName ="§6§lクエストを中断する"
        meta2.lore = mutableListOf("§4§lクエストを中断した場合","§4§lもう一度はじめからクエストを始める必要があります")
        retire.itemMeta = meta2

        inv.setItem(11,ex)
        inv.setItem(15,retire)

        player.openInventory(inv)

    }

    //////////////////////////////////
    //event
    //////////////////////////////////

    @EventHandler
    fun clickEvent(e:InventoryClickEvent){

        if (e.inventory.title.indexOf("§m§a§n§1§0§Q§u§e§s§t") <= 0){
            return
        }

        e.isCancelled = true

        val p = e.whoClicked as Player


        ////////////////type/////////////////////
        if (e.inventory.title.indexOf("§e§lクエストタイプを選択") >=0){

            if(e.slot == 0){
                openHideQuest(1,p)
                return
            }

            if (e.slot == 9 || e.slot == 17){
                if (e.currentItem == null || !e.currentItem.hasItemMeta()){ return }

                openQuestType(e.currentItem.itemMeta.lore[0].toInt(),p)
                return
            }

            if (e.slot == 11||e.slot == 13||e.slot == 15){
                if (e.currentItem == null || !e.currentItem.hasItemMeta()){ return }
                openQuestMenu(plugin.questData.getName(e.currentItem),e.whoClicked as Player)
            }
            return
        }
        ////////////////type hide/////////////////////
        if (e.inventory.title.indexOf("§0§l裏クエスト") >=0){

            if(e.slot == 0){
                openQuestType(1,p)
                return
            }

            if (e.slot == 9 || e.slot == 17){
                openHideQuest(e.currentItem.itemMeta.lore[0].toInt(),p)
                return
            }

            if (e.slot == 11||e.slot == 13||e.slot == 15){
                if (e.currentItem == null || !e.currentItem.hasItemMeta()){ return }
                openQuestMenu(plugin.questData.getName(e.currentItem),e.whoClicked as Player)
            }
            return
        }
        ////////////////quest
        if (e.inventory.title.indexOf("§e§lクエストを選択") >=0){
            if (e.currentItem == null || !e.currentItem.hasItemMeta()){
                return
            }

            if (!plugin.questData.name[plugin.questData.getName(e.currentItem)]!!.start){
                p.sendMessage("§4§lこのクエストは現在受けられません")
                return
            }
            p.closeInventory()
            plugin.playerData.playerQuest[p] = plugin.questData.name[plugin.questData.getName(e.currentItem)]!!
            p.sendMessage("§e§lクエストを開始しました")
            p.sendMessage(plugin.questData.name[plugin.questData.getName(e.currentItem)]!!.description)
            return
        }
        ////////////////////// quest menu
        if (e.slot == 11){
            p.closeInventory()
            p.sendMessage(plugin.questData.name[plugin.questData.getName(e.currentItem)]!!.description)
            return
        }
        if (e.slot == 15){
            plugin.playerData.playerQuest.remove(p)
            p.closeInventory()
            p.sendMessage("§e§lクエストを中断しました")
            return
        }

    }


    @EventHandler
    fun msgEvent(e:AsyncPlayerChatEvent){

        val p = e.player
        if (!plugin.playerData.isPlay(p)){ return}

        val data = plugin.playerData.playerQuest[p]!!

        if (data.msg != e.message || data.msg == "none"){ return}
        finish(p,data)

    }

    @EventHandler
    fun cmdEvent(e:PlayerCommandPreprocessEvent){

        val p = e.player
        if (!plugin.playerData.isPlay(p)){ return}

        val data = plugin.playerData.playerQuest[p]!!

        if (data.cmd != e.message || data.cmd == "none"){ return}
        finish(p,data)

    }

    @EventHandler
    fun itemClick(e:PlayerInteractEvent){
        if (e.action == Action.RIGHT_CLICK_AIR ||
                e.action == Action.RIGHT_CLICK_BLOCK) {
            val p = e.player

            if (!plugin.playerData.isPlay(p))return


            val item = p.inventory.itemInMainHand?:return

            if (item.itemMeta == null)return
            if (!item.hasItemMeta())return
            if (!item.itemMeta.hasLore())return

            if (item.itemMeta.lore[0].indexOf("§6右クリックでクエストクリア！") >0)return

            val name = item.itemMeta.lore[0].replace("§6右クリックでクエストクリア！","").replace("§","")

            if (plugin.questData.name[name] != null){

                p.inventory.removeItem(item)

                if (plugin.playerData.isFinish(p,name))return

                finish(p,plugin.playerData.playerQuest[p]!!)

            }
        }

    }

    fun questCard(name: String): ItemStack {
        val data = plugin.questData.name[name]!!
        if (data.hide){
            val item = ItemStack(Material.DIAMOND_HOE,1,plugin.damage2.toShort())
            val meta = item.itemMeta
            meta.displayName = "§kXX§r§7§l裏クエスト"+data.title+"§8§l達成の証§kXX§r"
            meta.lore = mutableListOf("§7裏クエスト達成おめでとうございます","§8ぜひ豪華な景品と交換してください！")
            meta.isUnbreakable = true
            meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS,1,true)
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            meta.addItemFlags(ItemFlag.HIDE_DESTROYS)
            meta.addItemFlags(ItemFlag.HIDE_PLACED_ON)
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            item.itemMeta = meta

            return item

        }
        val item = ItemStack(Material.DIAMOND_HOE,1,plugin.damage1.toShort())
        val meta = item.itemMeta
        meta.displayName = data.title+"§e§l達成の証"
        meta.lore = mutableListOf("§6このカードを持って報酬と交換しよう！")
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS)
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON)
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.isUnbreakable = true

        item.itemMeta = meta
        return item
    }

    fun replicaCard(name:String):ItemStack{
        val data = plugin.questData.name[name]!!
        if (data.hide){
            val item = ItemStack(Material.DIAMOND_HOE,1,plugin.damage2.toShort())
            val meta = item.itemMeta
            meta.displayName = "§kXX§r§7§l裏クエスト"+data.title+"§8§l達成の証§kXX§r"
            meta.lore = mutableListOf("§7飾り用の証",data.replicaTitle)
            meta.isUnbreakable = true
            meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS,1,true)
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            meta.addItemFlags(ItemFlag.HIDE_DESTROYS)
            meta.addItemFlags(ItemFlag.HIDE_PLACED_ON)
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)

            item.itemMeta = meta

            return item

        }
        val item = ItemStack(Material.DIAMOND_HOE,1,plugin.damage1.toShort())
        val meta = item.itemMeta
        meta.displayName = data.title+"§e§l達成の証"
        meta.lore = mutableListOf("§6飾り用の証",data.replicaTitle)
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS)
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON)
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.isUnbreakable = true


        item.itemMeta = meta
        return item

    }

    fun finishCard(name:String):ItemStack{
        val data = plugin.questData.name[name]!!

        val sb = StringBuilder()

        for(c in data.name){
            sb.append("§$c")
        }

        val item = ItemStack(Material.DIAMOND_HOE,1,plugin.damage1.toShort())
        val meta = item.itemMeta
        meta.displayName = data.title+"§e§l達成カード"
        meta.lore = mutableListOf("§6右クリックでクエストクリア！$sb")
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS)
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON)
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.isUnbreakable = true

        item.itemMeta = meta
        return item

    }

    fun finish(p:Player,data: Data){
        p.inventory.addItem(questCard(data.name))
        if (data.once){
            Thread(Runnable {
                plugin.playerData.finish(p,data.name)
            }).start()
        }
        plugin.playerData.playerQuest.remove(p)
        p.sendMessage(data.finishMessage)

    }


}