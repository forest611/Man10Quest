package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import red.man10.man10drugplugin.MySQLManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class QuestEvent(private val plugin:Man10Quest) : Listener{

    val prize = HashMap<String,ItemStack>()

    @EventHandler
    fun clickEvent(e: InventoryClickEvent){


        if (e.view.title.indexOf("§m§a§n§1§0§Q§u§e§s§t") <= 0){
            return
        }

        e.isCancelled = true

        val p = e.whoClicked as Player
        val item = e.currentItem?:return

        ////////////////type/////////////////////
        if (e.view.title.indexOf("§e§lクエストタイプを選択") >=0){

            if(e.slot == 0){
                plugin.questInventory.openQuestType(1,p,true)
                return
            }

            if (e.slot == 9 || e.slot == 17){
                if (item.hasItemMeta()){ return }

                plugin.questInventory.openQuestType(item.itemMeta.lore!![0].toInt(),p,false)
                return
            }

            if (e.slot == 11||e.slot == 13||e.slot == 15){
                if (!item.hasItemMeta()){ return }
                plugin.questInventory.openQuestMenu(plugin.questData.getName(item),e.whoClicked as Player)
            }
            return
        }
        ////////////////type hide/////////////////////
        if (e.view.title.indexOf("§0§l裏クエスト") >=0){

            if(e.slot == 0){
                plugin.questInventory.openQuestType(1,p,false)
                return
            }

            if (e.slot == 9 || e.slot == 17){
                plugin.questInventory.openQuestType(item.itemMeta.lore!![0].toInt(),p,true)
                return
            }

            if (e.slot == 11||e.slot == 13||e.slot == 15){
                if (!item.hasItemMeta()){ return }
                plugin.questInventory.openQuestMenu(plugin.questData.getName(item),e.whoClicked as Player)
            }
            return
        }
        ////////////////quest menu
        if (e.view.title.indexOf("§e§lクエストを選択") >=0){
            if (!item.hasItemMeta()){
                return
            }

            if (e.slot >= 45){
                plugin.questInventory.openQuestType(1,p,false)
                return
            }

            if (!plugin.questData.get(plugin.questData.getName(item)).start){
                p.sendMessage("§4§lこのクエストは現在受けられません")
                return
            }
            p.closeInventory()
            plugin.playerData.playerQuest[p] = plugin.questData.get(plugin.questData.getName(item))
            p.sendMessage("§e§lクエストを開始しました")
            p.sendMessage(plugin.questData.name[plugin.questData.getName(item)]!!.description)
            return
        }
        ////////////////////// quest menu
        if (e.slot == 11){
            p.closeInventory()
            p.sendMessage(plugin.questData.name[plugin.questData.getName(item)]!!.description)
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
                    Bukkit.dispatchCommand(p,e.message.substring(1))
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

            val item = p.inventory.itemInMainHand

            if (!item.hasItemMeta())return
            if (!item.itemMeta.hasLore())return

            if ((item.itemMeta.lore?:return)[0].indexOf("§6右クリックでクエストクリア！") >0)return
            val name = item.itemMeta.lore!![0].replace("§6右クリックでクエストクリア！","").replace("§","")

            if (plugin.questData.name[name] != null){

                if (name != plugin.playerData.playerQuest[p]!!.name)return

                item.amount = item.amount -1

                if (plugin.playerData.isFinish(p,name))return

                finish(p,plugin.playerData.playerQuest[p]!!)
            }
        }

    }

    @EventHandler
    fun login(e: PlayerJoinEvent){
        Thread(Runnable {
            plugin.playerData.getFinishQuest(e.player)
        }).start()
        Bukkit.getLogger().info("${e.player} ... loaded DB")
    }

    fun finish(p:Player,data: Data){
        p.sendMessage("§e§lクエストクリア！！")
        if (prize[data.name] != null){
            p.inventory.addItem(prize[data.name])
        }
        if (data.once){
            Thread(Runnable {
                plugin.playerData.finish(p,data.name)
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

        val sb = StringBuilder()

        for(c in data.name){
            sb.append("§$c")
        }

        val item = ItemStack(Material.DIAMOND_HOE,1)
        val meta = item.itemMeta
        meta.setCustomModelData(plugin.damage1)
        meta.setDisplayName(data.title+"§e§l達成カード")
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

    fun loadPrize(){
        val mysql = MySQLManager(plugin,"quest")

        val rs = mysql.query("select * from prize;")?:return

        while (rs.next()){
            prize[rs.getString("quest")] = itemFromBase64(rs.getString("prize"))!!
        }
        rs.close()
        mysql.close()
    }

    fun setPrize(name:String,stack: ItemStack){
        MySQLManager(plugin,"quest").execute("INSERT INTO prize VALUES('$name','${itemToBase64(stack)}');")

        prize[name] = stack
    }


    fun itemFromBase64(data: String): ItemStack? {
        try {
            val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
            val dataInput = BukkitObjectInputStream(inputStream)
            val items = arrayOfNulls<ItemStack>(dataInput.readInt())

            // Read the serialized inventory
            for (i in items.indices) {
                items[i] = dataInput.readObject() as ItemStack
            }

            dataInput.close()
            return items[0]
        } catch (e: Exception) {
            return null
        }

    }

    @Throws(IllegalStateException::class)
    fun itemToBase64(item: ItemStack): String {
        try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)
            val items = arrayOfNulls<ItemStack>(1)
            items[0] = item
            dataOutput.writeInt(items.size)

            for (i in items.indices) {
                dataOutput.writeObject(items[i])
            }

            dataOutput.close()
            val base64: String = Base64Coder.encodeLines(outputStream.toByteArray())

            return base64

        } catch (e: Exception) {
            throw IllegalStateException("Unable to save item stacks.", e)
        }
    }
}