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
  private var onItemClickListener: ((Int) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupChatViewHolder {
    val view = inflater.inflate(R.layout.item_group_chat, parent, false)
    val viewHolder = GroupChatViewHolder(view)

    // Set the click listener when a new view is created
    viewHolder.itemView.setOnClickListener {
      onItemClickListener?.invoke(viewHolder.adapterPosition)
    }

    return viewHolder
  }

  override fun onBindViewHolder(holder: GroupChatViewHolder, position: Int) {
    val groupChat = groupChats[position]
    holder.groupChatName.text = groupChat.name
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

  fun setOnItemClickListener(listener: (Int) -> Unit) {
    onItemClickListener = listener
  }
}
