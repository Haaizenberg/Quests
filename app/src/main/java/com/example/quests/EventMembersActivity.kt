package com.example.quests

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_created_events.*
import kotlinx.android.synthetic.main.activity_event_members.*

class EventMembersActivity : AppCompatActivity() {

    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_members)

        val eventId = intent.getStringExtra("eventId")
        val eventTitle = intent.getStringExtra("eventTitle")

        getEventByIdAndUploadMembers(eventId!!)

        listenBottomNavigationBar()

        supportActionBar?.title = eventTitle
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.event_nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_back -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun listenBottomNavigationBar() {
        bottom_navigation_event_members.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                (R.id.action_map) -> {
                    val intent = Intent(this, MapsActivity::class.java)
                    //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                (R.id.action_home) -> {
                    val intent = Intent(this, StartActivity::class.java)
                    //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                (R.id.action_search) -> {
                    val intent = Intent(this, CreatedEventsActivity::class.java)
                    //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                else -> { }
            }
            true
        }
    }

    private fun getEventByIdAndUploadMembers(eventId: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("events").child(eventId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val ev = p0.getValue(Event::class.java)
                    if (ev != null) {
                        if (ev.id == eventId) {
                            event = ev
                            val adapter = GroupAdapter<GroupieViewHolder>()
                            ev.members.forEach {
                                adapter.add(MemberItem(it))
                            }

                            recyclerview_created_event_members.adapter = adapter
                        }
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) { }
        })
    }
}
