package aoa.ui.profile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import aoa.R
import aoa.game.ItemModel
import aoa.game.PlayerModel
import aoa.game.UnitModel
import aoa.ui.IconList

class CompanionFragment : Fragment() {
    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_list, container, false)
        val listView = root.findViewById(R.id.list_list) as ListView
        val player = arguments?.getParcelable<PlayerModel>("player")
        val unitMap = arguments?.getSerializable("unit map") as HashMap<String, UnitModel>

        val titles = ArrayList<String?>()
        val subtitles = ArrayList<String>()
        val icons = ArrayList<Int>()
        for (i in player!!.companion){
            val name = unitMap[i]?.name ?: ""
            if (name != "") {
                titles.add(name)
                subtitles.add(resources.getString(R.string.companion))
                icons.add(R.drawable.ic_friend)
            }
        }
        listView.adapter = IconList(activity as Activity, titles, subtitles, icons)
        return root
    }
}