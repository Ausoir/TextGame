package aoa.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import aoa.R
import aoa.game.PlayerModel
import aoa.ui.TabAdapter
import aoa.ui.profile.PlayerFragment
import com.google.android.material.tabs.TabLayout
import java.io.Serializable


class ShopFragment : Fragment() {
    private lateinit var player: PlayerModel
    private lateinit var itemMap: Serializable
    private lateinit var itemList: ArrayList<String>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.tabbed_fragment, container, false)
        val tabLayout = root.findViewById(R.id.tab_layout) as TabLayout
        val pager = root.findViewById(R.id.pager) as ViewPager
        val adapter = TabAdapter(parentFragmentManager)
        player = arguments?.getParcelable("player")!!
        itemMap = arguments?.getSerializable("item map")!!
        itemList = arguments?.getStringArrayList("stock")!!
        adapter.addFragment("", makeFragment(PlayerFragment(), arrayOf("player")))
        adapter.addFragment("Buy", makeFragment(BuyFragment(), arrayOf("player", "item map", "stock")))
        adapter.addFragment("Sell", makeFragment(SellFragment(), arrayOf("player", "item map", "equip")))
        pager.adapter = adapter
        tabLayout.setupWithViewPager(pager)
        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_player)
        tabLayout.addOnTabSelectedListener (object: TabLayout.OnTabSelectedListener{
            private val titles = arrayOf("You", "Buy", "Sell")
            override fun onTabSelected(p0: TabLayout.Tab?) {
                activity!!.title = titles[p0!!.position]
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {}

            override fun onTabReselected(p0: TabLayout.Tab?) {}
        })
        return root
    }

    private fun makeFragment(fragment: Fragment, arguments: Array<String>) : Fragment {
        val bundle = Bundle()
        for (arg in arguments){
            when (arg){
                "player" -> bundle.putParcelable("player", player)
                "item map" -> bundle.putSerializable("item map", itemMap)
                "stock" -> bundle.putStringArrayList("stock", itemList)
                "equip" -> bundle.putStringArrayList("stock", player.equip)
            }
        }
        fragment.arguments = bundle
        return fragment
    }
}