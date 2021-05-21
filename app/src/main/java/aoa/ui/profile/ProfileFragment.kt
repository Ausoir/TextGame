package aoa.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import aoa.R
import aoa.game.PlayerModel
import aoa.ui.TabAdapter
import com.google.android.material.tabs.TabLayout
import java.io.Serializable


class ProfileFragment : Fragment() {
    private lateinit var player: PlayerModel
    private lateinit var itemMap: Serializable
    private lateinit var moveset: Serializable

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.tabbed_fragment, container, false)
        val tabLayout = root.findViewById(R.id.tab_layout) as TabLayout
        val pager = root.findViewById(R.id.pager) as ViewPager
        val adapter = TabAdapter(parentFragmentManager)
        moveset = arguments?.getSerializable("moveset") ?: return root
        player = arguments?.getParcelable("player") ?: return root
        itemMap = arguments?.getSerializable("item map") ?: return root

        adapter.addFragment("", makeFragment(PlayerFragment(), arrayOf("player")))
        adapter.addFragment("", makeFragment(EquipmentFragment(), arrayOf("player", "item map")))
        adapter.addFragment("", makeFragment(MovesetFragment(), arrayOf("moveset")))
        adapter.addFragment("", makeFragment(CompanionFragment(), arrayOf("player")))
        adapter.addFragment("", makeFragment(SettingFragment(), arrayOf("player")))
        pager.adapter = adapter
        tabLayout.setupWithViewPager(pager)
        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_player)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_armor)
        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_sword)
        tabLayout.getTabAt(3)?.setIcon(R.drawable.ic_friend)
        tabLayout.getTabAt(4)?.setIcon(R.drawable.ic_setting)
        tabLayout.addOnTabSelectedListener (object: TabLayout.OnTabSelectedListener{
            private val titles = arrayOf("You", "Equipment", "Moveset", "Companion", "Setting")
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
                "moveset" -> bundle.putSerializable("moveset", moveset)
            }
        }
        fragment.arguments = bundle
        return fragment
    }
}