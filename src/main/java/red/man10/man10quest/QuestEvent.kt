package red.man10.man10quest

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.persistence.PersistentDataType


class QuestEvent(private val plugin:Man10Quest) : Listener {

    @EventHandler
    fun inventoryClick(e:InventoryClickEvent){

        val item = e.currentItem?:return
        val p = e.whoClicked

        if (p !is Player)return

        when(e.view.title){

            "§e§lクエストタイプを選ぶ" ->{
                e.isCancelled = true

                if (item.itemMeta.displayName == "§6§l次のページ" ||item.itemMeta.displayName==  "§6§l前のページ"){
                    Man10Quest.inv.openMainMenu(p,item.itemMeta.persistentDataContainer[NamespacedKey(plugin,"page"), PersistentDataType.INTEGER]!!)
                    return
                }

                Man10Quest.inv.openQuestMenu(p,item.itemMeta.persistentDataContainer[NamespacedKey(plugin,"name"), PersistentDataType.STRING]?:return)

                return
            }

            "§6§lクエスト一覧" ->{

                e.isCancelled = true

                Man10Quest.playerData.start(p,item.itemMeta.persistentDataContainer[NamespacedKey(plugin,"name"), PersistentDataType.STRING]?:return)

                p.closeInventory()
            }

        }

    }

    @EventHandler
    fun inventoryClose(e:InventoryCloseEvent){

        val title = e.view.title

        if (title.indexOf("クエスト報酬") == 0){
            Man10Quest.quest.setPrize(title.replace("クエスト報酬",""),e.inventory)
            e.player.sendMessage("報酬を設定しました")

            return
        }

        if (title.indexOf("納品アイテム") == 0){
            Man10Quest.quest.setDelivery(title.replace("納品アイテム",""),e.inventory)
            e.player.sendMessage("納品アイテムを設定しました")

            return
        }

    }


}