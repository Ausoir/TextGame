package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

class VariableController(fileManager: FileManager) {
    val model = makeVariable(fileManager.readAsset("variable.json") !!)

    fun makeVariable(data: String): HashMap<String, ArrayList<String>> {
        val listType = object : TypeToken<HashMap<String, ArrayList<String>>?>() {}.type
        val result: HashMap<String, ArrayList<String>> = Gson().fromJson(data, listType)
        return result
    }

    fun getRandom(index: String, seed: Int = -1): String{
        val variables = model[index]
        return if (variables != null && variables.isNotEmpty()){
            if (seed == -1){
                variables.shuffled()[0]
            } else {
                variables[seed % variables.size]
            }
        } else {
            "invalid"
        }
    }

    fun getRandomList(index: String): MutableList<String>{
        val variables = model[index] ?: return arrayListOf()
        val result = variables.shuffled()
        return result.subList(0, (1 until result.size).random()).toMutableList()
    }
}