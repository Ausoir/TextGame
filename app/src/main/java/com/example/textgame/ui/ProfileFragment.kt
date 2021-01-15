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
import com.example.textgame.R
import com.example.textgame.game.DataAccess
import com.example.textgame.game.Model
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject


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
        val player = arguments?.getParcelable<Model>("player") ?: return root
        textView.text = resources.getString(R.string.profile_header, player.getClassName(), player.money, player.getEquipment())
        textView.setOnClickListener { _ ->
            Snackbar.make(root, "${player.getClassDescription()}.", Snackbar.LENGTH_LONG).show()
        }
        val dialog = DataAccess(activity as Context)
        val items = ArrayList<Map<String, String>>()
        player.moves.forEach { move ->
            val moveset: JSONObject = dialog.getMoveset(move)
            val datum = HashMap<String, String>(2)
            datum["Name"] = move
            datum["Description"] = moveset.getString("desc")
            items.add(datum)
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.two_line_list_item, arrayOf("Name", "Description"), intArrayOf(android.R.id.text1, android.R.id.text2))
        listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            Snackbar.make(root, getString(R.string.whatis_ability), Snackbar.LENGTH_LONG).show()
        }
        return root
    }
}