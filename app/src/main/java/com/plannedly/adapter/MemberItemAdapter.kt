package com.plannedly.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.plannedly.R
import com.plannedly.model.User
import com.plannedly.util.Constants
import de.hdodenhof.circleimageview.CircleImageView

open class MemberItemAdapter(private val context: Context, private var list: ArrayList<User>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_member, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val memberNameTv: TextView = holder.itemView.findViewById(R.id.member_name_tv)
        val memberEmailTv: TextView = holder.itemView.findViewById(R.id.member_email_tv)
        val memberImageIv: CircleImageView = holder.itemView.findViewById(R.id.member_image_iv)
        val selectedMemberIv: ImageView =
            holder.itemView.findViewById(R.id.selected_member_iv)

        val model = list[position]

        if (holder is MyViewHolder) {
            memberNameTv.text = model.name
            memberEmailTv.text = model.email
            Glide
                .with(context)
                .load(model.image)
                .fitCenter()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(memberImageIv)

            if (model.selected) {
                selectedMemberIv.visibility = View.VISIBLE
            } else {
                selectedMemberIv.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    if (model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    } else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}