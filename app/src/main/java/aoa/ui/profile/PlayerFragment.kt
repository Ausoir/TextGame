package aoa.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import aoa.R
import aoa.game.PlayerModel
import aoa.ui.HtmlTextFragment


class PlayerFragment : HtmlTextFragment() {
    private lateinit var player: PlayerModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        textView = root.findViewById(R.id.text_home)
        player = arguments?.getParcelable("player") ?: return root
        setText(player.getDescription())
        return root
    }
}