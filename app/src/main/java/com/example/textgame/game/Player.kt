package com.example.textgame.game

import android.os.Parcel
import android.os.Parcelable
import com.example.textgame.tools.FileManager
import com.example.textgame.tools.Grammar
import com.google.gson.Gson

data class PlayerModel(var seed: Int, var time: Int, var money: Int, var damaged: Int,
                       var equip: ArrayList<String>, var item: String, val vars: HashMap<String, String>, var unit: UnitModel) : Parcelable {
    @Suppress("JAVA_CLASS_ON_COMPANION")
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
            parcel.readArrayList(String.javaClass.classLoader) as ArrayList<String>,
        parcel.readString() ?: "",
            parcel.readSerializable() as HashMap<String, String>,
        parcel.readSerializable() as UnitModel
    )

    fun gameStart(){
        if (seed == -1){
            seed = (0..9999).random()
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(seed)
        dest.writeInt(time)
        dest.writeInt(money)
        dest.writeInt(damaged)
        dest.writeList(equip)
        dest.writeString(item)
        dest.writeSerializable(vars)
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

    fun sell(items: HashMap<String, ItemModel>){
        val itemModel: ItemModel = items[item] ?: return
        item = ""
        money += itemModel.value
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
        if ("wounded" in unit.status){
            val percentage = unit.getHealthPercentage().toString()
            result += "You are wounded ($percentage%)\n"
        }
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
        return if (data == null){
            val finalData: String = fileManager.readAsset("template.json") !!
            val player = gson.fromJson(finalData, PlayerModel::class.java)
            player.gameStart()
            player
        } else {
            gson.fromJson(data, PlayerModel::class.java)
        }
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
        model.unit.foes = arrayListOf()
        val json = Gson().toJson(model)
        fileManager.write("save", json)
    }
}