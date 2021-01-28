package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class Status(val effect: String, val target: Int, val text: String?, val req: List<Status>?): Serializable{
    fun perform(target: UnitModel){
        when (effect) {
            "stunned" -> {
                target.points = 0
            }
            "dazed" -> {
                target.points -= 1
            }
            "shuffle" -> {
                target.deck.shuffle()
            }
        }
    }
}

data class MoveModel(val effects: List<Status>, val req: List<Status>, val speed: Int, val desc: String, val text: String): Serializable{
    companion object {
        lateinit var shuffle: MoveModel
        fun getWait(text: String): MoveModel{
            return MoveModel(emptyList(), emptyList(), 1, "", text)
        }
    }

    fun perform(me: UnitModel): String{
        var damage = false
        val results = arrayListOf<String>()
        for (status in effects) {
            val reqText = if (status.req != null) {
                me.isReqMet(status.req)
            } else {
                ""
            }
            if (reqText.isEmpty()){
                when (status.target) {
                    1 -> {
                        me.status.add(status.effect)
                        status.perform(me)
                    }
                    2 -> {
                        if (status.effect == "wounded"){
                            damage = true
                        }
                        if (me.target != null) {
                            me.target!!.status.add(status.effect)
                            status.perform(me.target!!)
                        }
                    }
                    3 -> {
                        if (status.effect == "wounded"){
                            damage = true
                        }
                        me.status.add(status.effect)
                        status.perform(me)
                        if (me.target != null) {
                            me.target!!.status.add(status.effect)
                            status.perform(me.target!!)
                        }
                    }
                    -1 -> {
                        me.status.remove(status.effect)
                    }
                    -2 -> {
                        me.target?.status?.remove(status.effect)
                    }
                    -3 -> {
                        me.status.remove(status.effect)
                        me.target?.status?.remove(status.effect)
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
        if (damage){
            val health = me.target!!.getHealth()
            if (health == "dead"){
                return health
            }
            result += ".\n%f ${health}."
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