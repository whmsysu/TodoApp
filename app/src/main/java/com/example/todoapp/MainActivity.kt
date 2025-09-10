package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapter.TodoAdapter
import com.example.todoapp.data.Todo
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.viewmodel.TodoFilter
import com.example.todoapp.viewmodel.TodoViewModel
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TodoViewModel by viewModels()
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupChips()
        setupFab()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            onTodoClick = { todo -> openEditTodo(todo) },
            onTodoCheck = { todo, isChecked -> viewModel.updateTodoStatus(todo.id, isChecked) },
            onTodoDelete = { todo -> viewModel.deleteTodo(todo) }
        )
        binding.recyclerView.apply {
            adapter = todoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupChips() {
        binding.chipAll.setOnClickListener { viewModel.setFilter(TodoFilter.ALL) }
        binding.chipPending.setOnClickListener { viewModel.setFilter(TodoFilter.PENDING) }
        binding.chipCompleted.setOnClickListener { viewModel.setFilter(TodoFilter.COMPLETED) }
        binding.chipDaily.setOnClickListener { viewModel.setFilter(TodoFilter.DAILY) }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, AddEditTodoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.getFilteredTodos().observe(this, Observer { todos ->
            todoAdapter.submitList(todos)
            binding.tvEmpty.visibility = if (todos.isEmpty()) View.VISIBLE else View.GONE
        })

        viewModel.currentFilter.observe(this, Observer { filter ->
            updateChipSelection(filter)
        })
    }

    private fun updateChipSelection(filter: TodoFilter) {
        binding.chipAll.isChecked = filter == TodoFilter.ALL
        binding.chipPending.isChecked = filter == TodoFilter.PENDING
        binding.chipCompleted.isChecked = filter == TodoFilter.COMPLETED
        binding.chipDaily.isChecked = filter == TodoFilter.DAILY
    }

    private fun openEditTodo(todo: Todo) {
        val intent = Intent(this, AddEditTodoActivity::class.java).apply {
            putExtra(AddEditTodoActivity.EXTRA_TODO_ID, todo.id)
        }
        startActivity(intent)
    }
}
