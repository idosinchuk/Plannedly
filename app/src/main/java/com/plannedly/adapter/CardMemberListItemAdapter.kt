package com.plannedly.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.plannedly.R
import com.plannedly.model.SelectedMembers
import de.hdodenhof.circleimageview.CircleImageView

open class CardMemberListItemAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMembers>,
    private val assignedMembers: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    R.layout.item_card_selected_member,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        val addMemberIv: CircleImageView = holder.itemView.findViewById(R.id.add_member_iv)
        val selectedMemberImageIv: CircleImageView =
            holder.itemView.findViewById(R.id.selected_member_image_iv)

        if (holder is MyViewHolder) {
            if (position == list.size - 1 && assignedMembers) {
                addMemberIv.visibility = View.VISIBLE
                selectedMemberImageIv.visibility = View.GONE
            } else {
                addMemberIv.visibility = View.GONE
                selectedMemberImageIv.visibility = View.VISIBLE

                //set users image
                Glide
                    .with(context)
                    .load(model.image)
                    .fitCenter()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(selectedMemberImageIv)
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
                }
            }
        }
    }


    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick()
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}