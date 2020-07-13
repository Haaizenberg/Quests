package com.example.quests

class User(
    val uid: String,
    val username: String,
    val profileImageUrl: String,
    val events: MutableList<SimpleEvent>) {

    constructor(): this ("", "", "", mutableListOf())

    fun toSimpleUser(): SimpleUser {
        return SimpleUser(this.uid, this.username, this.profileImageUrl)
    }
}

class SimpleUser(val uid: String, val username: String, val profileImageUrl: String)
{
    constructor(): this("", "", "")
}