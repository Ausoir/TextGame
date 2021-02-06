package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class UnitModel(val name: String, val speed: Int, val moves: MutableList<String>,
                     val status: ArrayList<String>, val health: List<String>): Serializable{
    lateinit var foes: ArrayList<UnitModel>
    var points = speed
    var target: UnitModel? = null
    var deck: Deck? = null

    fun initialize(moves: ArrayList<MoveModel>, handSize: Int){
        deck = Deck(this, moves, handSize)
        deck?.shuffle()
    }

    fun recharge(){
        points = speed
    }

    fun copy(): UnitModel{
        return UnitModel(name, speed, moves.toMutableList(), ArrayList(status), health.toMutableList())
    }

    fun processStatus() {
        status.removeAll(listOf("dodge"))
    }

    fun isDead(): Boolean{
        return status.count{ it == "wounded" } == health.size
    }

    fun getHealth(): String{
        val wounds = status.count{ it == "wounded" } - 1
        return if (wounds < health.size) {
            health[wounds]
        } else {
            health.last()
        }
    }

    private fun getFoesWithStatus(effect: String, has: Boolean = true): ArrayList<UnitModel>{
        val valids = ArrayList<UnitModel>()
        for (foe in foes){
            if (effect in foe.status == has){
                valids.add(foe)
            }
        }
        return valids
    }

    fun getTargetables(target: Int, effect: String): List<UnitModel>{
        return when (target) {
            1 -> {
                if (status.contains(effect)){
                    foes
                } else {
                    emptyList()
                }
            }
            2 -> {
                getFoesWithStatus(effect)
            }
            3 -> {
                if (status.contains(effect)){
                    getFoesWithStatus(effect)
                } else {
                    emptyList()
                }
            }
            -1 -> {
                if (!status.contains(effect)){
                    foes
                } else {
                    emptyList()
                }
            }
            -2 -> {
                getFoesWithStatus(effect, false)
            }
            -3 -> {
                if (!status.contains(effect)){
                    getFoesWithStatus(effect, false)
                } else {
                    emptyList()
                }
            }
            else -> {
                emptyList()
            }
        }
    }

    fun isReqMet(reqs: List<Status>): String{
        for (req in reqs){
            if (req.effect == "50"){
                return listOf(req.text, "").shuffled()[0]!!
            }
            val reqMet = getTargetables(req.target, req.effect)
            if (reqMet.isEmpty()){
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