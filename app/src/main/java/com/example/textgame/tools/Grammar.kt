package com.example.textgame.tools

import java.util.*

object Grammar {
    fun getTime(time: Int): String{
        return when {
            time == 12 -> {
                "Noon"
            }
            time == 0 -> {
                "Midnight"
            }
            time < 5 -> {
                "Late Night"
            }
            time < 9 -> {
                "Early Morning"
            }
            time < 12 -> {
                "Morning"
            }
            time < 18 -> {
                "Evening"
            }
            time < 20 -> {
                "Night"
            }
            else -> {
                "Late Night"
            }
        }
    }
    fun getArticle(word: String): String{
        return when (word.toLowerCase(Locale.getDefault())[0]){
            'a', 'e', 'i', 'o', 'u' -> {
                "an $word"
            }
            else -> {
                "a $word"
            }
        }
    }
}