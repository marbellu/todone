package com.example.taskbookwithfire.ui.screens

import com.example.taskbookwithfire.ui.adapter.TaskAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskbookwithfire.R
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.taskbookwithfire.data.Task

class MainActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private var currentSelectedTag: String = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser?.uid

        val query: Query = FirebaseFirestore.getInstance()
            .collection("tasks")
            .whereEqualTo("userId", currentUser)

        adapterHelper(query)

        // sort button logic
        val sortButton = findViewById<ImageButton>(R.id.sort_button)

        sortButton.setOnClickListener{
            val popupMenu = PopupMenu(this@MainActivity, sortButton)
            popupMenu.menuInflater.inflate(R.menu.popup_sort_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Sort by due date" -> {
                        filterAndSort(currentSelectedTag, "date")
                        true
                    }
                    "Show completed tasks" -> {
                        filterAndSort(currentSelectedTag, "completed")
                        true
                    }

                    "Show all" -> {
                        filterAndSort(currentSelectedTag, "all")
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // sort for tags
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("tasks")
                .whereEqualTo("userId", currentUser)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("Tags", "Error getting tags", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val tags = snapshot.mapNotNull { it.getString("tag") }.distinct().toMutableList()
                        tags.add(0, "all")

                        val spinner = findViewById<Spinner>(R.id.shown)
                        val currentSelection = spinner.selectedItemPosition.takeIf { it >= 0 } ?: 0

                        val adapter = ArrayAdapter(
                            this,
                            android.R.layout.simple_spinner_item,
                            tags
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }

                        spinner.adapter = adapter
                        spinner.setSelection(minOf(currentSelection, tags.size - 1))

                        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                val selectedTag = parent.getItemAtPosition(position) as String
                                currentSelectedTag = selectedTag
                                filterAndSort(currentSelectedTag, null)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                                currentSelectedTag = "all"
                                filterAndSort("all", null)
                            }
                        }
                    }
                }
        }
        // fab
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, NewTaskActivity::class.java)
            startActivity(intent)
        }

        //main popupmenu
        val menu = findViewById<ImageButton>(R.id.menu_icon)
        menu.setOnClickListener {
            val popMenu = PopupMenu(this@MainActivity, menu)
            popMenu.menuInflater.inflate(R.menu.popup_menu, popMenu.menu)

            popMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Sign out" -> {
                        auth.signOut()
                        Toast.makeText(this,
                            "You have been logged out",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popMenu.show()
        }
    }

    private fun filterAndSort(tag: String, sortType: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid

        var query: Query = FirebaseFirestore.getInstance()
            .collection("tasks")
            .whereEqualTo("userId", currentUser)

        if (tag != "all") {
            query = query.whereEqualTo("tag", tag)
        }

        when (sortType) {
            "date" -> query = query.orderBy("date")
            "completed" -> query = query.whereEqualTo("done", true)
            "all" -> query
        }

        adapterHelper(query)
    }

    private fun adapterHelper(query: Query) {
        val options = FirestoreRecyclerOptions.Builder<Task>()
            .setQuery(query, Task::class.java)
            .build()

        if (::taskAdapter.isInitialized) {
            taskAdapter.updateOptions(options)
        } else {
            taskAdapter = TaskAdapter(options, lifecycleScope)
            recyclerView.adapter = taskAdapter
        }
        taskAdapter.startListening()
    }


    override fun onStart(){
        super.onStart()
        if (::taskAdapter.isInitialized) {
            taskAdapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::taskAdapter.isInitialized) {
            taskAdapter.stopListening()
        }
    }

}