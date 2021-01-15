package com.example.textgame.game

import android.content.Context
import com.example.textgame.tools.FileManager
import org.json.JSONObject

class DataAccess(context: Context){
    private val fileManager = FileManager(context)

    fun getPage(page: String): JSONObject{
        val dialog = JSONObject(fileManager.readAsset("dialog.json") ?: "")
        return dialog.getJSONObject(page)
    }

    fun getMoveset(page: String): JSONObject{
        val dialog = JSONObject(fileManager.readAsset("moveset.json") ?: "")
        return dialog.getJSONObject(page)
    }
}