package com.dgut.calendar.schedule

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View


class ScheduleRecyclerView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {
    val isScrollTop: Boolean
        get() = computeVerticalScrollOffset() == 0

    override fun requestChildFocus(child: View, focused: View) {
        super.requestChildFocus(child, focused)
        if (onFocusChangeListener != null) {
            onFocusChangeListener.onFocusChange(child, false)
            onFocusChangeListener.onFocusChange(focused, true)
        }
    }
}