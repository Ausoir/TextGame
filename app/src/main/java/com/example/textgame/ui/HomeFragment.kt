package com.example.textgame.ui

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.textgame.App
import com.example.textgame.R
import com.example.textgame.game.DataAccess
import com.example.textgame.game.Model


class HomeFragment : Fragment() {
    private lateinit var layout: LinearLayout
    private lateinit var dialog: DataAccess

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        textView.movementMethod = ScrollingMovementMethod()
        layout = root.findViewById(R.id.home_button_container) as LinearLayout
        val page = arguments?.getParcelable<Model>("player")?.page ?: "Start"

        dialog = DataAccess(activity as Context)
        val currentPage = dialog.getPage(page)
        textView.text = currentPage.getString("text")
        val options = currentPage.getJSONObject("opt")
        val keys = options.keys()
        for (i in 0 until options.length()) {
            val option = keys.next()
            addButton(options.getString(option)).setOnClickListener {
                val app: App = activity as App
                app.updatePage(option)
                app.refresh()
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