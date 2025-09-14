package com.example.pregnancydiary.data.local

import android.content.Context
import com.example.pregnancydiary.data.model.DailyInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

class DatabaseSeeder(
    private val context: Context,
    private val db: AppDatabase
) {
    suspend fun seed() {
        withContext(Dispatchers.IO) {
            try {
                val dailyInfoList = mutableListOf<DailyInfo>()
                val inputStream: InputStream = context.assets.open("Days.xlsx")
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                // Skip header row, start from the second row
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i)
                    if (row != null) {
                        // Assuming format: dayOfPregnancy (A), weekNumber (B), title (C), description (D)
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

                // Insert all at once
                if (dailyInfoList.isNotEmpty()) {
                    db.dailyInfoDao().insertAll(dailyInfoList)
                }
            } catch (e: Exception) {
                // Handle exceptions, e.g., file not found or parsing error
                e.printStackTrace()
            }
        }
    }
}
