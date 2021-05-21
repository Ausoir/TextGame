package aoa.game

import aoa.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class Status(val id: String?, val effect: String, val target: Int, var duration: Int, val value: String?,
                  val start_text: String?, val text: String?, val end_text: String?, val reqs: List<Status>?): Serializable{
    fun onRemove(target: UnitModel): String{
        when (effect){
            "test" -> {
                target.healthPt -= value?.toInt() ?: 0
            }
        }
        if (target.block < 0){
            target.block = 0
        }
        return if (end_text != null){
            "$end_text\n"
        } else {
            ""
        }
    }

    fun add(target: UnitModel, imposer: UnitModel): String{
        if (target.isDead()){
            return ""
        }
        if (imposer != target) {
            if (target.getStatus("invulnerable") != null) {
                return target.replaceNouns("%Ur unaffected.\n")
            } else if (target.position != imposer.position && target.getStatus("redirect") != null) {
                target.removeStatusByEffect("redirect")
                return target.replaceNouns("%U redirect%s the attack!\n") + add(imposer, target)
            } else if (target.position == imposer.position && target.getStatus("parry") != null) {
                target.removeStatusByEffect("parry")
                imposer.points = 0
                return target.replaceNouns("%U parr%ies the attack!\n") + "%Ur stunned.\n"
            }
        }
        return if (duration != 0) {
            target.addStatus(this)
            getStatusDescription(target, start_text)
        } else {
            process(target, start_text, imposer=imposer)
        }
    }

    private fun getStatusDescription(target: UnitModel, desc: String?): String{
        return if (desc != null) {
            var result = target.replaceNouns(desc)
            if (value != null) {
                result = result.replace("%-v", "-$value")
                result = result.replace("%v", value)
            }
            "$result\n"
        } else {
            ""
        }
    }

    fun process(target: UnitModel, add_text: String?, imposer: UnitModel? = null): String{
        var result = ""
        var healthChange = 0
        when (effect) {
            "gather" -> {
                if ("tactical retreat" in target.skills){
                    target.position = 0
                    while (target.getFoeInRange().isNotEmpty()){
                        target.position += 1
                    }
                }
                if ("combat healing" in target.skills && target.getFoeInRange().isEmpty()){
                    target.inflict(1)
                }
                if ("mana regeneration" in target.skills){
                    target.mana += 1
                }
            }
            "shuffle" -> {
                target.deck?.shuffle()
            }
            "engage" -> {
                val cripple = target.getStatus("cripple")
                if (cripple != null){
                    result += getStatusDescription(target, "%Us move failed because of cripple.")
                    return result
                } else {
                    val stop = target.target!!.getStatus("stop")
                    if (stop != null) {
                        target.points = 0
                        result += getStatusDescription(target, "%Us advance was stopped by %F.")
                    } else {
                        target.position = target.target!!.position
                    }
                }
            }
            "disengage" -> {
                val cripple = target.getStatus("cripple")
                if (cripple != null){
                    result += getStatusDescription(target, "%Us move failed because of cripple.")
                } else {
                    target.position = 0
                    while (target.getFoeInRange().isNotEmpty()) {
                        target.position += 1
                    }
                    target.removeStatusById("strangle")
                }
            }
            "break" -> {
                target.block = 0
            }
            "unblock" -> {
                healthChange = -(target.block * 2)
            }
            "stun" -> {
                target.points = 0
            }
            "speed" -> {
                target.points += value?.toInt() ?: 0
            }
            "haste" -> {
                target.points += target.mana + 1
                target.mana = 0
            }
            "health", "blood", "pierce" -> {
                if (imposer?.getStatus("injury") == null) {
                    healthChange = value?.toInt() ?: 0
                }
            }
            "torment" -> {
                healthChange = -(target.healthPt / 2)
            }
            "decapitate" -> {
                if (target.healthPt <= value?.toInt() ?: 0){
                    healthChange = -80
                }
            }
            "rot" -> {
                healthChange = -(target.health - target.healthPt)
            }
            "bash" -> {
                healthChange = -(imposer?.block ?: 0)
            }
            "mana" -> {
                target.mana += value?.toInt() ?: 0
            }
            "siphon" -> {
                if (target.mana > 0) {
                    if (imposer != null) {
                        imposer.mana += target.mana
                    }
                    target.mana = 0
                    result += "<font color='#000055'>${target.replaceNouns("%Us mana is siphoned by %F")}</font>\n"
                }
            }
            "retarget" -> {
                for (foe in target.foes.shuffled()){
                    if (foe != target.target) target.target = foe
                }
            }
            "block" -> {
                if (target.getStatus("injury") == null) {
                    target.block += value?.toInt() ?: 0
                }
            }
            "observe" -> {
                val moveNames = target.deck!!.getQueues()
                result += "<font color='#000055'>${target.replaceNouns("%U")} may use: ${moveNames.joinToString { it }}</font>\n"
            }
            "taunt" -> {
                target.position = imposer?.position ?: target.position
            }
            "defend" -> {
                target.block += target.foes.size
                result += "${target.replaceNouns("%U gain%s")} ${target.foes.size} block.\n"
            }
            "dispel" -> {
                result += target.removeAllStatus()
            }
            "return form" -> {
                target.deck?.switchMoves()
            }
            "shapeshift" -> {
                target.deck?.switchMoves()
                target.deck?.clearMoves()
            }
            "ability" -> {
                target.deck?.addNextMove(MoveModel.mapping[value] ?: MoveModel.shuffle)
            }
        }
        result += getStatusDescription(target, add_text)
        if (healthChange != 0) {
            if (target.getStatus("vulnerable") != null) {
                target.removeStatusByEffect("vulnerable")
                healthChange *= 2
            }
            val oldHp = target.healthPt
            if (effect == "pierce") {
                val block = target.block
                target.block = 0
                result += target.inflict(healthChange)
                target.block = block
            } else {
                result += target.inflict(healthChange)
            }
            if (imposer != null) {
                if (effect == "blood" && oldHp != target.healthPt) {
                    result += imposer.inflict(oldHp - target.healthPt)
                    result += getStatusDescription(target, end_text)
                }
                if (target.getStatus("thorn") != null){
                    result += target.replaceNouns("%U retaliate%s.\n")
                    result += imposer.inflict(-1)
                }
            }
        }
        return result
    }
}

