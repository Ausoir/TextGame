package aoa.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import aoa.App
import aoa.R
import aoa.game.DialogModel


class HomeFragment : HtmlTextFragment() {
    private lateinit var layout: LinearLayout

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        textView = root.findViewById(R.id.text_home)
        textView.movementMethod = ScrollingMovementMethod()
        layout = root.findViewById(R.id.home_button_container) as LinearLayout
        val currentPage = arguments?.getParcelable<DialogModel>("page")
        if (currentPage != null) {
            addDialogText(currentPage.text)
            val options = currentPage.opt
            if (currentPage.text == "redirect") {
                val app: App = activity as App
                app.openDialog(options[0].to)
            } else {
                options.reversed().forEach { option ->
                    addButton(option.text).setOnClickListener {
                        val app: App = activity as App
                        app.openDialog(option.to)
                    }
                }
            }
        } else {
            textView.text = resources.getString(R.string.dialog_not_found)
            addButton("Go Back").setOnClickListener {
                val app: App = activity as App
                app.openDialog("back")
            }
        }
        return root
    }

    private fun addButton(name: String): Button {
        val button = Button(activity)
        button.text = name
        layout.addView(button, 0)
        return button
    }

    private fun addDialogText(text: String) {
        var newText = text
        newText = newText.replace("Lucid:", "<font color='#334488'><b>Lucid</b></font>:")
        newText = newText.replace("Pemmi:", "<font color='#882266'><b>Pemmi</b></font>:")
        newText = newText.replace("Cheese:", "<font color='#887733'><b>Cheese</b></font>:")
        newText = newText.replace("Cobby:", "<font color='#774422'><b>Cobby</b></font>:")
        newText = newText.replace("Sugar:", "<font color='#992222'><b>Sugar</b></font>:")
        newText = newText.replace("Bigs:", "<font color='#443355'><b>Bigs</b></font>:")
        newText = newText.replace("Priest:", "<font color='#335533'><b>Priest</b></font>:")
        val quotes = newText.split("]")
        if (quotes.size > 1){
            var result = ""
            for (quote in quotes){
                if (quote.isNotEmpty()) {
                    val split = quote.split("[")
                    result += if (split.size > 1) split[0] + "[<font color='#DCDCDC'>${split[1]}</font>]"
                    else split[0]
                }
            }
            addText(result)
        } else {
            addText(newText)
        }
    }
}