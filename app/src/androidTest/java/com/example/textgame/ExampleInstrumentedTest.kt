package com.example.textgame

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.textgame.game.Game
import com.example.textgame.tools.Grammar
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private val logID = "console6"
    @Test
    fun validate_chapter() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val game = Game(appContext)
        game.player.model.vars["chapter"] = "1"
        game.refreshChapter()
        recurse_dialog(game, arrayListOf(), "start")
    }

    private fun recurse_dialog(game: Game, history: MutableList<String>, page: String){
        if (history.count{ it == page } > 1) {
            Log.d(logID, "RETURNED TO PAGE: $page. ABORTING")
        } else {
            history.add(page)
            Log.d(logID, "PAGE: $page")
            assertEquals(false, page == "invalid")
            assertEquals(false, page.contains("%"))
            if (page !in game.dialog.model){
                Log.d(logID, "INVALID PAGE: $page. ABORTING")
            } else {
                val currentPage = game.dialog.model[page]!!
                val args = game.getVarMap(currentPage.filler)
                val dialog = currentPage.fillVariables(args).validate(game.player.model)
                assertEquals(false, dialog.text.contains("%"))
                for (opt in dialog.opt) {
                    assertEquals(false, opt.text.contains("%"))
                    Log.d(logID, "OPT: ${opt.to}")
                    recurse_dialog(game, history.toMutableList(), getDialogPage(opt.to))
                }
            }
        }
    }

    private fun getDialogPage(to: String): String{
        if (to.startsWith(">")) {
            val split = to.split(">")
            val index = when (split[1]) {
                "custom" -> {
                    3
                }
                "save" -> {
                    4
                }
                "load" -> {
                    -1
                }
                "if" -> {
                    5
                }
                "back" -> {
                    -1
                }
                "time" -> {
                    3
                }
                "timeset" -> {
                    3
                }
                "defeat" -> {
                    2
                }
                "encounter" -> {
                    4
                }
                "combat" -> {
                    4
                }
                "join" -> {
                    3
                }
                "drop" -> {
                    3
                }
                "learn" -> {
                    3
                }
                "forget" -> {
                    3
                }
                "specialize" -> {
                    3
                }
                "view shop" -> {
                    3
                }
                "view learn" -> {
                    2
                }
                "view forget" -> {
                    2
                }
                "view advance" -> {
                    3
                }
                "heal" -> {
                    2
                }
                "item" -> {
                    3
                }
                "trash" -> {
                    3
                }
                "refresh chapter" -> {
                    2
                }
                "reset" -> {
                    2
                } else -> {
                    -1
                }
            }
            if (index == -1){
                return "invalid"
            }
            return getDialogPage(Grammar.substringAfter(to, ">", index))
        } else {
            return to
        }
    }
}