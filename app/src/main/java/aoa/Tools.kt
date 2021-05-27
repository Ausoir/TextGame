package aoa

import android.util.Log

import aoa.game.Game

@Suppress("unused")
object Tools {
    private const val logID = "log1"
    private val knownTags = listOf("time", "timeset", "heal", "view rest", "dropAll", "removeAll", "view quest", "view forget", "ad")
    private val chainTags = listOf("encounter", "join", "drop", "view advance", "view shop", "combat", "loadplayer", "saveplayer", "questFill",
        "add", "save", "remove part", "item", "force item", "learn", "if", "refresh chapter", "remove item", "custom", "moneySet", "moneyAdd",
            "view gem", "view specialize", "forget")
    private val errors = arrayListOf<String>()
    private val history = arrayListOf<String>()

    fun validateChapters(game: Game){
        for (i in arrayOf("0", "1", "2", "3", "4", "5")) {
            Log.d(logID, "---- CHAPTER $i ----")
            validateChapter(game, i)
        }
    }

    fun validateChapter(game: Game, chapter: String) {
        history.clear()
        errors.clear()
        game.player.model.vars["chapter"] = chapter
        game.refreshChapter()
        recurse_dialog(game, "start")
        Log.d(logID, "---- VALIDATION COMPLETE. LISTING ERRORS ----")
        for (error in errors){
            Log.d(logID, error)
        }
    }

    private fun assert(value: Boolean, text: String){
        if (value){
            Log.d(logID, text)
        }
    }
    private fun recurse_dialog(game: Game, page: String){
        if (history.count{ it == page } < 2) {
            history.add(page)
            assert(page == "invalid", "Page invalid")
            this.assert(page.contains("%"), "Page has %")
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
                this.assert(dialog.text.contains("%"), "Text has %")
                for (opt in dialog.opt) {
                    val tags = opt.to.split(">>")
                    for (i in tags.indices){
                        var asdf = tags[i]
                        if (i != 0 || asdf.startsWith("@")){
                            asdf = ">$asdf"
                        }
                        val split2 = asdf.substring(1).split(">")
                        if (!split2[0].startsWith("@")) {
                            if (split2[0] !in knownTags && split2[0] !in chainTags) {
                                if (i < 1) {
                                    errors.add("UNKNOWN SINGLE TAG: ${split2[0]}")
                                } else if (tags[i - 1] !in chainTags) {
                                    errors.add("UNKNOWN TAG: ${split2[0]}")
                                }
                            } else {
                                if (split2[0] == "encounter" && split2[1] !in game.encounters.model) {
                                    errors.add("UNKNOWN ENCOUNTER: ${split2[1]}")
                                } else if (split2[0] == "item" && split2[1] !in game.item.model) {
                                    errors.add("UNKNOWN ITEM: ${split2[1]}")
                                } else if (split2[0] == "force item" && split2[1] !in game.item.model) {
                                    errors.add("UNKNOWN ITEM: ${split2[1]}")
                                } else if (split2[0] == "combat" && split2[1] !in game.variables.model) {
                                    errors.add("UNKNOWN UNIT: ${split2[1]}")
                                } else if (split2[0] == "view shop" && split2[1] !in game.variables.model) {
                                    errors.add("UNKNOWN SHOP: ${split2[1]}")
                                } else if (split2[0] == "view gem" && split2[1] !in game.variables.model) {
                                    errors.add("UNKNOWN GEM SHOP: ${split2[1]}")
                                } else if (split2[0] == "join" && split2[1] !in game.units.model) {
                                    errors.add("UNKNOWN JOIN: ${split2[1]}")
                                } else if (split2[0] == "drop" && split2[1] !in game.units.model) {
                                    errors.add("UNKNOWN DROP: ${split2[1]}")
                                }
                            }
                        }
                        if (split2.size > 2){
                            if (split2.size == 4 && split2[0] !in arrayOf("add", "save", "encounter", "combat")){
                                errors.add("LONG TAG (4): $split2")
                            } else if (split2.size == 5 && split2[0] != "if"){
                                errors.add("LONG TAG (5): $split2")
                            } else if (split2.size > 4){
                                errors.add("LONG TAG: $split2")
                            }
                        }
                        for (ii in split2){
                            if (ii.startsWith("@")) {
                                recurse_dialog(game, ii.substring(1))
                            }
                        }
                    }
                }
            }
        }
    }
}