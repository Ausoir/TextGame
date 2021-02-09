package com.example.textgame

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
import com.example.textgame.ui.CombatFragment
import com.example.textgame.ui.EquipmentFragment
import com.example.textgame.ui.HomeFragment
import com.example.textgame.ui.ProfileFragment


class App : AppCompatActivity() {
    private lateinit var game: Game
    private var menu: Menu? = null

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
        menu?.getItem(0)?.isEnabled = true
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
        menu?.getItem(0)?.isEnabled = true
        val frag = ProfileFragment()
        val bundle = Bundle()
        bundle.putParcelable("player", game.player.model)
        bundle.putSerializable("moveset", game.moves.getMoves(game.player.model.unit.moves))
        frag.arguments = bundle
        title = getString(R.string.profile_title, game.player.model.getDay())
        transition(frag)
    }

    fun viewEquipment(){
        menu?.getItem(0)?.isEnabled = true
        val frag = EquipmentFragment()
        val bundle = Bundle()
        bundle.putSerializable("inventory", game.item.model)
        bundle.putStringArrayList("equip", game.player.model.equip)
        bundle.putString("item", game.player.model.item)
        frag.arguments = bundle
        title = getString(R.string.equipment_title)
        transition(frag)
    }

    private fun viewCombat(encounter: String, defeat: String, page: String, ambush: Boolean){
        menu?.getItem(0)?.isEnabled = false
        val frag = CombatFragment()
        val bundle = Bundle()
        val encounterModel = game.encounters.model[encounter] ?: game.encounters.model["invalid"]!!
        val enemyModels = encounterModel.getEnemies(game.units.model)
        for (enemyModel in enemyModels){
            enemyModel.initialize(game.moves.getMoves(enemyModel.moves), 1)
            enemyModel.chealth = enemyModel.health
        }
        game.player.model.unit.chealth = game.player.model.unit.health - game.player.model.damaged
        if (ambush){
            game.player.model.unit.points = 0
        } else {
            game.player.model.unit.recharge()
        }
        game.player.model.unit.initialize(game.moves.getMoves(game.player.model.unit.moves), 3)
        bundle.putParcelable("player", game.player.model)
        bundle.putSerializable("items", game.item.model)
        bundle.putSerializable("enemies", enemyModels)
        bundle.putStringArrayList("loots", encounterModel.loots)
        bundle.putString("desc", encounterModel.desc)
        bundle.putString("page", page)
        bundle.putString("defeat", defeat)
        frag.arguments = bundle
        title = getString(R.string.combat_title, encounter.capitalize())
        transition(frag)
    }

    fun openDialog(to: String){
        if (to.startsWith(":")) {
            val split = to.split(":")
            when (split[1]) {
                "save" -> {
                    game.player.model.vars[split[2]] = split[3]
                    openDialog(Grammar.substringAfter(to, ":", 4))
                }
                "load" -> {
                    openDialog(game.player.model.vars[split[2]] ?: "invalid")
                }
                "back" -> {
                    viewPage(game.player.model.vars["prev"]!!)
                    viewHome()
                }
                "defeat" -> {
                    game.player.reset()
                    openDialog(Grammar.substringAfter(to, ":", 2))
                }
                "encounter" -> {
                    viewCombat(split[2], split[3], Grammar.substringAfter(to, ":", 4), false)
                }
                "combat" -> {
                    val encounter = game.variables.getRandom(split[2])
                    viewCombat(encounter, split[3], Grammar.substringAfter(to, ":", 4), false)
                }
                "ambush" -> {
                    val encounter = game.variables.getRandom(split[2])
                    viewCombat(encounter, split[3], Grammar.substringAfter(to, ":", 4), true)
                }
                "time" -> {
                    game.player.timePass(split[2].toInt())
                    openDialog(Grammar.substringAfter(to, ":", 3))
                }
                "item" -> {
                    game.player.model.equipItem(game.item.model, split[2])
                    openDialog(Grammar.substringAfter(to, ":", 3))
                }
                "sell" -> {
                    game.player.model.sell(game.item.model)
                    openDialog(Grammar.substringAfter(to, ":", 2))
                }
                "buy" -> {
                    game.player.model.item = split[2]
                    openDialog(Grammar.substringAfter(to, ":", 3))
                }
                "useitem" -> {
                    val itemName = game.player.model.item
                    game.player.model.item = ""
                    if (itemName in game.item.model){
                        val item = game.item.model[itemName]
                        if (item != null) {
                            if (item.type == "item") {
                                if (item.page != null) {
                                    openDialog(item.page)
                                    return
                                }
                            } else {
                                val unequipped = game.player.model.equipItem(game.item.model, itemName)
                                if (unequipped != null){
                                    game.player.model.item = unequipped
                                }
                            }
                            openDialog(Grammar.substringAfter(to, ":", 2))
                        }
                    }
                }
                "refreshChapter" -> {
                    game.refreshChapter()
                    openDialog(Grammar.substringAfter(to, ":", 2))
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
        if (game.player.model.isDay()) {
            setTheme(R.style.TextGame)
        } else {
            setTheme(R.style.TextGameDark)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        game = Game(this)
        viewPage(game.player.model.vars["page"]!!)
        viewHome()
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
            } else if (frag !is CombatFragment) {
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