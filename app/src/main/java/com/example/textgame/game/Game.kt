package com.example.textgame.game

import android.content.Context
import com.example.textgame.tools.FileManager

class Game(context: Context){
    private val fileManager: FileManager = FileManager(context)
    val player: Player = Player(fileManager)
    var dialog: DialogController = DialogController(fileManager, player.model.vars["chapter"]!!)
    val item: ItemController = ItemController(fileManager)
    val moves: MoveController = MoveController(fileManager)
    val units: UnitController = UnitController(fileManager)
    val encounters: EncounterController = EncounterController(fileManager)
    val variables: VariableController = VariableController(fileManager)
    private var varMap = hashMapOf("%time" to player.model.getTime(),
            "%itemValue" to "${item.model[player.model.item]?.value}", "%item" to player.model.item
    )

    init {
        val shopItem = variables.getRandom("Ant Hill Shop", player.model.seed + player.model.time)
        varMap["%AntHillShopItem"] = shopItem
        varMap["%AntHillShopValue"] = "${item.model[shopItem]?.value}"
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
            result.add(varMap[v])
        }
        return result
    }
}