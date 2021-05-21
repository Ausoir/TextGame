package aoa.tools

import android.content.Context
import java.io.*

class FileManager(private val context: Context) {
    fun readAsset(fileName: String): String? {
        val asset = context.assets?.open(fileName) ?: return null
        val inputreader = InputStreamReader(asset)
        val buffreader = BufferedReader(inputreader)
        var line: String?
        val text = StringBuilder()
        try {
            while (buffreader.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
        } catch (e: IOException) {
            return null
        }
        return text.toString()
    }

    fun read(fileName : String): String? {
        val fileDir = context.filesDir
        val file = File(fileDir, fileName)
        if (file.exists()){
            return file.bufferedReader().readLine()
        }
        return null
    }

    fun write(fileName: String, data: String) {
        val fileDir = context.filesDir
        val file = File(fileDir, fileName)
        file.bufferedWriter().use {
                out -> out.write(data)
        }
    }

    fun readPref(name: String) : String? {
        val sharedPref = context.getSharedPreferences("TGDefault", Context.MODE_PRIVATE) ?: return null
        return sharedPref.getString(name, null)
    }

    fun writePref(name: String, data: String) {
        val sharedPref = context.getSharedPreferences("TGDefault", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(name, data)
            apply()
        }

    }
}