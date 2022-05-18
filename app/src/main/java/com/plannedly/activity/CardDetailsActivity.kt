package com.plannedly.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plannedly.R
import com.plannedly.adapter.CardMemberListItemAdapter
import com.plannedly.dialog.LabelColorListDialog
import com.plannedly.dialog.MembersListDialog
import com.plannedly.firebase.FireStoreHandler
import com.plannedly.model.*
import com.plannedly.util.Constants
import java.util.*


class CardDetailsActivity : BaseActivity() {
    private lateinit var mBoardDetails: Board
    private lateinit var mMemberDetailList: ArrayList<User>
    private var mTaskListPosition = -1
    private var mCardListPosition = -1
    private var mSelectedLabelColor = ""
    private var mSelectedStartDateMS: Long = 0
    private var mSelectedDueDateMS: Long = 0

    private val GREEN_LABEL_COLOR: String = "#61bd4f"
    private val YELLOW_LABEL_COLOR: String = "#f2d600"
    private val ORANGE_LABEL_COLOR: String = "#ff9f1a"
    private val RED_LABEL_COLOR: String = "#eb5a46"
    private val PURPLE_LABEL_COLOR: String = "#c377e0"
    private val BLUE_LABEL_COLOR: String = "#0079bf"
    private val ACTIVE_CLOCK_ICON_COLOR: String = "#EC9488"

    private lateinit var nameCardDetailsEt: AppCompatEditText
    private lateinit var descriptionCardDetailsEt: AppCompatEditText
    private lateinit var selectLabelColorTv: TextView
    private lateinit var selectMembersTv: TextView
    private lateinit var updateCardDetailsBtn: Button
    private lateinit var selectStartDateTv: TextView
    private lateinit var selectDueDateTv: TextView
    private lateinit var toolbarCardDetailsActivity: Toolbar
    private lateinit var selectedMembersListRv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setActionBar()

        nameCardDetailsEt = findViewById(R.id.name_card_details_et)
        nameCardDetailsEt.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
        nameCardDetailsEt.setSelection(nameCardDetailsEt.text.toString().length)


