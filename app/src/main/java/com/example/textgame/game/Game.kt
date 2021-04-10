package com.example.textgame.game

import android.content.Context
import com.example.textgame.tools.FileManager

class Game(context: Context){
    private val fileManager: FileManager = FileManager(context)
    val player: Player = Player(fileManager)
    val special: SpecialController = SpecialController(fileManager)
    var dialog: DialogController = DialogController(fileManager, player.model.vars["chapter"]!!)
    val item: ItemController = ItemController(fileManager)
    val moves: MoveController = MoveController(fileManager)
    val units: UnitController = UnitController(fileManager)
    val encounters: EncounterController = EncounterController(fileManager)
    val variables: VariableController = VariableController(fileManager)

    private var varMap = hashMapOf("%time" to player.model.getTime()
    )

    init {
        if (player.model.seed == -1){
            player.model.seed = (0..9999).random()
        }
        player.model.unit.healthPt = player.model.unit.health - player.model.damaged
        player.model.unit.allies = arrayListOf()
        player.model.unit.foes = arrayListOf()
    }

    fun refreshChapter(){
        dialog = DialogController(fileManager, player.model.vars["chapter"]!!)
        player.model.unit.healthPt = player.model.unit.health
        player.model.damaged = 0
    }

    fun getVarMap(vars: List<String>?): ArrayList<String>{
        if (vars == null){
            return ArrayList()
        }
        val result = ArrayList<String>()
        for (v in vars){
            result.add(getCurrentVar(v))
        }
        return result
    }

    private fun getCurrentVar(variable: String): String{
        if (variable.startsWith("%?")){
            return player.model.vars[variable.substringAfter("%?")] ?: ""
        }
        return when (variable) {
            "%time" -> {
                player.model.getTime()
            }
            "%daynight" -> {
                if (player.model.isDay()){
                    "day"
                } else {
                    "night"
                }
            }
            "%rand2" -> {
                "" + (1..2).random()
            }
            "%rand3" -> {
                "" + (1..3).random()
            }
            "%trand2" -> {
                "${(player.model.seed + player.model.time) % 2 + 1}"
            }
            "%trand3" -> {
                "${(player.model.seed + player.model.time) % 3 + 1}"
            }
            else -> {
                varMap[variable] ?: ""
            }
        }
    }

    fun getNewMoves(): Array<String>{
        val spec = special.model[player.model.specialization] ?: special.model["invalid"]!!
        val moves = variables.model[spec.deck]?.shuffled()?.toMutableList() ?: mutableListOf("shuffle")
        moves.addAll(variables.model["common deck"]?.shuffled()?.toMutableList() ?: mutableListOf())
        return arrayOf(moves[0], moves[1], moves[2])
    }
}