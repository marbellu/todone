package com.example.taskbookwithfire.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.taskbookwithfire.R
import com.example.taskbookwithfire.data.Task
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class TaskAdapter(
    options: FirestoreRecyclerOptions<Task>,
    private val lifecycleScope: LifecycleCoroutineScope
) : FirestoreRecyclerAdapter<Task, TaskViewHolder>(options) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return snapshots.getSnapshot(position).id.hashCode().toLong()
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int, model: Task) {
        val task = getItem(position)
        holder.bind(task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.items_layout, parent, false)
        return TaskViewHolder(view, lifecycleScope)
    }

    override fun onViewRecycled(holder: TaskViewHolder) {
        super.onViewRecycled(holder)
        holder.cleanup()
    }
}