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
        bundle.putParcelable("page", game.getValidDialog())
        if (game.player.model.isTransitionTime()) {
            bundle.putString("time", game.player.model.getTime())
        }
        frag.arguments = bundle
        title = game.dialog.model[game.player.model.page]?.title ?: ""
        transition(frag)
    }

//    private fun viewCustomDialog(){
//        menu?.getItem(0)?.isEnabled = true
//        val frag = HomeFragment()
//        val bundle = Bundle()
//        bundle.putParcelable("page", game.getCustomDialog())
//        frag.arguments = bundle
//        title = ""
//        transition(frag)
//    }

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

    private fun viewCombat(encounter: String, page: String, ambush: Boolean){
        menu?.getItem(0)?.isEnabled = false
        val frag = CombatFragment()
        val bundle = Bundle()
        val encounterModel = game.encounters.model[encounter] ?: game.encounters.model["invalid"]!!
        val enemyModels = encounterModel.getEnemies(game.units.model)
        for (enemyModel in enemyModels){
            enemyModel.initialize(game.moves.getMoves(enemyModel.moves), 1)
        }
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
        frag.arguments = bundle
        title = getString(R.string.combat_title, encounter.capitalize())
        transition(frag)
    }

    fun openDialog(to: String){
        if (to.startsWith(":")) {
            val next = to.substring(1)
            val typeIndex = next.indexOf(":") + 1
            when (next.substringBefore(":")) {
                "back" -> {
                    viewPage(game.player.model.prev)
                    viewHome()
                }
                "defeat" -> {
                    val page = next.substringAfter(":")
                    game.player.reset()
                    viewPage(page)
                    viewHome()
                }
                "combat" -> {
                    val div = next.substring(typeIndex)
                    val param = div.substringBefore(":")
                    val page = div.substringAfter(":")
                    viewCombat(param, page, false)
                }
                "ambush" -> {
                    val div = next.substring(typeIndex)
                    val param = div.substringBefore(":")
                    val page = div.substringAfter(":")
                    viewCombat(param, page, true)
                }
                "item" -> {
                    val div = next.substring(typeIndex)
                    val param = div.substringBefore(":")
                    val page = div.substringAfter(":")
                    game.player.model.equipItem(game.item.model, param)
                    viewPage(page)
                    viewHome()
                }
                "useitem" -> {
                    val itemName = game.player.model.item
                    game.player.model.item = ""
                    if (itemName in game.item.model){
                        val item = game.item.model[itemName]
                        if (item != null) {
                            if (item.type == "item") {
                                if (item.page != null) {
                                    viewPage(item.page)
                                    viewHome()
                                    return
                                }
                            } else {
                                val unequipped = game.player.model.equipItem(game.item.model, itemName)
                                if (unequipped != null){
                                    game.player.model.item = unequipped
                                }
                            }
                            val page = next.substringAfter(":")
                            viewPage(page)
                            viewHome()
                        }
                    }
                }
            }
        } else {
            viewPage(to)
            viewHome()
        }
    }

    private fun viewPage(newPage: String, timePass: Int = 1){
        game.player.timePass(timePass)
        game.player.model.prev = game.player.model.page
        game.player.model.page = newPage
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
        viewPage(game.player.model.page, timePass=0)
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