package com.example.taskbookwithfire.ui.adapter

import android.content.Intent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.example.taskbookwithfire.R
import com.example.taskbookwithfire.data.Task
import com.example.taskbookwithfire.data.TaskRepository
import com.example.taskbookwithfire.ui.screens.TaskDetailActivity
import com.example.taskbookwithfire.util.Converters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewHolder(
    itemView: View,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val taskRepository: TaskRepository = TaskRepository()
) : RecyclerView.ViewHolder(itemView) {
    private val titleView: TextView = itemView.findViewById(R.id.title_textview)
    private val dateView: TextView = itemView.findViewById(R.id.date_view)
    private val tagView: TextView = itemView.findViewById(R.id.tag_text)
    private val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
    private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)

    private var updateJob: Job? = null

    fun bind(task: Task) {
        cleanup()

        titleView.text = task.title
        dateView.text = Converters.toLocalDate(task.date)?.let { date ->
            "${date.dayOfMonth}.${date.monthValue}.${date.year}"
        } ?: ""
        tagView.text = task.tag

        checkbox.setOnCheckedChangeListener(null)
        checkbox.isChecked = task.done

        checkbox.setOnCheckedChangeListener { _, _ ->
            updateJob?.cancel()
            updateJob = lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    taskRepository.updateTaskStatus(task)
                }
            }
        }


        deleteButton.setOnClickListener {
            showDeleteDialog(task)
        }

        titleView.setOnClickListener {
            navigateToDetail(task)
        }

        titleView.typeface = ResourcesCompat.getFont(itemView.context, R.font.inter_semibold)
    }

    fun cleanup() {
        updateJob?.cancel()
        checkbox.setOnCheckedChangeListener(null)
    }

    private fun showDeleteDialog(task: Task) {
        AlertDialog.Builder(itemView.context, R.style.LightDialogTheme)
            .setTitle("Delete following task: ${task.title}?")
            .setMessage("Are you sure you want to delete the following task: ${task.title}?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    taskRepository.deleteTask(task)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun navigateToDetail(task: Task) {
        val intent = Intent(itemView.context, TaskDetailActivity::class.java).apply {
            putExtra("TODO_TITLE", task.title)
            putExtra("TODO_DETAILS", task.description)
            putExtra("TODO_DATE", task.date)
            putExtra("TODO_IS_DONE", task.done)
            putExtra("TODO_TAG", task.tag)
            putExtra("TODO_ID", task.id)
        }
        itemView.context.startActivity(intent)
    }
}