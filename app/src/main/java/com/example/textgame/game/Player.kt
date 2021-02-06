package com.example.textgame.game

import android.os.Parcel
import android.os.Parcelable
import com.example.textgame.tools.FileManager
import com.example.textgame.tools.Grammar
import com.google.gson.Gson

data class PlayerModel(val name: String, var page: String, var prev : String, var time: Int, var money: Int,
                       var equip: ArrayList<String>, var item: String, var unit: UnitModel) : Parcelable {
    @Suppress("JAVA_CLASS_ON_COMPANION")
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
            parcel.readArrayList(String.javaClass.classLoader) as ArrayList<String>,
        parcel.readString() ?: "",
        parcel.readSerializable() as UnitModel
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(page)
        dest.writeString(prev)
        dest.writeInt(time)
        dest.writeInt(money)
        dest.writeList(equip)
        dest.writeString(item)
        dest.writeSerializable(unit)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getTime(): String{
        val hours = time % 24
        return Grammar.getTime(hours)
    }

    fun isTransitionTime(): Boolean{
        return listOf(5, 9, 12, 18, 20).contains(time % 24)
    }

    fun getDay(): Int{
        return 1 + (time / 24)
    }

    fun isDay(): Boolean{
        val hour = time % 24
        return hour in 6..19
    }

    fun equipItem(items: HashMap<String, ItemModel>, itemName: String): String?{
        var unequipped: String? = null
        val item: ItemModel = items[itemName] ?: return unequipped
        for (i in equip){
            val equippedItem = items[i] ?: return unequipped
            if (equippedItem.type == item.type){
                unequipped = i
                unequipItem(items, i)
                break
            }
        }
        if (item.moves != null) {
            for (move in item.moves) {
                unit.moves.add(move)
            }
        }
        equip.add(itemName)
        return unequipped
    }

    fun unequipItem(items: HashMap<String, ItemModel>, itemName: String){
        val item: ItemModel = items[itemName] ?: return
        if (item.moves != null) {
            for (move in item.moves) {
                unit.moves.remove(move)
            }
        }
        equip.remove(itemName)
    }

    fun getEquipment(items: HashMap<String, ItemModel>, part: String, fail: String = ""): String{
        for (i in equip){
            val equippedItem = items[i] ?: return fail
            if (equippedItem.type == part){
                return i
            }
        }
        return fail
    }

    fun getDescription(): String{
        var result = ""
        if (equip.isNotEmpty()){
            for (equipped in equip){
                result += "You have ${Grammar.getArticle(equipped)} equipped.\n"
            }
        }
        if (item.isNotEmpty()){
            result += "You are carrying ${Grammar.getArticle(item)}.\n"
        }
        result += "You have $${money}.\n"
        return result
    }

    companion object CREATOR : Parcelable.Creator<PlayerModel> {
        override fun createFromParcel(parcel: Parcel): PlayerModel {
            return PlayerModel(parcel)
        }

        override fun newArray(size: Int): Array<PlayerModel?> {
            return arrayOfNulls(size)
        }
    }
}

class Player(private val fileManager: FileManager) {
//    val model: Model = make(fileManager.read("save"))
    var model: PlayerModel = make(fileManager.readAsset("template.json"))

    private fun make(data: String?): PlayerModel {
        val gson = Gson()
        val finalData: String = data ?: fileManager.readAsset("template.json") !!
        return gson.fromJson(finalData, PlayerModel::class.java)
    }

    fun timePass(time: Int){
        model.time += time
    }

    fun reset(){
        model = make(fileManager.readAsset("template.json"))
    }

    fun save(){
        model.unit.deck = null
        model.unit.target = null
        model.unit.status.clear()
        val json = Gson().toJson(model)
        fileManager.write("save", json)
    }
}