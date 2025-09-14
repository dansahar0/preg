package com.example.pregnancydiary.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pregnancydiary.data.model.DailyInfo
import com.example.pregnancydiary.data.model.Note
import com.example.pregnancydiary.data.model.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class, Reminder::class, DailyInfo::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun dailyInfoDao(): DailyInfoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pregnancy_diary_db"
                )
                .addCallback(AppDatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Seeding the database
                    val dailyInfoDao = database.dailyInfoDao()
                    val dailyInfoList = parseExcelFile(context)
                    dailyInfoDao.insertAll(dailyInfoList)
                }
            }
        }

        private fun parseExcelFile(context: Context): List<DailyInfo> {
            val dailyInfoList = mutableListOf<DailyInfo>()
            try {
                val inputStream = context.assets.open("Days.xlsx")
                val workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i)
                    if (row != null) {
                        val dayOfPregnancy = row.getCell(0)?.numericCellValue?.toInt()
                        val weekNumber = row.getCell(1)?.numericCellValue?.toInt()
                        val title = row.getCell(2)?.stringCellValue ?: ""
                        val description = row.getCell(3)?.stringCellValue ?: ""

                        if (dayOfPregnancy != null && weekNumber != null) {
                            dailyInfoList.add(
                                DailyInfo(
                                    dayOfPregnancy = dayOfPregnancy,
                                    weekNumber = weekNumber,
                                    title = title,
                                    description = description
                                )
                            )
                        }
                    }
                }
                workbook.close()
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dailyInfoList
        }
    }
}