data class MoveModel(val effects: List<Status>, val reqs: List<Status>?, val speed: Int, val desc: String, val text: String): Serializable{
    companion object {
        lateinit var shuffle: MoveModel
        lateinit var mapping: HashMap<String, MoveModel>
        fun getWait(text: String): MoveModel {
            return MoveModel(emptyList(), emptyList(), 1, "", text)
        }
    }
    lateinit var name: String

    fun perform(me: UnitModel, everyone: ArrayList<UnitModel>): String{
        var result = "%U ${text}.\n"
        for (status in effects) {
            val reqText = if (status.reqs != null) {
                me.isReqMet(status.reqs)
            } else {
                ""
            }
            if (reqText.isEmpty()){
                when (status.target) {
                    100 -> { //player
                        for (someone in everyone) {
                            if (someone.name == "You"){
                                result += status.add(someone, me)
                            }
                        }
                    }
                    101 -> { //hurt ally
                        var hurt = me
                        for (someone in everyone) {
                            if (someone !in me.foes){
                                if (someone.getHealthPercentage() < hurt.getHealthPercentage()){
                                    hurt = someone
                                }
                            }
                        }
                        result += status.add(hurt, me)
                    }
                    1 -> {
                        result += status.add(me, me)
                    }
                    2 -> {
                        if (me.target != null) {
                            result += if (me.getStatus("confuse") != null){
                                result += me.replaceNouns("%Ur confused.\n")
                                status.add(me, me)
                            } else {
                                status.add(me.target!!, me)
                            }
                        }
                    }
                    3 -> {
                        result += status.add(me, me)
                        if (me.target != null) {
                            result += status.add(me.target!!, me)
                        }
                    }
                    4 -> { //aoe
                        for (someone in everyone) {
                            if (someone.position == me.position && someone != me) {
                                result += status.add(someone, me)
                            }
                        }
                    }
                    5 -> { //foes
                        for (someone in me.foes) {
                            result += status.add(someone, me)
                        }
                    }
                    6 -> { //all
                        for (someone in everyone) {
                            result += status.add(someone, me)
                        }
                    }
                    7 -> { //allies
                        for (someone in everyone) {
                            if (someone !in me.foes && someone != me) {
                                result += status.add(someone, me)
                            }
                        }
                    }
                    -1 -> {
                        result += me.removeStatus(status.id, status.effect)
                    }
                    -2 -> {
                        if (me.target != null) {
                            result += me.target!!.removeStatus(status.id, status.effect)
                        }
                    }
                    -3 -> {
                        result += me.removeStatus(status.id, status.effect)
                        if (me.target != null) {
                            result += me.target!!.removeStatus(status.id, status.effect)
                        }
                    }
                    -4 -> { //aoe
                        for (someone in everyone) {
                            if (someone.position == me.position && someone != me) {
                                result += someone.removeStatus(status.id, status.effect)
                            }
                        }
                    }
                    -5 -> { //foes
                        for (enemy in me.foes) {
                            result += enemy.removeStatus(status.id, status.effect)
                        }
                    }
                    -6 -> { //all
                        for (someone in everyone) {
                            result += someone.removeStatus(status.id, status.effect)
                        }
                    }
                }
            } else {
                result += "$reqText\n"
            }
        }
        return me.replaceNouns(result)
    }
}

class MoveController(fileManager: FileManager) {
    val model: HashMap<String, MoveModel> = makeItem(fileManager.readAsset("moveset.json") !!)

    private fun makeItem(data: String): HashMap<String, MoveModel> {
        val listType = object : TypeToken<HashMap<String, MoveModel>?>() {}.type
        val result: HashMap<String, MoveModel> = Gson().fromJson(data, listType)
        MoveModel.shuffle = result["shuffle"]!!
        MoveModel.mapping = result
        for ((key, value) in result){
            value.name = key
        }
        return result
    }

    fun getMoves(moves: List<String>): ArrayList<MoveModel>{
        val result = ArrayList<MoveModel>()
        moves.forEach { move ->
            if (model.containsKey(move)) {
                result.add(model[move]!!)
            }
        }
        return result
    }
}