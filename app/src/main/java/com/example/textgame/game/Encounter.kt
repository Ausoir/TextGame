package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class EncounterModel(val desc: String, val units: ArrayList<String>, val loots: ArrayList<String>): Serializable{
    fun getUnits(unitDict: HashMap<String, UnitModel>): ArrayList<UnitModel>{
        val enemies = ArrayList<UnitModel>()
        for (unit in units){
            val unitData = unitDict[unit] ?: unitDict["invalid"]!!
            enemies.add(unitData.copy())
        }
        return enemies
    }
}

class EncounterController(fileManager: FileManager) {
    val model = makeEncounter(fileManager.readAsset("encounter.json") !!)

    fun makeEncounter(data: String): HashMap<String, EncounterModel> {
        val listType = object : TypeToken<HashMap<String, EncounterModel>?>() {}.type
        val result: HashMap<String, EncounterModel> = Gson().fromJson(data, listType)
        return result
    }
}