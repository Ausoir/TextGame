package com.example.textgame.game

import android.content.Context
import com.example.textgame.tools.FileManager

class Game(context: Context){
    private val fileManager: FileManager = FileManager(context)
    val player: Player = Player(fileManager)
    val dialog: DialogController = DialogController(fileManager)
    val item: ItemController = ItemController(fileManager)
    val moves: MoveController = MoveController(fileManager)
    val units: UnitController = UnitController(fileManager)

    fun getValidDialog(): DialogModel{
        val currentPage = dialog.model[player.model.page] ?: dialog.model["invalid"]!!
        val validOptions = arrayListOf<DialogOptionModel>()
        for (option in currentPage.opt){
            if (Requirement.checkList(player.model, option.req)){
                validOptions.add(option)
            }
        }
        return DialogModel(currentPage.title, currentPage.text, validOptions)
    }

//    fun getCustomDialog(text: String, options: ArrayList<String>): DialogModel{
//        val optionModels = ArrayList<DialogOptionModel>(options.size)
//        for (i in options.indices){
//            optionModels[i] = DialogOptionModel(to, text, req)
//        }
//        return DialogModel("", text, validOptions)
//    }
}