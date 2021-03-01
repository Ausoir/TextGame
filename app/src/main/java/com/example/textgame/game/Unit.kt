package com.example.textgame.game

import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class UnitModel(val name: String, var level: Int, val speed: Int, var health: Int,
                     val moves: MutableList<String>, val skills: MutableList<String>, val flavors: HashMap<String, String>): Serializable{
    lateinit var foes: ArrayList<UnitModel>
    lateinit var status: ArrayList<Status>
    lateinit var statusDuration: ArrayList<Int>
    var id = 0
    var points = speed
    var healthPt = 1
    var block = 0
    var position = 0
    var mana = 0
    var target: UnitModel? = null
    var deck: Deck? = null

    fun initialize(moves: MutableList<MoveModel>, handSize: Int){
        deck = Deck(this, moves, handSize)
        deck?.shuffle()
        status = arrayListOf()
        statusDuration = arrayListOf()
        foes = arrayListOf()
    }

    fun recharge(){
        points = speed
    }

    fun copy(): UnitModel{
        return UnitModel(name, level, speed, health, moves.toMutableList(), skills.toMutableList(), HashMap(flavors))
    }

    fun inflict(healthChange: Int): String{
        return replaceNouns(when {
            healthChange < 0 -> {
                var result = ""
                block += healthChange
                if (block < 0) {
                    healthPt += block
                    if (healthChange != block) {
                        result = "%U block%s ${-(healthChange - block)} damage.\n"
                    }
                    result += "%U take%s ${-block} damage!\n${getHealthDescription()}\n"
                    block = 0
                } else {
                    result = "%U block%s ${-healthChange} damage.\n${getHealthDescription()}\n"
                }
                result
            }
            healthChange > 0 -> {
                healthPt += healthChange
                if (healthPt > health) {
                    healthPt = health
                }
                "%U receive%s $healthChange healing!\n${getHealthDescription()}\n"
            }
            else -> {
                ""
            }
        })
    }

    fun getStatus(p: String): Status?{
        for (stat in status){
            if (stat.effect == p){
                return stat
            }
        }
        return null
    }

    fun removeStatus(id: String?, effect: String?): String{
        var result = ""
        when {
            id != null -> {
                do {
                    val feedback = removeStatusById(id)
                    if (feedback != null) {
                        result += "$feedback\n"
                    }
                } while (feedback != null)
            }
            effect != null -> {
                do {
                    val feedback = removeStatusByEffect(effect)
                    if (feedback != null) {
                        result += "$feedback\n"
                    }
                } while (feedback != null)
            }
        }
        return result
    }

    fun removeStatusById(id: String?): String?{
        if (id != null) {
            for (i in status.indices.reversed()) {
                if (status[i].id == id) {
                    val result = status[i].onRemove(this)
                    status.removeAt(i)
                    statusDuration.removeAt(i)
                    return result
                }
            }
        }
        return null
    }

    fun removeStatusByEffect(p: String): String?{
        for (i in status.indices.reversed()){
            if (status[i].effect == p){
                val result = status[i].onRemove(this)
                status.removeAt(i)
                statusDuration.removeAt(i)
                return result
            }
        }
        return null
    }

    fun addStatus(effect: Status, duration: Int){
        status.add(effect)
        statusDuration.add(duration)
    }

    fun processStatus(): String {
        var result = ""
        for (i in status.indices.reversed()){
            result += status[i].process(this, status[i].text)
            if (statusDuration[i] > 0) {
                statusDuration[i] -= 1
            }
            if (statusDuration[i] == 0){
                result += status[i].onRemove(this)
                status.removeAt(i)
                statusDuration.removeAt(i)
            }
        }
        block = 0
        return result
    }

    fun isDead(): Boolean{
        return healthPt <= 0
    }

    fun getHealthPercentage(): Int{
        return ((healthPt / health.toDouble()) * 100).toInt()
    }

    fun getHealthDescription(): String{
        val percentage = getHealthPercentage()
        val result = when {
            percentage <= 0 -> {
                flavors["death"] ?: "%U collapse%s."
            }
            percentage <= 25 -> {
                "%Ur seriously wounded."
            }
            percentage <= 50 -> {
                "%Ur wounded."
            }
            percentage <= 75 -> {
                "%Ur lightly wounded."
            }
            else -> {
                "%Ur healthy."
            }
        }
        return "$result (${getHealthPercentage()}%)"
    }

    fun isReqMet(reqs: List<Status>?): String{
        if (reqs != null) {
            for (req in reqs) {
                if (req.effect == "50") {
                    return listOf(req.text, "").shuffled()[0]!!
                } else if (target == null) {
                    return "INVALID TARGET"
                } else if (req.effect == "melee") {
                    return if ((position == target?.position) == (req.target == 1)){
                        ""
                    } else {
                        "!"
                    }
                } else if (req.effect == "mana") {
                    return if (mana >= req.value){
                        mana -= req.value
                        ""
                    } else {
                        "!"
                    }
                } else {
                    val reqMet = when (req.target) {
                        1 -> {
                            getStatus(req.effect) != null
                        }
                        2 -> {
                            target?.getStatus(req.effect) != null
                        }
                        3 -> {
                            getStatus(req.effect) != null && (target?.getStatus(req.effect) != null)
                        }
                        12 -> {
                            getStatus(req.effect) != null || (target?.getStatus(req.effect) != null)
                        }
                        -1 -> {
                            getStatus(req.effect) == null
                        }
                        -2 -> {
                            target?.getStatus(req.effect) == null
                        }
                        -3 -> {
                            getStatus(req.effect) == null && (target?.getStatus(req.effect) == null)
                        }
                        -12 -> {
                            getStatus(req.effect) == null || (target?.getStatus(req.effect) == null)
                        }
                        else -> {
                            true
                        }
                    }
                    if (!reqMet) {
                        return req.text ?: "!"
                    }
                }
            }
        }
        return ""
    }

    private fun getPronouns(subject: UnitModel): List<String>{
        val name = if ("callname" in subject.flavors){
            subject.flavors["callname"]!!
        } else {
            subject.name
        }

        val thenoun = when {
            name[0].isUpperCase() -> {
                name
            }
            "ally" in subject.flavors -> {
                "your $name"
            }
            else -> {
                "the $name"
            }
        }
        if ("pronoun" in subject.flavors){
            when (subject.flavors["pronoun"]){
                "you" -> {
                    return listOf("s", "", "you are", "your", "yourself", "you are", "your", "you", "you")
                }
                "he" -> {
                    return listOf("", "s", "he is", "his", "himself", "$thenoun is", "$thenoun's", thenoun, "he")
                }
                "she" -> {
                    return listOf("", "s", "she is", "her", "herself", "$thenoun is", "$thenoun's", thenoun, "she")
                }
            }
        }
        return listOf("", "s", "it is", "its", "itself", "$thenoun is", "$thenoun's", thenoun, "it")
    }

    fun getFoeInRange(): ArrayList<UnitModel>{
        val result = arrayListOf<UnitModel>()
        for (foe in foes){
            if (foe.position == position){
                result.add(foe)
            }
        }
        return result
    }

    fun replaceNouns(text: String): String{
        var result = text.replace("%?", "none")
        if (result.contains("%w")){
            val weapon = flavors["weapon"]
            if (weapon != null) {
                result = result.replace("%w", weapon)
            } else {
                result = result.replace("%w", "weapon")
            }
        }
        val pronoun = ArrayList<String>()
        pronoun.addAll(getPronouns(this))
        pronoun.addAll(getPronouns(target ?: this))

        result = if (pronoun[1] == "s") {
            result.replace("%ies", "ies")
        } else {
            result.replace("%ies", "y")
        }
        result = result.replace("%ur", pronoun[2])
        result = result.replace("%us", pronoun[3])
        result = result.replace("%uf", pronoun[4])
        result = result.replace("%Ur", pronoun[5])
        result = result.replace("%Us", pronoun[6])
        result = result.replace("%U", pronoun[7])
        result = result.replace("%u", pronoun[8])

        result = result.replace("%fr", pronoun[11])
        result = result.replace("%fs", pronoun[12])
        result = result.replace("%ff", pronoun[13])
        result = result.replace("%Fr", pronoun[14])
        result = result.replace("%Fs", pronoun[15])
        result = result.replace("%F", pronoun[16])
        result = result.replace("%f", pronoun[17])

        result = result.replace("%!s", pronoun[0])
        result = result.replace("%s", pronoun[1])

        return result.lines().joinToString (separator = "\n") {
            it.capitalize()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + speed
        result = 31 * result + health
        result = 31 * result + moves.hashCode()
        result = 31 * result + flavors.hashCode()
        result = 31 * result + foes.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + points
        result = 31 * result + healthPt
        result = 31 * result + (target?.hashCode() ?: 0)
        result = 31 * result + (deck?.hashCode() ?: 0)
        return result
    }
}

class UnitController(fileManager: FileManager) {
    val model = makeUnit(fileManager.readAsset("unit.json") !!)

    private fun makeUnit(data: String): HashMap<String, UnitModel> {
        val listType = object : TypeToken<HashMap<String, UnitModel>?>() {}.type
        return Gson().fromJson(data, listType) as HashMap<String, UnitModel>
    }
}