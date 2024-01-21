package com.example.chatapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.dataclass.UserData

class PendingFriendRequestAdapter(private var userList: List<UserData>) :
  RecyclerView.Adapter<PendingFriendRequestAdapter.ViewHolder>() {

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val userNameTextView: TextView = itemView.findViewById(R.id.tvUserName)
    val userEmailTextView: TextView = itemView.findViewById(R.id.tvUserEmail)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_layout, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val sender = userList[position]
    holder.userNameTextView.text = sender.username
    holder.userEmailTextView.text = sender.email
  }

  override fun getItemCount(): Int {
    return userList.size
  }

  fun updateData(newList: List<UserData>) {
    userList = newList
    notifyDataSetChanged()
  }
}
