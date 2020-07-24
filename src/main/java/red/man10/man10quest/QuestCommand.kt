package red.man10.man10quest

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import red.man10.man10quest.Man10Quest.Companion.inv
import red.man10.man10quest.Man10Quest.Companion.playerData
import red.man10.man10quest.Man10Quest.Companion.quest
import red.man10.man10quest.Man10Quest.Companion.start

class QuestCommand(private val plugin:Man10Quest) : CommandExecutor{

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (args.isEmpty()){

            if (sender !is Player)return false

            if (!sender.hasPermission("man10quest.user"))return true

            if (playerData.isPlay(sender)){
                inv.openPlayingQuest(sender)
                return true
            }

            inv.openMainMenu(sender,0)

            return true
        }

        if (!sender.hasPermission("man10quest.op"))return true

        when(args[0]){

            "prize" ->{

                if (args.size < 3){
                    sender.sendMessage("§e/mq prize set/show <quest>")
                    sender.sendMessage("§e/mq prize give <quest> <player>")
                }

                when(args[1]){
                    "set"->{
                        if (sender !is Player)return false
                        inv.setPrize(sender,args[2])
                    }
                    "show"->{
                        if (sender !is Player)return false
                        inv.openPrize(sender,args[2])
                    }
                    "give"->{
                        val p = Bukkit.getPlayer(args[3])?:return false
                        inv.openPrize(p,args[2])
                    }

                }

            }

            "delivery" ->{

                when(args[1]){
                    "set"->{
                        if (sender !is Player)return false
                        inv.setDelivery(sender,args[2])
                    }
                    "show"->{
                        if (sender !is Player)return false
                        inv.openDelivery(sender,args[2])
                    }
                    // mq delivery check <quest> <player> <true/false>
                    "check"->{
                        val p = Bukkit.getPlayer(args[3])?:return false

                        if (args.size == 4){
                            if (quest.checkDelivery(p,args[2],false)){
                                playerData.finish(p,args[2])
                            }
                            return true
                        }
                        if (quest.checkDelivery(p,args[2],args[4].toBoolean())){
                            playerData.finish(p,args[2])
                        }


                    }

                }

                return true

            }

            "status" ->{

                when(args[1]){

                    //mq status set quest player status
                    "set"->{
                        val p = Bukkit.getPlayer(args[3])?:return false
                        val status = when(args[4]){
                            "lock"->QuestStatus.LOCK
                            "unlock"->QuestStatus.UNLOCK
                            "clear"->QuestStatus.CLEAR
                            else -> QuestStatus.ERROR

                        }
                        playerData.setStatus(p,args[2],status)

                    }
                    "show"->{

                    }
                    "delete"->{

                    }

                }

            }

            "on"->{
                start = true
                sender.sendMessage("§a§lプラグインをONにしました")
                return true
            }

            "off"->{
                start = false
                sender.sendMessage("§a§lプラグインをOFFにしました")
                return true
            }

            "reload"->{

                GlobalScope.launch {
                    sender.sendMessage("§a§lリロード開始")

                    quest.loadQuest()

                    for (p in Bukkit.getOnlinePlayers()){
                        playerData.load(p)
                    }

                    sender.sendMessage("§a§lリロード完了！")
                }
                return true

            }

            "finish" ->{//mq finish <quest> <player>
                if (args.size != 3)return true

                playerData.finish(Bukkit.getPlayer(args[2])!!,args[1])

            }

            "interruption" ->{//mq interruption <player>
                if (args.size != 2)return true

                playerData.interruption(Bukkit.getPlayer(args[1])!!)
            }

            "start" ->{
                if (args.size != 3)return true

                playerData.start(Bukkit.getPlayer(args[2])!!,args[1])
            }

        }


        return false
    }

}