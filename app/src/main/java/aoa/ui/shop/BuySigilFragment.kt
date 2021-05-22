package aoa.ui.shop

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import aoa.R
import aoa.tools.Grammar
import aoa.ui.EquipmentUI
import com.google.android.material.snackbar.Snackbar

class BuySigilFragment : EquipmentUI() {
    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val itemName = itemList[position]
            val item = itemMap[itemName] ?: return@OnItemClickListener
            val overlap = player.getSamePart(itemMap, item.name)
            if (overlap == null) {
                if (player.real >= item.gem) {
                    AlertDialog.Builder(activity)
                            .setTitle(R.string.buy)
                            .setMessage(resources.getString(R.string.buy_confirm, itemName, item.gem, Grammar.getPlural(" Gem", item.gem)))
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                player.real -= item.gem
                                player.equipItem(itemMap, itemName)
                                itemList.removeAt(position)
                                val feedback = resources.getString(R.string.buy_done, itemName, item.gem, Grammar.getPlural(" Gem", item.gem)).capitalize()
                                Snackbar.make(listView, feedback, Snackbar.LENGTH_LONG).show()
                            }
                            .setNegativeButton(android.R.string.cancel) { _, _ -> return@setNegativeButton }
                            .show()
                } else {
                    val feedback = resources.getString(R.string.buy_fail, "Gems").capitalize()
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