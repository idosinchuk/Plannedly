package com.plannedly.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plannedly.R
import com.plannedly.adapter.MemberItemAdapter
import com.plannedly.model.User

abstract class MembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
) : Dialog(context) {

    private var adapter: MemberItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {

        val dialogTitleTv: TextView = view.findViewById(R.id.dialog_title_tv)
        val dialogListRv: RecyclerView = view.findViewById(R.id.dialog_list_rv)

        dialogTitleTv.text = title

        if (list.size > 0) {
            dialogListRv.layoutManager = LinearLayoutManager(context)
            adapter = MemberItemAdapter(context, list)
            dialogListRv.adapter = adapter
            adapter!!.setOnClickListener(object : MemberItemAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User, color: String)
}