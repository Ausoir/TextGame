package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class Status(val id: String?, val effect: String, val target: Int, val duration: Int, val value: Int, val type: String?,
                  val start_text: String?, val text: String?, val end_text: String?, val reqs: List<Status>?): Serializable{
    companion object {
        fun make(effect: String, duration: Int, value: Int = 1, type: String? = null, id: String? = null): Status{
            return Status(id, effect, 1, duration, value, type, null, null, null, null)
        }
    }

    fun onRemove(target: UnitModel): String{
        when (effect){
            "test" -> {
                target.healthPt -= value
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
            if (target.getStatus("dodge") != null) {
                target.removeStatusByEffect("dodge")
                return "%F dodge%!s.\n"
            } else if (target.getStatus("invulnerable") != null) {
                return "%Fr unaffected.\n"
            } else if (target.position != imposer.position && target.getStatus("redirect") != null) {
                target.removeStatusByEffect("redirect")
                return "%F redirect the attack!\n" + add(imposer, target)
            } else if (target.position == imposer.position && target.getStatus("parry") != null) {
                target.removeStatusByEffect("parry")
                imposer.points = 0
                return "%F parr%ies the attack!\n%Ur stunned.\n"
            }
        }
        return if (duration != 0) {
            target.addStatus(this, duration)
            if (type == "buff"){
                process(target, start_text, imposer=imposer)
            } else {
                getStatusDescription(target, start_text)
            }
        } else {
            process(target, start_text, imposer=imposer)
        }
    }

    private fun getStatusDescription(target: UnitModel, desc: String?): String{
        return if (desc != null) {
            var result = target.replaceNouns(desc)
            result = result.replace("%-v", "${-value}")
            result = result.replace("%v", "$value")
            "$result\n"
        } else {
            ""
        }
    }

    fun process(target: UnitModel, add_text: String?, imposer: UnitModel? = null): String{
        if (imposer == null && type == "buff"){
            return ""
        }
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
            }
            "shuffle" -> {
                target.deck?.shuffle()
            }
            "engage" -> {
                target.position = target.target!!.position
            }
            "disengage" -> {
                target.position = 0
                while (target.getFoeInRange().isNotEmpty()){
                    target.position += 1
                }
            }
            "break" -> {
                target.block = 0
            }
            "stun" -> {
                target.points = 0
            }
            "speed" -> {
                target.points += value
            }
            "health", "blood" -> {
                healthChange = value
            }
            "bash" -> {
                healthChange = imposer?.block ?: 0
            }
            "mana" -> {
                target.mana += value
            }
            "block" -> {
                if (target.getStatus("injury") == null) {
                    target.block += value
                }
            }
            "observe" -> {
                val moveNames = target.deck!!.getQueues()
                result += "${target.replaceNouns("%U")} may use: ${moveNames.joinToString { it }}\n"
            }
            "taunt" -> {
                for (foe in target.foes){
                    foe.position = target.position
                }
            }
            "defend" -> {
                target.block += target.foes.size
            }
        }
        result += getStatusDescription(target, add_text)
        if (healthChange != 0) {
            if (target.getStatus("vulnerable") != null) {
                target.removeStatusByEffect("vulnerable")
                healthChange *= 2
            }
            val oldHp = target.healthPt
            result += target.inflict(healthChange)
            if (imposer != null && effect == "blood" && oldHp != target.healthPt){
                result += imposer.inflict(oldHp - target.healthPt)
                result += getStatusDescription(target, end_text)
            }
        }
        return result
    }
}

data class MoveModel(val effects: List<Status>, val reqs: List<Status>?, val speed: Int, val desc: String, val text: String): Serializable{
    companion object {
        lateinit var shuffle: MoveModel
        fun getWait(text: String): MoveModel{
            return MoveModel(emptyList(), emptyList(), 1, "", text)
        }
    }
    lateinit var name: String

    fun perform(me: UnitModel, everyone: ArrayList<UnitModel>): String{
        if (me.getStatus("confuse") != null){
            me.target = me
        }
        var result = "%U ${text}.\n"
        for (status in effects) {
            val reqText = if (status.reqs != null) {
                me.isReqMet(status.reqs)
            } else {
                ""
            }
            if (reqText.isEmpty()){
                when (status.target) {
                    1 -> {
                        result += status.add(me, me)
                    }
                    2 -> {
                        if (me.target != null) {
                            result += status.add(me.target!!, me)
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
        return result
    }
}

class MoveController(fileManager: FileManager) {
    val model: HashMap<String, MoveModel> = makeItem(fileManager.readAsset("moveset.json") !!)

    private fun makeItem(data: String): HashMap<String, MoveModel> {
        val listType = object : TypeToken<HashMap<String, MoveModel>?>() {}.type
        val result: HashMap<String, MoveModel> = Gson().fromJson(data, listType)
        MoveModel.shuffle = result["shuffle"]!!
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