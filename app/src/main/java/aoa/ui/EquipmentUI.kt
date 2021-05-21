package aoa.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import aoa.R
import aoa.game.ItemModel

open class EquipmentUI : Fragment() {
    private val iconMap = mapOf(
            "weapon" to R.drawable.ic_sword,
            "offhand" to R.drawable.ic_hand,
            "item" to R.drawable.ic_consumable,
            "food" to R.drawable.ic_food,
            "sigil" to R.drawable.ic_sigil
    )
    lateinit var listView: ListView
    lateinit var titles: ArrayList<String>
    lateinit var itemMap: HashMap<String, ItemModel>
    lateinit var itemList: ArrayList<String>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_list, container, false)
        listView = root.findViewById(R.id.list_list) as ListView
        @Suppress("UNCHECKED_CAST")
        itemMap = arguments?.getSerializable("item map") as HashMap<String, ItemModel>

        titles = ArrayList()
        val subtitles = ArrayList<String>()
        val icons = ArrayList<Int>()
        for (i in itemList) {
            val itemMapped = itemMap[i] ?: itemMap["invalid"]!!
            titles.add(i.capitalize() + " (${itemMapped.type})")
            subtitles.add(itemMapped.getDescription())
            icons.add(iconMap[itemMapped.type] ?: R.drawable.ic_armor)
        }
        listView.adapter = IconList(activity as Activity, titles.toTypedArray(), subtitles.toTypedArray(), icons.toTypedArray())
        return root
    }
}