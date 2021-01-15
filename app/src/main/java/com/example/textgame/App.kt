package com.example.textgame

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.textgame.game.Game
import com.example.textgame.ui.HomeFragment
import com.example.textgame.ui.ProfileFragment


class App : AppCompatActivity() {
    private var game: Game? = null

    fun refresh() {
        val destination = HomeFragment()
        setPage(destination)
        supportFragmentManager.commit {
            setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
            )
            replace(R.id.home, destination)
            addToBackStack(null)
        }
    }

    fun updatePage(newPage: String, timePass: Int = 1){
        game?.player?.timePass(timePass)
        game?.player?.model?.page = newPage
        game?.player?.save()
        title = getString(R.string.home_title, game?.player?.model?.getTime(), newPage)
        if (game?.player?.model?.isDay() != false) {
            setTheme(R.style.TextGame)
        } else {
            setTheme(R.style.TextGameDark)
        }
    }

    private fun setPage(fragment: Fragment?){
        val bundle = Bundle()
        bundle.putParcelable("player", game?.player?.model)
        fragment?.arguments = bundle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        game = Game(this)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                val bundle = Bundle()
                bundle.putParcelable("player", game?.player?.model)
                add<HomeFragment>(R.id.home, args=bundle)
                updatePage(game?.player?.model?.page ?: "Start", timePass=0)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_action, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_profile -> {
            val frag = if (supportFragmentManager.findFragmentById(R.id.home) is ProfileFragment){
                title = getString(R.string.home_title, game?.player?.model?.getTime(), game?.player?.model?.page)
                HomeFragment()
            } else {
                title = getString(R.string.profile_title, game?.player?.model?.getDay())
                ProfileFragment()
            }
            val bundle = Bundle()
            bundle.putParcelable("player", game?.player?.model)
            frag.arguments = bundle
            supportFragmentManager.commit {
                setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                )
                replace(R.id.home, frag)
                addToBackStack(null)
            }
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            super.onOptionsItemSelected(item)
        }
    }

    @Suppress("DEPRECATION")
    private fun fullscreen() {
        if (Build.VERSION.SDK_INT < 19) { // lower api
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val decorView = window.decorView
            val uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            decorView.systemUiVisibility = uiOptions
        }
    }
}