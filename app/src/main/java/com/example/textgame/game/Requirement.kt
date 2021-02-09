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
        return when {
            req == model.getTime() -> {
                true
            }
            req == "daytime" -> {
                model.getTime() in listOf("early morning", "morning", "evening")
            }
            req == "nighttime" -> {
                model.getTime() in listOf("night", "late night")
            }
            req == "item" -> {
                model.item.isNotEmpty()
            }
            req == "!item" -> {
                model.item.isEmpty()
            }
            req.startsWith("var:") -> {
                val split = req.split(":")
                model.vars[split[1]] == split[2]
            }
            req.startsWith("seed%") -> {
                model.seed % req.substringAfter("seed%").toInt() == 0
            }
            req.startsWith("money>=") -> {
                model.money >= req.substringAfter("money>=").toInt()
            }
            req.startsWith("money<=") -> {
                model.money <= req.substringAfter("money<=").toInt()
            }
            req.startsWith("money<") -> {
                model.money < req.substringAfter("money<").toInt()
            }
            req.startsWith("money>") -> {
                model.money > req.substringAfter("money>").toInt()
            }
            else -> {
                false
            }
        }
    }
}