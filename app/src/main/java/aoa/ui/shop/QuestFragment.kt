package aoa.ui.shop

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import aoa.App
import aoa.R
import aoa.game.PlayerModel
import aoa.ui.IconList


class QuestFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_list, container, false)
        val listView = root.findViewById(R.id.list_list) as ListView
        val player = arguments?.getParcelable<PlayerModel>("player")!!

        val titles = ArrayList<String?>()
        val subtitles = ArrayList<String>()
        val pages = ArrayList<String>()
        val icons = ArrayList<Int>()
        for (i in player.quests){
            val split = i.split(">")
            titles.add(split[0])
            pages.add(split[1])
            subtitles.add(split[2])
            icons.add(R.drawable.ic_map)
        }
        listView.adapter = IconList(activity as Activity, titles, subtitles, icons)
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            AlertDialog.Builder(activity)
                    .setTitle(R.string.quest_title)
                    .setMessage(resources.getString(R.string.quest_confirm))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        player.quests.removeAt(position)
                        val app: App = activity as App
                        app.openDialog(pages[position])
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> return@setNegativeButton }
                    .show()
        }
        return root
    }
}