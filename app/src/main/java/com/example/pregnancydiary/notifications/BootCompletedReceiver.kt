package com.example.pregnancydiary.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pregnancydiary.data.PregnancyRepository
import com.example.pregnancydiary.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // In a real app, use a DI framework to get these instances.
            // For simplicity, we create them directly here.
            val database = AppDatabase.getDatabase(context)
            val repository = PregnancyRepository(database.noteDao(), database.dailyInfoDao(), database.reminderDao())
            val scheduler = AlarmSchedulerImpl(context)

            CoroutineScope(Dispatchers.IO).launch {
                val reminders = repository.getAllReminders().first()
                for (reminder in reminders) {
                    scheduler.schedule(reminder)
                }
            }
        }
    }
}
