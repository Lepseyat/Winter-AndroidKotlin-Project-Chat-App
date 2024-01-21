package com.example.chatapp.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.model.GroupChat

class GroupChatAdapter(private val context: Context, private var groupChats: List<GroupChat>) :
  RecyclerView.Adapter<GroupChatAdapter.GroupChatViewHolder>() {

  private val inflater: LayoutInflater = LayoutInflater.from(context)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupChatViewHolder {
    val view = inflater.inflate(R.layout.item_group_chat, parent, false)
    return GroupChatViewHolder(view)
  }

  override fun onBindViewHolder(holder: GroupChatViewHolder, position: Int) {
    val groupChat = groupChats[position]
    holder.groupChatName.text = groupChat.name
    // Set other views as needed
  }

  override fun getItemCount(): Int {
    return groupChats.size
  }

  inner class GroupChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var groupChatName: TextView = itemView.findViewById(R.id.textViewGroupName)
  }

  fun updateData(newData: List<GroupChat>) {
    groupChats = newData
    notifyDataSetChanged()
  }
}
