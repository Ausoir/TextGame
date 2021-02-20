package com.example.textgame.game

object Requirement{
    fun checkList(model: PlayerModel, reqList: Array<String>?): Boolean{
        if (reqList != null) {
            for (req in reqList) {
                if (!check(model, req)) {
                    return false
                }
            }
        }
        return true
    }

    private fun check(model: PlayerModel, req: String): Boolean{
        var reverse = false
        val split = if (req.startsWith("!")){
            reverse = true
            req.substring(1).split(">")
        } else {
            req.split(">")
        }
        val result = when {
            split[0] == model.getTime() -> {
                true
            }
            split[0] == "seed" -> {
                model.seed % split[1].toInt() == 0
            }
            split[0] == "daytime" -> {
                model.getTime() in listOf("early morning", "morning", "evening")
            }
            split[0] == "nighttime" -> {
                model.getTime() in listOf("night", "late night")
            }
            split[0] == "skill>" -> {
                split[1] in model.unit.skills
            }
            split[0] == "item" -> {
                split[1] in model.equip
            }
            split[0] == "ally" -> {
                split[1] in model.companion
            }
            split[0] == "var" -> {
                model.vars[split[1]] == split[2]
            }
            split[0] == "hurt" -> {
                model.damaged > split[1].toInt()
            }
            split[0] == "time" -> {
                model.time < split[1].toInt()
            }
            split[0] == "time" -> {
                model.time > split[1].toInt()
            }
            split[0] == "time" -> {
                model.time <= split[1].toInt()
            }
            split[0] == "time" -> {
                model.time >= split[1].toInt()
            }
            split[0] == "money" -> {
                model.money >= split[1].toInt()
            }
            split[0] == "money" -> {
                model.money <= split[1].toInt()
            }
            split[0] == "money" -> {
                model.money < split[1].toInt()
            }
            split[0] == "money" -> {
                model.money > split[1].toInt()
            }
            else -> {
                false
            }
        }
        return if (reverse){
            !result
        } else {
            result
        }
    }
}