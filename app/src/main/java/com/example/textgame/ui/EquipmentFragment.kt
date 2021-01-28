package com.example.textgame.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.textgame.R
import com.example.textgame.game.ItemModel
import com.example.textgame.tools.Grammar

class EquipmentFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_equipment, container, false)
        val textView: TextView = root.findViewById(R.id.text_equipment)
        textView.movementMethod = ScrollingMovementMethod()

        val serial = arguments?.getSerializable("inventory") ?: return root
        val inventory = serial as HashMap<String, ItemModel>
        val equip = arguments?.getStringArrayList("equip")
        val item = arguments?.getString("item")
        var description = ""
        if (equip != null && equip.isNotEmpty()){
            for (equipped in equip){
                description += "You have ${Grammar.getArticle(equipped)} equipped. It's ${inventory[equipped]?.desc}\n"
            }
        }
        if (item != null && item.isNotEmpty()){
            description += "You are carrying ${inventory[item]?.desc}"
        }
        textView.text = description
        return root
    }
}
