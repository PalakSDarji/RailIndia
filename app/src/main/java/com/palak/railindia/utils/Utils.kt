package com.palak.railindia.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import com.palak.railindia.model.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.sql.Date
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class Utils {

    companion object {

        fun convertFirebaseEntryToEntry(
            firebaseEntry: FirebaseEntry,
            listOfFirebaseComponentEntry: MutableList<FirebaseComponentEntry>,
            dateFormat: SimpleDateFormat
        ): Entry {

            val entry = Entry()
            entry.id = UUID.randomUUID().toString()
            val date = Date(dateFormat.parse(firebaseEntry.date).time)
            entry.date = date
            entry.month = firebaseEntry.month
            entry.qty = firebaseEntry.totalQty
            entry.synced = true

            var listOfComponentEntry = mutableListOf<ComponentEntry>()

            listOfFirebaseComponentEntry.forEach {
                val componentEntry = convertFirebaseComponentEntryToComponentEntry(it)
                componentEntry.entryId = entry.id
                listOfComponentEntry.add(componentEntry)
            }
            entry.componentEntry = listOfComponentEntry

            return entry
        }

        private fun convertFirebaseComponentEntryToComponentEntry(
            firebaseComponentEntry: FirebaseComponentEntry
        ): ComponentEntry {

            val componentEntry = ComponentEntry()
            componentEntry.id = firebaseComponentEntry.id
            componentEntry.pass = firebaseComponentEntry.pass
            componentEntry.fail = firebaseComponentEntry.fail
            componentEntry.componentId = firebaseComponentEntry.componentId

            return componentEntry
        }

        fun createSpreadSheet(
            context: Context,
            month: String,
            dateMonthSdf: SimpleDateFormat,
            timeStampSdf: SimpleDateFormat,
            entryList: List<Entry>,
            isUnsupportedDevice: Boolean,
            listOfComponent: List<Component>
        ) {

            val mFormat = DecimalFormat("00")

            println("entryList final $entryList")

            val workbook = if (isUnsupportedDevice) {
                HSSFWorkbook()
            } else {
                XSSFWorkbook()
            }

            val sheet = workbook.createSheet(month)
            var row = sheet.createRow(0)
            row.createCell(0)
            row.createCell(1)
            row.createCell(2).also {
                it.setCellValue("Date")
            }

            var iForDate = 3
            entryList.forEach {
                val dateInDigit = /*mFormat.format(*/dateMonthSdf.format(it.date!!)/*.toDouble())*/
                sheet.addMergedRegion(CellRangeAddress(0, 0, iForDate, iForDate + 1))
                row.createCell(iForDate).also {
                    it.setCellValue(dateInDigit)
                    iForDate++
                    iForDate++
                }
            }

            row = sheet.createRow(1)
            row.createCell(0)
            row.createCell(1).also {
                it.setCellValue("Name of Componenets")
            }

            row.createCell(2).also {
                it.setCellValue("PL No")
            }

            var iForPassFailStatic = 3

            entryList.forEach {

                row.createCell(iForPassFailStatic).also {
                    it.setCellValue("Pass")
                }

                iForPassFailStatic++
                row.createCell(iForPassFailStatic).also {
                    it.setCellValue("Fail")
                }

                iForPassFailStatic++
            }

            var componentIndex = 2

            listOfComponent.forEachIndexed { index, component ->

                row = sheet.createRow(index + componentIndex)
                row.createCell(0).also {
                    it.setCellValue((index + 1).toString())
                }

                row.createCell(1).also {
                    it.setCellValue(component.name + " (" + component.qty + ")")
                }
                row.createCell(2).also {
                    it.setCellValue(component.id.toString())
                }

                var iForPassFail = 3
                entryList.forEach { entry ->

                    val componentEntry = entry.componentEntry?.first {
                        it.componentId == component.id
                    }

                    row.createCell(iForPassFail).also {
                        it.setCellValue(componentEntry?.pass.toString())
                    }

                    iForPassFail++
                    row.createCell(iForPassFail).also {
                        it.setCellValue(componentEntry?.fail.toString())
                    }

                    iForPassFail++
                }
            }

            var fos: FileOutputStream? = null
            try {
                val dir = File("//sdcard//Download//")
                val timeStamp = timeStampSdf.format(Date())
                val file = File(dir, "RailComponents_$timeStamp.xls")

                fos = FileOutputStream(file)
                workbook.write(fos)

                val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.addCompletedDownload(
                    file.getName(),
                    file.getName(),
                    true,
                    "application/excel",
                    file.getAbsolutePath(),
                    file.length(),
                    true
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}