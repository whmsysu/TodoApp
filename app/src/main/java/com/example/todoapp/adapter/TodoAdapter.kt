package com.example.todoapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.Priority
import com.example.todoapp.data.Todo
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(
    private val onTodoClick: (Todo) -> Unit,
    private val onTodoCheck: (Todo, Boolean) -> Unit
) : ListAdapter<Todo, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        private val priorityTextView: TextView = itemView.findViewById(R.id.tv_priority)
        private val dueDateTextView: TextView = itemView.findViewById(R.id.tv_due_date)
        private val dailyTimeTextView: TextView = itemView.findViewById(R.id.tv_daily_time)
        private val completedCheckBox: CheckBox = itemView.findViewById(R.id.cb_completed)

        @SuppressLint("SetTextI18n")
        fun bind(todo: Todo) {
            // Debug: Check if todo has empty title
            if (todo.title.isBlank()) {
                titleTextView.text = "[空标题]"
                titleTextView.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            } else {
                titleTextView.text = todo.title
                titleTextView.setTextColor(itemView.context.getColor(android.R.color.black))
            }
            
            priorityTextView.text = getPriorityText(todo.priority)
            priorityTextView.setBackgroundColor(getPriorityColor(itemView.context, todo.priority))
            
            todo.dueDate?.let {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeText = todo.dueTime?.let { time -> " $time" } ?: ""
                dueDateTextView.text = "${dateFormat.format(it)}$timeText"
                dueDateTextView.visibility = View.VISIBLE
            } ?: run {
                dueDateTextView.visibility = View.GONE
            }

            // Handle daily todo display
            if (todo.isDaily) {
                val timeText = todo.dailyTime?.let { " ${it}" } ?: ""
                val endDateText = todo.dailyEndDate?.let { endDate ->
                    val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
                    " 至 ${dateFormat.format(endDate)}"
                } ?: ""
                dailyTimeTextView.text = "每日重复$timeText$endDateText"
                dailyTimeTextView.visibility = View.VISIBLE
            } else {
                dailyTimeTextView.visibility = View.GONE
            }

            completedCheckBox.isChecked = todo.isCompleted
            titleTextView.paint.isStrikeThruText = todo.isCompleted

            itemView.setOnClickListener { onTodoClick(todo) }
            completedCheckBox.setOnClickListener {
                onTodoCheck(todo, completedCheckBox.isChecked)
            }
        }

        private fun getPriorityText(priority: Priority): String {
            return when (priority) {
                Priority.HIGH -> "高"
                Priority.MEDIUM -> "中"
                Priority.LOW -> "低"
            }
        }

        private fun getPriorityColor(context: android.content.Context, priority: Priority): Int {
            return when (priority) {
                Priority.HIGH -> ContextCompat.getColor(context, R.color.red)
                Priority.MEDIUM -> ContextCompat.getColor(context, R.color.orange)
                Priority.LOW -> ContextCompat.getColor(context, R.color.green)
            }
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }
    }
}
