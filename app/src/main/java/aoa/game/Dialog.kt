package aoa.game

import android.os.Parcel
import android.os.Parcelable
import aoa.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class DialogOptionModel(var to: String, var text: String, var reqs: Array<String>?): Parcelable{
    @Suppress("JAVA_CLASS_ON_COMPANION", "UNCHECKED_CAST")
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readArray(String.javaClass.classLoader) as Array<String>)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(to)
        parcel.writeString(text)
        parcel.writeSerializable(reqs)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        return other == this
    }

    override fun hashCode(): Int {
        var result = to.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + (reqs?.contentHashCode() ?: 0)
        return result
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

data class DialogModel(val title: String?, val text: String, val filler: List<String>?, val opt: List<DialogOptionModel>): Parcelable {
    @Suppress("CAST_NEVER_SUCCEEDS")
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readStringList(emptyList()) as List<String>?,
            parcel.readTypedList(emptyList(), DialogOptionModel) as List<DialogOptionModel>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(text)
        parcel.writeList(filler)
        parcel.writeList(opt)
    }

    override fun describeContents(): Int {
        return 0
    }

    private fun replaceText(args: List<String?>, text: String?): String{
        var result = text ?: ""
        if (filler != null) {
            for (i in args.indices) {
                result = result.replace(filler[i], args[i] ?: filler[i])
            }
        }
        return result
    }

    fun fillVariables(args: List<String?> = ArrayList()): DialogModel {
        val newOptions = ArrayList<DialogOptionModel>()
        for (option in opt){
            val newTo = replaceText(args, option.to)
            val newText = replaceText(args, option.text)
            if (option.reqs != null) {
                val newReqs = ArrayList<String>()
                for (req in option.reqs!!) {
                    newReqs.add(replaceText(args, req))
                }
                newOptions.add(DialogOptionModel(newTo, newText, newReqs.toTypedArray()))
            } else {
                newOptions.add(DialogOptionModel(newTo, newText, null))
            }
        }
        return DialogModel(replaceText(args, title), replaceText(args, text), filler, newOptions)
    }

    fun validate(player: PlayerModel): DialogModel?{
        if (text.startsWith(">redirect>")){
            player.vars["page"] = text.substringAfter(">redirect>")
            return null
        }
        val validOptions = arrayListOf<DialogOptionModel>()
        for (option in opt){
            if (Requirement.checkList(player, option.reqs)){
                validOptions.add(option)
            }
        }
        if (validOptions.isEmpty()){
            validOptions.add(DialogOptionModel(":back", "Go back", null))
        }
        return DialogModel(title, text, filler, validOptions)
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

class DialogController(fileManager: FileManager, chapter: String) {
    val model = makeDialog(fileManager.readAsset("chapters/$chapter.json") !!)
    val model2 = makeDialog(fileManager.readAsset("chapters/common.json") !!)

    private fun makeDialog(data: String): HashMap<String, DialogModel> {
        val listType = object : TypeToken<HashMap<String, DialogModel>?>() {}.type
        return Gson().fromJson(data, listType) as HashMap<String, DialogModel>
    }

    fun getCustom(text: String, next: String, nextDesc: String): DialogModel {
        return DialogModel("", text, emptyList(), listOf(DialogOptionModel(next, nextDesc, null)))
    }

    fun getCustom2(text: String, opt1: String, opt1c: String, opt2: String, opt2c: String): DialogModel {
        val options = listOf(
                DialogOptionModel(opt1c, opt1, null),
                DialogOptionModel(opt2c, opt2, null)
        )
        return DialogModel("", text, emptyList(), options)
    }
}