package com.example.textgame

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.textgame.game.DialogController
import com.example.textgame.tools.FileManager
import com.example.textgame.tools.Grammar
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

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
        val dialog: String = Grammar.getArticleList(arrayOf<String>("coat", "hanger"))
        assertEquals("sdf,fff", "")
    }
}