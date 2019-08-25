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
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class QuestEvent(private val plugin:Man10Quest) : Listener{

    @EventHandler
    fun clickEvent(e: InventoryClickEvent){

        if (e.inventory.title.indexOf("§m§a§n§1§0§Q§u§e§s§t") <= 0){
            return
        }

        e.isCancelled = true

        val p = e.whoClicked as Player


        ////////////////type/////////////////////
        if (e.inventory.title.indexOf("§e§lクエストタイプを選択") >=0){

            if(e.slot == 0){
                plugin.questInventory.openHideQuest(1,p)
                return
            }

            if (e.slot == 9 || e.slot == 17){
                if (e.currentItem == null || !e.currentItem.hasItemMeta()){ return }

                plugin.questInventory.openQuestType(e.currentItem.itemMeta.lore[0].toInt(),p)
                return
            }

            if (e.slot == 11||e.slot == 13||e.slot == 15){
                if (e.currentItem == null || !e.currentItem.hasItemMeta()){ return }
                plugin.questInventory.openQuestMenu(plugin.questData.getName(e.currentItem),e.whoClicked as Player)
            }
            return
        }
        ////////////////type hide/////////////////////
        if (e.inventory.title.indexOf("§0§l裏クエスト") >=0){

            if(e.slot == 0){
                plugin.questInventory.openQuestType(1,p)
                return
            }

            if (e.slot == 9 || e.slot == 17){
                plugin.questInventory.openHideQuest(e.currentItem.itemMeta.lore[0].toInt(),p)
                return
            }

            if (e.slot == 11||e.slot == 13||e.slot == 15){
                if (e.currentItem == null || !e.currentItem.hasItemMeta()){ return }
                plugin.questInventory.openQuestMenu(plugin.questData.getName(e.currentItem),e.whoClicked as Player)
            }
            return
        }
        ////////////////quest menu
        if (e.inventory.title.indexOf("§e§lクエストを選択") >=0){
            if (e.currentItem == null || !e.currentItem.hasItemMeta()){
                return
            }

            if (e.slot >= 45){
                plugin.questInventory.openQuestType(1,p)
                return
            }

            if (!plugin.questData.get(plugin.questData.getName(e.currentItem)).start){
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
    fun msgEvent(e: AsyncPlayerChatEvent){

        val p = e.player
        if (!plugin.playerData.isPlay(p)){ return}

        val data = plugin.playerData.playerQuest[p]!!

        if (data.msg.isEmpty() || data.msg.indexOf(e.message) <0){return}
        finish(p,data)
    }

    @EventHandler
    fun cmdEvent(e: PlayerCommandPreprocessEvent){

        val p = e.player
        if (!plugin.playerData.isPlay(p)){ return}

        val data = plugin.playerData.playerQuest[p]!!

        if (data.cmd.isEmpty()){ return}
        for (c in data.cmd){
            e.message.indexOf(c)
            if (e.message.indexOf(c) == 0){
                e.isCancelled = true

                p.sendMessage("コマンドは5秒後に実行されます...")
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
                    Bukkit.dispatchCommand(p,e.message)
                },100)


                finish(p,data)
                return
            }
        }

    }

    @EventHandler
    fun itemClick(e: PlayerInteractEvent){
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

    @EventHandler
    fun login(e:PlayerLoginEvent){
        Thread(Runnable {
            plugin.playerData.getFinishQuest(e.player)
        })
    }

    fun finish(p:Player,data: Data){
        p.inventory.addItem(questCard(data.name))
        if (data.once){
            Thread(Runnable {
                plugin.playerData.finish(p,data.name)
                plugin.playerData.getFinishQuest(p)
            }).start()
        }
        plugin.playerData.playerQuest.remove(p)
        p.sendMessage(data.finishMessage)
        for (c in data.dispatchCmd){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),c)
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


}