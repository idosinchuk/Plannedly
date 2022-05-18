package com.plannedly.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import com.plannedly.R


class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        hideStatusBarAndMakeFullSplashScreen()

        val signUpBtn: Button = findViewById(R.id.sign_up_btn)
        val signInBtn: Button = findViewById(R.id.sign_in_btn)

        signUpBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        signInBtn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

    }

    private fun hideStatusBarAndMakeFullSplashScreen() {
        window.setDecorFitsSystemWindows(false)
        val controller = window.insetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}