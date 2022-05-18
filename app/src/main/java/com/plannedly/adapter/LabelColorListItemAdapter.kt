package com.plannedly.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.plannedly.R

class LabelColorListItemAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_label_color, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        val viewMain: View = holder.itemView.findViewById(R.id.view_main)
        val selectedColorIv: ImageView = holder.itemView.findViewById(R.id.selected_color_iv)

        if (holder is MyViewHolder) {
            viewMain.setBackgroundColor(Color.parseColor(item))
            if (item == mSelectedColor) {
                selectedColorIv.visibility = View.VISIBLE
            } else {
                selectedColorIv.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick(position, item)
                }
            }
        }
    }


    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}