package com.example.taskbookwithfire.data

data class Task (
    val userId: String = "",
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val done: Boolean = false,
    val date: String = "",
    val tag: String = ""
) {
    fun toMap() = hashMapOf(
        "userId" to userId,
        "id" to id,
        "title" to title,
        "description" to description,
        "done" to done,
        "tag" to tag,
        "date" to date
    )
}