package aoa.ui.shop

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import aoa.Billing
import aoa.R
import aoa.ui.IconList

class BuyGemFragment : Fragment() {
    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_list, container, false)
        val listView = root.findViewById(R.id.list_list) as ListView

        val titles = arrayListOf<String?>("Purchase a gem", "Purchase 3 gems", "Purchase 7 gems")
        val subtitles = arrayListOf("2.00 USD", "5.00 USD", "10.00 USD")
        val icons = arrayListOf(R.drawable.ic_1, R.drawable.ic_3, R.drawable.ic_7)
        listView.adapter = IconList(activity as Activity, titles, subtitles, icons)
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            Billing.purchaseReady = true
            Billing.requestPurchase(position)
        }
        return root
    }
}