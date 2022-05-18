package com.plannedly.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.plannedly.R
import com.plannedly.firebase.FireStoreHandler

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        hideStatusBar()

        val controller = window.insetsController

        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val currentUserID = FireStoreHandler().getCurrentUserId()
                if (currentUserID.isNotEmpty()) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else startActivity(Intent(this, LoginActivity::class.java))

                finish()
            },
            2500
        )
    }

    private fun hideStatusBar() {
        window.setDecorFitsSystemWindows(false)
    }
}