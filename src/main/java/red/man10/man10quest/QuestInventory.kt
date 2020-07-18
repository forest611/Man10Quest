package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class QuestInventory(private val plugin:Man10Quest) {


    fun openPrize(p: Player, name: String){

        val quest = Man10Quest.quest.questMap[name]?:return
        val inv = Bukkit.createInventory(null,54,"§a§l報酬")

        if (quest.prize.isEmpty()){
            return
        }

        for (item in quest.prize){
            inv.addItem(item)
        }

        p.openInventory(inv)
    }

    fun openDelivery(p: Player, name: String){

        val quest = Man10Quest.quest.questMap[name]?:return
        val inv = Bukkit.createInventory(null,54,"§a§l報酬")

        if (quest.prize.isEmpty()){
            return
        }

        for (item in quest.prize){
            inv.addItem(item)
        }

        p.openInventory(inv)
    }

    fun setPrize(p:Player,name: String){

    }

    fun setDelivery(p:Player,name: String){

    }

    fun openMainMenu(p:Player){



    }

    fun openQuestMenu(p:Player){

    }

    fun openPlayingQuest(p:Player){



    }

}