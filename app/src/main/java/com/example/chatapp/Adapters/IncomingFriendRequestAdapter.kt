package com.example.chatapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.dataclass.IncomingInviteData

class IncomingFriendRequestAdapter(private var userList: List<IncomingInviteData>) :
  RecyclerView.Adapter<IncomingFriendRequestAdapter.ViewHolder>() {

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val userNameTextView: TextView = itemView.findViewById(R.id.tvUserName)
    val userEmailTextView: TextView = itemView.findViewById(R.id.tvUserEmail)
    val friendRequestStatusTextView: TextView = itemView.findViewById(R.id.tvFriendRequestStatus)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_layout, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val incomingInvite = userList[position]
    holder.userNameTextView.text = incomingInvite.username
    holder.userEmailTextView.text = incomingInvite.email
    holder.friendRequestStatusTextView.text = incomingInvite.status ?: "Accept / Reject"
  }

  override fun getItemCount(): Int {
    return userList.size
  }

  // Method to update the data in the adapter with a list of RecipientData objects
  fun updateData(newList: List<IncomingInviteData>) {
    userList = newList
    notifyDataSetChanged()
  }
}
