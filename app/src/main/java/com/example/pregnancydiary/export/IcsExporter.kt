package com.example.pregnancydiary.export

import com.example.pregnancydiary.data.model.Reminder
import biweekly.Biweekly
import biweekly.ICalendar
import biweekly.component.VEvent
import java.util.Date

class IcsExporter {
    fun export(reminders: List<Reminder>): String {
        val ical = ICalendar()
        for (reminder in reminders) {
            val event = VEvent()
            event.setSummary(reminder.title)
            event.setDateStart(Date(reminder.dueDate), true)
            // You might want to add an end date as well, e.g., one hour after the start
            event.setDateEnd(Date(reminder.dueDate + 3600 * 1000), true)
            ical.addEvent(event)
        }
        return Biweekly.write(ical).go()
    }
}
