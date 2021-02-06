package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class ItemModel(val type: String, val value: Int, val desc: String, val moves: List<String>?, val page: String?): Serializable

class ItemController(fileManager: FileManager) {
    val model = makeItem(fileManager.readAsset("item.json") !!)

    fun makeItem(data: String): HashMap<String, ItemModel> {
        val listType = object : TypeToken<HashMap<String, ItemModel>?>() {}.type
        val result: HashMap<String, ItemModel> = Gson().fromJson(data, listType)
        return result
    }
}