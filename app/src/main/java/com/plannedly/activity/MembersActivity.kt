package com.plannedly.activity

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plannedly.R
import com.plannedly.adapter.MemberItemAdapter
import com.plannedly.firebase.FireStoreHandler
import com.plannedly.model.Board
import com.plannedly.model.User
import com.plannedly.util.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMembersList: ArrayList<User>
    private var anyChangesMade: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
            showProgressDialog()
            FireStoreHandler().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
        }
        setActionBar()
    }

    fun memberDetails(user: User) {
        mBoardDetails.assignedTo.add(user.id)
        FireStoreHandler().assignMemberToBoard(this, mBoardDetails, user)
    }

    fun setUpMembersList(list: ArrayList<User>) {
        mAssignedMembersList = list
        hideProgressDialog()

        val membersListRv: RecyclerView = findViewById(R.id.members_list_rv)

        membersListRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        membersListRv.setHasFixedSize(true)

        val adapter = MemberItemAdapter(this, list)
        membersListRv.adapter = adapter
    }

    private fun setActionBar() {
        val membersActivityToolbar: Toolbar = findViewById(R.id.members_activity_toolbar)

        setSupportActionBar(membersActivityToolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white)
            actionBar.title = R.string.invite_to_board.toString()
        }
        membersActivityToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_serach_member)

        val addTv: TextView = dialog.findViewById(R.id.add_tv)
        val emailSearchMemberEt: AppCompatEditText =
            dialog.findViewById(R.id.email_search_member_et)

        addTv.setOnClickListener {
            val email = emailSearchMemberEt.text.toString()
            if (email.isNotEmpty()) {
                showProgressDialog()
                FireStoreHandler().getMemberDetails(this, email)
                dialog.dismiss()
            } else {
                Toast.makeText(this, R.string.please_enter_email_address, Toast.LENGTH_SHORT).show()
            }
        }

        val cancelTv: TextView = dialog.findViewById(R.id.cancel_tv)

        cancelTv.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onBackPressed() {
        if (anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangesMade = true
        setUpMembersList(mAssignedMembersList)
        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken)
    }

    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String) :
        AsyncTask<Any, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result: String
            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,
                    "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )
                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(
                    Constants.FCM_KEY_TITLE,
                    R.string.assigned_to_board.toString() + " " + boardName
                )
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    R.string.you_have_been_asigned_to_board.toString() + " " + { mAssignedMembersList[0].name }
                )
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val stringBuilder = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
        }
    }
}