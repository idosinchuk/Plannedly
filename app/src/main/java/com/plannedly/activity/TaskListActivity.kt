package com.plannedly.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plannedly.R
import com.plannedly.adapter.TaskItemAdapter
import com.plannedly.firebase.FireStoreHandler
import com.plannedly.model.Board
import com.plannedly.model.Card
import com.plannedly.model.Task
import com.plannedly.model.User
import com.plannedly.util.Constants

class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private lateinit var mBoardDocumentID: String
    lateinit var mAssignedMembersDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentID = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }
        showProgressDialog()
        FireStoreHandler().getBoardDetails(this, mBoardDocumentID)
    }

    fun boardDetails(board: Board) {
        mBoardDetails = board
        hideProgressDialog()
        setActionBar()

        showProgressDialog()
        FireStoreHandler().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                getMembers.launch(intent)
                return true
            }
            R.id.action_delete_board -> {
                val boardName =
                    mBoardDetails.name
                alertDialogForDeleteBoard(boardName)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun alertDialogForDeleteBoard(boardName: String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.alert)
        builder.setMessage(R.string.are_you_sure_you_want_delete.toString() + " " + boardName + "?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(R.string.yes) { dialog, _ ->
            dialog.dismiss()
            deleteBoard()
        }

        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun deleteBoard() {
        showProgressDialog()
        FireStoreHandler().deleteBoard(this, mBoardDetails)
    }

    fun boardDeletedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
        startActivity(intent)
    }

    private val getMembers =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK
            ) {
                showProgressDialog()
                FireStoreHandler().getBoardDetails(this, mBoardDocumentID)
            } else {
                Log.e(R.string.cancelled.toString(), " " + R.string.cancelled)
            }
        }

    private val getCardDetails =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK
            ) {
                showProgressDialog()
                FireStoreHandler().getBoardDetails(this, mBoardDocumentID)
            } else {
                Log.e(R.string.cancelled.toString(), " " + R.string.cancelled)
            }
        }

    private fun setActionBar() {
        val taskListActivityToolbar: Toolbar = findViewById(R.id.task_list_activity_toolbar)

        setSupportActionBar(taskListActivityToolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white)
            actionBar.title = mBoardDetails.name
        }
        taskListActivityToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog()
        FireStoreHandler().getBoardDetails(this, mBoardDetails.documentID)
    }

    fun createTaskList(taskName: String) {
        val task = Task(taskName, FireStoreHandler().getCurrentUserId())
        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }

    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }

    fun addCardToTask(position: Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        val currentUserID = FireStoreHandler().getCurrentUserId()
        cardAssignedUsersList.add(currentUserID)
        val card = Card(cardName, "", currentUserID, cardAssignedUsersList)

        val cardList = mBoardDetails.taskList[position].cards
        cardList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardList
        )

        mBoardDetails.taskList[position] = task

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
        getCardDetails.launch(intent)
    }

    fun boardMembersDetailList(list: ArrayList<User>) {
        mAssignedMembersDetailList = list
        hideProgressDialog()

        val addTaskList = Task(R.string.add_task_list.toString())
        mBoardDetails.taskList.add(addTaskList)

        val taskListRv: RecyclerView = findViewById(R.id.task_list_rv)

        taskListRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        taskListRv.setHasFixedSize(true)

        val adapter = TaskItemAdapter(this, mBoardDetails.taskList)
        taskListRv.adapter = adapter

    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        mBoardDetails.taskList[taskListPosition].cards = cards
        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }
}