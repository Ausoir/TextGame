package com.example.textgame.game

import android.os.Parcel
import android.os.Parcelable
import com.example.textgame.tools.FileManager
import com.example.textgame.tools.Grammar
import com.google.gson.Gson

data class PlayerModel(var seed: Int, var time: Int, var money: Int, var damaged: Int,
                       var equip: ArrayList<String>, val vars: HashMap<String, String>,
                       var unit: UnitModel, var companion: ArrayList<String>, var companionMoves: HashMap<String, ArrayList<String>>,
                       var specialization: String, var quests: ArrayList<String>) : Parcelable {
    @Suppress("JAVA_CLASS_ON_COMPANION", "UNCHECKED_CAST")
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
            parcel.readArrayList(String.javaClass.classLoader) as ArrayList<String>,
            parcel.readSerializable() as HashMap<String, String>,
        parcel.readSerializable() as UnitModel,
        parcel.readArrayList(String.javaClass.classLoader) as ArrayList<String>,
        parcel.readSerializable() as HashMap<String, ArrayList<String>>,
        parcel.readString() ?: "",
        parcel.readArrayList(String.javaClass.classLoader) as ArrayList<String>
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(seed)
        dest.writeInt(time)
        dest.writeInt(money)
        dest.writeInt(damaged)
        dest.writeList(equip)
        dest.writeSerializable(vars)
        dest.writeSerializable(unit)
        dest.writeList(companion)
        dest.writeSerializable(companionMoves)
        dest.writeString(specialization)
        dest.writeList(quests)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getTime(): String{
        val hours = time % 24
        return Grammar.getTime(hours)
    }

    fun isDay(): Boolean{
        val hour = time % 24
        return hour in 6..19
    }

    fun getAllies(unitDict: HashMap<String, UnitModel>): ArrayList<UnitModel>{
        val enemies = ArrayList<UnitModel>()
        for (unit in companion){
            val unitData = unitDict[unit] ?: unitDict["invalid"]!!
            enemies.add(unitData.copyAll())
        }
        return enemies
    }

    fun equipItem(items: HashMap<String, ItemModel>, itemName: String): String?{
        var unequipped: String? = null
        val item: ItemModel = items[itemName] ?: return null
        for (i in equip){
            val equippedItem = items[i] ?: return null
            if (equippedItem.type == item.type){
                unequipped = unequipItem(items, i)
                break
            }
        }
        equip.add(itemName)
        if (item.type != "item" && item.type != "food"){
            unit.health += item.rating
        }
        if (item.type == "weapon"){
            unit.flavors["weapon"] = itemName
        }
        return unequipped
    }

    fun unequipPart(items: HashMap<String, ItemModel>, part: String): String?{
        for (equipped in equip){
            val item: ItemModel = items[equipped] ?: return null
            if (item.type == part){
                return unequipItem(items, equipped)
            }
        }
        return null
    }

    fun unequipItem(items: HashMap<String, ItemModel>, itemName: String): String?{
        val item: ItemModel = items[itemName] ?: return null
        if (item.type != "item" && item.type != "food"){
            unit.health -= item.rating
        }
        equip.remove(itemName)
        return itemName
    }

    fun getSamePart(items: HashMap<String, ItemModel>, itemName: String): String?{
        val incoming = items[itemName] ?: return null
        for (equipped in equip){
            val item: ItemModel = items[equipped] ?: return null
            if (item.type == incoming.type){
                return equipped
            }
        }
        return null
    }

    fun getItemMoves(items: HashMap<String, ItemModel>): ArrayList<String>{
        val result = arrayListOf<String>()
        for (equipped in equip){
            val item: ItemModel = items[equipped] ?: items["invalid"]!!
            if (item.moves != null) {
                result.addAll(item.moves)
            }
        }
        return result
    }

    fun getDescription(): String{
        var result = "You are a $specialization.\n"
        result += "You have $${money}.\n"
        if (unit.healthPt < unit.health){
            result += "You are wounded (${unit.healthPt}/${unit.health})\n"
        } else {
            result = "You are healthy (${unit.healthPt}/${unit.health})"
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
    var model: PlayerModel = make(fileManager.read("save.json"))
//    var model: PlayerModel = make(fileManager.readAsset("player.json"))

    private fun make(data: String?): PlayerModel {
        val gson = Gson()
        val result = if (data == null){
            val finalData: String = fileManager.readAsset("player.json") !!
            val player = gson.fromJson(finalData, PlayerModel::class.java)
            player
        } else {
            gson.fromJson(data, PlayerModel::class.java)
        }
        result.unit = result.unit.copyAll()
        return result
    }

    fun timePass(time: Int){
        model.time += time
    }

    fun timeSet(time: Int){
//        val diff = model.time % 24 - time
//        model.time += diff
        model.time = time
    }

    fun reset(){
        model = make(fileManager.readAsset("player.json"))
    }

    fun load(to: String){
        model = make(fileManager.readAsset("$to.json"))
    }

    fun save(to: String){
        val json = Gson().toJson(model)
        fileManager.write("$to.json", json)
    }
}