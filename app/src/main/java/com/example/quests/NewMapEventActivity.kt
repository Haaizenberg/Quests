package com.example.quests

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_new_event.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.ExperimentalTime

class NewMapEventActivity : AppCompatActivity() {

    @ExperimentalTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)
        val selectedDate = Calendar.getInstance()
        val cal = Calendar.getInstance()
        var now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        supportActionBar?.title = "New event"

        new_event_date.setOnClickListener {
            val y = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                if (selectedDate < now) {
                    Toast.makeText(this, "Select future date!", Toast.LENGTH_SHORT).show()
                    return@OnDateSetListener
                }
                new_event_date.text = dateFormat.format(selectedDate.time)
            }, y, month, day)
            dpd.show()
        }

        new_event_time.setOnClickListener {
            val hours = cal.get(Calendar.HOUR_OF_DAY)
            val minutes = cal.get(Calendar.MINUTE)
            val tp = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                now = Calendar.getInstance()
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)

                if (selectedDate < now) {
                    Toast.makeText(this, "Select future time!", Toast.LENGTH_SHORT).show()
                    return@OnTimeSetListener
                }
                new_event_time.text = timeFormat.format(selectedDate.time)
            }, hours, minutes, true)
            tp.show()
        }

        new_event_cancel_btn.setOnClickListener {
            finish()
        }

        new_event_save_btn.setOnClickListener {
            when  {
                (new_event_title.text.isEmpty()) -> {
                    Toast.makeText(this, "Enter correct title!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                (new_event_date.text.isEmpty()) -> {
                    Toast.makeText(this, "Enter correct date!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                (new_event_time.text.isEmpty()) -> {
                    Toast.makeText(this, "Enter correct time!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                (new_event_members_count.text.isEmpty() || new_event_members_count.text.toString().toLong() <= 0) -> {
                    Toast.makeText(this, "Enter correct amount of members", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else -> {
                    val intent = Intent()
                    intent.putExtra("title", new_event_title.text.toString())
                    intent.putExtra("timeshtamp", selectedDate.timeInMillis)
                    intent.putExtra("membersCount", new_event_members_count.text.toString().toInt())
                    setResult(Activity.RESULT_OK, intent)
                    Log.d("MapsActivityLog", new_event_title.text.toString())
                    finish()
                }
            }
        }
    }
}