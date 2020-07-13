package com.example.quests

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_new_adress_event.*
import kotlinx.android.synthetic.main.activity_new_event.*
import java.text.SimpleDateFormat
import java.util.*

class NewAdressEventActivity : AppCompatActivity() {
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_adress_event)

        supportActionBar?.title = "Новое событие"
        getCurrentUser()

        val selectedDate = Calendar.getInstance()
        val cal = Calendar.getInstance()
        var now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        new_adress_event_date.setOnClickListener {
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
                new_adress_event_date.text = dateFormat.format(selectedDate.time)
            }, y, month, day)
            dpd.show()
        }

        new_adress_event_time.setOnClickListener {
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
                new_adress_event_time.text = timeFormat.format(selectedDate.time)
            }, hours, minutes, true)
            tp.show()
        }

        new_adress_event_save_btn.setOnClickListener {
            if (correctInputFields()) {
                if (saveEventInDatabase(selectedDate.timeInMillis))
                    finish()
                else Toast.makeText(
                    this,
                    "Адрес не найден! Попробуйте создать событие через карту.",
                    Toast.LENGTH_LONG)
                    .show()
            }

        }

        new_adress_event_cancel_btn.setOnClickListener {
            finish()
        }
    }

    private fun correctInputFields(): Boolean {
        return !(new_adress_event_title.text.isEmpty() || new_adress_event_date.text.isEmpty() ||
            new_adress_event_time.text.isEmpty() || new_adress_event_adress.text.isEmpty() ||
            new_adress_event_amount.text.isEmpty() ||
            new_adress_event_amount.text.toString().toInt() <= 0)

    }

    private fun saveEventInDatabase(timeshtamp: Long) : Boolean {
        val root = FirebaseDatabase.getInstance().reference
        val curUserId = FirebaseAuth.getInstance().uid!!
        val eventId = root.child("events").push().key!!
        val geocoder = Geocoder(this)
        val positions =
            geocoder.getFromLocationName("Петрозаводск, ${new_adress_event_adress.text}", 1)
                ?: return false
        val position = positions[0]

        val event = Event(eventId, new_adress_event_title.text.toString(),
            timeshtamp, new_adress_event_amount.text.toString().toInt(),
            position.latitude, position.longitude, curUserId,
            mutableListOf())

        currentUser!!.events.add(event.toSimpleEvent())

        root.child("events").child(eventId).setValue(event)
        root.child("users").child(curUserId).setValue(currentUser)

        return true
    }

    private fun getCurrentUser() {
        val userId = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    if (user != null)
                        currentUser = user
                }
            }
            override fun onCancelled(p0: DatabaseError) { }
        })
    }
}
