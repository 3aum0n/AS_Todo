package com.dgut.calendar.schedule

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent


class OnScheduleScrollListener(private val mScheduleLayout: ScheduleLayout) :
    SimpleOnGestureListener() {
    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        mScheduleLayout.onCalendarScroll(distanceY)
        return true
    }
}