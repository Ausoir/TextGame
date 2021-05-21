package aoa.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.fragment.app.Fragment
import aoa.R
import aoa.game.MoveModel

class MovesetFragment : Fragment() {
    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_list, container, false)
        val listView = root.findViewById(R.id.list_list) as ListView
        val moveset = arguments?.getSerializable("moveset") as ArrayList<MoveModel>

        val items = ArrayList<Map<String, String>>()
        for (i in moveset.indices){
            val item = HashMap<String, String>(2)
            item["Name"] = moveset[i].name.capitalize()
            item["Description"] = moveset[i].desc.capitalize()
            items.add(item)
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.two_line_list_item, arrayOf("Name", "Description"), intArrayOf(android.R.id.text1, android.R.id.text2))
        return root
    }
}