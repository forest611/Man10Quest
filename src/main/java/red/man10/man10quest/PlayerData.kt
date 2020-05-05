package red.man10.man10quest

import org.bukkit.entity.Player
import red.man10.man10drugplugin.MySQLManager
import java.util.concurrent.ConcurrentHashMap


class PlayerData(private val plugin:Man10Quest) {

    val playerQuest = HashMap<Player,Data?>()
    val finishQuest = ConcurrentHashMap<Player,MutableList<Data>>()


    fun getFinishQuest(p:Player) : MutableList<Data>{
        val list = mutableListOf<Data>()

        val mysql = MySQLManager(plugin,"quest")

        val rs = mysql.query("SELECT * FROM finish_player WHERE uuid='${p.uniqueId}';")!!

        while (rs.next()){
            list.add(plugin.questData.get(rs.getString("quest")))
        }
        rs.close()
        mysql.close()

        finishQuest[p] = list

        return list
    }

    fun isFinish(p: Player,quest :String): Boolean {

        if (finishQuest[p]?.indexOf(plugin.questData.get(quest)) == -1)return false

        return true

    }

    fun isUnlock(p:Player,quest:Data): Boolean{

        if (quest.unlock.isEmpty())return true

        val data = finishQuest[p]?:return false

        for (d in quest.unlock){
            if (data.indexOf(plugin.questData.get(d)) < 0){
                return false
            }
        }
        return true
    }

    fun finish(player:Player,name:String){
        val mysql = MySQLManager(plugin,"quest")

        mysql.execute("INSERT INTO finish_player VALUE('${player.name}','${player.uniqueId}','$name',now());")
    }

    fun isPlay(player: Player): Boolean {
        if(playerQuest[player] == null){
            return false
        }
        return true
    }
    fun remove(player:Player, name:String){
        val mysql = MySQLManager(plugin,"quest")

        mysql.execute("DELETE FROM finish_player WHERE player='${player.name}'and quest='$name';")
    }
}