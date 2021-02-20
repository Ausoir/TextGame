package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class Status(val effect: String, val target: Int, var duration: Int, val value: Int, val start_text: String?, val text: String?, val end_text: String?, val reqs: List<Status>?): Serializable{
    fun checkEnd(): Boolean{
        if (duration == 0) {
            return true
        } else if (duration > 0) {
            duration -= 1
        }
        return false
    }

    fun onRemove(target: UnitModel): String{
        when (effect){
            "harden" -> {
                target.barrier -= value
            }
        }
        if (target.barrier < 0){
            target.barrier = 0
        }
        return if (end_text != null){
            "$end_text\n"
        } else {
            ""
        }
    }

    fun add(target: UnitModel): String{
        target.addStatus(effect, duration, value)
        return process(target, start_text)
    }

    fun process(target: UnitModel, add_text: String?): String{
        var result = ""
        var healthChange = 0
        when (effect) {
            "shuffle" -> {
                target.deck?.shuffle()
            }
            "strangle" -> {
                healthChange = value
            }
            "stun" -> {
                target.points = 0
            }
            "taunt" -> {
                target.deck?.addNextMove("engage")
            }
            "maim" -> {
                target.deck?.maim()
            }
            "speed" -> {
                target.points += value
            }
            "damage" -> {
                healthChange = -value
            }
            "heal" -> {
                healthChange = value
            }
            "harden" -> {
                target.barrier = value
            }
        }
        if (add_text != null){
            result += "$add_text\n"
        }
        if (healthChange != 0) {
            if (target.hasStatus("vulnerable")) {
                target.inflict(healthChange * 2)
            } else {
                target.inflict(healthChange)
            }
            result += "${target.getHealthDescription()}\n"
        }
        return target.replaceNouns(result.replace("%v", "$healthChange"))
    }
}

data class MoveModel(val effects: List<Status>, val reqs: List<Status>?, val speed: Int, val desc: String, val text: String): Serializable{
    companion object {
        lateinit var shuffle: MoveModel
        fun getWait(text: String): MoveModel{
            return MoveModel(emptyList(), emptyList(), 1, "", text)
        }
    }

    fun perform(me: UnitModel, everyone: ArrayList<UnitModel>): String{
        if (me.hasStatus("confuse")){
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
                        result += status.add(me)
                    }
                    2 -> {
                        if (me.target != null) {
                            result += when {
                                me.target!!.hasStatus("dodge") -> {
                                    me.target!!.removeStatus("dodge")
                                    "%F dodge%!s."
                                }
                                me.target!!.hasStatus("parry") -> {
                                    me.target!!.removeStatus("parry")
                                    "%F parry the attack!\n" + status.add(me)
                                }
                                else -> {
                                    status.add(me.target!!)
                                }
                            }
                        }
                    }
                    3 -> {
                        result += status.add(me)
                        if (me.target != null) {
                            result += status.add(me.target!!)
                        }
                    }
                    4 -> { //allies
                        for (someone in everyone) {
                            if (someone !in me.foes && someone != me) {
                                result += status.add(someone)
                            }
                        }
                    }
                    5 -> { //foes
                        for (someone in me.foes) {
                            result += status.add(someone)
                        }
                    }
                    6 -> { //all
                        for (someone in everyone) {
                            result += status.add(someone)
                        }
                    }
                    -1 -> {
                        result += me.removeStatus(status.effect)
                    }
                    -2 -> {
                        if (me.target != null) {
                            result += me.target!!.removeStatus(status.effect)
                        }
                    }
                    -3 -> {
                        result += me.removeStatus(status.effect)
                        if (me.target != null) {
                            result += me.target!!.removeStatus(status.effect)
                        }
                    }
                    -4 -> { //allies
                        for (someone in everyone) {
                            if (someone !in me.foes) {
                                result += someone.removeStatus(status.effect)
                            }
                        }
                    }
                    -5 -> { //foes
                        for (enemy in me.foes) {
                            result += enemy.removeStatus(status.effect)
                        }
                    }
                    -6 -> { //all
                        for (someone in everyone) {
                            result += someone.removeStatus(status.effect)
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