package com.example.textgame.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.textgame.App
import com.example.textgame.R
import com.example.textgame.game.ItemModel
import com.example.textgame.game.MoveModel
import com.example.textgame.game.PlayerModel
import com.example.textgame.game.UnitModel
import com.example.textgame.tools.Grammar

@SuppressLint("DefaultLocale")
class CombatFragment : Fragment() {
    private lateinit var layout: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var loots: ArrayList<ItemModel?>
    private lateinit var page: String
    private lateinit var defeat: String
    private lateinit var player: PlayerModel
    private lateinit var allies: ArrayList<UnitModel>
    private lateinit var unitMap: HashMap<String, UnitModel>
    private lateinit var textView: TextView
    private var stringBuilder = SpannableStringBuilder()
    private var isTargetingMode = false
    private var buttons: ArrayList<Button> = ArrayList()
    private var turn = -1
    private var round = 0
    private var idCount = 0
    private var combatants: ArrayList<UnitModel> = ArrayList()

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
            if (isTargetingMode){
                switchToCombat()
            } else {
                switchToTargeting()
            }
            isTargetingMode = !isTargetingMode
        }
        textView.movementMethod = ScrollingMovementMethod()
        player = arguments?.getParcelable("player") ?: return root
        unitMap = arguments?.getSerializable("unitMap") as HashMap<String, UnitModel>
        player.unit.foes = arguments?.getSerializable("enemies") as ArrayList<UnitModel>
        allies = arguments?.getSerializable("allies") as ArrayList<UnitModel>
        loots = arguments?.getStringArrayList("loots") as ArrayList<ItemModel?>
        addCombatant(player.unit, 0)
        allies.forEach {addCombatant(it, 1)}
        player.unit.foes.forEach {addCombatant(it, -1)}
        for (combatant in combatants){
            if ("initiative" in combatant.skills){
                combatant.recharge()
            }
        }
        page = arguments?.getString("page") ?: "back"
        defeat = arguments?.getString("defeat") ?: "defeat"
        addText("${(arguments?.getString("desc") ?: "")}\n\n")
        nextTurn()
        return root
    }

    private fun addText(text: String){
        val split = text.split("\n")
        for (i in split.indices){
            if (split[i].startsWith("<color:")){
                var modText = split[i].substringAfter("<color:")
                val color = Color.parseColor(modText.substringBefore(">"))
                modText = modText.substringAfter(">")
                val spannableString = SpannableString(modText)
                spannableString.setSpan(ForegroundColorSpan(color), 0, modText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                stringBuilder.append(spannableString)
            } else {
                stringBuilder.append(split[i])
            }
            if (i != split.size - 1){
                stringBuilder.append("\n")
            }
        }
        textView.setText(stringBuilder, TextView.BufferType.SPANNABLE)
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

    private fun addCombatant(combatant: UnitModel, faction: Int = -1){
        combatant.id = idCount++
        combatant.position = idCount
        if (faction == 1){
            combatant.flavors["ally"] = "true"
            combatant.foes = player.unit.foes
        } else if (faction == -1){
            val targetables = ArrayList<UnitModel>()
            targetables.addAll(allies)
            targetables.add(player.unit)
            combatant.foes = targetables
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
        combatant.target = combatant.foes.shuffled()[0]
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
            addText(feedback)
        }
    }

    private fun npcTurn(enemy: UnitModel){
        if (!checkBattleEnd()) {
            if (enemy.points > 0) {
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
                nextTurn()
            }
        }
    }

    private fun nextTurn(){
        turn += 1
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (turn >= combatants.size) {
                    turn = 0
                }
                if (combatants[turn] == player.unit){
                    val turnText = player.unit.replaceNouns(player.unit.processStatus())
                    if (round > 0) {
                        addText("--- Round $round ---\n${player.unit.points} Actions, ${player.unit.healthPt} Health, ${player.unit.block} Block\n")
                    }
                    round += 1
                    addText(turnText)
                    playerTurn()
                } else {
                    addText(combatants[turn].replaceNouns(combatants[turn].processStatus()))
                    npcTurn(combatants[turn])
                }
            }, 300
        )
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
                    addText("The ${combatants[i].name} explodes, dealing 1 damage to everything near it!\n")
                    for (combatant in combatants) {
                        if (combatant != combatants[i] && combatant.position == combatants[i].position) {
                            addText(combatant.inflict(-1))
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
        player.damaged = player.unit.health - player.unit.healthPt
        if (player.unit !in combatants){
            combatants.add(player.unit)
        }
        for (combatant in combatants){
            combatant.deck = null
            combatant.target = null
            combatant.status.clear()
            combatant.statusDuration.clear()
            combatant.foes.clear()
        }
        clearButtons()
    }

    private fun victory(){
        endBattle()
        if ("herbal remedy" in player.unit.skills && player.damaged > 0){
            player.damaged -= 1
        }
        if (loots.isNotEmpty()) {
            val loot = loots.shuffled()[0]
            if (loot != null) {
                val text = "${resources.getString(R.string.victory)}\n\nYou find ${Grammar.getArticle(loot.name)}:\n${loot.desc}"
                page = ">item>$text>${loot.name}>$page"
            }
        }
        addText(resources.getString(R.string.victory))
        addButton(resources.getString(R.string.proceed)).setOnClickListener {
            victoryProceed()
        }
        scrollView.scrollToBottom()
    }

    private fun victoryProceed(){
        clearButtons()
        stringBuilder.clear()
        addText("\n\nYou improve your abilities!")
        addButton(resources.getString(R.string.level_move)).setOnClickListener {
            val app: App = activity as App
            app.openDialog(">view learn>$page")
        }
        addButton(resources.getString(R.string.level_forget)).setOnClickListener {
            val app: App = activity as App
            app.openDialog(">view forget>$page")
        }
    }

    private fun defeat(){
        player.unit.healthPt = 1
        player.damaged = player.unit.health - 1
        player.unit.moves.removeAt((0 until player.unit.moves.size).random())
        player.unit.moves.add("flinch")
        endBattle()
        clearButtons()
        addText(resources.getString(R.string.defeat))
        addButton(resources.getString(R.string.proceed)).setOnClickListener {
            val app: App = activity as App
            app.openDialog(defeat)
        }
        scrollView.scrollToBottom()
    }
}