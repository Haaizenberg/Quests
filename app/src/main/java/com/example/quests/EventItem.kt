package com.example.quests

import android.location.Geocoder
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.event_row.view.*
import java.text.SimpleDateFormat
import java.util.*

class EventItem(val event: Event, val flag: Int): Item<GroupieViewHolder>() {
    private val dateFormat= SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val curUserId = FirebaseAuth.getInstance().uid
        when {
            (flag == 0) -> {
                viewHolder.itemView.delete_btn_event_row.visibility = View.INVISIBLE
                viewHolder.itemView.leave_btn_event_row.visibility = View.INVISIBLE
            }
            (event.creatorUid != curUserId) -> {
                viewHolder.itemView.delete_btn_event_row.visibility = View.INVISIBLE
                viewHolder.itemView.leave_btn_event_row.visibility = View.VISIBLE
            }
            else -> {
                viewHolder.itemView.delete_btn_event_row.visibility = View.VISIBLE
                viewHolder.itemView.leave_btn_event_row.visibility = View.INVISIBLE
            }
        }

        val adresses = Geocoder(viewHolder.itemView.context, Locale.getDefault()).getFromLocation(event.x, event.y, 1)
        val adress = "${adresses[0].thoroughfare}, ${adresses[0].featureName}"
        viewHolder.itemView.adress_textView.text = adress

        viewHolder.itemView.title_textView.text = event.title
        viewHolder.itemView.date_textView.text = dateFormat.format(event.timeshtamp)
        viewHolder.itemView.time_textView.text = timeFormat.format(event.timeshtamp)
        viewHolder.itemView.amount_event_row.text = "${event.members.size}/${event.maxMembersCount}"

        viewHolder.itemView.delete_btn_event_row.setOnClickListener {
            deleteEvent(event.toSimpleEvent())
            Toast.makeText(it.context, "${event.title} successfuly deleted!", Toast.LENGTH_LONG).show()
            viewHolder.itemView.alpha = 0f
        }

        viewHolder.itemView.leave_btn_event_row.setOnClickListener {
            leaveEvent(event.toSimpleEvent())
            Toast.makeText(it.context, "You leave ${event.title}!", Toast.LENGTH_LONG).show()
            viewHolder.itemView.alpha = 0f
        }
    }
    override fun getLayout(): Int {
        return R.layout.event_row
    }

    private fun deleteEvent(deleteEvent: SimpleEvent) {
        val members: MutableList<String> = mutableListOf()
        val refEvent = FirebaseDatabase.getInstance().reference.child("events").child(deleteEvent.id)

        refEvent.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val event = p0.getValue(Event::class.java)
                    if (event != null && event.members.size == 0) {
                        refEvent.removeValue()
                        removeEventFromUser(event.creatorUid, deleteEvent)
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) { }
        })

        /* Получаем список текущих участников события */
        val refEventMembers = refEvent.child("members")
        refEventMembers.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        val su = it.getValue(SimpleUser::class.java)
                        if (su != null)
                            members.add(su.uid)
                    }
                    /* Добавляем создателя, т.к в ветке users у него тоже хранится запись об этом событии */
                    members.add(deleteEvent.creatorUid)
                }
                refEvent.removeValue() // Удаляем само событие

                /* Удаляем данное событие у всех участников */
                members.forEach { uid ->
                    removeEventFromUser(uid, deleteEvent)
                }
            }
            override fun onCancelled(p0: DatabaseError) { }
        })
    }

    private fun removeEventFromUser(uid: String, deleteEvent: SimpleEvent) {
        val refUsers = FirebaseDatabase.getInstance().reference.child("users")
        val memberEvents: MutableList<SimpleEvent> = mutableListOf()
        val ref = refUsers.child(uid).child("events")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        val se = it.getValue(SimpleEvent::class.java)
                        if (se != null && se.id != deleteEvent.id)
                            memberEvents.add(se)
                    }
                    ref.setValue(memberEvents)
                }
            }
            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun leaveEvent(leaveEvent: SimpleEvent) {
        /* Удаляем пользователя из события */
        val curUserId = FirebaseAuth.getInstance().uid
        val refEvent = FirebaseDatabase.getInstance().reference
            .child("events")
            .child(leaveEvent.id)
            .child("members")
        val members: MutableList<SimpleUser> = mutableListOf()

        refEvent.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        val su = it.getValue(SimpleUser::class.java)
                        if (su != null && su.uid != curUserId)
                            members.add(su)
                    }
                }
                refEvent.setValue(members)
            }
            override fun onCancelled(p0: DatabaseError) { }
        })

        /* Удаляем событие из пользователя */
        val events: MutableList<SimpleEvent> = mutableListOf()
        val refUser = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(curUserId!!)
            .child("events")
        refUser.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        val se = it.getValue(SimpleEvent::class.java)
                        if (se != null && se.id != leaveEvent.id)
                            events.add(se)
                    }
                }
                refUser.setValue(events)
            }
            override fun onCancelled(p0: DatabaseError) { }
        })
    }
}