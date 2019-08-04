package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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

        // /mq check player quest
        if (args[0] == "check"){

            Bukkit.getScheduler().runTask(plugin) {

                if (plugin.playerData.isFinish(Bukkit.getPlayer(args[1]),args[2])){
                    Bukkit.getLogger().info("finish")
                    return@runTask
                }
                Bukkit.getLogger().info("not finish")

            }
            return true
        }

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

            plugin.questInventory.finish(p,data)

        }

        if (args[0] == "remove"){
            Thread(Runnable {
                plugin.playerData.remove(Bukkit.getPlayer(args[1]),args[2])
            }).start()
            return true
        }

        if (sender !is Player){
            return true
        }

        if (args[0] == "help"){
            sender.sendMessage("§e§lMan10Quest")
            sender.sendMessage("§d§l=================================")
            sender.sendMessage("§b§l/mq check [player] [quest] 指定プレイヤーのクエストが終了してるかチェックします")
            sender.sendMessage("§b§l/mq finish [player] [quest] クエストを終了させます(script blockに埋め込む場合はconsoleコマンドとして埋めてください)")
            sender.sendMessage("§b§l/mq remove [player] [quest] クエストクリアを取り消します")
            sender.sendMessage("§b§l/mq on [quest] 指定クエストをonにします onのみでプラグインをonにします")
            sender.sendMessage("§b§l/mq on [quest] 指定クエストをoffにします onのみでプラグインをoffにします")
            sender.sendMessage("§b§l/mq replica [quest]指定クエストの達成の証(レプリカ)を発行します")
            sender.sendMessage("§b§l/mq finishCard [quest] クエストを終了させるためのカードを発行します ドロップでクエストクリアなどに")
            sender.sendMessage("§b§l/mq reload クエストを再読込します")
            sender.sendMessage("§b§l/mq list 読み込まれているクエストを確認します")
            sender.sendMessage("§d§l=================================")
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
            sender.inventory.addItem(plugin.questInventory.replicaCard(args[1]))
        }

        if (args[0] == "finishCard"){
            sender.inventory.addItem(plugin.questInventory.finishCard(args[1]))
        }

        if (args[0] == "reload"){
            Thread(Runnable{
                plugin.questData.loadQuest()
                sender.sendMessage("§e§lクエストを読み込みました")
            }).start()
        }

        if (args[0] == "list"){
            for (d in plugin.questData.quest){
                sender.sendMessage("§e§l${d.name}:§r§f${d.title}")
            }
        }


        return true
    }

}