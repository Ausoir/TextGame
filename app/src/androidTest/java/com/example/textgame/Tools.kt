package com.example.textgame

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.example.textgame.game.Game
import org.junit.Assert.assertEquals
import org.junit.Test

class Tools {
    private val logID = "log2"
    private val knownTags = listOf("time", "timeset", "heal", "view rest", "dropAll", "removeAll", "view quest", "view forget")
    private val chainTags = listOf("encounter", "join", "drop", "view advance", "view shop", "combat", "loadplayer", "saveplayer", "questFill",
        "add", "save", "remove part", "item", "force item", "learn", "if", "refresh chapter", "remove item", "custom", "moneySet", "moneyAdd")
    private val errors = arrayListOf<String>()
    private val history = arrayListOf<String>()

    @Test
    fun reset_save() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val game = Game(appContext)
        game.player.load("player")
        game.player.save("save")
    }

    @Test
    fun modify_save() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val game = Game(appContext)
        game.player.load("save")
        game.player.model.vars["page"] = "@ant hill good"
        game.player.model.vars["chapter"] = "5"
        game.player.model.damaged = 0
        game.player.save("save")
    }

    @Test
    fun validate_chapter() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val game = Game(appContext)
        game.player.model.vars["chapter"] = "5"
        game.refreshChapter()
        recurse_dialog(game, "start")
        Log.d(logID, "---- VALIDATION COMPLETE. LISTING ERRORS ----")
        for (error in errors){
            Log.d(logID, error)
        }
    }

    private fun recurse_dialog(game: Game, page: String){
        if (history.count{ it == page } > 1) {
//            Log.d(logID, "RETURNED TO PAGE: $page. ABORTING")
        } else {
            history.add(page)
            Log.d(logID, "PAGE: $page")
            assertEquals(false, page == "invalid")
            assertEquals(false, page.contains("%"))
            val inChapter = page in game.dialog.model
            val inCommon = page in game.dialog.model2
            if (!inChapter && !inCommon){
                Log.d(logID, "INVALID PAGE: $page")
                errors.add("INVALID PAGE: $page")
            } else {
                val currentPage = if (inChapter) {
                    game.dialog.model[page]!!
                } else {
                    game.dialog.model2[page]!!
                }
                val args = game.getVarMap(currentPage.filler)
                val dialog = currentPage.fillVariables(args)
                assertEquals(false, dialog.text.contains("%"))
                for (opt in dialog.opt) {
                    Log.d(logID, "OPT: ${opt.to}")
                    val tags = opt.to.split(">")
                    for (i in tags.indices){
                        if (tags[i].startsWith("@")) {
                            recurse_dialog(game, tags[i].substring(1))
                        } else if (tags[i].isNotEmpty() && tags[i] !in knownTags && tags[i] !in chainTags && tags[i].toIntOrNull() == null){
                            if (i < 1){
                                Log.d(logID, "UNKNOWN SINGLE TAG: ${tags[i]}")
                                errors.add("UNKNOWN SINGLE TAG: ${tags[i]}")
                            } else if (tags[i - 1] !in chainTags){
                                Log.d(logID, "UNKNOWN TAG: ${tags[i]}")
                                errors.add("UNKNOWN TAG: ${tags[i]}")
                            }
                        }
                    }
                }
            }
        }
    }
}