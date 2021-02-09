package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Status(val effect: String, val target: Int, val value: Int?, val text: String?, val reqs: List<Status>?): Serializable{
    fun perform(target: UnitModel): String{
        var result = ""
        var health = ""
        var textValue = 0
        when (effect) {
            "wounded" -> {
                textValue = value ?: 1
                target.chealth -= textValue
                val healthDesc = target.getHealthDescription()
                if (healthDesc.isNotEmpty()) {
                    health = "$healthDesc\n"
                }
            }
            "stunned" -> {
                target.points = 0
            }
            "dazed" -> {
                target.points -= 1
            }
            "shuffle" -> {
                target.deck?.shuffle()
            }
        }
        if (text != null){
            result += if (textValue > 0){
                "${text.replace("%v", "$textValue")}\n"
            } else {
                "$text\n"
            }
        }
        result += health
        return result
    }

    fun unperform(target: UnitModel, effect: String): String{
        if (effect in target.status && text != null){
            return "$text\n"
        }
        return ""
    }
}

data class MoveModel(val effects: List<Status>, val reqs: List<Status>, val speed: Int, val desc: String, val text: String): Serializable{
    companion object {
        lateinit var shuffle: MoveModel
        fun getWait(text: String): MoveModel{
            return MoveModel(emptyList(), emptyList(), 1, "", text)
        }
    }

    fun getValidTargets(me: UnitModel): ArrayList<UnitModel>{
        val validList = ArrayList<UnitModel>(me.foes)
        for (req in reqs){
            val targets = me.getTargetables(req.target, req.effect)
            if (targets.isNotEmpty()){
                for (i in validList.indices.reversed()){
                    if (validList[i] !in targets){
                        validList.removeAt(i)
                    }
                }
            }
        }
        return validList
    }

    fun perform(me: UnitModel, everyone: ArrayList<UnitModel>): String{
        var result = ""
        for (status in effects) {
            val reqText = if (status.reqs != null) {
                me.isReqMet(status.reqs)
            } else {
                ""
            }
            if (reqText.isEmpty()){
                when (status.target) {
                    1 -> {
                        me.status.add(status.effect)
                        result += status.perform(me)
                        if (status.effect == "run"){
                            return "run"
                        }
                    }
                    2 -> {
                        if (me.target != null) {
                            me.target!!.status.add(status.effect)
                            result += status.perform(me.target!!)
                        }
                    }
                    3 -> {
                        me.status.add(status.effect)
                        result += status.perform(me)
                        if (me.target != null) {
                            me.target!!.status.add(status.effect)
                            result += status.perform(me.target!!)
                        }
                    }
                    4 -> { //allies
                        for (someone in everyone) {
                            if (someone !in me.foes) {
                                someone.status.add(status.effect)
                                result += status.perform(someone)
                            }
                        }
                    }
                    5 -> { //foes
                        for (someone in me.foes) {
                            someone.status.add(status.effect)
                            result += status.perform(someone)
                        }
                    }
                    6 -> { //all
                        for (someone in everyone) {
                            someone.status.add(status.effect)
                            result += status.perform(someone)
                        }
                    }
                    -1 -> {
                        result += status.unperform(me, status.effect)
                        me.status.removeAll(Collections.singleton(status.effect))
                    }
                    -2 -> {
                        if (me.target != null) {
                            result += status.unperform(me.target!!, status.effect)
                            me.target!!.status.removeAll(Collections.singleton(status.effect))
                        }
                    }
                    -3 -> {
                        result += status.unperform(me, status.effect)
                        me.status.removeAll(Collections.singleton(status.effect))
                        if (me.target != null) {
                            result += status.unperform(me.target!!, status.effect)
                            me.target?.status?.removeAll(Collections.singleton(status.effect))
                        }
                    }
                    -4 -> { //allies
                        for (someone in everyone) {
                            if (someone !in me.foes) {
                                result += status.unperform(someone, status.effect)
                                someone.status.removeAll(Collections.singleton(status.effect))
                            }
                        }
                    }
                    -5 -> { //foes
                        for (enemy in me.foes) {
                            result += status.unperform(enemy, status.effect)
                            enemy.status.removeAll(Collections.singleton(status.effect))
                        }
                    }
                    -6 -> { //all
                        for (someone in everyone) {
                            result += status.unperform(someone, status.effect)
                            someone.status.removeAll(Collections.singleton(status.effect))
                        }
                    }
                }
            } else {
                result += reqText
            }
        }
        return result
    }
}

class MoveController(fileManager: FileManager) {
    val model: HashMap<String, MoveModel> = makeItem(fileManager.readAsset("moveset.json") !!)

    fun makeItem(data: String): HashMap<String, MoveModel> {
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