package com.dgut.calendar.schedule

import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation


class AutoMoveAnimation(private val mView: View?, private val mDistance: Int) : Animation() {
    private val mPositionY: Float
    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)
        mView!!.y = mPositionY + interpolatedTime * mDistance
    }

    init {
        duration = 200
        interpolator = DecelerateInterpolator(1.5f)
        mPositionY = mView!!.y
    }
}