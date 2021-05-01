package com.example.textgame.ui

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

open class CustomFragment : Fragment() {
    protected lateinit var layout: LinearLayout
    protected lateinit var textView: TextView
    private var stringBuilder = SpannableStringBuilder()

    protected fun addText(text: String){
        val split = text.split("\n")
        for (i in split.indices){
            when {
                split[i].startsWith("<color:") -> {
                    var modText = split[i].substringAfter("<color:")
                    val color = Color.parseColor(modText.substringBefore(">"))
                    modText = modText.substringAfter(">")
                    val spannableString = SpannableString(modText)
                    spannableString.setSpan(ForegroundColorSpan(color), 0, modText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    stringBuilder.append(spannableString)
                }
                split[i].startsWith("<size:") -> {
                    var modText = split[i].substringAfter("<size:")
                    val size = modText.substringBefore(">").toFloat()
                    modText = modText.substringAfter(">")
                    val spannableString = SpannableString(modText)
                    spannableString.setSpan(RelativeSizeSpan(size), 0, modText.length, 0)
                    stringBuilder.append(spannableString)
                }
                else -> {
                    stringBuilder.append(split[i])
                }
            }
            if (i != split.size - 1){
                stringBuilder.append("\n")
            }
        }
        textView.setText(stringBuilder, TextView.BufferType.SPANNABLE)
    }
}