package aoa.ui.shop

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import aoa.R
import aoa.game.PlayerModel
import aoa.ui.EquipmentUI
import com.google.android.material.snackbar.Snackbar

class BuyFragment : EquipmentUI() {
    lateinit var player: PlayerModel
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        itemList = arguments?.getStringArrayList("stock")!!
        val root = super.onCreateView(inflater, container, savedInstanceState)

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val itemName = titles[position].toLowerCase().substringBefore(" (")
            val item = itemMap[itemName] ?: return@OnItemClickListener
            val overlap = player.getSamePart(itemMap, item.name)
            if (overlap == null) {
                if (player.money >= item.value) {
                    AlertDialog.Builder(activity)
                            .setTitle(R.string.buy)
                            .setMessage(resources.getString(R.string.buy_confirm, itemName, item.value, "g"))
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                player.money -= item.value
                                player.equipItem(itemMap, itemName)
                                itemList.removeAt(position)
                                val feedback = resources.getString(R.string.buy_done, itemName, item.value, "g").capitalize()
                                Snackbar.make(listView, feedback, Snackbar.LENGTH_LONG).show()
                            }
                            .setNegativeButton(android.R.string.cancel) { _, _ -> return@setNegativeButton }
                            .show()
                } else {
                    val feedback = resources.getString(R.string.buy_fail, "gold").capitalize()
                    Snackbar.make(listView, feedback, Snackbar.LENGTH_LONG).show()
                }
            } else {
                val feedback = resources.getString(R.string.buy_full, overlap).capitalize()
                Snackbar.make(listView, feedback, Snackbar.LENGTH_LONG).show()
            }
        }
        return root
    }
}