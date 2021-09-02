package com.palak.railindia.utils

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
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

        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
            return false
        }

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
            entryList.forEach { entry->
                val dateInDigit = /*mFormat.format(*/dateMonthSdf.format(entry.date!!)/*.toDouble())*/
                sheet.addMergedRegion(CellRangeAddress(0, 0, iForDate, iForDate + 1))
                row.createCell(iForDate).also {
                    it.setCellValue(dateInDigit + " (Qty: " + entry.qty + ")")
                    iForDate++
                    iForDate++
                }
            }

            row = sheet.createRow(1)
            row.createCell(0).also {
                it.setCellValue("#")
            }
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

                    val componentEntry = entry.componentEntry?.firstOrNull {
                        it.componentId == component.id
                    }

                    row.createCell(iForPassFail).also {
                        it.setCellValue((componentEntry?.pass ?: "-").toString())
                    }

                    iForPassFail++
                    row.createCell(iForPassFail).also {
                        it.setCellValue((componentEntry?.fail ?: "-").toString())
                    }

                    iForPassFail++
                }
            }

            var fos: FileOutputStream? = null
            try {
                val newPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                println("NewPATH: $newPath")
                val dir = File("//sdcard//Download//")
                val timeStamp = timeStampSdf.format(Date())
                val file = File(newPath, "RailComponents_$timeStamp.xls")

                fos = FileOutputStream(file)
                workbook.write(fos)

                downloadFile(context, file)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun downloadFile(context: Context, file: File){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // You can add more columns.. Complete list of columns can be found at
                // https://developer.android.com/reference/android/provider/MediaStore.Downloads
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Downloads.TITLE, file.name)
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/excel")
                contentValues.put(MediaStore.Downloads.SIZE, file.length())

                // If you downloaded to a specific folder inside "Downloads" folder
                contentValues.put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + File.separator + "Temp"
                );

                // Insert into the database
                val database = context.contentResolver
                database.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            } else {
                val downloadManager =  context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.addCompletedDownload(
                    file.getName(), file.getName(), true,
                    "application/excel", file.getAbsolutePath(),
                    file.length(), true
                )
            }

            Toast.makeText(
                context,
                "Downloaded file ${file.name} in Download folder!",
                Toast.LENGTH_LONG
            ).show()
        }

    }


}