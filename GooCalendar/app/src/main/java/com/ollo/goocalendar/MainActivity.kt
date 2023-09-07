package com.ollo.goocalendar

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.CalendarContract.ACTION_EVENT_REMINDER
import android.provider.CalendarContract.CalendarAlerts
import android.provider.CalendarContract.Reminders
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private var adapter: RecordAdapter? = null
    private var recordsArr: ArrayList<RecordModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        initView()
        initRecyclerView()

        // var cal = Calendar.getInstance()

        var edText = findViewById<TextInputEditText>(R.id.edText)

        var btnCheck = findViewById<Button>(R.id.btnCheck)
        btnCheck.setOnClickListener {
            addEventsArr(edText.text.toString())
        }

        var btnDelete = findViewById<Button>(R.id.btnDelete)
        btnDelete.setOnClickListener {
            edText.text?.clear()
            recordsArr.clear()
        }
        adapter?.setOnClickBtnSave { saveRecordInGoo(it) }

        var btnExData = findViewById<Button>(R.id.btnExampleData)
        btnExData.setOnClickListener { edText.setText(addExampleData()) }

        adapter?.setOnClickBtnEdit {
            openPopupEditForm(it)
        }

        var btnInfo = findViewById<Button>(R.id.btnInfo)
        btnInfo.setOnClickListener { openInfoWindow() }
    }

    fun openInfoWindow() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setNegativeButton(R.string.ok) { dialog, _ ->
            dialog.dismiss()
        }
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.info_layout, null)
        builder.setView(dialogLayout)
        builder.show()
    }

    fun openPopupEditForm(record: RecordModel) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }
        val inflater = layoutInflater
        builder.setTitle("Dienst Ändern")
        val dialogLayout = inflater.inflate(R.layout.edit_popup, null)
        var editDienstNummer = dialogLayout.findViewById<EditText>(R.id.editDienstNummer)
        var editTime = dialogLayout.findViewById<EditText>(R.id.editTime)
        var editLocation = dialogLayout.findViewById<EditText>(R.id.editLocation)
        var editLohnstunden = dialogLayout.findViewById<EditText>(R.id.editLohnstunden)
        var editDate = dialogLayout.findViewById<EditText>(R.id.editDate)
        editDienstNummer.setText(record.dienstNummer)
        editTime.setText(record.time)
        editLocation.setText(record.location)
        editLohnstunden.setText(record.lohnstunden)
        editDate.setText(record.date)
        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.save) { dialogInterface, i ->
            Toast.makeText(
                applicationContext,
                "Gut gemacht!",
                Toast.LENGTH_SHORT
            ).show()
            recordsArr.get(recordsArr.indexOf(record)).dienstNummer =
                editDienstNummer.text.toString()

            recordsArr.get(recordsArr.indexOf(record)).time = editTime.text.toString()
            recordsArr.get(recordsArr.indexOf(record)).location = editLocation.text.toString()
            recordsArr.get(recordsArr.indexOf(record)).lohnstunden = editLohnstunden.text.toString()
            recordsArr.get(recordsArr.indexOf(record)).date = editDate.text.toString()
            adapter?.addRecords(recordsArr)
        }

        builder.show()
    }

    fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecordAdapter()
        recyclerView.adapter = adapter
    }

    fun addExampleData(): String {
        var text =
            "3255308\n11:31-20:19\nOrt: GRIEE\nLohnstunden:\n07:58\n01.01.2023\n" +
                    "3255510\n04:40-09:53\nOrt: GRIEE\nLohnstunden:\n09:15\n02.01.2023\n" +
                    "3255511\n04:30-09:58\nOrt: GRIEE\nLohnstunden:\n09:20\n03.01.2023\n" +
                    "3255512\n04:31-09:51\nOrt: GRIEE\nLohnstunden:\n09:11\n04.01.2023"

        return text
    }

    fun addEventsArr(text: String) {
        adapter?.addRecords(convertRecordModelArr(text))
    }

    fun saveRecordInGoo(record: RecordModel) {
        val patternQuest = ".+\\?\$".toRegex()
        if (record.time.matches(patternQuest) || record.date.matches(patternQuest)) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.edit_fields_que)
            builder.setCancelable(true)
            builder.setNegativeButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            val alert = builder.create()
            alert.show()
            return
        } else {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, record.dienstNummer)
                putExtra(CalendarContract.Events.EVENT_LOCATION, record.location)
                putExtra(CalendarContract.Events.DESCRIPTION, record.lohnstunden)
                putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    startTimeConverter(record.date, record.time)
                )
                putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    endTimeConverter(record.date, record.time)
                )
                type = "vnd.android.cursor.dir/event"
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            recordsArr.removeAt(recordsArr.indexOf(record))
            adapter?.addRecords(recordsArr)
        }
    }

    fun checkTimeFormat(time: String): String {
        var timeTrimmed = time.replace(" ", "")
        val patternTimeExact = "\\d{2}:\\d{2}-\\d{2}:\\d{2}".toRegex()
        val patternTimeAsk = "\\d{2}\\:\\d{2}\\-\\d{2}\\:\\d{2}".toRegex()
        if (timeTrimmed.matches(patternTimeExact)) {
            return timeTrimmed
        } else if(timeTrimmed.matches(patternTimeAsk)){
            return timeTrimmed + "?"
        } else {
            return "false"
        }
    }

    fun checkDateFormat(date: String): String {
        var dateTrimmed = date.replace(" ", "")
        val patternDateExact = "\\d{2}.\\d{2}.\\d{4}".toRegex()
        val patternDateCheck = "\\d{2}\\.\\d{2}\\.\\d{4}".toRegex()
        if (dateTrimmed.matches(patternDateExact)) {
            return dateTrimmed
        } else if(dateTrimmed.matches(patternDateCheck) ){
            return dateTrimmed + "?"
        } else {
            return "false"
        }
    }

    fun startTimeConverter(date: String, time: String): Long {
        val startMillis = Calendar.getInstance().run {
            set(
                date.substring(6, 10).toInt(),
                date.substring(3, 5).toInt() - 1,
                date.substring(0, 2).toInt(),
                time.substring(0, 2).toInt(),
                time.substring(3, 5).toInt()
            )
            timeInMillis
        }
        return startMillis
    }

    fun endTimeConverter(date: String, time: String): Long {
        var endMillis: Long = 0
        endMillis = Calendar.getInstance().run {
            set(
                date.substring(6, 10).toInt(),
                date.substring(3, 5).toInt() - 1,
                date.substring(0, 2).toInt(),
                time.substring(6, 8).toInt(),
                time.substring(9, 11).toInt()
            )
            timeInMillis
        }
        return endMillis
    }

    fun convertRecordModelArr(text: String): ArrayList<RecordModel> {
        val patternLine = "\n+|\\s".toRegex()
        var splitedLines = text.split(patternLine)

        for ((i, v) in splitedLines.withIndex()) {
            var record = RecordModel()
            val patternNo7break = "\\d{2}:|.\\d{2}-\\d{2}:|.\\d{2}".toRegex()
            val patternNo7 = "\\d{5,}".toRegex()
            val patternNo4 = "\\d{4,}".toRegex()
            val patternDate = "\\d{2}\\.\\d{2}\\.\\d{4}".toRegex()

            if (v.matches(patternNo4) && splitedLines[i + 3].matches(patternNo7break)) {
                record.id = recordsArr.size
                record.dienstNummer = splitedLines[i]
                record.time = checkTimeFormat(
                    splitedLines[i + 1].substring(0, 6) + splitedLines[i + 4].substring(6, 11)
                )
                record.location = splitedLines[i + 2] + " " + splitedLines[i + 3]
                record.lohnstunden = splitedLines[i + 5] + " " + splitedLines[i + 6]
                var plusI =
                    checkNeighborPositions(splitedLines as ArrayList<String>, patternDate, i, 6)
                record.date = checkDateFormat(splitedLines[plusI])
                if(record.time != "false" || record.date != "false") {
                    recordsArr.add(record)
                }
            } else if (v.matches(patternNo7)) {
                record.id = recordsArr.size
                record.dienstNummer = splitedLines[i]
                record.time = checkTimeFormat(splitedLines[i + 1])
                record.location = splitedLines[i + 2] + " " + splitedLines[i + 3]
                record.lohnstunden = splitedLines[i + 4] + " " + splitedLines[i + 5]
                var plusI =
                    checkNeighborPositions(splitedLines as ArrayList<String>, patternDate, i, 5)
                record.date = checkDateFormat(splitedLines[plusI])
                if(record.time != "false" || record.date != "false") {
                    recordsArr.add(record)
                }
            }
        }
        if(recordsArr.size ==0) {
            Toast.makeText(this, "Daten stimmen nicht überein", Toast.LENGTH_SHORT).show()
        }
        return recordsArr
    }


    fun checkNeighborPositions(arr: ArrayList<String>, pattern: Regex, i: Int, pos: Int): Int {
        if (arr[i].matches(pattern)) {
            return i + pos
        } else if (arr[i + pos + 1].matches(pattern)) {
            return i + pos + 1
        } else if (arr[i + pos + 2].matches(pattern)) {
            return i + pos + 2
        } else if (arr[i + pos + 3].matches(pattern)) {
            return i + pos + 3
        }else if (arr[i + pos + 4].matches(pattern)) {
            return i + pos + 4
        }else if (arr[i + pos + 5].matches(pattern)) {
            return i + pos + 5
        } else {
            return i
        }
    }

    fun initView() {
        recyclerView = findViewById(R.id.recyclerView)
    }
}


//    fun timeConverter(timeString: String): Long {
//        // Parsing the date and time to SimpleDateFormat
//        val mSimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
//        val timeSimpleFormat = ""
//        val mTime = mSimpleDateFormat.parse(timeSimpleFormat)
//        return mTime.time
//    }

//    fun addEvent(title: String, location: String, description: String, begin: Long, end: Long) {
//        val intent = Intent(Intent.ACTION_INSERT).apply {
//            data = CalendarContract.Events.CONTENT_URI
//            putExtra(CalendarContract.Events.TITLE, title)
//            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
//            putExtra(CalendarContract.Events.DESCRIPTION, description)
//            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
//            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
//            type = "vnd.android.cursor.dir/event"
//        }
//
//        if (intent.resolveActivity(packageManager) != null) {
//            startActivity(intent)
//        }
//    }


//    private fun getRecords() {
//        val recordsList = convertToRecordModelArr("ewqr")
//        adapter?.addRecords(recordsList)
//    }
