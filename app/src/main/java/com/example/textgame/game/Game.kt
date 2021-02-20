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
    val descriptions: DescriptionController = DescriptionController(fileManager)

    private var varMap = hashMapOf("%time" to player.model.getTime()
    )

    init {
        val shopItem = variables.getRandom("Ant Hill Shop", player.model.seed + player.model.time)
        varMap["%AntHillShopItem"] = shopItem
        varMap["%AntHillShopValue"] = "${item.model[shopItem]?.value}"
        player.model.unit.healthPt = player.model.unit.health - player.model.damaged
    }

    fun refreshChapter(){
        dialog = DialogController(fileManager, player.model.vars["chapter"]!!)
    }

    fun getVarMap(vars: List<String>?): ArrayList<String?>{
        if (vars == null){
            return ArrayList()
        }
        val result = ArrayList<String?>()
        for (v in vars){
            if (v == "%temp"){
                result.add(player.model.vars["temp"] ?: "")
            } else {
                result.add(varMap[v])
            }
        }
        return result
    }

    fun getSpecialMove(): MoveModel{
        val spec = special.model[player.model.specialization] ?: special.model["basic"]!!
        return moves.getMoves(listOf(spec.move))[0]
    }

    fun getNewMoves(): Array<String>{
        val spec = special.model[player.model.specialization] ?: special.model["basic"]!!
        val moves = variables.model[spec.deck]?.shuffled() ?: listOf("shuffle")
        return arrayOf(moves[0], moves[1], moves[2])
    }
}