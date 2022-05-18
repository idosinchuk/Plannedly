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
import com.plannedly.model.User
import com.plannedly.util.Constants
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        setActionBar()

        val myProfileUserImage: CircleImageView = findViewById(R.id.my_profile_user_image)

        myProfileUserImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                getPickupImage.launch(galleryIntent)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        FireStoreHandler().loadUserData(this)

        val myProfileUpdateBtn: Button = findViewById(R.id.my_profile_update_btn)

        myProfileUpdateBtn.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog()
                updateUserProfileData()
            }
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

    private fun checkUserSelectPhotoFromGallery(it: ActivityResult) =
        (it.resultCode == RESULT_OK
                && it.data!!.data != null)

    private fun setNewPhotoToProfileImage(createBoardImage: CircleImageView) {
        Glide.with(this)
            .load(mSelectedImageFileUri)
            .fitCenter()
            .placeholder(R.drawable.ic_board_place_holder)
            .into(createBoardImage)
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
                        R.string.can_allow_from_settings, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setActionBar() {
        val toolbaMyProfileActivity: Toolbar = findViewById(R.id.toolbar_my_profile_activity)

        setSupportActionBar(toolbaMyProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white)
            actionBar.title = R.string.my_profile.toString()
        }
        toolbaMyProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user: User) {
        val myProfileUserImage: CircleImageView = findViewById(R.id.my_profile_user_image)

        mUserDetails = user

        setUserImage(user, myProfileUserImage)

        val myProfileNameEt: AppCompatEditText = findViewById(R.id.my_profile_name_et)
        val myProfileEmailEt: AppCompatEditText = findViewById(R.id.my_profile_email_et)

        myProfileNameEt.setText(user.name)
        myProfileEmailEt.setText(user.email)

        val myProfileMobileEt: AppCompatEditText = findViewById(R.id.my_profile_mobile_et)

        if (user.mobile != 0L) {
            myProfileMobileEt.setText(user.mobile.toString())
        }
    }

    private fun setUserImage(
        user: User,
        myProfileUserImage: CircleImageView
    ) {
        Glide.with(this)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(myProfileUserImage)
    }

    private fun uploadUserImage() {
        showProgressDialog()

        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance()
                .reference
                .child(
                    Constants.USER_IMAGE_PATH + System.currentTimeMillis()
                            + "." + Constants.getFileExtension(this, mSelectedImageFileUri)
                )

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.e(
                    Constants.FIREBASE_IMAGE_URL,
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.e(Constants.DOWNLOADED_IMAGE_URL, uri.toString())
                    mProfileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        }
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        var anyChangesMade = false

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
            anyChangesMade = true
        }

        val myProfileNameEt: AppCompatEditText = findViewById(R.id.my_profile_name_et)

        if (myProfileNameEt.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = myProfileNameEt.text.toString()
            anyChangesMade = true
        }

        val myProfileMobileEt: AppCompatEditText = findViewById(R.id.my_profile_mobile_et)

        if (myProfileMobileEt.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = myProfileMobileEt.text.toString().toLong()
            anyChangesMade = true
        }

        if (anyChangesMade)
            FireStoreHandler().updateUserProfileData(this, userHashMap)
        else {
            Toast.makeText(this, R.string.nothing_as_been_updated, Toast.LENGTH_SHORT).show()
            hideProgressDialog()
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }


}