package com.plannedly.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plannedly.R
import com.plannedly.activity.TaskListActivity
import com.plannedly.model.Card
import com.plannedly.model.SelectedMembers

open class CardListItemAdapter(private val context: Context, private var list: ArrayList<Card>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_card, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val model = list[holder.bindingAdapterPosition]

        val cardNameTv: TextView = holder.itemView.findViewById(R.id.card_name_tv)

        if (holder is MyViewHolder) {

            cardNameTv.text = model.name

            val viewLabelColor: View = holder.itemView.findViewById(R.id.view_label_color)

            if (model.labelColor.isNotEmpty() && model.labelColor != Color.TRANSPARENT.toString()) {
                viewLabelColor.visibility = View.VISIBLE
                viewLabelColor.setBackgroundColor(
                    Color.parseColor(model.labelColor)
                )
            } else {
                viewLabelColor.visibility = View.GONE
            }

            if ((context as TaskListActivity).mAssignedMembersDetailList.size > 0) {

                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
                val assignedMembers = context.mAssignedMembersDetailList

                for (i in assignedMembers.indices) {
                    for (j in model.assignedTo) {
                        if (assignedMembers[i].id == j) {
                            val selectedMember =
                                SelectedMembers(assignedMembers[i].id, assignedMembers[i].image)
                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                val cardSelectedMembersListRv: RecyclerView =
                    holder.itemView.findViewById(R.id.card_selected_members_list_rv)

                if (selectedMembersList.size > 0) {
                    if (selectedMembersList.size == 1
                        && selectedMembersList[0].id == model.createdBy
                    ) {
                        cardSelectedMembersListRv.visibility = View.GONE
                    } else {
                        cardSelectedMembersListRv.visibility = View.VISIBLE
                        cardSelectedMembersListRv.layoutManager =
                            GridLayoutManager(context, 4)
                        val adapter = CardMemberListItemAdapter(context, selectedMembersList, false)
                        cardSelectedMembersListRv.adapter = adapter
                        adapter.setOnClickListener(object :
                            CardMemberListItemAdapter.OnClickListener {
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                } else {
                    cardSelectedMembersListRv.visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener

    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}