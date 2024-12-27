package com.example.taskbookwithfire.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Singleton
class TaskRepository {
    private val db = FirebaseFirestore.getInstance().collection("tasks")

    suspend fun addTask(task: Task): Result<String> = try {
        val documentId = db.document().id
        val taskMap = task.copy(id = documentId).toMap()
        db.document(documentId).set(taskMap).await()
        Result.success(documentId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteTask(task: Task): Result<String> = try {
        db.document(task.id).delete().await()
        Result.success(task.id)
    } catch (e: Exception) {
        Result.failure(e)
    }


    suspend fun updateTaskStatus(task: Task): Result<String> = try {
        db.document(task.id).update(mapOf("done" to !task.done)).await()
        Result.success(task.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateTask(task: Task): Result<String> = try {
        db.document(task.id)
            .update(task.toMap())
            .await()
        Result.success(task.id)
    } catch(e: Exception) {
        Result.failure(e)
    }
}