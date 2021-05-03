package com.example.textgame

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.textgame.game.Game
import com.example.textgame.tools.Grammar
import com.example.textgame.ui.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class App : AppCompatActivity() {
    private lateinit var game: Game
    private var menu: Menu? = null
    private lateinit var sound: MediaPlayer
    private var mInterstitialAd: InterstitialAd? = null

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
        val page = (game.player.model.vars["page"]!!).substring(1)
        val currentPage = game.dialog.model[page] ?: game.dialog.model2[page] ?: game.dialog.model2["invalid"]!!
        val args = game.getVarMap(currentPage.filler)
        val dialog = currentPage.fillVariables(args).validate(game.player.model)
        bundle.putParcelable("page", dialog)
        frag.arguments = bundle
        title = currentPage.title ?: ""
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

    private fun viewQuest(page: String){
        menu?.getItem(0)?.isVisible = false
        val frag = QuestFragment()
        val bundle = Bundle()
        bundle.putString("page", page)
        bundle.putParcelable("player", game.player.model)
        frag.arguments = bundle
        title = getString(R.string.quest_title)
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

    private fun viewAdvance(branch: Array<String>?, page: String){
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
        val playerModel = game.player.model
        playerModel.unit.healthPt = playerModel.unit.health - playerModel.damaged
        playerModel.unit.points = 0
        val companionModels = playerModel.getAllies(game.units.model)
        for (i in companionModels.indices){
            val allyMoves = game.moves.getMoves(companionModels[i].moves)
            allyMoves.addAll(game.moves.getMoves(playerModel.companionMoves[playerModel.companion[i]]
                    ?: listOf()))
            companionModels[i].initialize(game.moves.getMoves(companionModels[i].moves), 1)
            companionModels[i].points = 0
            companionModels[i].healthPt = companionModels[i].health
        }
        val totalMoves = game.moves.getMoves(playerModel.getItemMoves(game.item.model))
        totalMoves.addAll(game.moves.getMoves(playerModel.unit.moves))
        playerModel.unit.initialize(totalMoves, 3)
        bundle.putParcelable("player", playerModel)
        bundle.putSerializable("unitMap", game.units.model)
        bundle.putSerializable("allies", companionModels)
        bundle.putSerializable("enemies", enemyModels)
        bundle.putSerializable("loots", game.item.getItems(encounterModel.loots))
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
                "copy" -> {
                    game.player.model.vars[split[2]] = game.player.model.vars[split[3]] ?: ""
                    openDialog(Grammar.substringAfter(to, ">", 4))
                }
                "saveplayer" -> {
                    game.player.save(split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "loadplayer" -> {
                    game.player.load(split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "add" -> {
                    if (split[2] in game.player.model.vars) {
                        game.player.model.vars[split[2]] = (game.player.model.vars[split[2]]!!.toInt() + split[3].toInt()).toString()
                    } else {
                        game.player.model.vars[split[2]] = split[3]
                    }
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
                "to" -> {
                    viewPage(game.player.model.vars[split[2]]!!)
                    viewHome()
                }
                "time" -> {
                    game.player.timePass(split[2].toInt())
                    adjustTheme()
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "timeset" -> {
                    game.player.timeSet(split[2].toInt())
                    adjustTheme()
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
                "dropAll" -> {
                    game.player.model.companion.clear()
                    openDialog(Grammar.substringAfter(to, ">", 2))
                }
                "questFill" -> {
                    for (quest in game.variables.model[split[2]] ?: arrayListOf()) {
                        game.player.model.quests.add(quest)
                    }
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "quest" -> {
                    game.player.model.quests.add(split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "forfeitAll" -> {
                    game.player.model.quests.clear()
                    openDialog(Grammar.substringAfter(to, ">", 2))
                }
                "removeAll" -> {
                    game.player.model.equip.clear()
                    openDialog(Grammar.substringAfter(to, ">", 2))
                }
                "learn" -> {
                    game.player.model.unit.moves.add(split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "allyLearn" -> {
                    if (split[2] !in game.player.model.companionMoves) {
                        game.player.model.companionMoves[split[2]] = arrayListOf()
                    }
                    game.player.model.companionMoves[split[2]]!!.add(split[3])
                    openDialog(Grammar.substringAfter(to, ">", 4))
                }
                "maxhealth" -> {
                    game.player.model.unit.health += split[2].toInt()
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "moneyAdd" -> {
                    game.player.model.money += split[2].toInt()
                    if (game.player.model.money < 0) {
                        game.player.model.money = 0
                    }
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "moneySet" -> {
                    game.player.model.money = split[2].toInt()
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
                "view rest" -> {
                    val foodName = game.player.model.unequipPart(game.item.model, "food")
                    if (foodName == null) {
                        viewCustom("You have no food.\nYou can't rest hungry.", Grammar.substringAfter(to, ">", 2))
                    } else {
                        val food = game.item.model[foodName] ?: game.item.model["invalid"]!!
                        viewCustom("You rest and heal ${food.rating} health.",
                                ">time>8>>remove part>food>>heal>${food.rating}>" + Grammar.substringAfter(to, ">", 2))
                    }
                }
                "view shop" -> {
                    viewShop(split[2], Grammar.substringAfter(to, ">", 3))
                }
                "view quest" -> {
                    viewQuest(Grammar.substringAfter(to, ">", 2))
                }
                "view learn" -> {
                    viewLearn(Grammar.substringAfter(to, ">", 2), false)
                }
                "view forget" -> {
                    viewLearn(Grammar.substringAfter(to, ">", 2), true)
                }
                "view advance" -> {
                    viewAdvance(null, Grammar.substringAfter(to, ">", 2))
                }
                "view specialize" -> {
                    viewAdvance(split[2].split(":").toTypedArray(), Grammar.substringAfter(to, ">", 3))
                }
                "advance" -> {
                    game.player.model.vars["t1"] = "You improve your abilities!"
                    game.player.model.vars["t2"] = ">view learn>${Grammar.substringAfter(to, ">", 2)}"
                    game.player.model.vars["t3"] = "Learn a new move"
                    game.player.model.vars["t4"] = ">view forget>${Grammar.substringAfter(to, ">", 2)}"
                    game.player.model.vars["t5"] = "Forget a move"
                    viewPage("@custom2")
                    viewHome()
                }
                "heal" -> {
                    game.player.model.unit.healthPt += split[2].toInt()
                    if (game.player.model.unit.healthPt > game.player.model.unit.health) {
                        game.player.model.unit.healthPt = game.player.model.unit.health
                    } else if (game.player.model.unit.healthPt < 1) {
                        game.player.model.unit.healthPt = 1
                    }
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "item" -> {
                    val piece = game.player.model.getSamePart(game.item.model, split[2])
                    val desc = game.item.model[split[2]]?.desc ?: "No description found."
                    game.player.model.vars["t1"] = "You find ${Grammar.getArticle(split[2])}:\n\n  ${desc.capitalize()}"
                    game.player.model.vars["t2"] = ">force item>${split[2]}>${Grammar.substringAfter(to, ">", 3)}"
                    if (piece == null) {
                        game.player.model.vars["t3"] = "Take ${split[2]}"
                    } else {
                        game.player.model.vars["t3"] = "Throw away $piece and take ${split[2]}"
                    }
                    game.player.model.vars["t4"] = Grammar.substringAfter(to, ">", 3)
                    game.player.model.vars["t5"] = "Leave the item and continue"
                    viewPage("@custom2")
                    viewHome()
                }
                "force item" -> {
                    game.player.model.equipItem(game.item.model, split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "remove item" -> {
                    game.player.model.unequipItem(game.item.model, split[2])
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "remove part" -> {
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
                "ad" -> {
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this)
                    } else {
                        Log.d("ad", "The interstitial ad wasn't ready yet.")
                    }
                    openDialog(Grammar.substringAfter(to, ">", 2))
                }
                "quit" -> {
                    finishAffinity()
                }
            }
        } else if (to.startsWith("@")) {
            game.player.model.vars["prev"] = game.player.model.vars["page"]!!
            game.player.model.vars["page"] = to
            game.player.save("save")
            viewPage(to)
            viewHome()
        }
    }

    private fun viewPage(newPage: String){
        game.player.model.vars["page"] = newPage
        title = getString(R.string.page_title, game.player.model.getTime().capitalize(), game.dialog.model[newPage]?.title
                ?: "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) { }
        MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                        .setTestDeviceIds(listOf("ABCDEF012345"))
                        .build()
        )


        sound = MediaPlayer.create(this, R.raw.fastflip)
        sound.setOnCompletionListener {
            it.stop()
            it.prepare()
        }
        sound.start()
        game = Game(this)
        adjustTheme()

        loadAd()
    }

    override fun onStart(){
        super.onStart()
        openDialog(game.player.model.vars["page"]!!)
    }

    fun loadAd(){
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("ad", adError.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("ad", "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("ad", "Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.d("ad", "Ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("ad", "Ad showed fullscreen content.")
                mInterstitialAd = null;
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_action, menu)
        when (supportFragmentManager.findFragmentById(R.id.home)) {
            is ProfileFragment -> {
                menu?.getItem(0)?.isVisible = false
            }
            is CombatFragment -> {
                menu?.getItem(0)?.isVisible = false
            }
            else -> {
                menu?.getItem(0)?.isVisible = true
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_profile -> {
            val frag = supportFragmentManager.findFragmentById(R.id.home)
            if (frag is ProfileFragment) {
                openDialog(game.player.model.vars["page"]!!)
            } else if (frag is HomeFragment) {
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