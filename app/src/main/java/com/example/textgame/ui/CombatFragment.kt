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

@SuppressLint("DefaultLocale")
class CombatFragment : Fragment() {
    private lateinit var layout: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var loots: ArrayList<String>
    private lateinit var page: String
    private lateinit var player: PlayerModel
    private lateinit var items: HashMap<String, ItemModel>
    private lateinit var textView: TextView
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
        button.add(root.findViewById(R.id.combat_action_1))
        button.add(root.findViewById(R.id.combat_action_2))
        button.add(root.findViewById(R.id.combat_action_3))
        scrollView = root.findViewById(R.id.scroll_combat)
        textView = root.findViewById(R.id.text_combat)
        textView.movementMethod = ScrollingMovementMethod()
        layout = root.findViewById(R.id.combat_button_container) as LinearLayout
        player = arguments?.getParcelable("player") ?: return root
        items = arguments?.getSerializable("items") as HashMap<String, ItemModel>
        player.unit.foes = arguments?.getSerializable("enemies") as ArrayList<UnitModel>
        loots = arguments?.getStringArrayList("loots") as ArrayList<String>
        player.unit.target = player.unit.foes[0]
        for (enemy in player.unit.foes){
            enemy.target = player.unit
            enemy.foes = ArrayList()
            enemy.foes.add(player.unit)
        }
        combatants.add(player.unit)
        combatants.addAll(player.unit.foes)
        page = arguments?.getString("page") ?: "back"
        textView.text = arguments?.getString("desc") ?: ""
        textView.append("\n\n")
        if ("sneak" in player.unit.moves && player.unit.points > 0){
            player.unit.points += 2
        }
        playerTurn()
        return root
    }

    private fun ScrollView.scrollToBottom() {
        post{
            smoothScrollTo(0, textView.layout.getLineTop(textView.lineCount))
        }
    }

    private fun turnAction(move: MoveModel, me: UnitModel){
        me.points -= 1
        var text = ""
        if (move.text.isNotEmpty()) {
            text = "%u ${move.text}.\n"
        }
        val feedback = move.perform(me, combatants)
        when {
            feedback == "run" -> {
                turn = -1
                me.points = 0
            }
            feedback.isNotEmpty() -> {
                text += feedback
                textView.append(replaceNouns(text, me))
            }
            else -> {
                textView.append(replaceNouns(text, me))
            }
        }
    }

    private fun enemyTurn(enemy: UnitModel){
        val hand = enemy.deck!!.getCard(0)
        turnAction(hand, enemy)
        if (hand != MoveModel.shuffle) {
            enemy.deck!!.draw(0)
        }
        Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (!checkBattleEnd()) {
                        if (enemy.points > 0) {
                            enemyTurn(enemy)
                        } else {
                            scrollView.scrollToBottom()
                            enemy.recharge()
                            nextTurn()
                        }
                    }
                }, 100
        )
    }

    private fun playerTurn(){
        scrollView.scrollToBottom()
        if (player.unit.points > 0){
            for (i in 0..2) {
                val hand = player.unit.deck!!.getCard(i)
                val foe = hand.getValidTargets(player.unit).shuffled()[0]
                player.unit.target = foe
                button[2 - i].text = replaceNouns(hand.text, player.unit)
                button[2 - i].setOnClickListener {
                    player.unit.target = foe
                    turnAction(hand, player.unit)
                    if (hand != MoveModel.shuffle) {
                        player.unit.deck!!.draw(i)
                    }
                    player.unit.deck!!.validate()
                    if (!checkBattleEnd()) {
                        playerTurn()
                    }
                }
            }
        } else {
            player.unit.recharge()
            for (i in 0..2) {
                button[i].text = ""
                button[i].setOnClickListener {}
            }
            Handler(Looper.getMainLooper()).postDelayed(
                    {
                        nextTurn()
                    }, 400
            )
        }
    }

    private fun nextTurn(){
        if (turn == -1){
            escape()
        }
        turn += 1
        if (turn >= combatants.size) {
            turn = 0
        }
        if (combatants[turn] == player.unit){
            textView.append("\n")
            player.unit.processStatus()
            playerTurn()
        } else {
            combatants[turn].processStatus()
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
                    player.unit.foes.remove(combatants[i])
                    combatants.removeAt(i)
                    if (player.unit.foes.isEmpty()){
                        victory()
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun getEnemyPronoun(subject: UnitModel?): String{
        var finalSubject = subject
        if (finalSubject == player.unit){
            finalSubject = player.unit.target
        }
        if (finalSubject == null){
            return "nothing"
        }
        var count = 0
        for (enemy in player.unit.foes){
            if (enemy.name == finalSubject.name){
                count += 1
            }
        }
        if (count > 1){
            when {
                finalSubject == player.unit.foes.first() -> {
                    return "${finalSubject.name} on the left"
                }
                finalSubject == player.unit.foes.last() -> {
                    return "${finalSubject.name} on the right"
                }
                count == 3 -> {
                    return "${finalSubject.name} in the middle"
                }
                count > 3 -> {
                    return "one of the ${finalSubject.name}"
                }
            }
        }
        return finalSubject.name
    }

    private fun replaceNouns(text: String, subject: UnitModel): String{
        var result = text.replace("%?", "none")
        result = result.replace("%w", player.getEquipment(items, "weapon", "fist"))
        val enemyName = getEnemyPronoun(subject)
        val playerPronouns = listOf("s", "you are", "your", "yourself", "you", "you")
        val enemyPronouns = listOf("", "it is", "its", "itself", "the $enemyName", "it")
        val pronoun = ArrayList<String>()
        if (subject == player.unit){
            pronoun.addAll(playerPronouns)
            pronoun.addAll(enemyPronouns)
        } else {
            pronoun.addAll(enemyPronouns)
            pronoun.addAll(playerPronouns)
        }
        result = result.replace("%!s", pronoun[0])
        result = result.replace("%ur", pronoun[1])
        result = result.replace("%us", pronoun[2])
        result = result.replace("%uf", pronoun[3])
        result = result.replaceFirst("%u", pronoun[4])
        result = result.replace("%u", pronoun[5])
        result = result.replace("%s", pronoun[6])
        result = result.replace("%fr", pronoun[7])
        result = result.replace("%fs", pronoun[8])
        result = result.replace("%ff", pronoun[9])
        result = result.replaceFirst("%f", pronoun[10])
        result = result.replace("%f", pronoun[11])
        return result.lines().joinToString (separator = "\n") {
            it.capitalize()
        }
    }

    private fun victory(){
        player.unit.status.removeAll {
            it !in listOf("wounded")
        }
        textView.append(resources.getString(R.string.victory))
        val proceed = button[2]
        val pickup = button[1]
        layout.removeAllViews()
        if (loots.isNotEmpty()) {
            val loot = loots.shuffled()[0]
            if (loot.isNotEmpty() && loot != player.item) {
                pickup.text = if (player.item.isEmpty()) {
                    "Pick up $loot"
                } else {
                    "Throw away ${player.item} and pick up $loot."
                }
                pickup.setOnClickListener {
                    player.item = loot
                    val app: App = activity as App
                    app.openDialog(page)
                }
            }
        }
        proceed.text = resources.getString(R.string.proceed)
        proceed.setOnClickListener {
            val app: App = activity as App
            app.openDialog(page)
        }
        layout.addView(proceed)
        layout.addView(pickup)
        scrollView.scrollToBottom()
    }

    private fun defeat(){
        textView.append(resources.getString(R.string.defeat))
        val proceed = button[2]
        layout.removeAllViews()
        proceed.text = resources.getString(R.string.proceed)
        proceed.setOnClickListener {
            val app: App = activity as App
            app.openDialog("defeat")
        }
        layout.addView(proceed)
        scrollView.scrollToBottom()
    }

    private fun escape(){
        textView.append(resources.getString(R.string.escape))
        val proceed = button[2]
        layout.removeAllViews()
        proceed.text = resources.getString(R.string.proceed)
        proceed.setOnClickListener {
            val app: App = activity as App
            app.openDialog(page)
        }
        layout.addView(proceed)
        scrollView.scrollToBottom()
    }
}