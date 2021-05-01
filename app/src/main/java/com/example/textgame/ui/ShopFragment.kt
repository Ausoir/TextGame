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
import com.example.textgame.game.ItemModel
import com.example.textgame.game.PlayerModel


class ShopFragment : Fragment() {
    private lateinit var layout: LinearLayout
    private lateinit var listView: ListView
    private lateinit var textView: TextView
    private lateinit var player: PlayerModel
    private lateinit var itemMap: HashMap<String, ItemModel>
    private lateinit var itemList: MutableList<String>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        layout = root.findViewById(R.id.profile_button_container)
        listView = root.findViewById(R.id.profile_container)
        textView = root.findViewById(R.id.text_profile)
        addButton(resources.getString(R.string.leave)).setOnClickListener{
            val app: App = activity as App
            app.openDialog(arguments?.getString("page") ?: ">to>page")
        }
        addButton(resources.getString(R.string.sell)).setOnClickListener{
            showSell()
        }
        addButton(resources.getString(R.string.buy)).setOnClickListener{
            showBuy()
        }
        player = arguments?.getParcelable("player") ?: return root
        itemList = arguments?.getStringArrayList("stock") ?: return root
        itemMap = arguments?.getSerializable("item map") as HashMap<String, ItemModel>
        reset()
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
        textView.text = resources.getString(R.string.money, player.money)
        textView.setOnClickListener {}
        listView.onItemClickListener = OnItemClickListener { _, _, _, _ -> }
    }

    private fun showBuy(){
        reset()
        activity?.title = resources.getString(R.string.buy)
        val items = ArrayList<Map<String, String>>()
        for (i in itemList){
            val item = HashMap<String, String>(2)
            item["Name"] = i.capitalize() + " (${itemMap[i]?.type ?: "Other"})"
            item["Description"] = itemMap[i]?.desc?.capitalize() ?: ""
            items.add(item)
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.two_line_list_item, arrayOf("Name", "Description"), intArrayOf(android.R.id.text1, android.R.id.text2))
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val itemName = (items[position]["Name"] ?: return@OnItemClickListener).toLowerCase().substringBefore(" (")
            val item = itemMap[itemName] ?: return@OnItemClickListener
            AlertDialog.Builder(activity)
                .setTitle(R.string.buy)
                .setMessage(resources.getString(R.string.buy_confirm, itemName, item.value))
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    player.money -= item.value
                    player.equipItem(itemMap, itemName)
                    itemList.removeAt(position)
                    showBuy()
                    textView.text = resources.getString(R.string.buy_done, itemName, item.value).capitalize()
                }
                .setNegativeButton(android.R.string.no) { _, _ -> return@setNegativeButton }
                .show()
         }
    }

    private fun showSell(){
        reset()
        activity?.title = resources.getString(R.string.sell)
        val items = ArrayList<Map<String, String>>()
        for (i in player.equip){
            val item = HashMap<String, String>(2)
            val itemMapped = itemMap[i] ?: itemMap["invalid"]!!
            if (itemMapped.value < 1000) {
                item["Name"] = i.capitalize() + " (${itemMapped.type})"
                item["Description"] = itemMapped.desc.capitalize()
                items.add(item)
            }
        }
        listView.adapter = SimpleAdapter(activity as Context, items, android.R.layout.two_line_list_item, arrayOf("Name", "Description"), intArrayOf(android.R.id.text1, android.R.id.text2))
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val itemName = (items[position]["Name"] ?: return@OnItemClickListener).toLowerCase().substringBefore(" (")
            val item = itemMap[itemName] ?: return@OnItemClickListener
            AlertDialog.Builder(activity)
                .setTitle(R.string.sell)
                .setMessage(resources.getString(R.string.sell_confirm, itemName, item.value))
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    val selling = itemMap[player.unequipItem(itemMap, itemName)] ?: return@setPositiveButton
                    player.money += selling.value
                    showSell()
                    textView.text = resources.getString(R.string.sell_done, itemName, item.value).capitalize()
                }
                .setNegativeButton(android.R.string.no) { _, _ -> return@setNegativeButton }
                .show()
        }
    }
}