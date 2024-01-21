package com.example.chatapp.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.dataclass.UserData

class FriendsListAdapter(private var userList: List<UserData>) :
  RecyclerView.Adapter<FriendsListAdapter.ViewHolder>() {

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val userNameTextView: TextView = itemView.findViewById(R.id.tvUserName)
    val userEmailTextView: TextView = itemView.findViewById(R.id.tvUserEmail)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_layout, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val incomingInvite = userList[position]
    holder.userNameTextView.text =
      incomingInvite.username.takeIf { it?.isNotBlank() == true } ?: "N/A"
    holder.userEmailTextView.text =
      incomingInvite.email.takeIf { it?.isNotBlank() == true } ?: "N/A"
    println(
      "Bind position $position: ${holder.userNameTextView.text}, ${holder.userEmailTextView.text}"
    )
  }

  override fun getItemCount(): Int {
    return userList.size
  }

  @SuppressLint("NotifyDataSetChanged")
  fun updateData(newList: List<UserData>) {
    userList = newList
    notifyDataSetChanged()
    println("Updated data. New list size: ${newList.size}")
  }
}
