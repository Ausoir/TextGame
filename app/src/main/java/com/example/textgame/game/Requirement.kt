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
        return when (req) {
            model.getTime() -> {
                true
            }
            "daytime" -> {
                model.getTime() in listOf("early morning", "morning", "evening")
            }
            "nighttime" -> {
                model.getTime() in listOf("night", "late night")
            }
            else -> {
                false
            }
        }
    }
}