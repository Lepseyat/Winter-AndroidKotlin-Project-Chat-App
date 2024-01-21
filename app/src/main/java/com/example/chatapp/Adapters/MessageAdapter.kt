package com.example.chatapp.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.model.Message
import java.text.SimpleDateFormat
import java.util.Date

class MessageAdapter(private val context: Context, private val messages: List<Message>) :
  RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

  class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textViewUsername: TextView = itemView.findViewById(R.id.textViewUsername)
    val textViewContent: TextView = itemView.findViewById(R.id.textViewContent)
    val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
    return MessageViewHolder(view)
  }

  override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
    val message = messages[position]

    // Bind data to views
    Log.d("MessageAdapter", "Binding message: $message")
    holder.textViewUsername.text = message.sender.username
    holder.textViewContent.text = message.content

    val formattedTimestamp = convertUnixTimestampToString(message.timestamp.toLong())
    holder.textViewTimestamp.text = formattedTimestamp
  }

  override fun getItemCount(): Int {
    return messages.size
  }

  private fun convertUnixTimestampToString(unixTimestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
    val date = Date(unixTimestamp)
    return dateFormat.format(date)
  }
}
