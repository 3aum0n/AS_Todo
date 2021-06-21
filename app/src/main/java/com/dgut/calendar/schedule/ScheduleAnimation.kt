package com.dgut.calendar.schedule

import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation


class ScheduleAnimation(
    private val mScheduleLayout: ScheduleLayout,
    private val mState: ScheduleState?,
    private val mDistanceY: Float
) : Animation() {
    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)
        if (mState == ScheduleState.OPEN) {
            mScheduleLayout.onCalendarScroll(mDistanceY)
        } else {
            mScheduleLayout.onCalendarScroll(-mDistanceY)
        }
    }

    init {
        duration = 300
        interpolator = DecelerateInterpolator(1.5f)
    }
}