package com.example.textgame.ui

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
import com.example.textgame.game.ItemModel
import com.example.textgame.game.MoveModel
import com.example.textgame.game.PlayerModel


class ProfileFragment : Fragment() {
    private lateinit var layout: LinearLayout
    private lateinit var listView: ListView
    private lateinit var textView: TextView
    private lateinit var player: PlayerModel
    private lateinit var moveset: ArrayList<MoveModel>
    private lateinit var description: HashMap<String, String>
    private lateinit var itemMap: HashMap<String, ItemModel>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        layout = root.findViewById(R.id.profile_button_container)
        listView = root.findViewById(R.id.profile_container)
        textView = root.findViewById(R.id.text_profile)
        addButton(resources.getString(R.string.profile_button_equipment)).setOnClickListener{
            showEquipment()
        }
        addButton(resources.getString(R.string.profile_button_moveset)).setOnClickListener{
            showMoveset()
        }
        addButton(resources.getString(R.string.profile_button_skill)).setOnClickListener{
            showSkill()
        }
        addButton(resources.getString(R.string.profile_button_companion)).setOnClickListener{
            showCompanion()
        }
        addButton(resources.getString(R.string.profile_button_chapter)).setOnClickListener{
            showChapter()
        }
        val serial = arguments?.getSerializable("moveset") ?: return root
        player = arguments?.getParcelable("player") ?: return root
        description = arguments?.getSerializable("description") as HashMap<String, String>
        itemMap = arguments?.getSerializable("item map") as HashMap<String, ItemModel>
        moveset = serial as ArrayList<MoveModel>
        textView.text = player.getDescription()
        textView.setOnClickListener {}
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

    private fun reset(){
        textView.text = player.getDescription()
        textView.setOnClickListener {}
        listView.onItemClickListener = OnItemClickListener { _, _, _, _ -> }
    }

    private fun showEquipment(){
        reset()
        val items = ArrayList<Map<String, String>>()
        for (i in player.equip){
            val item = HashMap<String, String>(2)
            item["Name"] = i.capitalize() + " (${itemMap[i]?.type ?: "Other"})"
            item["Description"] = itemMap[i]?.desc?.capitalize() ?: ""
            items.add(item)
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.two_line_list_item, arrayOf("Name", "Description"), intArrayOf(android.R.id.text1, android.R.id.text2))
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val itemName = (items[position]["Name"] ?: return@OnItemClickListener).toLowerCase().substringBefore(" (")
            val item = itemMap[itemName] ?: return@OnItemClickListener
            if (item.page != null) {
                textView.text = resources.getString(R.string.use_confirm, itemName)
                textView.setOnClickListener {
                    val app: App = activity as App
                    app.openDialog(item.page)
                }
            } else {
                textView.text = resources.getString(R.string.use_unavailable, itemName)
            }
        }
    }

    private fun showMoveset(){
        reset()
        val items = ArrayList<Map<String, String>>()
        for (i in moveset.indices){
            val item = HashMap<String, String>(2)
            item["Name"] = player.unit.moves[i].capitalize()
            item["Description"] = player.unit.replaceNouns(moveset[i].desc).capitalize()
            items.add(item)
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.two_line_list_item, arrayOf("Name", "Description"), intArrayOf(android.R.id.text1, android.R.id.text2))
    }

    private fun showSkill(){
        reset()
        val items = ArrayList<Map<String, String>>()
        for (i in player.unit.skills){
            val item = HashMap<String, String>(2)
            item["Name"] = i.capitalize()
            item["Description"] = description[i]?: ""
            items.add(item)
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.two_line_list_item, arrayOf("Name", "Description"), intArrayOf(android.R.id.text1, android.R.id.text2))
    }

    private fun showCompanion(){
        reset()
        val items = ArrayList<Map<String, String>>()
        for (i in player.companion){
            val item = HashMap<String, String>(2)
            item["Name"] = i.capitalize()
            item["Description"] = description[i]?: ""
            items.add(item)
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.simple_list_item_1, arrayOf("Name"), intArrayOf(android.R.id.text1))
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val selected = items[position]["Name"]
            textView.text = resources.getString(R.string.kick_confirm, selected)
            textView.setOnClickListener {
                val app: App = activity as App
                app.openDialog(">drop>$selected>")
            }
        }
    }

    private fun showChapter(){
        reset()
        val items = ArrayList<Map<String, String>>()
        val chapter = player.vars["chapter"] ?: "0"
        for (i in (0..chapter.toInt())){
            val item = HashMap<String, String>(2)
            item["Name"] = "Chapter $i"
            items.add(item)
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.simple_list_item_1, arrayOf("Name"), intArrayOf(android.R.id.text1))
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val selected = items[position]["Name"]
            textView.text = resources.getString(R.string.chapter_confirm, selected)
            textView.setOnClickListener {
                val app: App = activity as App
                app.openDialog(">save>chapter>$position>>refresh chapter>start")
            }
        }
    }
}