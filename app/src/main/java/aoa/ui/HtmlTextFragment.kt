package aoa.ui

import android.text.Html
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

open class HtmlTextFragment : Fragment() {
    protected lateinit var textView: TextView
    private var mainText = ""

    protected fun setText(text: String){
        textView.text = Html.fromHtml(text.replace("\n", "<br>"))
    }

    protected fun addText(text: String){
        mainText += text.replace("\n", "<br>")
        textView.text = Html.fromHtml(mainText)
    }
}