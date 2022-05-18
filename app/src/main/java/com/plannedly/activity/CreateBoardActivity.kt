package com.plannedly.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.plannedly.R
import com.plannedly.firebase.FireStoreHandler
import com.plannedly.model.Board
import com.plannedly.util.Constants
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageURL: String = ""

    private lateinit var createBoardImage: CircleImageView
    private lateinit var createBoardCreateBtn: Button
    private lateinit var createBoardNameEt: AppCompatEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)
        setActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        createBoardImage = findViewById(R.id.create_board_image)
        createBoardCreateBtn = findViewById(R.id.create_board_create_btn)
        createBoardNameEt = findViewById(R.id.create_board_name_et)

        createBoardImage.setOnClickListener {
            if (hasReadPermission()
            ) {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                getPickupImage.launch(galleryIntent)
            } else {
                requestPermissions()
            }
        }

        createBoardCreateBtn.setOnClickListener {
            showProgressDialog()

            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            }
            createBoard()
        }
    }

    private fun hasReadPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            Constants.READ_STORAGE_PERMISSION_CODE
        )
    }

    private fun createBoard() {
        val assignedUsers: ArrayList<String> = ArrayList()
        assignedUsers.add(getCurrentUserID())

        createBoardNameEt = findViewById(R.id.create_board_name_et)

        val board = Board(
            createBoardNameEt.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsers
        )

        FireStoreHandler().createBoard(this, board)
    }

    private fun uploadBoardImage() {

        val sRef: StorageReference = FirebaseStorage.getInstance()
            .reference
            .child(
                Constants.BOARD_IMAGE_PATH + System.currentTimeMillis()
                        + "." + Constants.getFileExtension(this, mSelectedImageFileUri)
            )

        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
            Log.e(
                Constants.FIREBASE_IMAGE_URL,
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.e(Constants.DOWNLOADED_IMAGE_URL, uri.toString())
                mBoardImageURL = uri.toString()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            hideProgressDialog()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                getPickupImage.launch(galleryIntent)
            }
        } else {
            Toast.makeText(
                this, R.string.denied_permission_storage.toString() + " " +
                        R.string.can_allow_from_settings.toString(), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val getPickupImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (checkUserSelectPhotoFromGallery(it)
            ) {
                val createBoardImage: CircleImageView = findViewById(R.id.create_board_image)

                mSelectedImageFileUri = it.data!!.data

                try {
                    setNewPhotoToProfileImage(createBoardImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private fun setNewPhotoToProfileImage(createBoardImage: CircleImageView) {
        Glide.with(this)
            .load(mSelectedImageFileUri)
            .fitCenter()
            .placeholder(R.drawable.ic_board_place_holder)
            .into(createBoardImage)
    }

    private fun checkUserSelectPhotoFromGallery(it: ActivityResult) =
        (it.resultCode == RESULT_OK
                && it.data!!.data != null)

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }


    private fun setActionBar() {
        val toolbarCreateBoardActivity: Toolbar = findViewById(R.id.toolbar_create_board_activity)

        setSupportActionBar(toolbarCreateBoardActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white)
            actionBar.title = R.string.create_board.toString()
        }
        toolbarCreateBoardActivity.setNavigationOnClickListener { onBackPressed() }
    }
}