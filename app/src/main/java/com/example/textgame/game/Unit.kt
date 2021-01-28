package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class UnitModel(val speed: Int, val desc: String, val moves: MutableList<String>, val status: ArrayList<String>,
                     val health: List<String>, val loot: List<String>): Serializable{
    var points = speed
    var target: UnitModel? = null
    lateinit var deck: Deck

    fun initialize(moves: ArrayList<MoveModel>, handSize: Int){
        deck = Deck(this, moves, handSize)
        deck.shuffle()
    }

    fun recharge(){
        points = speed
    }

    fun copy(): UnitModel{
        return UnitModel(speed, desc, moves.toMutableList(), ArrayList(status), health.toMutableList(), loot.toMutableList())
    }

    fun getHealth(): String{
        val wounds = status.count{ it == "wounded" } - 1
        return if (wounds < health.size) {
            health[wounds]
        } else {
            "dead"
        }
    }

    fun isReqMet(reqs: List<Status>): String{
        for (req in reqs){
            val reqMet = when (req.target) {
                1 -> {
                    status.contains(req.effect)
                }
                2 -> {
                    target?.status?.contains(req.effect) ?: false
                }
                3 -> {
                    status.contains(req.effect) && (target?.status?.contains(req.effect) ?: false)
                }
                -1 -> {
                    !status.contains(req.effect)
                }
                -2 -> {
                    !(target?.status?.contains(req.effect) ?: false)
                }
                -3 -> {
                    !(status.contains(req.effect) && (target?.status?.contains(req.effect) ?: false))
                }
                else -> {
                    true
                }
            }
            if (!reqMet){
                return req.text ?: "!"
            }
        }
        return ""
    }
}

class UnitController(fileManager: FileManager) {
    val model = makeUnit(fileManager.readAsset("unit.json") !!)

    private fun makeUnit(data: String): HashMap<String, UnitModel> {
        val listType = object : TypeToken<HashMap<String, UnitModel>?>() {}.type
        return Gson().fromJson(data, listType) as HashMap<String, UnitModel>
    }
}