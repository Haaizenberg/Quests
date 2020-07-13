package com.example.quests

class Event(
    val id: String,
    val title: String,
    val timeshtamp: Long,
    val maxMembersCount: Int,
    val x: Double, val y: Double,
    val creatorUid: String,
    val members: MutableList<SimpleUser>)
{
    constructor(): this("" ,"", 0, 0, 0.0, 0.0, "", mutableListOf())

    fun toSimpleEvent(): SimpleEvent{
        return SimpleEvent(this.id, this.title, this.timeshtamp, this.x, this.y, this.creatorUid)
    }
}

class SimpleEvent(
    val id: String,
    val title: String,
    val timeshtamp: Long,
    val x: Double, val y: Double,
    val creatorUid: String)
{
    constructor(): this("" ,"", 0, 0.0, 0.0, "")

    fun toEvent(members: MutableList<SimpleUser>, maxMembersCount: Int): Event {
        return Event(this.id, this.title, this.timeshtamp, maxMembersCount, this.x, this.y,
        this.creatorUid, members)
    }
}
