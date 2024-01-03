package com.example.chatapp.items

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MessageItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    // Apply spacing to all items except the last one
    if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
      outRect.bottom = spaceHeight
    }
  }
}
