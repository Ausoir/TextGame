package aoa.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import aoa.App
import aoa.R
import aoa.ui.EquipmentUI
import com.google.android.material.snackbar.Snackbar




class EquipmentFragment : EquipmentUI() {
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
            if (item.page != null) {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.use)
                        .setMessage(resources.getString(R.string.use_confirm, itemName))
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            val app: App = activity as App
                            app.openDialog(item.page)
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> return@setNegativeButton }
                        .show()
            } else {
                val feedback = resources.getString(R.string.use_unavailable, itemName).capitalize()
                Snackbar.make(listView, feedback, Snackbar.LENGTH_LONG).show()
            }
        }
        return root
    }
}