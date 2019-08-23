package red.man10.man10quest

import org.bukkit.entity.Player


class PlayerData(private val plugin:Man10Quest) {

    val playerQuest = HashMap<Player,Data?>()


    fun getFinishQuest(p:Player) : MutableList<Data>{
        val list = mutableListOf<Data>()

        val mysql = MySQLManagerV2(plugin,"quest")

        val qu = mysql.query("SELECT * FROM finish_player WHERE uuid='${p.uniqueId}';")

        val rs = qu.rs

        while (rs.next()){
            list.add(plugin.questData.get(rs.getString("quest")))
        }
        rs.close()
        qu.close()

        return list
    }

    fun isFinish(player: Player,quest :String): Boolean {
        val mysql = MySQLManagerV2(plugin,"quest")

        val q = mysql.query("SELECT * FROM finish_player WHERE uuid='${player.uniqueId}' and quest='$quest';")
        if(q.rs.next()){
            q.close()
            return true
        }
        q.close()
        return false

    }

    fun isUnlock(p:Player,quest:Data): Boolean{

        if (quest.unlock.isEmpty())return true

        val data = getFinishQuest(p)

        for (d in quest.unlock){
            if (data.indexOf(plugin.questData.get(d)) < 0){
                return false
            }
        }
        return true
    }

    fun finish(player:Player,name:String){
        val mysql = MySQLManagerV2(plugin,"quest")

        mysql.execute("INSERT INTO finish_player VALUE('${player.name}','${player.uniqueId}','$name',now());")
    }

    fun isPlay(player: Player): Boolean {
        if(playerQuest[player] == null){
            return false
        }
        return true
    }
    fun remove(player:Player, name:String){
        val mysql = MySQLManagerV2(plugin,"quest")

        mysql.execute("DELETE FROM finish_player WHERE player='${player.name}'and quest='$name';")
    }
}