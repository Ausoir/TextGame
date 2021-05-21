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

class SellFragment : EquipmentUI() {
    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val player = arguments?.getParcelable<PlayerModel>("player")!!
        itemList = player.equip
        val root = super.onCreateView(inflater, container, savedInstanceState)

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val itemName = titles[position].toLowerCase().substringBefore(" (")
            val item = itemMap[itemName] ?: return@OnItemClickListener
            if (item.value < 1000) {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.sell)
                        .setMessage(resources.getString(R.string.sell_confirm, itemName, item.value, "g"))
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            val selling = itemMap[player.unequipItem(itemMap, itemName)]
                                    ?: return@setPositiveButton
                            player.money += selling.value
                            val feedback = resources.getString(R.string.sell_done, itemName, item.value, "g").capitalize()
                            Snackbar.make(listView, feedback, Snackbar.LENGTH_LONG).show()
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> return@setNegativeButton }
                        .show()
            } else {
                val feedback = resources.getString(R.string.sell_fail)
                Snackbar.make(listView, feedback, Snackbar.LENGTH_LONG).show()
            }
        }
        return root
    }
}