package com.example.todoapp

import android.app.DatePickerDialog
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
import java.util.*

class AddEditTodoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditTodoBinding
    private val viewModel: AddEditTodoViewModel by viewModels()
    private var selectedPriority: Priority = Priority.MEDIUM
    private var selectedDueDate: Date? = null

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
        setupSaveButton()
        observeViewModel()

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
        binding.chipGroupPriority.setOnCheckedStateChangeListener { _, _ ->
            when {
                binding.chipHigh.isChecked -> selectedPriority = Priority.HIGH
                binding.chipMedium.isChecked -> selectedPriority = Priority.MEDIUM
                binding.chipLow.isChecked -> selectedPriority = Priority.LOW
            }
        }
        // Set default selection
        binding.chipMedium.isChecked = true
    }

    private fun setupDueDateButton() {
        binding.btnDueDate.setOnClickListener {
            showDatePicker()
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
        binding.etDescription.setText(todo.description)
        
        when (todo.priority) {
            Priority.HIGH -> binding.chipHigh.isChecked = true
            Priority.MEDIUM -> binding.chipMedium.isChecked = true
            Priority.LOW -> binding.chipLow.isChecked = true
        }
        
        todo.dueDate?.let {
            selectedDueDate = it
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.btnDueDate.text = dateFormat.format(it)
        }
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
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveTodo() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveTodo(title, description, selectedPriority, selectedDueDate)
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
