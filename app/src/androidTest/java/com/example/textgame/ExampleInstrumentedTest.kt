package com.example.textgame

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.textgame.tools.FileManager
import org.json.JSONObject

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val reader = JSONObject("{\n" +
                "  \"Startout\": {\n" +
                "    \"ChoiceName\": [\"Option 1\", \"Option 2\"],\n" +
                "    \"ChoiceCb\": [\"Cb1\", \"Cb2\"]\n" +
                "  }\n" +
                "}")
        val startout = reader.getJSONObject("Startout")
        val choices = startout.getJSONArray("ChoiceName")
        assertEquals("Option 2", choices[1])
    }
}