package com.plannedly.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.installations.FirebaseInstallations
import com.plannedly.R
import com.plannedly.adapter.BoardItemAdapter
import com.plannedly.firebase.FireStoreHandler
import com.plannedly.model.Board
import com.plannedly.model.User
import com.plannedly.util.Constants
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setActionBar()

        val navigatorView: NavigationView = findViewById(R.id.navigator_view)

        navigatorView.setNavigationItemSelectedListener(this)

        mSharedPreferences =
            this.getSharedPreferences(Constants.POJECTMANAGER_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        if (tokenUpdated) {
            showProgressDialog()
            FireStoreHandler().loadUserData(this, true)
        } else {
            FirebaseInstallations.getInstance().id.addOnSuccessListener(this@MainActivity) { instanceResult ->
                updateFcmToken(instanceResult)
            }
        }

        FireStoreHandler().loadUserData(this, true)

        val createBoardFab: FloatingActionButton = findViewById(R.id.create_board_fab)

        createBoardFab.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            getResultCreateBoard.launch(intent)
        }
    }

    fun populateBoardsToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

        val boardsListRv: RecyclerView = findViewById(R.id.boards_list_rv)
        val noBoardsTv: TextView = findViewById(R.id.no_boards_tv)

        if (boardsList.size > 0) {
            boardsListRv.visibility = View.VISIBLE
            noBoardsTv.visibility = View.GONE

            boardsListRv.layoutManager = LinearLayoutManager(this)
            boardsListRv.setHasFixedSize(true)

            val adapter = BoardItemAdapter(this, boardsList)
            boardsListRv.adapter = adapter

            adapter.setOnClickListener(object : BoardItemAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentID)
                    startActivity(intent)
                }
            })
        } else {
            boardsListRv.visibility = View.GONE
            noBoardsTv.visibility = View.VISIBLE
        }
    }

    private fun setActionBar() {
        val toolbarMainActivity: Toolbar = findViewById(R.id.toolbar_main_activity)

        setSupportActionBar(toolbarMainActivity)
        toolbarMainActivity.setNavigationIcon(R.drawable.ic_navigation_manu)
        toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    private fun toggleDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // set logged user image and name to the navigator
    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        hideProgressDialog()
        mUserName = user.name

        val navUserImage: CircleImageView = findViewById(R.id.nav_user_image)
        val userNameTv: TextView = findViewById(R.id.user_name_tv)

        // add image
        Glide.with(this)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navUserImage)
        // add name
        userNameTv.text = user.name

        if (readBoardsList) {
            showProgressDialog()
            FireStoreHandler().getBoardsList(this)
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)

        when (item.itemId) {
            R.id.nav_my_profile -> {
                getResultMyProfile.launch(Intent(this, MyProfileActivity::class.java))
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                mSharedPreferences.edit().clear()
                    .apply() //reset the shared prefs - make sure that shared preferences not stored
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private val getResultMyProfile =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                FireStoreHandler().getBoardsList(this)
            } else {
                Log.e("MainOnActivityResult", R.string.cancelled.toString())
            }
        }

    private val getResultCreateBoard =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                FireStoreHandler().getBoardsList(this)
            } else {
                Log.e("MainOnActivityResult", R.string.cancelled.toString())
            }
        }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog()
        FireStoreHandler().loadUserData(this, true)
    }

    private fun updateFcmToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog()
        FireStoreHandler().updateUserProfileData(this, userHashMap)
    }
}