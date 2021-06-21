package com.dgut.calendar.week

import android.content.Context
import android.content.res.TypedArray
import android.support.v4.view.PagerAdapter
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.dgut.todo.R
import org.joda.time.DateTime


class WeekAdapter(
    private val mContext: Context,
    private val mArray: TypedArray,
    weekCalendarView: WeekCalendarView
) :
    PagerAdapter() {
    private val mViews: SparseArray<WeekView>
    private val mWeekCalendarView: WeekCalendarView
    private var mStartDate: DateTime? = null
    var weekCount = 220

    private fun initStartDate() {
        mStartDate = DateTime()
        mStartDate = mStartDate!!.plusDays(-mStartDate!!.dayOfWeek % 7)
    }

    override fun getCount(): Int {
        return weekCount
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        for (i in 0..2) {
            if (position - 2 + i in 0 until weekCount && mViews[position - 2 + i] == null) {
                instanceWeekView(position - 2 + i)
            }
        }
        container.addView(mViews[position])
        return mViews[position]
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    val views: SparseArray<WeekView>
        get() = mViews

    fun instanceWeekView(position: Int): WeekView {
        val weekView = WeekView(mContext, mArray, mStartDate!!.plusWeeks(position - weekCount / 2))
        weekView.setId(position)
        weekView.setLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        weekView.setOnWeekClickListener(mWeekCalendarView)
        weekView.invalidate()
        mViews.put(position, weekView)
        return weekView
    }

    init {
        mWeekCalendarView = weekCalendarView
        mViews = SparseArray<WeekView>()
        initStartDate()
        weekCount = mArray.getInteger(R.styleable.WeekCalendarView_week_count, 220)
    }
}