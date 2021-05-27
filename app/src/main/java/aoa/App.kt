package aoa

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import aoa.game.Game
import aoa.tools.Grammar
import aoa.ui.AdvanceFragment
import aoa.ui.CombatFragment
import aoa.ui.HomeFragment
import aoa.ui.LearnFragment
import aoa.ui.profile.ProfileFragment
import aoa.ui.shop.GemShopFragment
import aoa.ui.shop.QuestFragment
import aoa.ui.shop.ShopFragment


class App : AppCompatActivity() {
    private lateinit var game: Game
    private lateinit var soundPool: SoundPool
    private var savedInstanceState: Bundle? = null
    private var menu: Menu? = null
    private val sounds = hashMapOf<String, Int>()
    private var returnPage = ""
    private var lastGemShop = ""

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
        val frag = HomeFragment()
        val bundle = Bundle()
        val page = (game.player.model.vars["page"]!!).substring(1)
        val currentPage = game.dialog.model[page] ?: game.dialog.model2[page] ?: game.dialog.model2["invalid"]!!
        val args = game.getVarMap(currentPage.filler)
        val dialog = currentPage.fillVariables(args).validate(game.player.model)
        if (currentPage.title.isNullOrBlank()) {
            menu?.getItem(0)?.isVisible = false
        } else {
            menu?.getItem(0)?.isVisible = true
            menu?.getItem(0)?.setIcon(R.drawable.ic_profile)
        }
        bundle.putParcelable("page", dialog)
        frag.arguments = bundle
        title = currentPage.title ?: ""
        transition(frag)
    }

    private fun viewProfile(){
        menu?.getItem(0)?.isVisible = true
        menu?.getItem(0)?.setIcon(R.drawable.ic_exit)
        val frag = ProfileFragment()
        val bundle = Bundle()
        bundle.putParcelable("player", game.player.model)
        val totalMoves = game.moves.getMoves(game.player.model.getItemMoves(game.item.model))
        totalMoves.addAll(game.moves.getMoves(game.player.model.unit.moves))
        bundle.putSerializable("moveset", totalMoves)
        bundle.putSerializable("item map", game.item.model)
        bundle.putSerializable("unit map", game.units.model)
        frag.arguments = bundle
        title = getString(R.string.profile_title)
        transition(frag)
    }

    private fun viewShop(stock: String, page: String){
        menu?.getItem(0)?.isVisible = true
        menu?.getItem(0)?.setIcon(R.drawable.ic_exit)
        val frag = ShopFragment()
        val bundle = Bundle()
        bundle.putString("page", page)
        bundle.putParcelable("player", game.player.model)
        bundle.putSerializable("item map", game.item.model)
        bundle.putStringArrayList("stock", game.getRandomList(stock))
        frag.arguments = bundle
        title = getString(R.string.shop)
        transition(frag)
    }

    private fun viewGem(stock: String, page: String){
        menu?.getItem(0)?.isVisible = true
        menu?.getItem(0)?.setIcon(R.drawable.ic_exit)
        val frag = GemShopFragment()
        val bundle = Bundle()
        bundle.putString("page", page)
        bundle.putParcelable("player", game.player.model)
        bundle.putSerializable("item map", game.item.model)
        bundle.putSerializable("stock", game.variables.model[stock])
        frag.arguments = bundle
        title = getString(R.string.shop)
        transition(frag)
    }

    private fun viewQuest(page: String){
        menu?.getItem(0)?.isVisible = true
        menu?.getItem(0)?.setIcon(R.drawable.ic_exit)
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
        transition(frag)
    }

    private fun viewCustom2(text: String, opt1: String, opt1c: String, opt2: String, opt2c: String){
        menu?.getItem(0)?.isVisible = false
        val frag = HomeFragment()
        val bundle = Bundle()
        bundle.putParcelable("page", game.dialog.getCustom2(text, opt1, opt1c, opt2, opt2c))
        frag.arguments = bundle
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
        playerModel.unit.points = 0
        val companionModels = playerModel.getAllies(game.units.model)
        for (i in companionModels.indices){
            val allyMoves = game.moves.getMoves(companionModels[i].moves)
            allyMoves.addAll(
                game.moves.getMoves(
                    playerModel.companionMoves[playerModel.companion[i]]
                        ?: listOf()
                )
            )
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
        val typedValue = TypedValue()
        val a = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimaryDark))
        window.decorView.setBackgroundColor(a.getColor(0, 0))
        a.recycle()
    }

    fun playSound(file: String){
        if ("mute" !in game.player.model.vars) {
            if (file in sounds.keys){
                soundPool.play(sounds[file]!!, .7f, .7f, 0, 0, 1f)
            }
        }
    }

    fun openDialog(to: String){
        if (to.isEmpty()){
            return
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
                        game.player.model.vars[split[2]] =
                            (game.player.model.vars[split[2]]!!.toInt() + split[3].toInt()).toString()
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
                    game.special.change(
                        game.player.model,
                        game.player.model.specialization,
                        split[2]
                    )
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "view rest" -> {
                    val foodName = game.player.model.unequipPart(game.item.model, "food")
                    if (foodName == null) {
                        viewCustom(
                            "You have no food.\nYou can't rest hungry.", Grammar.substringAfter(
                                to,
                                ">",
                                2
                            )
                        )
                    } else {
                        val food = game.item.model[foodName] ?: game.item.model["invalid"]!!
                        viewCustom(
                            "You rest and heal ${food.rating} health.",
                            ">time>8>>remove part>food>>heal>${food.rating}>" + Grammar.substringAfter(
                                to,
                                ">",
                                2
                            )
                        )
                    }
                }
                "view shop" -> {
                    viewShop(split[2], Grammar.substringAfter(to, ">", 3))
                }
                "view gem" -> {
                    lastGemShop = split[2]
                    returnPage = Grammar.substringAfter(to, ">", 3)
                    viewGem(split[2], returnPage)
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
                    viewAdvance(
                        split[2].split(":").toTypedArray(), Grammar.substringAfter(
                            to,
                            ">",
                            3
                        )
                    )
                }
                "advance" -> {
                    val text = "You improve your abilities!"
                    val opt1c =
                        ">view learn>${Grammar.substringAfter(to, ">", 2)}"
                    val opt1 = "Learn a new move"
                    val opt2c = ">view forget>${
                        Grammar.substringAfter(to, ">", 2)
                    }"
                    val opt2 = "Forget a move"
                    viewCustom2(text, opt1, opt1c, opt2, opt2c)
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
                "overheal" -> {
                    game.player.model.unit.healthPt += split[2].toInt()
                    openDialog(Grammar.substringAfter(to, ">", 3))
                }
                "loot" -> {
                    val item = game.variables.getRandom(split[2])
                    openDialog(">item>$item>" + Grammar.substringAfter(to, ">", 3))
                }
                "item" -> {
                    val piece = game.player.model.getSamePart(game.item.model, split[2])
                    val desc = game.item.model[split[2]]?.desc ?: "No description found."
                    val text = "You find ${Grammar.getArticle(split[2])}:\n\n  ${desc.capitalize()}"
                    val opt1c = ">force item>${split[2]}>${
                        Grammar.substringAfter(to, ">", 3)
                    }"
                    val opt1 = if (piece == null) {
                        "Take ${split[2]}"
                    } else {
                        "Throw away $piece and take ${split[2]}"
                    }
                    val opt2c = Grammar.substringAfter(to, ">", 3)
                    val opt2 = "Leave the item and continue"
                    viewCustom2(text, opt1, opt1c, opt2, opt2c)
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
                "ad" -> { //InterstitialAd removed
                    openDialog(Grammar.substringAfter(to, ">", 2))
                }
                "quit" -> {
                    finishAffinity()
                }
            }
        } else if (to.startsWith("@")) {
            playSound("flip")
            game.player.model.vars["prev"] = game.player.model.vars["page"]!!
            game.player.model.vars["page"] = to
            game.player.save("save")
            viewPage(to)
            viewHome()
        }
    }

    private fun viewPage(newPage: String){
        title = getString(
            R.string.page_title,
            game.player.model.getTime().capitalize(),
            game.dialog.model[newPage]?.title
                ?: ""
        )
    }

    override fun onStart(){
        super.onStart()
        openDialog(game.player.model.vars["page"]!!)
//        Tools.validateChapters(game)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main)
        fullscreen()

        soundPool = if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .build()
            SoundPool.Builder()
                    .setMaxStreams(3)
                    .setAudioAttributes(
                        audioAttributes
                    )
                    .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(
                3,
                AudioManager.STREAM_MUSIC,
                0
            )
        }

        sounds["flip"] = soundPool.load(this, R.raw.flip, 1)
        sounds["beep"] = soundPool.load(this, R.raw.beep, 1)
        game = Game(this)
        adjustTheme()

        Billing.setupBillingClient(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_action, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_profile -> {
            val frag = supportFragmentManager.findFragmentById(R.id.home)
            if (frag is HomeFragment) {
                viewProfile()
            } else {
                viewPage(game.player.model.vars["page"]!!)
                viewHome()
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
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

    fun record(code: String, value: Int){
        game.player.model.vars[code] = "$value"
    }

    fun onPurchase(type: Int){
        if (Billing.purchaseReady) {
            Billing.purchaseReady = false
            when (type) {
                0 -> {
                    openDialog(">custom>${resources.getString(R.string.transaction_cancel)}>>view gem>$lastGemShop>$returnPage")
                }
                in 1..9 -> {
                    game.player.model.real += type
                    openDialog(">custom>${resources.getString(R.string.transaction_done)}>>view gem>$lastGemShop>$returnPage")
                }
                else -> openDialog(
                    ">custom>${
                        resources.getString(
                            R.string.transaction_fail,
                            type
                        )
                    }>>view gem>$lastGemShop>$returnPage"
                )
            }
        }
    }
}