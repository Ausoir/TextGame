package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class ItemModel(val type: String, val value: Int, val desc: String, val rating: Int = 0, val moves: List<String>?, val page: String?): Serializable{
    lateinit var name: String
}

class ItemController(fileManager: FileManager) {
    val model = makeItem(fileManager.readAsset("item.json") !!)

    private fun makeItem(data: String): HashMap<String, ItemModel> {
        val listType = object : TypeToken<HashMap<String, ItemModel>?>() {}.type
        val result = Gson().fromJson(data, listType) as HashMap<String, ItemModel>
        for ((key, value) in result){
            value.name = key
        }
        return result
    }

    fun getItems(itemNames: ArrayList<String>): ArrayList<ItemModel?> {
        val result = arrayListOf<ItemModel?>()
        itemNames.forEach { result.add(model[it]) }
        return result
    }
}