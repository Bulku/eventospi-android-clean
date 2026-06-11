package com.leonvelez.eventospi.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.Calendar

fun openDatePicker(
    context: android.content.Context,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val mm = (month + 1).toString().padStart(2, '0')
            val dd = dayOfMonth.toString().padStart(2, '0')
            onDateSelected("$year-$mm-$dd")
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun openTimePicker(
    context: android.content.Context,
    onTimeSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()

    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val hh = hourOfDay.toString().padStart(2, '0')
            val mm = minute.toString().padStart(2, '0')
            onTimeSelected("$hh:$mm:00")
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}

fun formatEventDateCompact(dateText: String): String {
    return try {
        val parsed = try {
            OffsetDateTime.parse(dateText).toLocalDateTime()
        } catch (_: Exception) {
            LocalDateTime.parse(dateText)
        }

        parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (_: Exception) {
        dateText
    }
}
