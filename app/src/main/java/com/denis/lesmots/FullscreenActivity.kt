package com.denis.lesmots

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.denis.lesmots.databinding.ActivityFullscreenBinding


class FullscreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenBinding

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()

        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun hideSystemBars() {
        supportActionBar?.hide()
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    fun onNewGameClick(view: View) {
        val intent = Intent(this, GameScreenActivity::class.java)
        intent.putExtra(Intent.EXTRA_TEXT, "Random")
        this.startActivity(intent)
    }

    fun onDailyGameClick(view: View) {
        val intent = Intent(this, GameScreenActivity::class.java)
        intent.putExtra(Intent.EXTRA_TEXT, "Daily")
        this.startActivity(intent)
    }


}