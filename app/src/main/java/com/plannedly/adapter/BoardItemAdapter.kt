package com.plannedly.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.plannedly.R
import com.plannedly.model.Board
import de.hdodenhof.circleimageview.CircleImageView

open class BoardItemAdapter(private val context: Context, private var list: ArrayList<Board>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_board, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            val itemBoardIv: CircleImageView = holder.itemView.findViewById(R.id.item_board_iv)
            val itemBoardNameTv: TextView = holder.itemView.findViewById(R.id.item_board_name_tv)
            val itemBoardCreatedByTv: TextView =
                holder.itemView.findViewById(R.id.item_board_created_by_tv)

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(itemBoardIv)
            //set text
            itemBoardNameTv.text = model.name
            itemBoardCreatedByTv.text = R.string.item_board_created_by.toString() + " " + model.createdBy

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener

    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}