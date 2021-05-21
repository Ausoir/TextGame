package aoa.tools

import java.util.*

object Grammar {
    fun getPlural(word: String, value: Int): String{
        return if (value > 1) "${word}s"
        else word
    }
    fun getTime(time: Int): String{
        return when {
            time < 5 -> {
                "late night"
            }
            time < 9 -> {
                "early morning"
            }
            time < 12 -> {
                "morning"
            }
            time < 18 -> {
                "evening"
            }
            time < 20 -> {
                "night"
            }
            else -> {
                "late night"
            }
        }
    }
    fun getArticleList(words: List<String>): String{
        if (words.size == 1){
            return getArticle(words[0])
        }
        if (words.size == 2){
            return getArticle(words[0]) + " and " + getArticle(words[1])
        }
        val last = words.last()
        return words.joinToString { it ->
            if (it == last){
                "and " + getArticle(it)
            } else {
                getArticle(it)
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
    fun getOrderNumber(number: Int): String{
        return when (number) {
            1 -> {
                "1st"
            }
            2 -> {
                "2nd"
            }
            3 -> {
                "3rd"
            }
            4 -> {
                "4th"
            }
            5 -> {
                "5th"
            }
            6 -> {
                "6th"
            }
            7 -> {
                "7th"
            }
            8 -> {
                "8th"
            }
            9 -> {
                "9th"
            }
            else -> {
                ""
            }
        }
    }
    fun ordinalIndexOf(str: String, substr: String?, nth: Int): Int {
        var i = nth
        var pos = str.indexOf(substr!!)
        while (--i > 0 && pos != -1) pos = str.indexOf(substr, pos + 1)
        return pos
    }
    fun substringAfter(str: String, substr: String?, nth: Int): String{
        return str.substring(ordinalIndexOf(str, substr, nth) + 1)
    }
}