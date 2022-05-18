package com.plannedly.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.plannedly.R
import com.plannedly.firebase.FireStoreHandler
import com.plannedly.model.User


class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setDecorFitsSystemWindows(false)
        val controller = window.insetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setActionBar()

        val signUpPageBtn: Button = findViewById(R.id.sign_up_page_btn)

        signUpPageBtn.setOnClickListener {
            registerUser()
        }
    }

    private fun setActionBar() {
        val toolbarSignUpActivity: Toolbar = findViewById(R.id.toolbar_sign_up_activity)

        setSupportActionBar(toolbarSignUpActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_black)
        }
        toolbarSignUpActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun registerUser() {
        val nameEt: AppCompatEditText = findViewById(R.id.name_et)
        val emailEt: AppCompatEditText = findViewById(R.id.email_et)
        val passwordEt: AppCompatEditText = findViewById(R.id.password_et)

        val name = nameEt.text.toString().trim { it <= ' ' }
        val email = emailEt.text.toString().trim { it <= ' ' }
        val password = passwordEt.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            showProgressDialog()
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //send user details for user creation in firebase
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FireStoreHandler().registerUser(this, user)
                    } else {
                        Toast.makeText(
                            this,
                            R.string.registration_failed_try_again,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            R.string.you_have_successfully_registered,
            Toast.LENGTH_SHORT
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar(R.string.please_enter_name.toString())
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(R.string.please_enter_email.toString())
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
}