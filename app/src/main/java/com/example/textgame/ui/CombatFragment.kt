package com.example.textgame.ui

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
    private lateinit var loots: ArrayList<String>
    private lateinit var page: String
    private lateinit var defeat: String
    private lateinit var player: PlayerModel
    private lateinit var allies: ArrayList<UnitModel>
    private lateinit var items: HashMap<String, ItemModel>
    private lateinit var textView: TextView
    private var isTargetingMode = false
    private var levelUp = false
    private var buttons: ArrayList<Button> = ArrayList()
    private var turn = -1
    private var round = 0
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
        items = arguments?.getSerializable("items") as HashMap<String, ItemModel>
        player.unit.foes = arguments?.getSerializable("enemies") as ArrayList<UnitModel>
        allies = arguments?.getSerializable("allies") as ArrayList<UnitModel>
        loots = arguments?.getStringArrayList("loots") as ArrayList<String>
        player.unit.target = player.unit.foes[0]
        var idCount = 1
        var nthEnemy = 0
        for (enemy in player.unit.foes){
            enemy.id = idCount++
            var count = 0
            for (foe in player.unit.foes) {
                if (foe.name == enemy.name) {
                    count += 1
                }
            }
            if (count > 1){
                nthEnemy += 1
                enemy.flavors["callname"] = Grammar.getOrderNumber(nthEnemy) + " ${enemy.name}"
            }
            val targetables = ArrayList<UnitModel>()
            targetables.addAll(allies)
            targetables.add(player.unit)
            enemy.target = targetables.shuffled()[0]
            enemy.foes = targetables
            if (enemy.level > player.unit.level){
                levelUp = true
            }
        }
        for (ally in allies){
            ally.id = idCount++
            ally.flavors["ally"] = "true"
            ally.target = player.unit.target
            ally.foes = player.unit.foes
        }
        combatants.add(player.unit)
        combatants.addAll(allies)
        combatants.addAll(player.unit.foes)
        for (i in combatants.indices) {
            combatants[i].position = i
            if ("sneak" in combatants[i].skills) {
                combatants[i].points += 2
            } else if ("manacharge" in combatants[i].skills) {
                combatants[i].mana += 2
            }
        }
        page = arguments?.getString("page") ?: "back"
        defeat = arguments?.getString("defeat") ?: "defeat"
        textView.text = arguments?.getString("desc") ?: ""
        textView.append("\n\n")
        nextTurn()
        return root
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

    private fun hideButtons(){
        for (button in buttons){
            button.text = ""
            button.setOnClickListener{}
        }
    }

    private fun ScrollView.scrollToBottom() {
        post{
            smoothScrollTo(0, textView.layout.getLineTop(textView.lineCount))
        }
    }

    private fun turnAction(move: MoveModel, me: UnitModel){
        me.points -= 1
        val feedback = move.perform(me, combatants)
        if (feedback.isNotEmpty()) {
            textView.append(me.replaceNouns(feedback))
        }
    }

    private fun enemyTurn(enemy: UnitModel){
        if (!checkBattleEnd()) {
            if (enemy.points > 0) {
                val hand = enemy.deck!!.getCard(0)
                turnAction(hand, enemy)
                if (hand != MoveModel.shuffle) {
                    enemy.deck!!.draw(0)
                }
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        enemyTurn(enemy)
                    }, 200
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
            val foeName = (player.unit.foes[i].flavors["callname"]
                    ?: player.unit.foes[i].name).capitalize()
            val foeHealth = player.unit.foes[i].getHealthPercentage()
            val text = if (player.unit.foes[i].block > 0) {
                player.unit.replaceNouns("Target $foeName ($foeHealth% +${player.unit.foes[i].block})")
            } else {
                player.unit.replaceNouns("Target $foeName ($foeHealth%)")
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
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        hideButtons()
                        nextTurn()
                    }, 200
                )
            }
        }
    }

    private fun nextTurn(){
        turn += 1
        if (turn >= combatants.size) {
            turn = 0
        }
        if (combatants[turn] == player.unit){
            round += 1
            textView.append("--- Round $round ---\n${player.unit.speed} Turns, ${player.unit.healthPt} Health, ${player.unit.block} Block\n")
            textView.append(player.unit.replaceNouns(player.unit.processStatus()))
            playerTurn()
        } else {
            textView.append(combatants[turn].replaceNouns(combatants[turn].processStatus()))
            enemyTurn(combatants[turn])
        }
    }

    private fun checkBattleEnd(): Boolean{
        var result = 0
        for (i in combatants.indices.reversed()){
            if (combatants[i].isDead()){
                if (combatants[i] == player.unit){
                    result = -1
                } else {
                    for (combatant in combatants){
                        combatant.foes.remove(combatants[i])
                    }
                    if (player.unit.foes.isEmpty()){
                        result = 1
                    }
                }
                for (combatant in combatants){
                    if (combatant.target == combatants[i] && combatant.foes.isNotEmpty()){
                        combatant.target = combatant.foes[0]
                    }
                }
                if ("self destruct" in combatants[i].skills){
                    textView.append("The ${combatants[i].name} explodes, dealing 1 damage to everything near it!\n")
                    for (combatant in combatants){
                        if (combatant != combatants[i] && combatant.position == combatants[i].position) {
                            textView.append(combatant.inflict(-1))
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
        player.unit.deck = null
        player.unit.target = null
        player.unit.status.clear()
        player.unit.foes.clear()
        clearButtons()
    }

    private fun victory(){
        endBattle()
        textView.append(resources.getString(R.string.victory))
        if ("herbal remedy" in player.unit.skills && player.damaged > 0){
            player.damaged -= 1
        }
        if (loots.isNotEmpty()) {
            val loot = loots.shuffled()[0]
            if (loot.isNotEmpty()) {
                textView.append("You find ${Grammar.getArticle(loot)}:\n${items[loot]?.desc ?: Grammar.getArticle(loot)}")
                val piece = player.getSamePart(items, loot)
                val text = if (piece == null) {
                    "Pick up $loot"
                } else {
                    "Throw away $piece and pick up $loot."
                }
                addButton(text).setOnClickListener {
                    player.equipItem(items, loot)
                    victoryProceed()
                }
            }
        }
        addButton(resources.getString(R.string.proceed)).setOnClickListener {
            victoryProceed()
        }
        scrollView.scrollToBottom()
    }

    private fun victoryProceed(){
        if (levelUp) {
            clearButtons()
            player.unit.level += 1
            textView.text = ""
            levelUp = false
            textView.append("\n\nYou improve your abilities!")
            addButton(resources.getString(R.string.level_move)).setOnClickListener {
                val app: App = activity as App
                app.openDialog(">view learn>$page")
            }
            addButton(resources.getString(R.string.level_forget)).setOnClickListener {
                val app: App = activity as App
                app.openDialog(">view forget>$page")
            }
        } else {
            player.unit.level -= 1
            val app: App = activity as App
            app.openDialog(page)
        }
    }

    private fun defeat(){
        player.unit.healthPt = 1
        player.damaged = player.unit.health - 1
        endBattle()
        player.unit.moves.removeAt((0 until player.unit.moves.size).random())
        player.unit.moves.add("flinch")
        textView.append(resources.getString(R.string.defeat))
        addButton(resources.getString(R.string.proceed)).setOnClickListener {
            val app: App = activity as App
            app.openDialog(defeat)
        }
        scrollView.scrollToBottom()
    }
}