package com.example.textgame.game

import android.os.Parcel
import android.os.Parcelable
import com.example.textgame.tools.FileManager
import com.example.textgame.tools.Grammar
import com.google.gson.Gson

data class Model(val name: String, var page: String, var time: Int, var money: Int,
                 var head: String, var body: String, var limb: String, var item: String,
                 var moves: Array<String>) : Parcelable {
    @Suppress("JAVA_CLASS_ON_COMPANION", "UNCHECKED_CAST")
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
            parcel.readArray(String.javaClass.classLoader) as Array<String>
    ) {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(page)
        dest?.writeInt(time)
        dest?.writeInt(money)
        dest?.writeString(head)
        dest?.writeString(body)
        dest?.writeString(limb)
        dest?.writeString(item)
        dest?.writeSerializable(moves)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getTime(): String{
        val hours = time % 24
        return Grammar.getTime(hours)
    }

    fun getDay(): Int{
        return 1 + (time / 24)
    }

    fun isDay(): Boolean{
        val hour = time % 24
        return hour in 6..19
    }

    fun getClassDescription(): String{
        val alimb = Grammar.getArticle(limb)
        val abody = Grammar.getArticle(body)
        val ahead = Grammar.getArticle(head)
        return "You have limbs of $alimb, the body of $abody, and the head of $ahead"
    }

    fun getClassName(): String{
        val name = if (body == limb && limb == head){
            body
        } else if (body == limb || limb == head){
            "$body $head"
        } else {
            "$limb-$body $head"
        }
        return Grammar.getArticle(name)
    }

    fun getEquipment(): String{
        return if (item.isEmpty()){
            "You are not carrying anything"
        } else {
            "You are carrying ${Grammar.getArticle(item)}"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Model
        if (head != other.head) return false
        if (body != other.body) return false
        if (limb != other.limb) return false
        return true
    }

    override fun hashCode(): Int {
        var result = head.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + limb.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<Model> {
        override fun createFromParcel(parcel: Parcel): Model {
            return Model(parcel)
        }

        override fun newArray(size: Int): Array<Model?> {
            return arrayOfNulls(size)
        }
    }
}

class Player(private val fileManager: FileManager) {
    val model: Model = make(fileManager.read("save"))

    private fun make(data: String?): Model {
        val gson = Gson()
        val finalData: String = data ?: fileManager.readAsset("template.json") !!
        return gson.fromJson(finalData, Model::class.java)
    }

    fun timePass(time: Int){
        model.time += time
    }

    fun save(){
        val gson = Gson()
        val json = gson.toJson(model)
        fileManager.write("save", json)
    }
}