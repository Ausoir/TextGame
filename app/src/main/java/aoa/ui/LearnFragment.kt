package aoa.ui

import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import aoa.App
import aoa.R
import aoa.game.MoveModel
import kotlin.math.min


class LearnFragment : HtmlTextFragment() {
    private lateinit var layout: LinearLayout

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        textView = root.findViewById(R.id.text_home)
        textView.movementMethod = ScrollingMovementMethod()
        layout = root.findViewById(R.id.home_button_container) as LinearLayout
        val page = arguments?.getString("page") ?: "invalid"
        val moveMap = arguments?.getSerializable("moveMap") as HashMap<String, MoveModel>
        val moves = arguments?.getSerializable("moves") as Array<String>
        val forget = arguments?.getBoolean("forget")
        if (forget == null || !forget) {
            addText(resources.getString(R.string.learn_move))
            for (move in moves.reversed()) {
                val text = moveMap[move]?.desc
                if (text != null) {
                    addText("<b><i>${move.capitalize()}</i></b>: $text\n\n")
                }
                addButton("Learn <b><i>${move.capitalize()}</i></b>").setOnClickListener {
                    val app: App = activity as App
                    app.openDialog(">learn>$move>$page")
                }
            }
        } else {
            if (moves.size <= 1){
                addText(resources.getString(R.string.forget_move_limit))
                addButton("But.. my BUILD...!").setOnClickListener {
                    val app: App = activity as App
                    app.openDialog(page)
                }
            } else {
                addText(resources.getString(R.string.forget_move))
                for (i in (0..min(2, moves.size - 1)).reversed()) {
                    val move = moves[i]
                    val text = moveMap[move]?.desc
                    if (text != null) {
                        addText("<${move.capitalize()}>: $text\n\n")
                    }
                    addButton("Forget <b><i>${move.capitalize()}</i></b>").setOnClickListener {
                        val app: App = activity as App
                        app.openDialog(">forget>$move>$page")
                    }
                }
            }
        }
        return root
    }

    private fun addButton(name: String) : Button {
        val button = Button(activity)
        button.text = Html.fromHtml(name)
        layout.addView(button, 0)
        return button
    }
}