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
    private lateinit var scrollView: ScrollView
    private lateinit var loots: ArrayList<String>
    private lateinit var page: String
    private lateinit var defeat: String
    private lateinit var player: PlayerModel
    private lateinit var special: MoveModel
    private lateinit var allies: ArrayList<UnitModel>
    private lateinit var items: HashMap<String, ItemModel>
    private lateinit var textView: TextView
    private var levelUp = false
    private var button: ArrayList<Button> = ArrayList()
    private var turn = 0
    private var combatants: ArrayList<UnitModel> = ArrayList()

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_combat, container, false)
        button.add(root.findViewById(R.id.combat_action_3))
        button.add(root.findViewById(R.id.combat_action_2))
        button.add(root.findViewById(R.id.combat_action_1))
        scrollView = root.findViewById(R.id.scroll_combat)
        textView = root.findViewById(R.id.text_combat)
        textView.movementMethod = ScrollingMovementMethod()
        player = arguments?.getParcelable("player") ?: return root
        special = arguments?.getSerializable("special") as MoveModel
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
        for (combatant in combatants) {
            if ("sneak" in combatant.skills) {
                combatant.points += 2
            }
        }
        page = arguments?.getString("page") ?: "back"
        defeat = arguments?.getString("defeat") ?: "defeat"
        textView.text = arguments?.getString("desc") ?: ""
        textView.append("\n\n")
        playerTurn(true)
        return root
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

    private fun playerTurn(choosing: Boolean){
        scrollView.scrollToBottom()
        if (!checkBattleEnd()) {
            for (i in 0..2) {
                button[i].text = ""
                button[i].setOnClickListener {}
            }
            if (player.unit.points > 0) {
                if (choosing){
                    button[0].text = player.unit.replaceNouns("Combat")
                    button[0].setOnClickListener {
                        playerTurn(false)
                    }
                    button[1].text = player.unit.replaceNouns("Choose target")
                    button[1].setOnClickListener {
                        for (i in 0..2){
                            if (i < player.unit.foes.size) {
                                val foeName = player.unit.foes[i].flavors["callname"]
                                    ?: player.unit.foes[i].name
                                val foeHealth = player.unit.foes[i].getHealthPercentage()
                                button[i].text = player.unit.replaceNouns("Target $foeName ($foeHealth%)")
                                button[i].setOnClickListener {
                                    player.unit.target = player.unit.foes[i]
                                    playerTurn(true)
                                }
                            } else {
                                button[i].text = ""
                                button[i].setOnClickListener {}
                            }
                        }
                    }
                    if (!player.unit.hasStatus("breath")) {
                        button[2].text = player.unit.replaceNouns(special.text)
                        button[2].setOnClickListener {
                            player.unit.addStatus("breath", -1)
                            player.unit.points = 0
                            turnAction(special, player.unit)
                            playerTurn(true)
                        }
                    }
                } else {
                    for (i in 0..2) {
                        val hand = player.unit.deck!!.getCard(i)
                        if (hand.text.isEmpty()) {
                            button[i].text = ""
                            button[i].setOnClickListener {}
                        } else {
                            button[i].text = player.unit.replaceNouns(hand.text)
                            button[i].setOnClickListener {
                                turnAction(hand, player.unit)
                                if (hand != MoveModel.shuffle) {
                                    player.unit.deck!!.draw(i)
                                }
                                player.unit.deck!!.validate()
                                playerTurn(false)
                            }
                        }
                    }
                }
            } else {
                player.unit.recharge()
                Handler(Looper.getMainLooper()).postDelayed(
                    {
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
            textView.append("\n")
            textView.append(player.unit.replaceNouns(player.unit.processStatus()))
            playerTurn(true)
        } else {
            textView.append(combatants[turn].replaceNouns(combatants[turn].processStatus()))
            enemyTurn(combatants[turn])
        }
    }

    private fun checkBattleEnd(): Boolean{
        for (i in combatants.indices.reversed()){
            if (combatants[i].isDead()){
                if (combatants[i] == player.unit){
                    defeat()
                    return true
                } else {
                    for (combatant in combatants){
                        combatant.foes.remove(combatants[i])
                    }
                    if (player.unit.foes.isEmpty()){
                        victory()
                        return true
                    }
                }
                for (combatant in combatants){
                    if (combatant.target == combatants[i]){
                        combatant.target = combatant.foes[0]
                    }
                }
                if ("self destruct" in combatants[i].skills){
                    textView.append("The ${combatants[i].name} explodes!\n")
                    combatants.removeAt(i)
                    for (combatant in combatants){
                        if (!combatant.hasStatus("range")) {
                            combatant.inflict(-1)
                        }
                    }
                    checkBattleEnd()
                } else {
                    combatants.removeAt(i)
                }
            }
        }
        return false
    }

    private fun endBattle(){
        player.damaged = player.unit.health - player.unit.healthPt
        player.unit.deck = null
        player.unit.target = null
        player.unit.status.clear()
        player.unit.foes.clear()
        for (i in 0..2) {
            button[i].text = ""
            button[i].setOnClickListener {}
        }
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
                textView.append("You find ${Grammar.getArticle(loot)}:\n${items[loot]?.desc ?: "No item description found."}")
                val piece = player.getSamePart(items, loot)
                button[1].text = if (piece == null) {
                    "Pick up $loot"
                } else {
                    "Throw away $piece and pick up $loot."
                }
                button[1].setOnClickListener {
                    player.equipItem(items, loot)
                    victoryProceed()
                }
            }
        }
        button[0].text = resources.getString(R.string.proceed)
        button[0].setOnClickListener {
            victoryProceed()
        }
        scrollView.scrollToBottom()
    }

    private fun victoryProceed(){
        if (levelUp) {
            player.unit.level += 1
            textView.text = ""
            levelUp = false
            textView.append("\n\nYou improve your abilities!")
            button[0].text = resources.getString(R.string.level_move)
            button[0].setOnClickListener {
                val app: App = activity as App
                app.openDialog(">view learn>$page")
            }
            button[1].text = resources.getString(R.string.level_forget)
            button[1].setOnClickListener {
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
        player.damaged = player.unit.health - 1
        endBattle()
        for (equipment in ArrayList(player.equip)){
            if ((0..1).random() == 0) {
                player.unequipItem(items, equipment)
            }
        }
        textView.append(resources.getString(R.string.defeat))
        button[0].text = resources.getString(R.string.proceed)
        button[0].setOnClickListener {
            val app: App = activity as App
            app.openDialog(defeat)
        }
        scrollView.scrollToBottom()
    }
}