package com.example.taskbookwithfire.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object Converters {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun toLocalDate(dateString: String?): LocalDate? = try {
        dateString?.let { LocalDate.parse(it, formatter) }
    } catch (e: DateTimeParseException) {
        null
    }

    fun formatDate(date: LocalDate): String =
        date.format(formatter)
}