package com.example.quests

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.android.synthetic.main.activity_start.view.*
import java.util.*

class StartActivity: BaseActivity(1) {
    private lateinit var currentUserUid: String
    private lateinit var lastEvents: MutableList<Event>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        lastEvents = mutableListOf()

        supportActionBar?.title = "Последние 10"
        currentUserUid = FirebaseAuth.getInstance().uid!!
        verifyIsUserLogIn()

        takeEventsFromDatabase()
        setupBottomNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_back -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.menu_create_new -> {
                startActivity(Intent(this, NewAdressEventActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun verifyIsUserLogIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun takeEventsFromDatabase() {
        val now = Calendar.getInstance()
        val refEvents = FirebaseDatabase.getInstance().reference.child("events")
        refEvents.limitToLast(10).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    lastEvents.clear()
                    p0.children.forEach {
                        val event = it.getValue(Event::class.java)
                        if (event != null && event.creatorUid != currentUserUid && event.timeshtamp > now.timeInMillis)
                            lastEvents.add(event)
                    }
                    processEvents()
                }
            }

            override fun onCancelled(p0: DatabaseError) { }
        })
    }

    private fun processEvents() {
        recyclerview_start_activity.invalidate()
        val adapter = GroupAdapter<GroupieViewHolder>()
        lastEvents.sortBy { event -> event.timeshtamp }

        lastEvents.forEach {
            adapter.add(EventItem(it, 0))
        }
        adapter.setOnItemClickListener { item, view ->
            val eventItem = item as EventItem
            val intent = Intent(view.context, EventMembersActivity::class.java)
            intent.putExtra("eventId", eventItem.event.id)
            intent.putExtra("eventTitle", eventItem.event.title)
            startActivity(intent)
        }
        recyclerview_start_activity.adapter = adapter
    }
}