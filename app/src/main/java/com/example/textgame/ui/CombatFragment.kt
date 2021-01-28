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
    private lateinit var enemy: UnitModel
    private lateinit var enemyName: String
    private lateinit var page: String
    private lateinit var player: PlayerModel
    private lateinit var items: HashMap<String, ItemModel>
    private lateinit var textView: TextView
    private var button: ArrayList<Button> = ArrayList()

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
        enemy = arguments?.getSerializable("enemy") as UnitModel
        enemyName = arguments?.getString("enemy name") ?: "The enemy"
        player.unit.target = enemy
        enemy.target = player.unit
        page = arguments?.getString("page") ?: "back"
        textView.text = enemy.desc
        textView.append("\n")
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

    private fun turnAction(move: MoveModel, subject: String, me: UnitModel): Boolean{
        me.points -= 1
        if (move.text.isNotEmpty()) {
            textView.append(replaceNouns("\n${subject.capitalize()} ${move.text}.", subject))
        }
        val feedback = move.perform(me)
        if (feedback == "dead"){
            battleEnd(subject)
            return true
        } else if (feedback == "run"){
            battleEnd("")
            return true
        }
        if (feedback.isNotEmpty()) {
            textView.append("\n${replaceNouns(feedback, subject).capitalize()}")
        }
        return false
    }

    private fun enemyTurn(){
        if (enemy.points > 0) {
            val hand = enemy.deck.getCard(0)
            if (turnAction(hand, "it", enemy)) {
                return
            }
            if (hand != MoveModel.shuffle) {
                enemy.deck.draw(0)
            }
            Handler(Looper.getMainLooper()).postDelayed(
                    {
                        enemyTurn()
                        scrollView.scrollToBottom()
                    }, 100
            )
            return
        }
        textView.append("\n")
        enemy.recharge()
        playerTurn()
    }

    private fun playerTurn(){
        scrollView.scrollToBottom()
        if (player.unit.points > 0){
            for (i in 0..2) {
                val hand = player.unit.deck.getCard(i)
                button[2 - i].text = replaceNouns(hand.text, "you").capitalize()
                button[2 - i].setOnClickListener {
                    if (turnAction(hand, "you", player.unit)) {
                        return@setOnClickListener
                    }
                    if (hand != MoveModel.shuffle) {
                        player.unit.deck.draw(i)
                    }
                    player.unit.deck.validate()
                    playerTurn()
                }
            }
            return
        }
        for (i in 0..2){
            button[i].text = ""
            button[i].setOnClickListener {}
        }
        textView.append("\n")
        Handler(Looper.getMainLooper()).postDelayed(
                {
                    player.unit.recharge()
                    enemyTurn()
                }, 200
        )
    }

    private fun replaceNouns(text: String, subject: String): String{
        var result = text.replace("%?", "none")
        result = result.replace("%w", player.getEquipment(items, "weapon", "fist"))
        if (subject == "you") {
            result = result.replace("%!s", "s")
            result = result.replace("%ur", "you are")
            result = result.replace("%us", "your")
            result = result.replace("%uf", "yourself")
            result = result.replace("%fr", "the $enemyName is")
            result = result.replace("%fs", "the $enemyName's")
            result = result.replace("%es", "")
            result = result.replace("%f", "the $enemyName")
            result = result.replace("%u", "you")
            result = result.replace("%s", "")
        } else {
            result = result.replace("%!s", "")
            result = result.replace("%ur", "it is")
            result = result.replace("%us", "its")
            result = result.replace("%uf", "itself")
            result = result.replace("%fr", "you are")
            result = result.replace("%fs", "your")
            result = result.replace("%es", "es")
            result = result.replace("%f", "you")
            result = result.replace("%u", "it")
            result = result.replace("%s", "s")
        }
        return result
    }

    private fun battleEnd(subject: String){
        when {
            subject.isEmpty() -> {
                val app: App = activity as App
                app.openDialog(page)
            }
            subject == "you" -> {
                victory()
            }
            else -> {
                defeat()
            }
        }
    }

    private fun victory(){
        player.unit.status.removeAll {
            it !in listOf("wounded", "bleeding")
        }
        textView.append(resources.getString(R.string.victory))
        val proceed = button[2]
        val pickup = button[1]
        layout.removeAllViews()
        if (enemy.loot.isNotEmpty()) {
            val loot = enemy.loot.shuffled()[0]
            if (loot != player.item) {
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
}