package aoa.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import aoa.App
import aoa.R
import aoa.game.PlayerModel
import aoa.ui.IconList

class SettingFragment : Fragment() {
    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_list, container, false)
        val listView = root.findViewById(R.id.list_list) as ListView
        val player = arguments?.getParcelable<PlayerModel>("player")!!

        val titles = ArrayList<String>()
        val subtitles = ArrayList<String>()
        val icons = ArrayList<Int>()
        titles.add("Start Over")
        subtitles.add("Erase data and start over from the very beginning.")
        icons.add(R.drawable.ic_restart)
        titles.add("Turn Bright")
        subtitles.add("")
        icons.add(R.drawable.ic_hand)
        if ("mute" !in player.vars){
            titles.add("Mute")
            subtitles.add("Mute all sounds.")
            icons.add(R.drawable.ic_mute)
        } else {
            titles.add("Unmute")
            subtitles.add("Unmute all sounds.")
            icons.add(R.drawable.ic_unmute)
        }
        listView.adapter = IconList(activity as Activity, titles.toTypedArray(), subtitles.toTypedArray(), icons.toTypedArray())
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (titles[position]) {
                "Start Over" -> {
                    AlertDialog.Builder(activity)
                            .setTitle(R.string.sell)
                            .setMessage(resources.getString(R.string.start_over))
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                val app: App = activity as App
                                app.openDialog("@restart")
                            }
                            .setNegativeButton(android.R.string.cancel) { _, _ -> return@setNegativeButton }
                            .show()
                }
                "Turn Bright" -> {
                    val app: App = activity as App
                    app.openDialog(">time>12>>to>page")
                }
                "Mute" -> {
                    player.vars["mute"] = "1"
                }
                "Unmute" -> {
                    player.vars.remove("mute")
                    val app: App = activity as App
                    app.playSound("beep")
                }
            }
        }
        return root
    }
}