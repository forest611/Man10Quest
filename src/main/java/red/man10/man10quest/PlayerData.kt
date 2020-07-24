package red.man10.man10quest

import org.bukkit.entity.Player
import red.man10.man10quest.MySQLManager.Companion.executeQueue
import red.man10.man10quest.QuestStatus.*
import java.util.concurrent.ConcurrentHashMap


class PlayerData(private val plugin:Man10Quest) {

    val playerQuest = HashMap<Player,String?>()//プレイ中のクエスト
    val playerData = ConcurrentHashMap<Player,HashMap<String,QuestStatus>>()


    fun setStatus(p:Player,quest:String,status:QuestStatus){

        val statusString = when(status){
            LOCK -> "lock"
            UNLOCK -> "unlock"
            CLEAR -> "clear"
            else -> "lock"
        }

        executeQueue("UPDATE player_quest_data SET status = '$statusString' " +
                "WHERE uuid = '${p.uniqueId}' AND quest_name='$quest';")

        playerData[p]!![quest] = status
    }


    //nullのときはロックされてる状態
    fun getStatus(p:Player,quest: String):QuestStatus{
        return playerData[p]?.get(quest)?:ERROR
    }

    //起動時にクエストのクリア状況等を読み込む
    fun load(p:Player){
        val map = HashMap<String,QuestStatus>()

        val mysql = MySQLManager(plugin,"quest")

        val rs = mysql.query("SELECT status,quest FROM player_quest_data WHERE uuid='${p.uniqueId}';")!!

       while (rs.next()){

            val quest = rs.getString("quest")

            map[quest] = when(rs.getString("status")){
                "lock" -> LOCK
                "unlock" -> UNLOCK
                "clear" -> CLEAR
                else -> LOCK
            }

        }
        rs.close()
        mysql.close()

        for (q in Man10Quest.quest.questMap.keys()){
            if (map[q] ==null){
                val data = Man10Quest.quest.questMap[q]!!

                executeQueue("INSERT INTO player_quest_data (player, uuid, quest_name, status) " +
                        "VALUES ('${p.name}', '${p.uniqueId}', '${q}', '${if (data.lock) "lock" else "unlock"}');")

                map[q] = if (data.lock) LOCK else UNLOCK
            }
        }

        for (type in Man10Quest.quest.questType.keys()){
            if (map[type] ==null){
                val data = Man10Quest.quest.questType[type]!!

                executeQueue("INSERT INTO player_quest_data (player, uuid, quest_name, status) " +
                        "VALUES ('${p.name}', '${p.uniqueId}', '${type}', '${if (data.lock) "lock" else "unlock"}');")

                map[type] = if (data.lock) LOCK else UNLOCK
            }

        }

        playerData[p] = map

        return
    }

    fun finish(p:Player,name:String){

        playerQuest[p] = null
        setStatus(p,name,CLEAR)
        val data = Man10Quest.quest.questMap[name]!!
        p.sendMessage(data.finishMessage)
    }

    fun interruption(p: Player){
        playerQuest[p] = null
    }

    fun start(p:Player, name: String){

        playerQuest[p] = name

        val data = Man10Quest.quest.questMap[name]!!
        p.sendMessage(data.description)
    }


    fun isPlay(p: Player): Boolean {
        if(playerQuest[p] == null){
            return false
        }
        return true
    }

    fun getPlayingQuest(p:Player): String? {
        return playerQuest[p]
    }

    fun getUnlockQuestTypes(p:Player):MutableList<String>{
        val list = mutableListOf<String>()

        for (type in Man10Quest.quest.questType.keys()){
            if (playerData[p]!![type] == UNLOCK){
                list.add(type)
            }
        }

        return list

    }

}
enum class QuestStatus{

    LOCK,
    UNLOCK,
    CLEAR,
    ERROR

}