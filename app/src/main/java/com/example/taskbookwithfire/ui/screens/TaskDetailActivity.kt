package com.example.taskbookwithfire.ui.screens

import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskbookwithfire.util.Converters
import com.example.taskbookwithfire.R
import com.example.taskbookwithfire.data.Task
import com.example.taskbookwithfire.data.TaskRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var titleSwitcher: ViewSwitcher
    private lateinit var tagSwitcher: ViewSwitcher
    private lateinit var detailsSwitcher: ViewSwitcher
    private lateinit var dateSwitcher: ViewSwitcher
    private lateinit var titleEdit: EditText
    private lateinit var detailsEdit: EditText
    private lateinit var dateButton: Button
    private lateinit var tagAutoComplete: AutoCompleteTextView
    private lateinit var isDone: Button
    private var selectedLocalDate: LocalDate? = null
    private var isEditMode = false
    private var isTaskDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        initializeSwitchers()
        loadInitialData()
        setupDatePicker()
        setupEditButton()


        val menu = findViewById<ImageButton>(R.id.menu_icon)
        menu.setOnClickListener {
            finish()
        }

        isDone.setOnClickListener {
            isTaskDone = !isTaskDone
            updateDoneButtonText()
            updateDoneStatusOnly()
        }
    }

    private fun initializeSwitchers() {
        titleSwitcher = findViewById(R.id.title_switcher)
        tagSwitcher = findViewById(R.id.tag_switcher)
        detailsSwitcher = findViewById(R.id.details_switcher)
        dateSwitcher = findViewById(R.id.date_switcher)

        titleEdit = findViewById(R.id.detail_title_edit)
        detailsEdit = findViewById(R.id.detail_details_edit)
        dateButton = findViewById(R.id.date_edit)
        tagAutoComplete = findViewById(R.id.tag_autocomplete)
        isDone = findViewById(R.id.done_button)
    }

    private fun loadInitialData() {
        val taskId = intent.getStringExtra("TODO_ID") ?: return

        Firebase.firestore.collection("tasks").document(taskId).get()
            .addOnSuccessListener { doc ->
                val task = doc.toObject(Task::class.java) ?: return@addOnSuccessListener

                titleEdit.setText(task.title)
                detailsEdit.setText(task.description)
                tagAutoComplete.setText(task.tag)
                selectedLocalDate = Converters.toLocalDate(task.date)
                findViewById<TextView>(R.id.detail_title).text = task.title
                findViewById<TextView>(R.id.detail_details).text = task.description
                findViewById<TextView>(R.id.tag_text).text = task.tag

                val formattedDate = Converters.toLocalDate(task.date)?.let {
                    "${it.month.toString().substring(0,3).lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.dayOfMonth}, ${it.year}"
                }
                findViewById<TextView>(R.id.date_value).text = formattedDate
                dateButton.text = formattedDate


                isTaskDone = task.done
                updateDoneButtonText()
            }
    }


    private fun updateDoneButtonText() {
        isDone.text = if (isTaskDone) "Mark as undone" else "Mark as done"
    }

    private fun updateDoneStatusOnly() {
        val taskId = intent.getStringExtra("TODO_ID") ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        lifecycleScope.launch {
            val title = findViewById<TextView>(R.id.detail_title).text.toString()
            val details = findViewById<TextView>(R.id.detail_details).text.toString()
            val tag = findViewById<TextView>(R.id.tag_text).text.toString()
            val date = selectedLocalDate?.toString() ?: intent.getStringExtra("TODO_DATE") ?: ""

            val updatedTask = Task(
                id = taskId,
                userId = userId,
                title = title,
                description = details,
                done = isTaskDone,
                date = date,
                tag = tag
            )

            TaskRepository().updateTask(updatedTask)

            Toast.makeText(
                this@TaskDetailActivity,
                if (isTaskDone) "Task is done" else "Task is not done",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupDatePicker() {
        dateButton.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val datePickerDialog = android.app.DatePickerDialog(
                this, R.style.date_picker_style, { _, yearInt, monthInt, dayInt ->
                    selectedLocalDate = LocalDate.of(yearInt, monthInt + 1, dayInt)
                    val formattedDate =
                        String.format(Locale.ROOT, "%02d.%02d.%04d", dayInt, monthInt + 1, yearInt)
                    dateButton.text = formattedDate
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    private fun setupEditButton() {
        val editButton = findViewById<Button>(R.id.edit_button)
        editButton.setOnClickListener {
            isEditMode = !isEditMode
            if (isEditMode) {
                switchToEditMode()
            } else {
                saveChanges()
                switchToViewMode()
            }
            editButton.text = if (isEditMode) "Save" else "Edit"
        }
    }

    private fun switchToEditMode() {
        titleSwitcher.showNext()
        tagSwitcher.showNext()
        detailsSwitcher.showNext()
        dateSwitcher.showNext()

        val detailTitle = findViewById<TextView>(R.id.detail_title)
        val detailDetails = findViewById<TextView>(R.id.detail_details)
        val tagText = findViewById<TextView>(R.id.tag_text)
        val dateValue = findViewById<TextView>(R.id.date_value)

        titleEdit.setText(detailTitle.text)
        detailsEdit.setText(detailDetails.text)
        tagAutoComplete.setText(tagText.text)
        dateButton.text = dateValue.text
    }

    private fun switchToViewMode() {
        titleSwitcher.showPrevious()
        tagSwitcher.showPrevious()
        detailsSwitcher.showPrevious()
        dateSwitcher.showPrevious()
    }

    private fun saveChanges() {
        if (selectedLocalDate == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val detailTitle = findViewById<TextView>(R.id.detail_title)
        val detailDetails = findViewById<TextView>(R.id.detail_details)
        val tagText = findViewById<TextView>(R.id.tag_text)
        val dateValue = findViewById<TextView>(R.id.date_value)

        lifecycleScope.launch {
            val task = FirebaseAuth.getInstance().currentUser?.uid?.let {
                Task(
                    userId = it,
                    id = intent.getStringExtra("TODO_ID") ?: "",
                    title = titleEdit.text.toString(),
                    description = detailsEdit.text.toString(),
                    done = isTaskDone,
                    date = selectedLocalDate.toString(),
                    tag = tagAutoComplete.text.toString()
                )
            }

            task?.let {
                TaskRepository().updateTask(it)

                detailTitle.text = it.title
                detailDetails.text = it.description
                tagText.text = it.tag
                dateValue.text = dateButton.text
            }
        }
    }

}