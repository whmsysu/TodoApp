package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.adapter.TodoAdapter
import com.example.todoapp.data.Todo
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.viewmodel.TodoFilter
import com.example.todoapp.viewmodel.TodoViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TodoViewModel by viewModels()
    private lateinit var todoAdapter: TodoAdapter
    private var deletedTodo: Todo? = null

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
            onTodoCheck = { todo, isChecked -> viewModel.updateTodoStatus(todo.id, isChecked) }
        )
        binding.recyclerView.apply {
            adapter = todoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        
        // Setup swipe to delete
        setupSwipeToDelete()
    }

    private fun setupChips() {
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
        viewModel.filteredTodos.observe(this, Observer { todos ->
            todoAdapter.submitList(todos)
            binding.tvEmpty.visibility = if (todos.isEmpty()) View.VISIBLE else View.GONE
        })

        viewModel.currentFilter.observe(this, Observer { filter ->
            updateChipSelection(filter)
        })
    }

    private fun updateChipSelection(filter: TodoFilter) {
        // Update checked state
        binding.chipPending.isChecked = filter == TodoFilter.PENDING
        binding.chipCompleted.isChecked = filter == TodoFilter.COMPLETED
        binding.chipDaily.isChecked = filter == TodoFilter.DAILY
        
        // Update chip styles based on selection
        updateChipStyle(binding.chipPending, filter == TodoFilter.PENDING)
        updateChipStyle(binding.chipCompleted, filter == TodoFilter.COMPLETED)
        updateChipStyle(binding.chipDaily, filter == TodoFilter.DAILY)
    }
    
    private fun updateChipStyle(chip: Chip, isSelected: Boolean) {
        if (isSelected) {
            chip.setChipBackgroundColorResource(R.color.purple_500)
            chip.setChipStrokeColorResource(R.color.purple_700)
            chip.chipStrokeWidth = 2f
            chip.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setChipStrokeColorResource(R.color.gray_light)
            chip.chipStrokeWidth = 1f
            chip.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }
    }


    private fun openEditTodo(todo: Todo) {
        val intent = Intent(this, AddEditTodoActivity::class.java).apply {
            putExtra(AddEditTodoActivity.EXTRA_TODO_ID, todo.id)
        }
        startActivity(intent)
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val todo = todoAdapter.currentList[position]
                    deletedTodo = todo
                    
                    // Delete the todo
                    viewModel.deleteTodo(todo)
                    
                    // Show undo snackbar
                    Snackbar.make(
                        binding.root,
                        "已删除: ${todo.title}",
                        Snackbar.LENGTH_LONG
                    ).setAction("撤销") {
                        deletedTodo?.let { todoToRestore ->
                            viewModel.insertTodo(todoToRestore)
                            deletedTodo = null
                        }
                    }.show()
                }
            }

            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = itemView.background
                
                if (dX > 0) { // Swiping to the right
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                } else if (dX < 0) { // Swiping to the left
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                } else {
                    background.setBounds(0, 0, 0, 0)
                }
                
                background.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
}
