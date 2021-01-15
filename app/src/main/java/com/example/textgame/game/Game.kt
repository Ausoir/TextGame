package com.example.textgame.game

import android.content.Context
import com.example.textgame.tools.FileManager

class Game(context: Context){
    private val fileManager: FileManager = FileManager(context)
    val player: Player = Player(fileManager)
}