package aoa.game

import aoa.tools.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class VariableController(fileManager: FileManager) {
    val model = makeVariable(fileManager.readAsset("variable.json") !!)

    private fun makeVariable(data: String): HashMap<String, ArrayList<String>> {
        val listType = object : TypeToken<HashMap<String, ArrayList<String>>?>() {}.type
        return Gson().fromJson(data, listType)
    }

    fun getRandom(index: String, seed: Int = -1): String{
        val variables = model[index]
        return if (variables != null && variables.isNotEmpty()){
            if (seed == -1){
                variables.shuffled()[0]
            } else {
                variables[seed % variables.size]
            }
        } else {
            "invalid"
        }
    }
}