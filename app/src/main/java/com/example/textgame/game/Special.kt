package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class SpecialModel(val text: String?, val flavor: String, val skill: String?,
                        val deck: String, val health: Int?, val branch: List<String>): Serializable{
    fun getDescription(name: String): String{
        var result = "The $name. Learn from the $deck."
        if (text != null){
            result += " $text"
        }
        if (health != null){
            result += " Health: $health"
        }
        return result
    }

    fun apply(player: PlayerModel){
        if (health != null){
            player.unit.health += health
            player.unit.healthPt += health
        }
        if (skill != null){
            if (skill !in player.unit.skills){
                player.unit.skills.add(skill)
            }
        }
    }

    fun unapply(player: PlayerModel){
        if (health != null){
            player.unit.health -= health
            player.unit.healthPt -= health
        }
        if (skill != null){
            if (skill in player.unit.skills){
                player.unit.skills.remove(skill)
            }
        }
    }
}

class SpecialController(fileManager: FileManager) {
    val model = makeSpecial(fileManager.readAsset("specialization.json") !!)

    private fun makeSpecial(data: String): HashMap<String, SpecialModel> {
        val listType = object : TypeToken<HashMap<String, SpecialModel>?>() {}.type
        return Gson().fromJson(data, listType) as HashMap<String, SpecialModel>
    }

    fun change(player: PlayerModel, from: String, to: String){
        model[from]?.unapply(player)
        player.specialization = to
        model[to]?.apply(player)
    }
}