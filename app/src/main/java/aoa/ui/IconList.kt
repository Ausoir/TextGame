package aoa.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import aoa.R

class IconList(
    private val context: Activity,
    private val maintitle: Array<String>,
    private val subtitle: Array<String>,
    private val imgid: Array<Int>
) :
    ArrayAdapter<String?>(context, R.layout.listview, maintitle) {
        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            val inflater = context.layoutInflater
            val rowView = inflater.inflate(R.layout.listview, null, true)
            val titleText = rowView.findViewById<View>(R.id.title) as TextView
            val imageView = rowView.findViewById<View>(R.id.icon) as ImageView
            val subtitleText = rowView.findViewById<View>(R.id.subtitle) as TextView
            titleText.text = maintitle[position]
            imageView.setImageResource(imgid[position])
            subtitleText.text = subtitle[position]
            return rowView
        }
}