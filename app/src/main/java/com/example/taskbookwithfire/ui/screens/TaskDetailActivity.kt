package com.example.taskbookwithfire.ui.screens

import android.os.Bundle
import android.widget.ArrayAdapter
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
    private var selectedLocalDate: LocalDate? = null
    private var isEditMode = false

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
    }

    private fun loadInitialData() {
        val toDo = intent.getStringExtra("TODO_TITLE")
        val details = intent.getStringExtra("TODO_DETAILS")
        val dateString = intent.getStringExtra("TODO_DATE")
        val tag = intent.getStringExtra("TODO_TAG")

        val tagAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )
        tagAutoComplete.setAdapter(tagAdapter)


        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            Firebase.firestore
                .collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val tags = documents.mapNotNull { it.getString("tag") }.distinct()
                    tagAdapter.clear()
                    tagAdapter.addAll(tags)
                    tagAdapter.notifyDataSetChanged()
                }
        }

        titleEdit.setText(toDo)
        detailsEdit.setText(details)
        tagAutoComplete.setText(tag)

        findViewById<TextView>(R.id.detail_title).text = toDo
        findViewById<TextView>(R.id.detail_details).text = details
        findViewById<TextView>(R.id.tag_text).text = tag

        val converters = Converters
        val date = converters.toLocalDate(dateString)
        selectedLocalDate = date
        val dueDate = date?.let {
            "${it.month.toString().substring(0, 3).lowercase().replaceFirstChar { it.uppercase() }} ${it.dayOfMonth}, ${it.year}"
        }
        dateButton.text = dueDate
        findViewById<TextView>(R.id.date_value).text = dueDate
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
        val isDoneView = findViewById<TextView>(R.id.detail_is_done_view)

        lifecycleScope.launch {
            val task = FirebaseAuth.getInstance().currentUser?.uid?.let {
                Task(
                    userId = it,
                    id = intent.getStringExtra("TODO_ID") ?: "",
                    title = titleEdit.text.toString(),
                    description = detailsEdit.text.toString(),
                    done = isDoneView.text == "Done",
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