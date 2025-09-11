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
import com.example.todoapp.core.database.data.Priority
import com.example.todoapp.core.database.data.Todo
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
        android.util.Log.d("TodoAdapter", "onBindViewHolder called for position $position, itemCount = $itemCount")
        holder.bind(getItem(position))
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        private val priorityTextView: TextView = itemView.findViewById(R.id.tv_priority)
        private val dueDateTextView: TextView = itemView.findViewById(R.id.tv_due_date)
        private val dailyTimeTextView: TextView = itemView.findViewById(R.id.tv_daily_time)
        private val completedTimeTextView: TextView = itemView.findViewById(R.id.tv_completed_time)
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

            val isCompleted = isTodoCompleted(todo)
            completedCheckBox.isChecked = isCompleted
            titleTextView.paint.isStrikeThruText = isCompleted

            // 显示完成时间
            if (isCompleted && todo.completedAt != null) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                completedTimeTextView.text = "完成于: ${dateFormat.format(todo.completedAt)}"
                completedTimeTextView.visibility = View.VISIBLE
            } else {
                completedTimeTextView.visibility = View.GONE
            }

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
        
        private fun isTodoCompleted(todo: Todo): Boolean {
            // 如果completedAt为空，说明未完成
            val completedAt = todo.completedAt ?: return false
            
            // 如果是每日TODO，需要检查是否是今天完成的
            if (todo.isDaily) {
                val today = Calendar.getInstance()
                val completedDate = Calendar.getInstance()
                completedDate.time = completedAt
                
                return today.get(Calendar.YEAR) == completedDate.get(Calendar.YEAR) &&
                       today.get(Calendar.DAY_OF_YEAR) == completedDate.get(Calendar.DAY_OF_YEAR)
            }
            
            // 普通TODO，只要有completedAt就算完成
            return true
        }
        
        private fun isDailyTodoExpired(todo: Todo): Boolean {
            if (!todo.isDaily) return false
            
            val dailyEndDate = todo.dailyEndDate ?: return false // 没有结束日期，不过期
            val today = Calendar.getInstance()
            val endDate = Calendar.getInstance()
            endDate.time = dailyEndDate
            
            // 设置时间为00:00:00进行比较
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)
            
            endDate.set(Calendar.HOUR_OF_DAY, 0)
            endDate.set(Calendar.MINUTE, 0)
            endDate.set(Calendar.SECOND, 0)
            endDate.set(Calendar.MILLISECOND, 0)
            
            return today.after(endDate)
        }
    }

    /**
     * Get todo at specific position
     */
    fun getTodoAt(position: Int): Todo? {
        return if (position >= 0 && position < itemCount) {
            getItem(position)
        } else {
            null
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            val result = oldItem.id == newItem.id
            android.util.Log.d("TodoDiffCallback", "areItemsTheSame: oldId=${oldItem.id}, newId=${newItem.id}, result=$result")
            return result
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            val result = oldItem == newItem
            android.util.Log.d("TodoDiffCallback", "areContentsTheSame: oldItem=$oldItem, newItem=$newItem, result=$result")
            return result
        }
    }
}
