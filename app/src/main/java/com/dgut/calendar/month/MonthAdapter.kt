package com.dgut.calendar.month

import android.content.Context
import android.content.res.TypedArray
import android.support.v4.view.PagerAdapter
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.dgut.todo.R
import org.joda.time.DateTime


class MonthAdapter(
    private val mContext: Context,
    private val mArray: TypedArray,
    monthCalendarView: MonthCalendarView
) :
    PagerAdapter() {
    private val mViews: SparseArray<MonthView>
    private val mMonthCalendarView: MonthCalendarView
    val monthCount: Int

    override fun getCount(): Int {
        return monthCount
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (mViews[position] == null) {
            val date = getYearAndMonth(position)
            val monthView = MonthView(mContext, mArray, date[0], date[1])
            monthView.setId(position)
            monthView.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            monthView.invalidate()
            monthView.setOnDateClickListener(mMonthCalendarView)
            mViews.put(position, monthView)
        }
        container.addView(mViews[position])
        return mViews[position]
    }

    private fun getYearAndMonth(position: Int): IntArray {
        val date = IntArray(2)
        var time = DateTime()
        time = time.plusMonths(position - monthCount / 2)
        date[0] = time.year
        date[1] = time.monthOfYear - 1
        return date
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    val views: SparseArray<MonthView>
        get() = mViews

    init {
        mMonthCalendarView = monthCalendarView
        mViews = SparseArray<MonthView>()
        monthCount = mArray.getInteger(R.styleable.MonthCalendarView_month_count, 48)
    }
}