package com.example.taskbookwithfire.ui.screens

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskbookwithfire.R
import com.example.taskbookwithfire.data.Task
import com.example.taskbookwithfire.data.TaskRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

class NewTaskActivity: AppCompatActivity() {
    private lateinit var editTaskView: EditText
    private lateinit var editDetailView: EditText
    private lateinit var selectedDate: Button
    private lateinit var selectedLocalDate: LocalDate
    private lateinit var tagAutoComplete: AutoCompleteTextView
    private var userTags = mutableListOf<String>()

    private val taskRepository = TaskRepository()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        val menu = findViewById<ImageButton>(R.id.menu_icon)
        menu.setOnClickListener {
            finish()
        }

        editTaskView = findViewById(R.id.add_title)
        editDetailView = findViewById(R.id.add_description)
        selectedDate = findViewById(R.id.date)

        tagAutoComplete = findViewById(R.id.tag_autocomplete)

        val tagAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            userTags
        )
        tagAutoComplete.setAdapter(tagAdapter)

        var currentUser = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUser != null) {
            Firebase.firestore
                .collection("tasks")
                .whereEqualTo("userId", currentUser)
                .get()
                .addOnSuccessListener { documents ->
                    val tags = documents.mapNotNull { it.getString("tag") }.distinct()
                    userTags.clear()
                    userTags.addAll(tags)
                    tagAdapter.notifyDataSetChanged()
                }
        }

        // date conversion
        selectedDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = android.app.DatePickerDialog(
                this, R.style.date_picker_style, { _, yearInt, monthInt, dayInt ->
                    selectedLocalDate = LocalDate.of(yearInt, monthInt + 1, dayInt)
                    val formattedDate =
                        String.format(Locale.ROOT, "%02d.%02d.%04d", dayInt, monthInt + 1, yearInt)
                    selectedDate.text = formattedDate
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            if (!::selectedLocalDate.isInitialized) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val title = editTaskView.text.toString()
            val description = editDetailView.text.toString()
            val done = false
            val date = selectedLocalDate.toString()
            val tag = tagAutoComplete.text.toString()

            currentUser = FirebaseAuth.getInstance().currentUser?.uid

            val replyIntent = Intent()

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date)) {
                setResult(RESULT_CANCELED, replyIntent)
            } else {
                lifecycleScope.launch {
                    val newTask = currentUser?.let { it1 ->
                        Task(
                            userId = it1,
                            title = title,
                            description = description,
                            done = done,
                            date = date,
                            tag = tag
                        )
                    }
                    if (newTask != null) {
                        taskRepository.addTask(newTask)
                    }
                    replyIntent.putExtra(EXTRA_REPLY, "$title;$description;$done;$date;$tag")
                }
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_REPLY = "com.example.taskbookwithfire.REPLY"
    }
}