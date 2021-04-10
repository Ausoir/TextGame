package com.example.textgame.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.example.textgame.App
import com.example.textgame.R
import com.example.textgame.game.SpecialModel
import com.example.textgame.tools.Grammar


class AdvanceFragment : CustomFragment() {
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
        val specMap = arguments?.getSerializable("specMap") as HashMap<String, SpecialModel>
        val specString = arguments?.getString("spec") ?: "basic"
        val branch = arguments?.getStringArray("branch") as Array<String>?
        val spec = specMap[specString]!!
        if (spec.branch.isNullOrEmpty()){
            addText("You have reached the end of your specialization.\nYou cannot advance your class any further!")
            addButton(resources.getString(R.string.proceed)).setOnClickListener {
                val app: App = activity as App
                app.openDialog(page)
            }
        } else {
            addText(
                    "<color:#709570>Your current class:\n\n${
                        spec.getDescription(
                                specString
                        )
                    }\n\n<color:#709570>Available specializations:\n\n"
            )
            val available = branch ?: spec.branch
            addButton("Stay as $specString").setOnClickListener {
                val app: App = activity as App
                app.openDialog(page)
            }
            for (adv in available) {
                val advSpec = specMap[adv]
                val text = advSpec?.getDescription(adv) ?: "INVALID"
                val flavor = advSpec?.flavor ?: "INVALID"
                addText("$text\n\n")
                addButton("Become ${Grammar.getArticle(adv)}").setOnClickListener {
                    val app: App = activity as App
                    app.openDialog(">custom>$flavor>>specialize>$adv>$page")
                }
            }
        }
        return root
    }

    private fun addButton(name: String) : Button {
        val button = Button(activity)
        button.text = name
        layout.addView(button, 0)
        return button
    }
}