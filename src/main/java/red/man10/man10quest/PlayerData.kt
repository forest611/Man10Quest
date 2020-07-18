package red.man10.man10quest

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap


class PlayerData(private val plugin:Man10Quest) {

    val playerQuest = HashMap<Player,Quest?>()//プレイ中のクエスト
    val playerData = ConcurrentHashMap<Player,HashMap<String,QuestStatus>>()

    //起動時にクエストのクリア状況等を読み込む
    fun load(p:Player) : HashMap<String,QuestStatus>{
        val map = HashMap<String,QuestStatus>()

        val mysql = MySQLManager(plugin,"quest")

        val rs = mysql.query("SELECT status,quest FROM player_quest_data WHERE uuid='${p.uniqueId}';")!!

       while (rs.next()){

            val quest = rs.getString("quest")

            map[quest] = when(rs.getString("status")){
                "lock" -> QuestStatus.LOCK
                "unlock" -> QuestStatus.UNLOCK
                "clear" -> QuestStatus.CLEAR
                else -> QuestStatus.ERROR
            }

        }
        rs.close()
        mysql.close()

        playerData[p] = map

        return map
    }

    fun create(p:Player){

        for (quest in Man10Quest.quest.questMap.keys()){

            MySQLManager.executeQueue("INSERT INTO player_quest_data (player, uuid, quest_name, status) " +
                    "VALUES ('${p.name}', '${p.uniqueId}', '$quest', 'lock');")

        }
        Bukkit.getLogger().info("${p.name}のクエストデータを生成")
    }

    fun get(p:Player,quest:String):QuestStatus{
        return playerData[p]?.get(quest)?:return QuestStatus.ERROR
    }

    fun finish(p:Player,name:String){

        playerQuest[p] = null
        setStatus(p,name,QuestStatus.CLEAR)
    }

    fun setStatus(p:Player,quest:String,status:QuestStatus){

        val statusString = when(status){
            QuestStatus.LOCK-> "lock"
            QuestStatus.UNLOCK-> "unlock"
            QuestStatus.CLEAR-> "clear"
            else -> "lock"
        }

        MySQLManager.executeQueue("UPDATE player_quest_data SET status = '$statusString' " +
                "WHERE uuid = '${p.uniqueId}' AND quest_name='$quest';")
    }

    fun isPlay(player: Player): Boolean {
        if(playerQuest[player] == null){
            return false
        }
        return true
    }

}
enum class QuestStatus{

    LOCK,
    UNLOCK,
    CLEAR,
    ERROR

}