package com.example.chatapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.dataclass.UserData

class MembersInAGroupChatAdapter(
  private var userList: List<UserData>,
  private var friendsEmails: List<String>,
) : RecyclerView.Adapter<MembersInAGroupChatAdapter.ViewHolder>() {

  private var onItemClickListener: ((UserData, String) -> Unit)? = null

  fun setOnItemClickListener(listener: (UserData, String) -> Unit) {
    onItemClickListener = listener
  }

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
    val friendEmail = friendsEmails[position]

    holder.userNameTextView.text = incomingInvite.username
    holder.userEmailTextView.text = incomingInvite.email

    holder.itemView.setOnClickListener { onItemClickListener?.invoke(incomingInvite, friendEmail) }
  }

  override fun getItemCount(): Int {
    return userList.size
  }

  fun updateData(newList: List<UserData>, friendsEmailsList: List<String>) {
    userList = newList
    friendsEmails = friendsEmailsList
    notifyDataSetChanged()
  }
}
