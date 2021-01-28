package com.example.textgame.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.textgame.App
import com.example.textgame.R
import com.example.textgame.game.MoveModel
import com.example.textgame.game.PlayerModel
import com.google.android.material.snackbar.Snackbar


class ProfileFragment : Fragment() {
    private lateinit var listView: ListView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        listView = root.findViewById(R.id.profile_container) as ListView
        val textView: TextView = root.findViewById(R.id.text_profile)
        val player = arguments?.getParcelable<PlayerModel>("player") ?: return root
        val serial = arguments?.getSerializable("moveset") ?: return root
        val moveset = serial as ArrayList<MoveModel>
        textView.text = player.getDescription()
        textView.setOnClickListener { _ ->
            val app: App = activity as App
            app.viewEquipment()
        }
        val items = ArrayList<Map<String, String>>()
        for (i in moveset.indices){
            val datum = HashMap<String, String>(2)
            datum["Name"] = player.unit.moves[i].capitalize()
            datum["Description"] = moveset[i].desc
            items.add(datum)
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.two_line_list_item, arrayOf("Name", "Description"), intArrayOf(android.R.id.text1, android.R.id.text2))
        listView.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            Snackbar.make(root, getString(R.string.whatis_ability), Snackbar.LENGTH_LONG).show()
        }
        return root
    }
}