package com.example.textgame.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import com.example.textgame.App
import com.example.textgame.R
import com.example.textgame.game.PlayerModel


class QuestFragment : Fragment() {
    private lateinit var layout: LinearLayout
    private lateinit var listView: ListView
    private lateinit var textView: TextView
    private lateinit var player: PlayerModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        layout = root.findViewById(R.id.profile_button_container)
        listView = root.findViewById(R.id.profile_container)
        textView = root.findViewById(R.id.text_profile)
        textView.text = resources.getString(R.string.quest)
        addButton(resources.getString(R.string.leave)).setOnClickListener{
            val app: App = activity as App
            app.openDialog(arguments?.getString("page") ?: ">back")
        }
        listView.onItemClickListener = OnItemClickListener { _, _, _, _ -> }
        player = arguments?.getParcelable("player") ?: return root
        fillList()
        return root
    }

    private fun addButton(name: String) : Button {
        val button = Button(activity)
        button.text = name
        button.textSize = 22f
        button.setPadding(20, 0, 20, 0)
        layout.addView(button, 0)
        return button
    }

    private fun fillList(){
        val items = ArrayList<Map<String, String>>()
        for (i in player.quests){
            val item = HashMap<String, String>(2)
            val split = i.split(">")
            item["Name"] = split[0]
            item["Page"] = split[1]
            item["Description"] = split[2]
            items.add(item)
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.two_line_list_item, arrayOf("Name", "Description"), intArrayOf(android.R.id.text1, android.R.id.text2))
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            AlertDialog.Builder(activity)
                .setTitle(R.string.quest_title)
                .setMessage(resources.getString(R.string.quest_confirm))
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    player.quests.removeAt(position)
                    val app: App = activity as App
                    app.openDialog(items[position]["Page"] ?: ">back")
                }
                .setNegativeButton(android.R.string.no) { _, _ -> return@setNegativeButton }
                .show()
         }
    }
}