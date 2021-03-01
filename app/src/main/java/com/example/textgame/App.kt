package com.example.textgame

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.textgame.game.Game
import com.example.textgame.tools.Grammar
import com.example.textgame.ui.*
import java.util.*


class App : AppCompatActivity() {
    private lateinit var game: Game
    private var menu: Menu? = null
    private lateinit var sound: MediaPlayer

    private fun transition(destination: Fragment){
        supportFragmentManager.commit {
            setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
            )
            replace(R.id.home, destination)
            addToBackStack(null)
        }
    }

    private fun viewHome(){
        menu?.getItem(0)?.isVisible = true
        val frag = HomeFragment()
        val bundle = Bundle()
        val currentPage = game.dialog.model[game.player.model.vars["page"]!!] ?: game.dialog.model["invalid"]!!
        val args = game.getVarMap(currentPage.filler)
        val dialog = currentPage.fillVariables(args).validate(game.player.model)
        bundle.putParcelable("page", dialog)
        frag.arguments = bundle
        title = game.dialog.model[game.player.model.vars["page"]!!]?.title ?: ""
        transition(frag)
    }

    private fun viewProfile(){
        menu?.getItem(0)?.isVisible = true
        val frag = ProfileFragment()
        val bundle = Bundle()
        bundle.putParcelable("player", game.player.model)
        val totalMoves = game.moves.getMoves(game.player.model.getItemMoves(game.item.model))
        totalMoves.addAll(game.moves.getMoves(game.player.model.unit.moves))
        bundle.putSerializable("moveset", totalMoves)
        bundle.putSerializable("item map", game.item.model)
        frag.arguments = bundle
        title = getString(R.string.profile_title)
        transition(frag)
    }

    private fun viewShop(stock: String, page: String){
        menu?.getItem(0)?.isVisible = false
        val frag = ShopFragment()
        val bundle = Bundle()
        bundle.putString("page", page)
        bundle.putParcelable("player", game.player.model)
        bundle.putSerializable("item map", game.item.model)
        bundle.putStringArrayList("stock", game.variables.getRandomList(stock) as ArrayList<String>)
        frag.arguments = bundle
        title = getString(R.string.shop_title)
        transition(frag)
    }

    private fun viewLearn(page: String, forget: Boolean){
        menu?.getItem(0)?.isVisible = false
        val frag = LearnFragment()
        val bundle = Bundle()
        bundle.putString("page", page)
        bundle.putSerializable("moveMap", game.moves.model)
        if (forget){
            bundle.putStringArray("moves", game.player.model.unit.moves.shuffled().toTypedArray())
        } else {
            bundle.putStringArray("moves", game.getNewMoves())
        }
        bundle.putBoolean("forget", forget)
        frag.arguments = bundle
        title = getString(R.string.learn_title)
        transition(frag)
    }

    private fun viewAdvance(branch: Array<String>, page: String){
        menu?.getItem(0)?.isVisible = false
        val frag = AdvanceFragment()
        val bundle = Bundle()
        bundle.putString("page", page)
        bundle.putSerializable("specMap", game.special.model)
        bundle.putSerializable("spec", game.player.model.specialization)
        bundle.putStringArray("branch", branch)
        frag.arguments = bundle
        title = getString(R.string.advance_title)
        transition(frag)
    }

    private fun viewCustom(text: String, page: String){
        menu?.getItem(0)?.isVisible = false
        val frag = HomeFragment()
        val bundle = Bundle()
        bundle.putParcelable("page", game.dialog.getCustom(text, page, "Proceed"))
        frag.arguments = bundle
        title = game.dialog.model[game.player.model.vars["page"]!!]?.title ?: ""
        transition(frag)
    }

    private fun viewCombat(encounter: String, defeat: String, page: String){
        menu?.getItem(0)?.isVisible = false
        val frag = CombatFragment()
        val bundle = Bundle()
        val encounterModel = game.encounters.model[encounter] ?: game.encounters.model["invalid"]!!
        val enemyModels = encounterModel.getUnits(game.units.model)
        for (enemyModel in enemyModels){
            enemyModel.initialize(game.moves.getMoves(enemyModel.moves), 1)
            enemyModel.healthPt = enemyModel.health
        }
        game.player.model.unit.healthPt = game.player.model.unit.health - game.player.model.damaged
        game.player.model.unit.recharge()
        val companionModels = game.player.model.getAllies(game.units.model)
        for (ally in companionModels){
            ally.initialize(game.moves.getMoves(ally.moves), 1)
            ally.recharge()
            ally.healthPt = ally.health
        }
        val totalMoves = game.moves.getMoves(game.player.model.getItemMoves(game.item.model))
        totalMoves.addAll(game.moves.getMoves(game.player.model.unit.moves))
        game.player.model.unit.initialize(totalMoves, 3)
        bundle.putParcelable("player", game.player.model)
        bundle.putSerializable("items", game.item.model)
        bundle.putSerializable("allies", companionModels)
        bundle.putSerializable("enemies", enemyModels)
        bundle.putStringArrayList("loots", encounterModel.loots)
        bundle.putString("desc", encounterModel.desc)
        bundle.putString("page", page)
        bundle.putString("defeat", defeat)
        frag.arguments = bundle
        title = getString(R.string.combat_title, encounter.capitalize())
        transition(frag)
    }

    private fun adjustTheme(){
        if (game.player.model.isDay()) {
            setTheme(R.style.TextGame)
        } else {
            setTheme(R.style.TextGameDark)
        }
    }

    fun openDialog(to: String){
        if (to.isEmpty()){
            return
        }
        if ("mute" !in game.player.model.vars) {
            sound.start()
        }
        if (to.startsWith(">")) {
            val split = to.split(">")
            when (split[1]) {
                "custom" -> {
                    viewCustom(split[2], Grammar.substringAfter(to, ">", 3))
                }
                "save" -> {
                    game.player.model.vars[split[2]] = split[3]
                    openDialog(Grammar.substringAfter(to, ">", 4))
                }
                "load" -> {
                    openDialog(game.player.model.vars[split[2]] ?: "invalid")
                }
                "if" -> {
                    if (split[2] in game.player.model.vars && game.player.model.vars[split[2]] == split[3]) {
                        openDialog(split[4])
                    } else {
                        openDialog(Grammar.substringAfter(to, ">", 5))
                    }
                }
                "now" -> {
                    viewPage(game.player.model.vars["page"]!!)
                    viewHome()
                }
                "back" -> {
                    viewPage(game.player.model.vars["prev"]!!)
                    viewHome()
                }
                "time" -> {
                    adjustTheme()
                    game.player.timePass(split[2].toInt())
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "timeset" -> {
                    adjustTheme()
                    game.player.timeSet(split[2].toInt())
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "defeat" -> {
                    game.player.reset()
                    openDialog(Grammar.substringAfter(to, ">", 2))
                }
                "encounter" -> {
                    viewCombat(split[2], split[3], Grammar.substringAfter(to, ">", 4))
                }
                "combat" -> {
                    val encounter = game.variables.getRandom(split[2])
                    viewCombat(encounter, split[3], Grammar.substringAfter(to, ">", 4))
                }
                "join" -> {
                    if (split[2] !in game.player.model.companion) {
                        game.player.model.companion.add(split[2])
                    }
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "drop" -> {
                    game.player.model.companion.remove(split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "learn" -> {
                    game.player.model.unit.moves.add(split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "forget" -> {
                    game.player.model.unit.moves.remove(split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "specialize" -> {
                    game.special.change(game.player.model, game.player.model.specialization, split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "view shop" -> {
                    viewShop(split[2], Grammar.substringAfter(to, ">", 3))
                }
                "view learn" -> {
                    viewLearn(Grammar.substringAfter(to, ">", 2), false)
                }
                "view forget" -> {
                    viewLearn(Grammar.substringAfter(to, ">", 2), true)
                }
                "view advance" -> {
                    viewAdvance(split[2].split(":").toTypedArray(), Grammar.substringAfter(to, ">", 3))
                }
                "heal" -> {
                    game.player.model.unit.healthPt += split[2].toInt()
                    if (game.player.model.unit.healthPt > game.player.model.unit.health){
                        game.player.model.unit.healthPt = game.player.model.unit.health
                    }
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "item" -> {
                    game.player.model.equipItem(game.item.model, split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "trash" -> {
                    game.player.model.unequipPart(game.item.model, split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "refresh chapter" -> {
                    game.refreshChapter()
                    openDialog(Grammar.substringAfter(to, ">", 2))
                }
                "reset" -> {
                    game.player.reset()
                    openDialog(Grammar.substringAfter(to, ">", 2))
                }
            }
        } else {
            viewPage(to)
            viewHome()
        }
    }

    private fun viewPage(newPage: String){
        game.player.model.vars["prev"] = game.player.model.vars["page"]!!
        game.player.model.vars["page"] = newPage
        game.player.save()
        title = getString(R.string.page_title, game.player.model.getTime().capitalize(), game.dialog.model[newPage]?.title ?: "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sound = MediaPlayer.create(this, R.raw.fastflip)
        sound.setOnCompletionListener {
            it.stop()
            it.prepare()
        }
        sound.start()
        game = Game(this)
        adjustTheme()
        openDialog(game.player.model.vars["page"]!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_action, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_profile -> {
            val frag = supportFragmentManager.findFragmentById(R.id.home)
            if (frag is ProfileFragment){
                viewHome()
            } else {
                viewProfile()
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    @Suppress("DEPRECATION")
    private fun fullscreen() {
        if (Build.VERSION.SDK_INT < 19) {
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            val decorView = window.decorView
            val uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            decorView.systemUiVisibility = uiOptions
        }
    }
}