        descriptionCardDetailsEt =
            findViewById(R.id.description_card_details_et)
        descriptionCardDetailsEt.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].description)
        descriptionCardDetailsEt.setSelection(descriptionCardDetailsEt.text.toString().length)

        selectLabelColorTv = findViewById(R.id.select_label_color_tv)
        selectMembersTv = findViewById(R.id.select_members_tv)
        updateCardDetailsBtn = findViewById(R.id.update_card_details_btn)
        selectStartDateTv = findViewById(R.id.select_start_date_tv)
        selectDueDateTv = findViewById(R.id.select_due_date_tv)
        toolbarCardDetailsActivity = findViewById(R.id.toolbar_card_details_activity)
        selectedMembersListRv = findViewById(R.id.selected_members_list_rv)

        initializeLabelColor()

        initilizeDates()

        setupSelectedMembersList()

        updateCardDetailsBtn.setOnClickListener {
            if (nameCardDetailsEt.text.toString().isNotEmpty()) {
                showProgressDialog()
                updateCardDetails()
            } else {
                Toast.makeText(this, R.string.please_enter_card_name, Toast.LENGTH_SHORT).show()
            }
        }

        selectLabelColorTv.setOnClickListener {
            showLabelColorsListDialog()
        }

        selectMembersTv.setOnClickListener {
            membersListDialog()
        }

        selectStartDateTv.setOnClickListener {
            val selectStartDateTv: TextView = findViewById(R.id.select_start_date_tv)
            showDatePicker(selectStartDateTv, Constants.START_DATE_PICKER)
        }

        selectDueDateTv.setOnClickListener {
            val selectDueDateTv: TextView = findViewById(R.id.select_due_date_tv)
            showDatePicker(selectDueDateTv, Constants.DUE_DATE_PICKER)
        }

    }

    private fun initializeLabelColor() {
        mSelectedLabelColor =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor
        if (mSelectedLabelColor.isNotEmpty()) {
            if (mSelectedLabelColor == Color.TRANSPARENT.toString()) {
                setLabelColor(Color.TRANSPARENT)
            } else {
                setLabelColor(Color.parseColor(mSelectedLabelColor))
            }
        }
    }

    private fun initilizeDates() {
        mSelectedStartDateMS =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].startDate

        mSelectedDueDateMS =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].dueDate

        if (mSelectedStartDateMS > 0) {
            selectStartDateTv.text = applySimpleDateFormat(mSelectedStartDateMS)
        }
        if (mSelectedDueDateMS > 0) {
            selectDueDateTv.text = applySimpleDateFormat(mSelectedDueDateMS)
        }

        changeClockIconColor()
    }

    private fun applySimpleDateFormat(date: Long): String {
        val simpleDateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.ENGLISH)
        return simpleDateFormat.format(Date(date))
    }

    private fun changeClockIconColor() {
        val compoundDrawables = selectStartDateTv.compoundDrawables
        val drawableLeft: Drawable = compoundDrawables[0].mutate()

        val clockColor: Int = if (mSelectedStartDateMS > 0 || mSelectedDueDateMS > 0) {
            Color.parseColor(ACTIVE_CLOCK_ICON_COLOR)
        } else {
            Color.BLACK
        }

        drawableLeft.colorFilter =
            PorterDuffColorFilter(clockColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setActionBar() {
        val toolbarCardDetailsActivity: Toolbar = findViewById(R.id.toolbar_card_details_activity)

        setSupportActionBar(toolbarCardDetailsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white)
            actionBar.title =
                mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        }

        toolbarCardDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getIntentData() {

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardListPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMemberDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {
        val card = Card(
            nameCardDetailsEt.text.toString(),
            descriptionCardDetailsEt.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
            mSelectedLabelColor,
            mSelectedStartDateMS,
            mSelectedDueDateMS
        )

        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card

        FireStoreHandler().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    private fun membersListDialog() {
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            for (i in mMemberDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMemberDetailList[i].id == j) {
                        mMemberDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMemberDetailList.indices) {
                mMemberDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this,
            mMemberDetailList,
            R.string.members_of_card.toString()
        ) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!mBoardDetails
                            .taskList[mTaskListPosition]
                            .cards[mCardListPosition]
                            .assignedTo.contains(user.id)
                    ) {
                        mBoardDetails
                            .taskList[mTaskListPosition]
                            .cards[mCardListPosition]
                            .assignedTo.add(user.id)
                    }
                } else {
                    mBoardDetails
                        .taskList[mTaskListPosition]
                        .cards[mCardListPosition]
                        .assignedTo.remove(user.id)

                    for (i in mMemberDetailList.indices) {
                        if (mMemberDetailList[i].id == user.id) {
                            mMemberDetailList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun deleteCard() {
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardListPosition)

        val tasksList: ArrayList<Task> = mBoardDetails.taskList
        tasksList.removeAt(tasksList.size - 1)

        tasksList[mTaskListPosition].cards = cardsList

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                val cardTitle =
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
                alertDialogForDeleteCard(cardTitle)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.alert)

        builder.setMessage(R.string.are_you_sure_you_want_delete.toString() + " " + cardName + "?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(R.string.yes) { dialog, _ ->
            dialog.dismiss()
            deleteCard()
        }

        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add(GREEN_LABEL_COLOR)
        colorsList.add(YELLOW_LABEL_COLOR)
        colorsList.add(ORANGE_LABEL_COLOR)
        colorsList.add(RED_LABEL_COLOR)
        colorsList.add(PURPLE_LABEL_COLOR)
        colorsList.add(BLUE_LABEL_COLOR)

        return colorsList
    }

    private fun setLabelColor(color: Int) {
        selectLabelColorTv.text = "    "
        selectLabelColorTv.setBackgroundColor(color)
    }

    private fun showLabelColorsListDialog() {
        val colorsList: ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(
            this,
            colorsList,
            R.string.select_label_color.toString(),
            mSelectedLabelColor
        ) {
            override fun onItemSelected(color: String) {
                if (mSelectedLabelColor == color) {
                    mSelectedLabelColor = Color.TRANSPARENT.toString()
                    setLabelColor(Color.TRANSPARENT)
                } else {
                    mSelectedLabelColor = color
                    setLabelColor(Color.parseColor(color))
                }
            }
        }
        listDialog.show()
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMemberDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMemberDetailList[i].id == j) {
                    val selectedMember =
                        SelectedMembers(mMemberDetailList[i].id, mMemberDetailList[i].image)
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("", ""))
            //selectMembersTv.visibility = View.GONE
            selectMembersTv.text = ""
            selectedMembersListRv.visibility = View.VISIBLE

            selectedMembersListRv.layoutManager = GridLayoutManager(
                this, 6
            )

            val adapter = CardMemberListItemAdapter(this, selectedMembersList, true)
            selectedMembersListRv.adapter = adapter
            adapter.setOnClickListener(
                object : CardMemberListItemAdapter.OnClickListener {
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )
        } else {
            selectMembersTv.visibility = View.VISIBLE
            selectedMembersListRv.visibility = View.GONE
            selectMembersTv.text = R.string.select_members.toString()
        }
    }

    private fun showDatePicker(selectDateTv: TextView, dateTypePicker: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialogListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                selectDateTv.text = selectedDate

                val sdf = SimpleDateFormat(Constants.DATE_FORMAT, Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                if (dateTypePicker == Constants.START_DATE_PICKER) {
                    mSelectedStartDateMS = theDate!!.time
                } else if (dateTypePicker == Constants.DUE_DATE_PICKER) {
                    mSelectedDueDateMS = theDate!!.time
                }
            }

        val datePickerDialog = DatePickerDialog(
            this,
            datePickerDialogListener,
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}