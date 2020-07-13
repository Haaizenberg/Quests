package com.example.quests

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.android.synthetic.main.bottom_navigation_view.*

abstract class BaseActivity(val navItemNumber: Int) : AppCompatActivity() {
    fun setupBottomNavigation() {
        bottom_navigation_view.setTextVisibility(false)
        bottom_navigation_view.enableItemShiftingMode(false)
        bottom_navigation_view.enableShiftingMode(false)
        bottom_navigation_view.enableAnimation(false)
        for (i in 0 until bottom_navigation_view.menu.size())
            bottom_navigation_view.setIconTintList(i, null)

        bottom_navigation_view.setOnNavigationItemSelectedListener {
            val nextActivity =
                when (it.itemId) {
                    (R.id.action_home) -> StartActivity::class.java
                    (R.id.action_search) -> CreatedEventsActivity::class.java
                    (R.id.action_add) -> NewAdressEventActivity::class.java
                    (R.id.action_map) -> MapsActivity::class.java
                    (R.id.action_profile) -> CreatedEventsActivity::class.java
                    else -> null
                }

            if (nextActivity != null) {
                val intent = Intent(this, nextActivity)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION // Отключаем анимацию перехода
                startActivity(intent)
                overridePendingTransition(0, 0) // Отключаем анимации
                true
            }
            else false
        }

        bottom_navigation_view.menu.getItem(navItemNumber).isChecked = true
    }
}