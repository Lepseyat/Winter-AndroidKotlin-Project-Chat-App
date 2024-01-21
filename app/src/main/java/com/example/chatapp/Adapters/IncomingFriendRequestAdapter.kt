package com.example.chatapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.dataclass.UserData

class IncomingFriendRequestAdapter(
  private var userList: List<UserData>,
  private var friendRequestId: List<Int>
) : RecyclerView.Adapter<IncomingFriendRequestAdapter.ViewHolder>() {

  private var onItemClickListener: ((UserData, Int) -> Unit)? = null

  fun setOnItemClickListener(listener: (UserData, Int) -> Unit) {
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
    val incomingFriendRequestId = friendRequestId[position]
    holder.userNameTextView.text = incomingInvite.username
    holder.userEmailTextView.text = incomingInvite.email

    holder.itemView.setOnClickListener {
      onItemClickListener?.invoke(incomingInvite, incomingFriendRequestId)
    }
  }

  override fun getItemCount(): Int {
    return userList.size
  }

  fun updateData(newList: List<UserData>, friendRequestIdList: List<Int>) {
    userList = newList
    friendRequestId = friendRequestIdList
    notifyDataSetChanged()
  }
}
