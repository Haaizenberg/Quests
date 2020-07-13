package com.example.quests

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : BaseActivity(0), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var newEventPosition: LatLng
    private lateinit var eventDict: MutableMap<String, Event>
    private lateinit var currentUser: User
    private val dateFormat = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        eventDict = mutableMapOf()
        supportActionBar?.title = "Активные события"

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        lateinit var curEvent:Event

        // Add a marker in PetrSU and move the camera.
        val petrsu = LatLng(61.7864, 34.352037)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(petrsu))

        Snackbar.make(findViewById(R.id.map), "Long tap to create an event", Snackbar.LENGTH_LONG).show()
        getCurrentUser()

        /* Получаем события из Бд и выводим на карту */
        listenEvents()

        setupBottomNavigation()

        /* Пользователь хочет создать событие */
        mMap.setOnMapLongClickListener {
            newEventPosition = it
            startActivityForResult(Intent(this, NewMapEventActivity::class.java), 1) //..., 1)
        }

        mMap.setOnInfoWindowClickListener {
            joined_members_txt.visibility = View.VISIBLE
            joined_members_title.visibility = View.VISIBLE

            /* Выводим кол-во присоединившихся */
            eventDict.forEach { (_, event) ->
                if (it.position.latitude == event.x && it.position.longitude == event.y) {
                    joined_members_txt.text = "${event.members.size}/${event.maxMembersCount}"
                    curEvent = event
                }
            }

            /* Регулируем видимость кнопок JOIN и LEAVE */
            leave_btn.visibility = View.INVISIBLE
            if (curEvent.creatorUid != currentUser.uid && curEvent.members.size < curEvent.maxMembersCount)
                join_btn.visibility = View.VISIBLE

            curEvent.members.forEach {member ->
                if (member.uid == currentUser.uid) {
                    leave_btn.visibility = View.VISIBLE
                    join_btn.visibility = View.INVISIBLE
                    return@forEach
                }
            }
        }

        leave_btn.setOnClickListener {
            curEvent.members.removeAll { su -> su.uid == currentUser.uid }
            currentUser.events.removeAll { se -> se.id == curEvent.id }

            joined_members_txt.text = "${curEvent.members.size}/${curEvent.maxMembersCount}"

            join_btn.visibility = View.VISIBLE
            leave_btn.visibility = View.INVISIBLE

            updateEventInDatabase(curEvent)
        }

        /* Прячем кнопки при выходе из события */
        mMap.setOnMapClickListener {
            joined_members_txt.visibility = View.INVISIBLE
            joined_members_title.visibility = View.INVISIBLE
            join_btn.visibility = View.INVISIBLE
            leave_btn.visibility = View.INVISIBLE
        }

        join_btn.setOnClickListener {
            curEvent.members.add(currentUser.toSimpleUser())
            currentUser.events.add(curEvent.toSimpleEvent())

            val forUpdate = Event(curEvent.id, curEvent.title, curEvent.timeshtamp,
                curEvent.maxMembersCount, curEvent.x, curEvent.y, curEvent.creatorUid, curEvent.members)

            joined_members_txt.text = "${forUpdate.members.size}/${forUpdate.maxMembersCount}"

            join_btn.visibility = View.INVISIBLE
            leave_btn.visibility = View.VISIBLE

            updateEventInDatabase(forUpdate) // Обновляем инфу о событии
        }
    }

    private fun processEvent(event: Event) {
        var contains = false
        currentUser.events.forEach { se ->
            if (se.id == event.id) {
                contains = true
                return@forEach
            }
        }

        /* Определение цвета маркера */
        val color = when {
            (event.creatorUid == currentUser.uid) ->
                BitmapDescriptorFactory.HUE_VIOLET
            (contains) ->
                BitmapDescriptorFactory.HUE_AZURE
            (event.maxMembersCount == event.members.size) ->
                BitmapDescriptorFactory.HUE_RED
            else -> BitmapDescriptorFactory.HUE_GREEN
        }

        val date = dateFormat.format(event.timeshtamp)
        val time = timeFormat.format(event.timeshtamp)
        mMap.addMarker(
            MarkerOptions()
                .title(event.title)
                .position(LatLng(event.x, event.y))
                .snippet("$date $time")
                .icon(BitmapDescriptorFactory.defaultMarker(color))
        )
    }

    private fun listenEvents() {
        val ref = FirebaseDatabase.getInstance().reference.child("events")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val now = Calendar.getInstance()
                if (p0.exists()) {
                    val event = p0.getValue(Event::class.java)
                    if (event != null && event.timeshtamp > now.timeInMillis) {
                        eventDict[event.id] = event
                        processEvent(event)
                    }
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                if (p0.exists()) {
                    val now = Calendar.getInstance()
                    val event = p0.getValue(Event::class.java)
                    if (event != null && event.timeshtamp > now.timeInMillis) {
                        eventDict[event.id] = event
                        refreshMapMarkers()
                    }
                }
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) { }

            override fun onChildRemoved(p0: DataSnapshot) {
                if (p0.exists()) {
                    val event = p0.getValue(Event::class.java)
                    if (event != null) {
                        eventDict.remove(event.id)
                        refreshMapMarkers()
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) { }
        })
    }

    private fun refreshMapMarkers() {
        val now = Calendar.getInstance()
        mMap.clear()
        eventDict.forEach { (_, event) ->
            if (event.timeshtamp > now.timeInMillis)
                processEvent(event)
        }
    }


    /* Вызовется, когда NewEventActivity завершится и мы получим данные созданного квеста */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            val ref = FirebaseDatabase.getInstance().reference.child("events")
            val title = data?.getStringExtra("title").toString()
            val timeshtamp = data?.getLongExtra("timeshtamp", 0).toString().toLong()
            val maxMembersCount = data?.getIntExtra("membersCount", 0).toString().toInt()
            val eventUid = ref.push().key!!

            val createdEvent = Event(eventUid, title, timeshtamp, maxMembersCount,
                newEventPosition.latitude, newEventPosition.longitude, currentUser.uid, mutableListOf())

            val formattedDate = dateFormat.format(createdEvent.timeshtamp)
            val formattedTime = timeFormat.format(createdEvent.timeshtamp)
            mMap.addMarker(MarkerOptions()
                .position(newEventPosition)
                .title(title)
                .snippet("$formattedDate $formattedTime")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))

            eventDict[eventUid] = createdEvent
            saveEventInDatabase(createdEvent)
        }
    }

    /* Сохраняем новое событие в БД */
    private fun saveEventInDatabase(event: Event) {
        currentUser.events.add(event.toSimpleEvent())

        val refEvent = FirebaseDatabase.getInstance().reference.child("events").child(event.id)
        refEvent.setValue(event)
            .addOnSuccessListener {
                Toast.makeText(this, "Event succsessful created!", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Fail to create event!", Toast.LENGTH_LONG).show()
            }

        val refUser = FirebaseDatabase.getInstance().reference.child("users").child(currentUser.uid)
        refUser.setValue(currentUser)
    }

    /* Сохранение в БД информации, что пользователь стал участником или покинул событие */
    private fun updateEventInDatabase(event: Event) {
        val ref = FirebaseDatabase.getInstance().reference.child("events").child(event.id)
        ref.setValue(event)

        val refU = FirebaseDatabase.getInstance().reference.child("users").child(currentUser.uid)
        refU.setValue(currentUser).addOnSuccessListener { }
    }

    /* Получение информации о текущем пользователе */
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