package aoa.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import aoa.App
import aoa.R
import aoa.game.ItemModel
import aoa.game.MoveModel
import aoa.game.PlayerModel
import aoa.game.UnitModel
import aoa.tools.Grammar

@SuppressLint("DefaultLocale")
class CombatFragment : HtmlTextFragment() {
    private lateinit var layout: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var loots: ArrayList<ItemModel?>
    private lateinit var page: String
    private lateinit var defeat: String
    private lateinit var player: PlayerModel
    private lateinit var unitMap: HashMap<String, UnitModel>
    private var isTargetingMode = false
    private var buttons: ArrayList<Button> = ArrayList()
    private var turn = -1
    private var round = 0
    private var idCount = 0
    private var combatants: ArrayList<UnitModel> = ArrayList()
    private var prompted = false

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_combat, container, false)
        layout = root.findViewById(R.id.combat_button_container) as LinearLayout
        scrollView = root.findViewById(R.id.scroll_combat)
        textView = root.findViewById(R.id.text_combat)
        textView.setOnClickListener {
            switchMode()
        }
        textView.movementMethod = ScrollingMovementMethod()
        player = arguments?.getParcelable("player") ?: return root
        unitMap = arguments?.getSerializable("unitMap") as HashMap<String, UnitModel>
        val allies = arguments?.getSerializable("allies") as ArrayList<UnitModel>
        val foes = arguments?.getSerializable("enemies") as ArrayList<UnitModel>
        loots = arguments?.getStringArrayList("loots") as ArrayList<ItemModel?>
        addCombatant(player.unit, 0, false)
        allies.forEach {addCombatant(it, 1, false)}
        foes.forEach {addCombatant(it, -1, false)}
        updateFaction()
        for (combatant in combatants){
            if ("initiative" in combatant.skills){
                combatant.recharge()
            }
        }
        page = arguments?.getString("page") ?: "back"
        defeat = arguments?.getString("defeat") ?: "defeat"
        addCombatText("${(arguments?.getString("desc") ?: "")}\n\n")
        nextTurn()
        return root
    }

    private fun addCombatText(text: String){
        super.addText(text)
        if (text.isNotEmpty()) {
            prompted = true
        }
    }

    private fun addButton(name: String) : Button {
        val button = Button(activity)
        button.text = name
        layout.addView(button)
        buttons.add(button)
        return button
    }

    private fun clearButtons(){
        for (button in buttons){
            layout.removeView(button)
        }
        buttons.clear()
    }

    private fun ScrollView.scrollToBottom() {
        post{
            smoothScrollTo(0, textView.layout.getLineTop(textView.lineCount))
        }
    }

    private fun addCombatant(unitName: String, faction: Int = -1){
        if (unitMap[unitName] != null) {
            addCombatant(unitMap[unitName]!!.copy(), faction)
        }
    }

    private fun updateFaction(){
        player.unit.target = player.unit.foes.shuffled()[0]
        for (ally in player.unit.allies){
            ally.allies = player.unit.allies.toMutableList() as ArrayList<UnitModel>
            ally.allies.remove(ally)
            ally.allies.add(player.unit)
            ally.foes = player.unit.foes
            ally.target = ally.foes.shuffled()[0]
        }
        for (foe in player.unit.foes){
            foe.allies = player.unit.foes.toMutableList() as ArrayList<UnitModel>
            foe.allies.remove(foe)
            foe.foes = arrayListOf(player.unit)
            foe.foes.addAll(player.unit.allies)
            foe.target = foe.foes.shuffled()[0]
        }
    }

    private fun addCombatant(combatant: UnitModel, faction: Int = -1, update: Boolean = true){
        combatant.id = idCount++
        combatant.position = idCount
        if (faction == 1){
            combatant.flavors["ally"] = "true"
            player.unit.allies.add(combatant)
        } else if (faction == -1){
            player.unit.foes.add(combatant)
            val sameNames = arrayListOf<UnitModel>()
            for (foe in player.unit.foes) {
                if (foe.name == combatant.name) {
                    sameNames.add(foe)
                }
            }
            if (sameNames.size > 1) {
                for ((nth, sameName) in sameNames.withIndex()) {
                    sameName.flavors["callname"] = Grammar.getOrderNumber(nth + 1) + " ${combatant.name}"
                }
            }
        }
        if (update){
            updateFaction()
        }
        if ("sneak" in combatant.skills) {
            combatant.points += 2
        } else if ("manacharge" in combatant.skills) {
            combatant.mana += 2
        }
        combatants.add(combatant)
    }

    private fun turnAction(move: MoveModel, me: UnitModel){
        me.points -= 1
        val feedback = move.perform(me, combatants)
        if (feedback.isNotEmpty()) {
            addCombatText(feedback)
        }
    }

    private fun npcTurn(enemy: UnitModel){
        if (!checkBattleEnd()) {
            if (!enemy.isDead() && enemy.points > 0) {
                val hand = enemy.deck!!.getCard(0)
                turnAction(hand, enemy)
                if (hand != MoveModel.shuffle) {
                    enemy.deck!!.draw(0)
                }
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        npcTurn(enemy)
                    }, 100
                )
            } else {
                scrollView.scrollToBottom()
                enemy.recharge()
                nextTurn()
            }
        }
    }

    private fun switchMode(){
        if (isTargetingMode) {
            switchToCombat()
        } else {
            switchToTargeting()
        }
        isTargetingMode = !isTargetingMode
    }

    private fun switchToCombat(){
        clearButtons()
        for (i in 0..2) {
            val hand = player.unit.deck!!.getCard(i)
            if (!hand.text.isEmpty()) {
                addButton(player.unit.replaceNouns(hand.text)).setOnClickListener {
                    turnAction(hand, player.unit)
                    if (hand != MoveModel.shuffle) {
                        player.unit.deck!!.draw(i)
                    }
                    player.unit.deck!!.validate()
                    playerTurn()
                }
            }
        }
    }

    private fun switchToTargeting(){
        clearButtons()
        for (i in 0 until player.unit.foes.size){
            val foeName = player.unit.foes[i].flavors["callname"] ?: player.unit.foes[i].name
            val foeHealth = player.unit.foes[i].getHealthPercentage()
            val engaged = if (player.unit.foes[i].position == player.unit.position){
                " (Engaged)"
            } else {
                ""
            }
            val text = if (player.unit.foes[i].block > 0) {
                player.unit.replaceNouns("Target $foeName ($foeHealth% +${player.unit.foes[i].block})$engaged")
            } else {
                player.unit.replaceNouns("Target $foeName ($foeHealth%)$engaged")
            }
            addButton(text).setOnClickListener {
                player.unit.target = player.unit.foes[i]
                playerTurn()
            }
        }
    }

    private fun playerTurn(){
        scrollView.scrollToBottom()
        if (!checkBattleEnd()) {
            if (player.unit.points > 0) {
                switchToCombat()
            } else {
                player.unit.recharge()
                clearButtons()
                prompted = false
                nextTurn()
            }
        }
    }

    private fun nextTurn(){
        if (prompted) {
            addButton("Continue").setOnClickListener {
                prompted = false
                clearButtons()
                processTurn()
            }
        } else {
            processTurn()
        }
    }

    private fun processTurn(){
        turn += 1
        if (turn >= combatants.size) {
            turn = 0
        }
        if ("hardened" in combatants[turn].skills){
            combatants[turn].block += 1
        }
        val feedback = combatants[turn].replaceNouns(combatants[turn].processStatus())
        if (combatants[turn] == player.unit){
            if (round > 0) {
                addCombatText("--- Round $round ---\n${player.unit.points} Actions, ${player.unit.healthPt}/${player.unit.health} Health, ${player.unit.block} Block\n")
                textView.setOnClickListener {
                    switchMode()
                }
            }
            round += 1
            addCombatText(feedback)
            playerTurn()
        } else {
            textView.setOnClickListener {}
            addCombatText(feedback)
            npcTurn(combatants[turn])
        }
    }

    private fun checkBattleEnd(): Boolean{
        var result = 0
        for (i in combatants.indices.reversed()){
            if (combatants[i].isDead()){
                if (combatants[i] == player.unit){
                    result = -1
                }
                for (combatant in combatants){
                    combatant.foes.remove(combatants[i])
                }
                if (player.unit.foes.isEmpty()){
                    result = 1
                }
                for (combatant in combatants){
                    if (combatant.target == combatants[i] && combatant.foes.isNotEmpty()){
                        combatant.target = combatant.foes[0]
                    }
                }
                if ("self destruct" in combatants[i].skills) {
                    addCombatText("The ${combatants[i].name} explodes, dealing 1 damage to everything near it!\n")
                    for (combatant in combatants) {
                        if (combatant != combatants[i] && combatant.position == combatants[i].position) {
                            addCombatText(combatant.inflict(-1))
                        }
                    }
                    combatants.removeAt(i)
                } else {
                    combatants.removeAt(i)
                }
            }
        }
        return when (result) {
            -1 -> {
                defeat()
                true
            }
            1 -> {
                if (player.unit.isDead()){ //self destruct dmg
                    defeat()
                } else {
                    victory()
                }
                true
            }
            else -> {
                return false
            }
        }
    }

    private fun endBattle(){
        if (player.unit !in combatants){
            combatants.add(player.unit)
        }
        for (combatant in combatants){
            combatant.mana = 0
            combatant.deck = null
            combatant.target = null
            combatant.status.clear()
            combatant.allies.clear()
            combatant.foes.clear()
        }
        if (player.unit.healthPt < player.unit.health){
            if ("herbal remedy" in player.unit.skills){
                player.unit.healthPt += 1
            }
            if ("spirit remedy" in player.unit.skills){
                player.unit.healthPt = player.unit.health
            }
        }
        clearButtons()
    }

    private fun victory(){
        endBattle()
        if (loots.isNotEmpty()) {
            val loot = loots.shuffled()[0]
            if (loot != null) {
                page = ">item>${loot.name}>>advance>$page"
            }
        }
        addCombatText(resources.getString(R.string.victory))
        addButton(resources.getString(R.string.proceed)).setOnClickListener {
            val app: App = activity as App
            app.openDialog(page)
        }
        scrollView.scrollToBottom()
    }

    private fun defeat(){
        player.money = 0
        player.unit.healthPt = player.unit.health / 2
        player.unit.moves.add("flinch")
        endBattle()
        clearButtons()
        addCombatText(resources.getString(R.string.defeat))
        addButton(resources.getString(R.string.proceed)).setOnClickListener {
            val app: App = activity as App
            if ("ankh" in player.equip){
                player.unit.healthPt = player.unit.health
                app.openDialog(">custom>${resources.getString(R.string.defeat)}>>remove part>sigil>>to>page")
            } else {
                app.openDialog(">remove part>weapon>>remove part>offhand>$defeat")
            }
        }
        scrollView.scrollToBottom()
    }
}