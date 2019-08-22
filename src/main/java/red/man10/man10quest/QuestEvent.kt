package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent

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

            if (e.slot in 48..50){
                plugin.questInventory.openQuestType(1,p)
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
    fun msgEvent(e: AsyncPlayerChatEvent){

        val p = e.player
        if (!plugin.playerData.isPlay(p)){ return}

        val data = plugin.playerData.playerQuest[p]!!

        if (data.msg.isEmpty() || data.msg.indexOf(e.message) <0){return}

        plugin.questInventory.finish(p,data)

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


                plugin.questInventory.finish(p,data)
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

                plugin.questInventory.finish(p,plugin.playerData.playerQuest[p]!!)

            }
        }

    }


}