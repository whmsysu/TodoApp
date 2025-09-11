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
import com.example.todoapp.error.ErrorHandler
import com.example.todoapp.viewmodel.TodoFilter
import com.example.todoapp.viewmodel.TodoViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
            setHasFixedSize(true) // 优化性能，当item大小固定时
            setItemViewCacheSize(20) // 增加视图缓存
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
            updateEmptyState(todos.isEmpty())
        })

        viewModel.currentFilter.observe(this, Observer { filter ->
            updateChipSelection(filter)
        })
        
        // Observe error messages
        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                showErrorSnackbar(it)
                viewModel.clearErrorMessage()
            }
        })
        
        // Observe detailed error info
        viewModel.errorInfo.observe(this, Observer { errorInfo ->
            errorInfo?.let {
                showDetailedErrorSnackbar(it)
                viewModel.clearErrorMessage()
            }
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
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            
            // Update empty state based on current filter
            val currentFilter = viewModel.currentFilter.value ?: TodoFilter.PENDING
            when (currentFilter) {
                TodoFilter.PENDING -> {
                    binding.tvEmptyTitle.text = "暂无待办任务"
                    binding.tvEmptySubtitle.text = "点击右下角的 + 按钮添加新任务"
                }
                TodoFilter.COMPLETED -> {
                    binding.tvEmptyTitle.text = "暂无已完成任务"
                    binding.tvEmptySubtitle.text = "完成一些任务后，它们会显示在这里"
                }
                TodoFilter.DAILY -> {
                    binding.tvEmptyTitle.text = "暂无每日任务"
                    binding.tvEmptySubtitle.text = "创建每日重复任务来建立好习惯"
                }
            }
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
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
    
    /**
     * 显示简单错误信息
     */
    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.ok)) {
                // 点击确定按钮的处理
            }
            .show()
    }
    
    /**
     * 显示详细错误信息，包含重试选项
     */
    private fun showDetailedErrorSnackbar(errorInfo: ErrorHandler.ErrorInfo) {
        val snackbar = Snackbar.make(binding.root, errorInfo.userMessage, Snackbar.LENGTH_LONG)
        
        // 如果错误支持重试，显示重试按钮
        if (errorInfo.shouldRetry) {
            snackbar.setAction(getString(R.string.retry)) {
                viewModel.retryLastOperation()
            }
        } else {
            snackbar.setAction(getString(R.string.ok)) {
                // 点击确定按钮的处理
            }
        }
        
        snackbar.show()
    }
}
