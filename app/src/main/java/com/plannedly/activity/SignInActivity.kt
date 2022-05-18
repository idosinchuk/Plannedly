package com.plannedly.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.plannedly.R
import com.plannedly.firebase.FireStoreHandler

class SignInActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        window.setDecorFitsSystemWindows(false)
        val controller = window.insetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setActionBar()

        auth = FirebaseAuth.getInstance()

        val signInPageBtn: Button = findViewById(R.id.sign_in_page_btn)

        signInPageBtn.setOnClickListener { signInUser() }
    }

    fun signInSuccess() {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun signInUser() {
        val signInEmailEt: AppCompatEditText = findViewById(R.id.sign_in_email_et)
        val signInPasswordEt: AppCompatEditText = findViewById(R.id.sign_in_password_et)

        val email = signInEmailEt.text.toString().trim { it <= ' ' }
        val password = signInPasswordEt.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        FireStoreHandler().loadUserData(this)
                    } else {
                        Toast.makeText(
                            baseContext,
                            R.string.authentication_failed.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(R.string.please_enter_email_address.toString())
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(R.string.please_enter_password.toString())
                false
            }
            else -> {
                true
            }
        }
    }

    private fun setActionBar() {
        val toolbarSignInActivity: Toolbar = findViewById(R.id.toolbar_sign_in_activity)

        setSupportActionBar(toolbarSignInActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_black)
        }
        toolbarSignInActivity.setNavigationOnClickListener { onBackPressed() }
    }
}