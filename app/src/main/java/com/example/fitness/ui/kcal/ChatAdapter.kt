package com.example.fitness.ui.kcal

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitness.data.app_model.ChatItem

class ChatAdapter : ListAdapter<ChatItem, ChatAdapter.ChatVH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatVH {
        val tv = TextView(parent.context).apply {
            setPadding(24, 16, 24, 16)
        }
        return ChatVH(tv)
    }

    override fun onBindViewHolder(holder: ChatVH, position: Int) {
        val item = getItem(position)
        (holder.itemView as TextView).text =
            if (item.isUser) "You: ${item.text}" else "Gemini: ${item.text}"
    }

    class ChatVH(view: TextView) : RecyclerView.ViewHolder(view)

    object Diff : DiffUtil.ItemCallback<ChatItem>() {
        override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem === newItem
        override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem == newItem
    }
}