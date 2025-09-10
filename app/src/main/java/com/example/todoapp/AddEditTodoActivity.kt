package com.example.todoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.todoapp.data.Priority
import com.example.todoapp.data.Todo
import com.example.todoapp.databinding.ActivityAddEditTodoBinding
import com.example.todoapp.viewmodel.AddEditTodoViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddEditTodoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditTodoBinding
    private val viewModel: AddEditTodoViewModel by viewModels()
    private var selectedPriority: Priority = Priority.MEDIUM
    private var selectedDueDate: Date? = null
    private var selectedDueTime: String? = null
    private var isDaily: Boolean = false
    private var selectedDailyTime: String? = null
    private var selectedDailyEndDate: Date? = null

    companion object {
        const val EXTRA_TODO_ID = "com.example.todoapp.EXTRA_TODO_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupPriorityChips()
        setupDueDateButton()
        setupDueTimeButton()
        setupDailySwitch()
        setupDailyTimeButton()
        setupDailyEndDateButton()
        setupSaveButton()
        observeViewModel()
        
        // Initialize time button state
        updateTimeButtonState()

        val todoId = intent.getIntExtra(EXTRA_TODO_ID, -1)
        if (todoId != -1) {
            viewModel.loadTodo(todoId)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
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

    private fun disableAllFields() {
        // 禁用标题输入
        binding.etTitle.isEnabled = false
        binding.etTitle.setTextColor(getColor(android.R.color.darker_gray))
        
        // 禁用优先级选择
        binding.radioHigh.isEnabled = false
        binding.radioMedium.isEnabled = false
        binding.radioLow.isEnabled = false
        
        // 禁用截止日期和时间按钮
        binding.btnDueDate.isEnabled = false
        binding.btnDueTime.isEnabled = false
        
        // 禁用每日任务开关
        binding.switchDaily.isEnabled = false
        
        // 禁用每日任务相关按钮
        binding.btnDailyTime.isEnabled = false
        binding.btnDailyEndDate.isEnabled = false
        
        // 隐藏保存按钮
        binding.btnSave.visibility = android.view.View.GONE
        
        // 显示提示信息
        binding.etTitle.hint = "此任务已完成，无法编辑"
    }

    private fun setupPriorityChips() {
        binding.radioHigh.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPriority = Priority.HIGH
            }
        }
        binding.radioMedium.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPriority = Priority.MEDIUM
            }
        }
        binding.radioLow.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPriority = Priority.LOW
            }
        }
        // Set default selection
        binding.radioMedium.isChecked = true
        selectedPriority = Priority.MEDIUM
    }

    private fun setupDueDateButton() {
        binding.btnDueDate.setOnClickListener {
            showDatePicker()
        }
        
        // Long click to clear date
        binding.btnDueDate.setOnLongClickListener {
            if (selectedDueDate != null) {
                selectedDueDate = null
                selectedDueTime = null
                binding.btnDueDate.text = "选择日期"
                binding.btnDueTime.text = "选择时间（可选）"
                updateTimeButtonState()
                Toast.makeText(this, "已清除日期和时间", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun setupDueTimeButton() {
        binding.btnDueTime.setOnClickListener {
            if (selectedDueDate != null) {
                showDueTimePicker()
            } else {
                Toast.makeText(this, "请先选择截止日期", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDailySwitch() {
        binding.switchDaily.setOnCheckedChangeListener { _, isChecked ->
            isDaily = isChecked
            binding.layoutDailyOptions.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
            
            // 当选择每日任务时，隐藏截止日期和时间
            if (isChecked) {
                binding.btnDueDate.visibility = android.view.View.GONE
                binding.btnDueTime.visibility = android.view.View.GONE
                // 清除已选择的日期和时间
                selectedDueDate = null
                selectedDueTime = null
                binding.btnDueDate.text = "选择截止日期"
                binding.btnDueTime.text = "选择时间（可选）"
            } else {
                binding.btnDueDate.visibility = android.view.View.VISIBLE
                binding.btnDueTime.visibility = android.view.View.VISIBLE
                updateTimeButtonState()
                // 清空每日任务相关数据
                selectedDailyTime = null
                selectedDailyEndDate = null
                binding.btnDailyTime.text = "选择提醒时间（可选）"
                binding.btnDailyEndDate.text = "选择结束日期（可选）"
            }
        }
    }

    private fun setupDailyTimeButton() {
        binding.btnDailyTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun setupDailyEndDateButton() {
        binding.btnDailyEndDate.setOnClickListener {
            showDailyEndDatePicker()
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveTodo()
        }
    }

    private fun observeViewModel() {
        viewModel.todo.observe(this, Observer { todo ->
            todo?.let {
                populateFields(it)
            }
        })

        viewModel.isEditMode.observe(this, Observer { isEditMode ->
            supportActionBar?.title = if (isEditMode) "编辑待办事项" else "添加待办事项"
        })
    }

    private fun populateFields(todo: Todo) {
        binding.etTitle.setText(todo.title)
        
        // 如果TODO已完成或每日TODO已过期，禁用所有编辑功能
        if (isTodoCompleted(todo) || isDailyTodoExpired(todo)) {
            disableAllFields()
            return
        }
        
        // Set the correct priority selection
        when (todo.priority) {
            Priority.HIGH -> binding.radioHigh.isChecked = true
            Priority.MEDIUM -> binding.radioMedium.isChecked = true
            Priority.LOW -> binding.radioLow.isChecked = true
        }
        selectedPriority = todo.priority
        
        todo.dueDate?.let {
            selectedDueDate = it
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.btnDueDate.text = dateFormat.format(it)
        }

        todo.dueTime?.let { time ->
            selectedDueTime = time
            binding.btnDueTime.text = time
        }

        // Handle daily todo fields
        isDaily = todo.isDaily
        binding.switchDaily.isChecked = isDaily
        binding.layoutDailyOptions.visibility = if (isDaily) android.view.View.VISIBLE else android.view.View.GONE
        
        // 如果是每日任务，隐藏截止日期和时间，并清空数据
        if (isDaily) {
            binding.btnDueDate.visibility = android.view.View.GONE
            binding.btnDueTime.visibility = android.view.View.GONE
            // 清空截止日期和时间数据
            selectedDueDate = null
            selectedDueTime = null
            binding.btnDueDate.text = "选择截止日期"
            binding.btnDueTime.text = "选择时间（可选）"
        } else {
            binding.btnDueDate.visibility = android.view.View.VISIBLE
            binding.btnDueTime.visibility = android.view.View.VISIBLE
            updateTimeButtonState()
            // 清空每日任务相关数据
            selectedDailyTime = null
            selectedDailyEndDate = null
            binding.btnDailyTime.text = "选择提醒时间（可选）"
            binding.btnDailyEndDate.text = "选择结束日期（可选）"
        }
        
        todo.dailyTime?.let { time ->
            selectedDailyTime = time
            binding.btnDailyTime.text = time
        }

        todo.dailyEndDate?.let { endDate ->
            selectedDailyEndDate = endDate
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.btnDailyEndDate.text = dateFormat.format(endDate)
        }
        
        // Update time button state after populating fields
        updateTimeButtonState()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        selectedDueDate?.let { calendar.time = it }
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0)
                selectedCalendar.set(Calendar.MILLISECOND, 0)
                
                // 检查选择的日期是否早于当前日期
                val currentDate = Calendar.getInstance()
                currentDate.set(Calendar.HOUR_OF_DAY, 0)
                currentDate.set(Calendar.MINUTE, 0)
                currentDate.set(Calendar.SECOND, 0)
                currentDate.set(Calendar.MILLISECOND, 0)
                
                if (selectedCalendar.before(currentDate)) {
                    Toast.makeText(this, "截止日期不能早于今天", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }
                
                selectedDueDate = selectedCalendar.time
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.btnDueDate.text = dateFormat.format(selectedCalendar.time)
                
                // Update time button state when date is selected
                updateTimeButtonState()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // 设置最小日期为今天
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateTimeButtonState() {
        if (selectedDueDate != null) {
            binding.btnDueTime.isEnabled = true
            binding.btnDueTime.alpha = 1.0f
        } else {
            binding.btnDueTime.isEnabled = false
            binding.btnDueTime.alpha = 0.5f
        }
    }

    private fun showDueTimePicker() {
        val calendar = Calendar.getInstance()
        selectedDueTime?.let { timeString ->
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            try {
                val time = timeFormat.parse(timeString)
                time?.let { calendar.time = it }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // 检查选择的时间是否早于当前时间（如果选择的是今天）
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                val currentTime = Calendar.getInstance()
                val currentTimeString = String.format("%02d:%02d", 
                    currentTime.get(Calendar.HOUR_OF_DAY), 
                    currentTime.get(Calendar.MINUTE))
                
                // 如果选择的是今天，检查时间不能早于当前时间
                if (selectedDueDate != null) {
                    val selectedDate = Calendar.getInstance()
                    selectedDate.time = selectedDueDate!!
                    val today = Calendar.getInstance()
                    
                    // 如果选择的是今天
                    if (selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                        
                        if (selectedTime <= currentTimeString) {
                            Toast.makeText(this, "截止时间不能早于当前时间", Toast.LENGTH_SHORT).show()
                            return@TimePickerDialog
                        }
                    }
                }
                
                selectedDueTime = selectedTime
                binding.btnDueTime.text = selectedDueTime
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        selectedDailyTime?.let { timeString ->
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            try {
                val time = timeFormat.parse(timeString)
                time?.let { calendar.time = it }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // 如果截止日期是今天，验证提醒时间不能早于当前时间
                if (selectedDailyEndDate != null) {
                    val today = Calendar.getInstance()
                    val endDate = Calendar.getInstance()
                    endDate.time = selectedDailyEndDate!!
                    
                    // 检查是否是今天
                    val isToday = today.get(Calendar.YEAR) == endDate.get(Calendar.YEAR) &&
                                  today.get(Calendar.DAY_OF_YEAR) == endDate.get(Calendar.DAY_OF_YEAR)
                    
                    if (isToday) {
                        val currentTime = Calendar.getInstance()
                        val selectedTime = Calendar.getInstance()
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        selectedTime.set(Calendar.MINUTE, minute)
                        selectedTime.set(Calendar.SECOND, 0)
                        selectedTime.set(Calendar.MILLISECOND, 0)
                        
                        if (selectedTime.before(currentTime)) {
                            Toast.makeText(this, "如果截止日期是今天，提醒时间不能早于当前时间", Toast.LENGTH_SHORT).show()
                            return@TimePickerDialog
                        }
                    }
                }
                
                selectedDailyTime = String.format("%02d:%02d", hourOfDay, minute)
                binding.btnDailyTime.text = selectedDailyTime
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24-hour format
        )
        timePickerDialog.show()
    }

    private fun showDailyEndDatePicker() {
        val calendar = Calendar.getInstance()
        selectedDailyEndDate?.let { calendar.time = it }
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0)
                selectedCalendar.set(Calendar.MILLISECOND, 0)
                
                // 验证选择的日期不能早于今天
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)
                
                if (selectedCalendar.before(today)) {
                    Toast.makeText(this, "每日重复的截止日期不能早于今天", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }
                
                selectedDailyEndDate = selectedCalendar.time
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.btnDailyEndDate.text = dateFormat.format(selectedCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // 设置最小日期为今天
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun saveTodo() {
        // 检查当前TODO是否已完成或每日TODO已过期，如果满足条件则不允许保存
        val currentTodo = viewModel.todo.value
        if (currentTodo != null && (isTodoCompleted(currentTodo) || isDailyTodoExpired(currentTodo))) {
            Toast.makeText(this, "已完成或已过期的任务无法编辑", Toast.LENGTH_SHORT).show()
            return
        }
        
        val title = binding.etTitle.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 验证截止时间不能早于当前时间
        if (selectedDueDate != null && selectedDueTime != null) {
            val selectedDateTime = Calendar.getInstance()
            selectedDateTime.time = selectedDueDate!!
            val timeParts = selectedDueTime!!.split(":")
            selectedDateTime.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            selectedDateTime.set(Calendar.MINUTE, timeParts[1].toInt())
            selectedDateTime.set(Calendar.SECOND, 0)
            selectedDateTime.set(Calendar.MILLISECOND, 0)
            
            val currentDateTime = Calendar.getInstance()
            
            if (selectedDateTime.before(currentDateTime)) {
                Toast.makeText(this, "截止时间不能早于当前时间", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        // 验证每日重复的截止日期不能早于今天
        if (isDaily && selectedDailyEndDate != null) {
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)
            
            val endDate = Calendar.getInstance()
            endDate.time = selectedDailyEndDate!!
            endDate.set(Calendar.HOUR_OF_DAY, 0)
            endDate.set(Calendar.MINUTE, 0)
            endDate.set(Calendar.SECOND, 0)
            endDate.set(Calendar.MILLISECOND, 0)
            
            if (endDate.before(today)) {
                Toast.makeText(this, "每日重复的截止日期不能早于今天", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        // 验证每日重复的提醒时间：如果截止日期是今天，提醒时间不能早于当前时间
        if (isDaily && selectedDailyEndDate != null && selectedDailyTime != null) {
            val today = Calendar.getInstance()
            val endDate = Calendar.getInstance()
            endDate.time = selectedDailyEndDate!!
            
            // 检查是否是今天
            val isToday = today.get(Calendar.YEAR) == endDate.get(Calendar.YEAR) &&
                          today.get(Calendar.DAY_OF_YEAR) == endDate.get(Calendar.DAY_OF_YEAR)
            
            if (isToday) {
                val currentTime = Calendar.getInstance()
                val timeParts = selectedDailyTime!!.split(":")
                val reminderTime = Calendar.getInstance()
                reminderTime.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                reminderTime.set(Calendar.MINUTE, timeParts[1].toInt())
                reminderTime.set(Calendar.SECOND, 0)
                reminderTime.set(Calendar.MILLISECOND, 0)
                
                if (reminderTime.before(currentTime)) {
                    Toast.makeText(this, "如果截止日期是今天，提醒时间不能早于当前时间", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
        
        viewModel.saveTodo(title, selectedPriority, selectedDueDate, selectedDueTime, isDaily, selectedDailyTime, selectedDailyEndDate)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
