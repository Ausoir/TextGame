package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Status(val effect: String, val target: Int, val text: String?, val reqs: List<Status>?): Serializable{
    fun perform(target: UnitModel){
        when (effect) {
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
        var damage = false
        val results = arrayListOf<String>()
        for (status in effects) {
            val reqText = if (status.reqs != null) {
                me.isReqMet(status.reqs)
            } else {
                ""
            }
            if (reqText.isEmpty()){
                if (status.effect == "wounded"){
                    damage = true
                }
                when (status.target) {
                    1 -> {
                        me.status.add(status.effect)
                        status.perform(me)
                        if (status.effect == "run"){
                            return "run"
                        }
                    }
                    2 -> {
                        if (me.target != null) {
                            me.target!!.status.add(status.effect)
                            status.perform(me.target!!)
                        }
                    }
                    3 -> {
                        me.status.add(status.effect)
                        status.perform(me)
                        if (me.target != null) {
                            me.target!!.status.add(status.effect)
                            status.perform(me.target!!)
                        }
                    }
                    4 -> { //allies
                        for (someone in everyone) {
                            if (someone !in me.foes) {
                                someone.status.add(status.effect)
                                status.perform(someone)
                            }
                        }
                    }
                    5 -> { //foes
                        for (someone in me.foes) {
                            someone.status.add(status.effect)
                            status.perform(someone)
                        }
                    }
                    6 -> { //all
                        for (someone in everyone) {
                            someone.status.add(status.effect)
                            status.perform(someone)
                        }
                    }
                    -1 -> {
                        me.status.removeAll(Collections.singleton(status.effect))
                    }
                    -2 -> {
                        me.target?.status?.removeAll(Collections.singleton(status.effect))
                    }
                    -3 -> {
                        me.status.removeAll(Collections.singleton(status.effect))
                        me.target?.status?.removeAll(Collections.singleton(status.effect))
                    }
                    -4 -> { //allies
                        for (someone in everyone) {
                            if (someone !in me.foes) {
                                someone.status.removeAll(Collections.singleton(status.effect))
                            }
                        }
                    }
                    -5 -> { //foes
                        for (enemy in me.foes) {
                            enemy.status.removeAll(Collections.singleton(status.effect))
                        }
                    }
                    -6 -> { //all
                        for (someone in everyone) {
                            someone.status.removeAll(Collections.singleton(status.effect))
                        }
                    }
                }
                if (!status.text.isNullOrEmpty()) {
                    results.add(status.text)
                }
            } else {
                results.add(reqText)
            }
        }
        if (results.isEmpty()){
            return ""
        }
        var result = results.joinToString {
            if (results.size > 1 && it == results.last()) {
                "and $it"
            } else {
                it
            }
        }
        result = "$result.\n"
        if (damage){
            result += "%f ${me.target!!.getHealth()}.\n"
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