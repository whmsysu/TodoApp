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
                selectedCalendar.set(year, month, dayOfMonth)
                selectedDueDate = selectedCalendar.time
                
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.btnDueDate.text = dateFormat.format(selectedCalendar.time)
                
                // Update time button state when date is selected
                updateTimeButtonState()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
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
                selectedDueTime = String.format("%02d:%02d", hourOfDay, minute)
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
                selectedCalendar.set(year, month, dayOfMonth)
                selectedDailyEndDate = selectedCalendar.time
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.btnDailyEndDate.text = dateFormat.format(selectedCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveTodo() {
        val title = binding.etTitle.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
            return
        }


        viewModel.saveTodo(title, "", selectedPriority, selectedDueDate, selectedDueTime, isDaily, selectedDailyTime, selectedDailyEndDate)
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
