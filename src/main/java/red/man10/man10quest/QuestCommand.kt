package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class QuestCommand(private val plugin:Man10Quest) : CommandExecutor{

    var start = true

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {

        if (!sender!!.hasPermission("quest.user")){ return true}

        if (args.isNullOrEmpty()){
            if (!start){
                sender.sendMessage("§e§l現在クエストは受けられません")
                return true
            }

            if (plugin.playerData.isPlay(sender as Player)){
                plugin.questInventory.openQuest(sender)
                return true
            }
            plugin.questInventory.openQuestType(1,sender)
            return true
        }

        if (!sender.hasPermission("quest.staff")){ return true}


        //  /mq finish player quest
        if (args[0] == "finish"){

            val p = Bukkit.getPlayer(args[1])

            if (!start){
                p.sendMessage("§e§l現在クエストは受けられません")
                return true
            }

            val data = plugin.playerData.playerQuest[p]
            if (data == null ||
                    data.name != args[2]){
                p.sendMessage("§4§lあなたはクエストをやっていません！")
                return true
            }

            plugin.event.finish(p,data)

        }


        if (sender !is Player){
            return true
        }

        if (args[0] == "help"){
            sender.sendMessage("§e§lMan10Quest")
            sender.sendMessage("§d§l=================================")
            sender.sendMessage("§b§l/mq check [player] 指定プレイヤーのクエストが終了してるかチェックします")
            sender.sendMessage("§b§l/mq finish [player] [quest] クエストを終了させます(script blockに埋め込む場合はconsoleコマンドとして埋めてください)")
            sender.sendMessage("§b§l/mq remove [player] [quest] クエストクリアを取り消します")
            sender.sendMessage("§b§l/mq on [quest] 指定クエストをonにします onのみでプラグインをonにします")
            sender.sendMessage("§b§l/mq off [quest] 指定クエストをoffにします onのみでプラグインをoffにします")
            sender.sendMessage("§b§l/mq replica [quest]指定クエストの達成の証(レプリカ)を発行します")
            sender.sendMessage("§b§l/mq finishCard [quest] クエストを終了させるためのカードを発行します")
            sender.sendMessage("§b§l/mq reload クエストを再読込します")
            sender.sendMessage("§b§l/mq list 読み込まれているクエストを確認します")
            sender.sendMessage("§b§l/mq prize [quest] 手持ちのアイテムをクエストの報酬にします リロードもされるので注意してください")
            sender.sendMessage("§d§l=================================")
        }

        // /mq check player
        if (args[0] == "check"){

            Bukkit.getScheduler().runTask(plugin) {

                val data = plugin.playerData.getFinishQuest(Bukkit.getPlayer(args[1]))

                for (d in data){
                    sender.sendMessage(d.title)
                }


            }
            return true
        }

        if (args[0] == "remove"){
            Thread(Runnable {
                plugin.playerData.remove(Bukkit.getPlayer(args[1]),args[2])
            }).start()
            sender.sendMessage("§e§l削除完了！")

            return true
        }


        if(args[0] == "on"){
            if (args.size == 2){
                plugin.questData.name[args[1]]!!.start = true
                sender.sendMessage("§e${args[1]}をonにしました")
                return true
            }
            start = true
            sender.sendMessage("§eプラグインをonにしました")
        }

        if (args[0] == "off"){
            if (args.size == 2){
                plugin.questData.name[args[1]]!!.start = false
                sender.sendMessage("§e${args[1]}をoffにしました")
                return true

            }
            start = false
            sender.sendMessage("§eプラグインをoffにしました")

        }


        if (args[0] == "replica"){
            sender.inventory.addItem(replicaCard(args[1]))
        }

        if (args[0] == "finishCard"){
            sender.inventory.addItem(plugin.event.questCard(args[1]))
        }

        if (args[0] == "reload"){
            Bukkit.broadcastMessage("§e§lMan10Questのリロード開始！")
            Bukkit.getScheduler().runTask(plugin) {
                plugin.event.loadPrize()
                sender.sendMessage("§e§l報酬を読み込みました")
                plugin.questData.loadQuest()
                sender.sendMessage("§e§lクエストを読み込みました")


                for (p in Bukkit.getOnlinePlayers()){
                    plugin.playerData.getFinishQuest(p)
                }
                sender.sendMessage("§e§lオンラインプレイヤーのデータを読み込みました")
                Bukkit.broadcastMessage("§e§lMan10Questのリロード完了！")
            }

        }

        if (args[0] == "list"){
            for (d in plugin.questData.quest){
                sender.sendMessage("§e§l${d.name}:§r§f${d.title}")
            }
        }

        if (args[0] == "prize"){

            if (plugin.questData.get(args[1]).name == ""){
                sender.sendMessage("§4§l存在しないクエストです！！！")
                return true
            }
            Bukkit.broadcastMessage("§e§lMan10Questのリロード開始！")

            Bukkit.getScheduler().runTask(plugin) {
                plugin.event.setPrize(args[1],sender.inventory.itemInMainHand)

                sender.sendMessage("§e§l報酬を登録できました！")

                plugin.event.loadPrize()
                sender.sendMessage("§e§l報酬を読み込みました")

                plugin.questData.loadQuest()
                sender.sendMessage("§e§lクエストを読み込みました")


                for (p in Bukkit.getOnlinePlayers()){
                    plugin.playerData.getFinishQuest(p)
                }
                sender.sendMessage("§e§lオンラインプレイヤーのデータを読み込みました")
                Bukkit.broadcastMessage("§e§lMan10Questのリロード完了！")
            }


        }

        return true
    }

    fun replicaCard(name:String): ItemStack {
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

}