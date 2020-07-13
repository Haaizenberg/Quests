package com.example.quests

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_created_events.*
import java.util.*

class CreatedEventsActivity : BaseActivity(2) {
    private lateinit var events: MutableList<Event>
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_created_events)
        events = mutableListOf()

        supportActionBar?.title = "Созданные"

        getCreatedEvents()
        setupBottomNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_my_events, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_created_events -> {
                supportActionBar?.title = "Созданные"
                getCreatedEvents()
            }
            R.id.menu_joined_events -> {
                supportActionBar?.title = "Участвую"
                getJoinedEvents()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun processEvents() {
        recyclerview_created_events.invalidate()
        adapter.clear()

        events.sortBy { event -> event.timeshtamp }

        events.forEach {
            adapter.add(EventItem(it, 1))
        }
        adapter.setOnItemClickListener { item, view ->
            val eventItem = item as EventItem
            val intent = Intent(view.context, EventMembersActivity::class.java)
            intent.putExtra("eventId", eventItem.event.id)
            intent.putExtra("eventTitle", eventItem.event.title)
            startActivity(intent)
        }
        if (recyclerview_created_events.adapter == null)
            recyclerview_created_events.adapter = adapter

        recyclerview_created_events.adapter!!.notifyDataSetChanged()
    }

    private fun getCreatedEvents() {
        val now = Calendar.getInstance()
        val currentUserUid = FirebaseAuth.getInstance().uid

        val refEvents = FirebaseDatabase.getInstance().reference.child("events")
        refEvents.orderByChild("creatorUid").equalTo(currentUserUid).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    events.clear()
                    p0.children.forEach {
                        val event = it.getValue(Event::class.java)
                        if (event != null && event.timeshtamp > now.timeInMillis)
                            events.add(event)
                    }
                    processEvents()
                }
            }

            override fun onCancelled(p0: DatabaseError) { }
        })
    }

    private fun getJoinedEvents() {
        val now = Calendar.getInstance()
        val currentUserUid = FirebaseAuth.getInstance().uid

        val refEvents = FirebaseDatabase.getInstance().reference.child("events")
        refEvents.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    events.clear()
                    p0.children.forEach {
                        val event = it.getValue(Event::class.java)
                        if (event != null && event.creatorUid != currentUserUid && event.timeshtamp > now.timeInMillis)
                            events.add(event)
                    }
                    processEvents()
                }
            }

            override fun onCancelled(p0: DatabaseError) { }
        })
    }
}