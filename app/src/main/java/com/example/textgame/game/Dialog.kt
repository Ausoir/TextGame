package com.example.textgame.game

import android.os.Parcel
import android.os.Parcelable
import com.example.textgame.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class DialogOptionModel(val to: String, var text: String, var reqs: Array<String>?): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readArray(String.javaClass.classLoader) as Array<String>) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(to)
        parcel.writeString(text)
        parcel.writeSerializable(reqs)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DialogOptionModel> {
        override fun createFromParcel(parcel: Parcel): DialogOptionModel {
            return DialogOptionModel(parcel)
        }

        override fun newArray(size: Int): Array<DialogOptionModel?> {
            return arrayOfNulls(size)
        }
    }
}

data class DialogModel(val title: String, val text: String, val opt: List<DialogOptionModel>): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readTypedList(emptyList(), DialogOptionModel.CREATOR) as List<DialogOptionModel> ?: emptyList<DialogOptionModel>()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DialogModel> {
        override fun createFromParcel(parcel: Parcel): DialogModel {
            return DialogModel(parcel)
        }

        override fun newArray(size: Int): Array<DialogModel?> {
            return arrayOfNulls(size)
        }
    }
}

class DialogController(fileManager: FileManager) {
    val model = makeDialog(fileManager.readAsset("dialog.json") !!)

    fun makeDialog(data: String): HashMap<String, DialogModel> {
        val listType = object : TypeToken<HashMap<String, DialogModel>?>() {}.type
        val result: HashMap<String, DialogModel> = Gson().fromJson(data, listType)
        return result
    }
}