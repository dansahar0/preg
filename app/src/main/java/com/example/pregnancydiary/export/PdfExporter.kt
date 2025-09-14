package com.example.pregnancydiary.export

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import com.example.pregnancydiary.data.model.DailyInfo
import com.example.pregnancydiary.data.model.Note
import java.io.File
import java.io.FileOutputStream

class PdfExporter {

    fun export(
        destination: File,
        weekNumber: Int,
        dailyInfo: List<DailyInfo>,
        notes: List<Note>
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 20f
            isFakeBoldText = true
        }
        val sectionPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 16f
            isFakeBoldText = true
        }
        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 12f
        }

        var yPos = 40f
        val xPos = 20f
        val contentWidth = pageInfo.pageWidth - (2 * xPos).toInt()

        // --- Document Title ---
        canvas.drawText("Pregnancy Diary - Week $weekNumber", xPos, yPos, titlePaint)
        yPos += 40

        // --- Daily Developments Section ---
        canvas.drawText("Daily Developments", xPos, yPos, sectionPaint)
        yPos += 25
        for (info in dailyInfo) {
            val infoTitleLayout = StaticLayout.Builder.obtain("${info.title}:", 0, info.title.length + 1, bodyPaint, contentWidth).build()
            infoTitleLayout.draw(canvas, xPos, yPos)
            yPos += infoTitleLayout.height
            val infoDescLayout = StaticLayout.Builder.obtain(info.description, 0, info.description.length, bodyPaint, contentWidth).build()
            infoDescLayout.draw(canvas, xPos, yPos)
            yPos += infoDescLayout.height + 10
        }

        yPos += 20

        // --- Notes Section ---
        canvas.drawText("My Notes", xPos, yPos, sectionPaint)
        yPos += 25
        for (note in notes) {
            val noteLayout = StaticLayout.Builder.obtain("- ${note.text}", 0, note.text.length + 2, bodyPaint, contentWidth).build()
            noteLayout.draw(canvas, xPos, yPos)
            yPos += noteLayout.height + 5
        }

        document.finishPage(page)

        try {
            val fos = FileOutputStream(destination)
            document.writeTo(fos)
            document.close()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun StaticLayout.draw(canvas: android.graphics.Canvas, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        this.draw(canvas)
        canvas.restore()
    }
}
