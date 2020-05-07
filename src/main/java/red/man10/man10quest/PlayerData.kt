package red.man10.man10quest

import org.bukkit.entity.Player
import red.man10.man10drugplugin.MySQLManager
import java.util.concurrent.ConcurrentHashMap


class PlayerData(private val plugin:Man10Quest) {

    val playerQuest = HashMap<Player,Data?>()//プレイ中のクエスト
    val finishQuest = ConcurrentHashMap<Player,MutableList<String>>()//クリアしたクエストのみ入っている


    fun getFinishQuest(p:Player) : MutableList<String>{
        val list = mutableListOf<String>()

        val mysql = MySQLManager(plugin,"quest")

        val rs = mysql.query("SELECT * FROM finish_player WHERE uuid='${p.uniqueId}';")!!

        while (rs.next()){
            list.add(rs.getString("quest"))
        }
        rs.close()
        mysql.close()

        finishQuest[p] = list

        return list
    }

    fun isFinish(p: Player,quest :String): Boolean {

        if (!(finishQuest[p]?:return false).contains(quest))return false

        return true

    }

    fun isUnlock(p:Player,quest:Data): Boolean{

        if (quest.unlock.isEmpty())return true

        val data = finishQuest[p]?:return false

        for (d in quest.unlock){
            if (data.indexOf(d) < 0){
                return false
            }
        }
        return true
    }

    fun finish(player:Player,name:String){
        val mysql = MySQLManager(plugin,"quest")

        mysql.execute("INSERT INTO finish_player VALUE('${player.name}','${player.uniqueId}','$name',now());")

        val list = finishQuest[player]?: mutableListOf()
        list.add(name)
        finishQuest[player] = list

    }

    fun isPlay(player: Player): Boolean {
        if(playerQuest[player] == null){
            return false
        }
        return true
    }
    fun remove(player:Player, name:String){

        val list = finishQuest[player]?:return
        list.remove(name)
        finishQuest[player] = list

        val mysql = MySQLManager(plugin,"quest")

        mysql.execute("DELETE FROM finish_player WHERE player='${player.name}'and quest='$name';")

    }
}