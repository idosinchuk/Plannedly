package com.plannedly.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plannedly.R
import com.plannedly.activity.TaskListActivity
import com.plannedly.model.Task
import java.util.*

open class TaskItemAdapter(private val context: Context, private var list: ArrayList<Task>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDP()).toPX(), 0, (40.toDP()).toPX(), 0)
        view.layoutParams = layoutParams
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val model = list[position]

        if (holder is MyViewHolder) {
            val addTaskListTv: TextView = holder.itemView.findViewById(R.id.add_task_list_tv)
            val taskListNameEt: EditText = holder.itemView.findViewById(R.id.task_list_name_et)
            val taskListTitleTv: TextView = holder.itemView.findViewById(R.id.task_list_title_tv)
            val editTaskListNameEt: EditText =
                holder.itemView.findViewById(R.id.edit_task_list_name_et)
            val cardNameEt: EditText = holder.itemView.findViewById(R.id.card_name_et)
            val addCardTv: TextView = holder.itemView.findViewById(R.id.add_card_tv)

            val taskItemLl: LinearLayout = holder.itemView.findViewById(R.id.task_item_ll)

            if (position == list.size - 1) {
                addTaskListTv.visibility = View.VISIBLE
                taskItemLl.visibility = View.GONE
            } else {
                addTaskListTv.visibility = View.GONE
                taskItemLl.visibility = View.VISIBLE
            }

            taskListTitleTv.text = model.title

            val addTaskListNameCv: CardView =
                holder.itemView.findViewById(R.id.add_task_list_name_cv)

            addTaskListTv.setOnClickListener {
                addTaskListTv.visibility = View.GONE
                addTaskListNameCv.visibility = View.VISIBLE
            }

            val closeListNameIb: ImageButton = holder.itemView.findViewById(R.id.close_list_name_ib)

            closeListNameIb.setOnClickListener {
                addTaskListTv.visibility = View.VISIBLE
                addTaskListNameCv.visibility = View.GONE
            }

            val doneListNameIb: ImageButton = holder.itemView.findViewById(R.id.done_list_name_ib)

            doneListNameIb.setOnClickListener {
                val listName = taskListNameEt.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, R.string.please_enter_list_name, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            val editListNameIb: ImageButton = holder.itemView.findViewById(R.id.edit_list_name_ib)
            val titleViewLl: LinearLayout = holder.itemView.findViewById(R.id.title_view_ll)
            val editTaskListNameCv: CardView =
                holder.itemView.findViewById(R.id.edit_task_list_name_cv)

            editListNameIb.setOnClickListener {
                editTaskListNameEt.setText(model.title)
                titleViewLl.visibility = View.GONE
                editTaskListNameCv.visibility = View.VISIBLE
            }

            val closeEditableViewIb: ImageButton =
                holder.itemView.findViewById(R.id.close_editable_view_ib)

            closeEditableViewIb.setOnClickListener {
                titleViewLl.visibility = View.VISIBLE
                editTaskListNameCv.visibility = View.GONE
            }

            val doneEditListNameIb: ImageButton =
                holder.itemView.findViewById(R.id.done_edit_list_name_ib)

            doneEditListNameIb.setOnClickListener {
                val listName = editTaskListNameEt.text.toString()
                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                } else {
                    Toast.makeText(context, R.string.please_enter_list_name, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            val deleteListIb: ImageButton = holder.itemView.findViewById(R.id.delete_list_ib)

            deleteListIb.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }

            val addCardCv: CardView = holder.itemView.findViewById(R.id.add_card_cv)

            addCardTv.setOnClickListener {
                addCardTv.visibility = View.GONE
                addCardCv.visibility = View.VISIBLE
            }

            val closeCardNameIb: ImageButton = holder.itemView.findViewById(R.id.close_card_name_ib)

            closeCardNameIb.setOnClickListener {
                addCardTv.visibility = View.VISIBLE
                addCardCv.visibility = View.GONE
            }

            val doneCardNameIb: ImageButton = holder.itemView.findViewById(R.id.done_card_name_ib)

            doneCardNameIb.setOnClickListener {
                val cardName = cardNameEt.text.toString()
                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.addCardToTask(position, cardName)
                    }
                } else {
                    Toast.makeText(context, R.string.please_enter_card_name, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            val cardListRv: RecyclerView = holder.itemView.findViewById(R.id.card_list_rv)

            cardListRv.layoutManager = LinearLayoutManager(context)

            val adapter = CardListItemAdapter(context, model.cards)
            cardListRv.adapter = adapter

            adapter.setOnClickListener(object :
                CardListItemAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {

                    if (context is TaskListActivity) {
                        context.cardDetails(position, cardPosition)
                    }
                }
            })

            val dividerItemDecoration = DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )

            cardListRv.addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(
                object :
                    ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val draggedPosition = viewHolder.bindingAdapterPosition
                        val targetPosition = target.bindingAdapterPosition

                        if (mPositionDraggedFrom == -1) {
                            mPositionDraggedFrom = draggedPosition
                        }

                        mPositionDraggedTo = targetPosition
                        Collections.swap(list[position].cards, draggedPosition, targetPosition)
                        adapter.notifyItemMoved(draggedPosition, targetPosition)
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    }

                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)

                        if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {
                            (context as TaskListActivity).updateCardsInTaskList(
                                position,
                                list[position].cards
                            )
                        }
                        mPositionDraggedFrom = -1
                        mPositionDraggedTo = -1
                    }
                }
            )

            helper.attachToRecyclerView(cardListRv)
        }
    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.alert)
        builder.setMessage(R.string.are_you_sure_you_want_delete.toString() + " " + title + "?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(R.string.yes) { dialog, _ ->
            dialog.dismiss()

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPX(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}