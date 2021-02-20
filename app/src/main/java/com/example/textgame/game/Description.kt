package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DescriptionController(fileManager: FileManager) {
    val model = makeItem(fileManager.readAsset("description.json") !!)

    fun makeItem(data: String): HashMap<String, String> {
        val listType = object : TypeToken<HashMap<String, String>?>() {}.type
        return Gson().fromJson(data, listType) as HashMap<String, String>
    }
}