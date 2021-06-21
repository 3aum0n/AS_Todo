package com.dgut.calendar.week

import android.content.Context
import android.content.res.TypedArray
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.SparseArray
import com.dgut.calendar.OnCalendarClickListener
import com.dgut.todo.R


class WeekCalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ViewPager(context, attrs), OnWeekClickListener {
    private var mOnCalendarClickListener: OnCalendarClickListener? = null
    var weekAdapter: WeekAdapter? = null
        private set

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        initWeekAdapter(
            context,
            context.obtainStyledAttributes(attrs, R.styleable.WeekCalendarView)
        )
    }

    private fun initWeekAdapter(context: Context, array: TypedArray) {
        weekAdapter = WeekAdapter(context, array, this)
        adapter = weekAdapter
        setCurrentItem(weekAdapter!!.weekCount / 2, false)
    }

    override fun onClickDate(year: Int, month: Int, day: Int) {
        mOnCalendarClickListener?.onClickDate(year, month, day)
    }

    private val mOnPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            val weekView: WeekView? = weekAdapter!!.views[position]
            if (weekView != null) {
                mOnCalendarClickListener?.onPageChange(
                    weekView.selectYear,
                    weekView.selectMonth,
                    weekView.selectDay
                )
                weekView.clickThisWeek(
                    weekView.selectYear,
                    weekView.selectMonth,
                    weekView.selectDay
                )
            } else {
                postDelayed({ onPageSelected(position) }, 50)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    /**
     * 设置点击日期监听
     * @param onCalendarClickListener
     */
    fun setOnCalendarClickListener(onCalendarClickListener: OnCalendarClickListener?) {
        mOnCalendarClickListener = onCalendarClickListener
    }

    val weekViews: SparseArray<WeekView>
        get() = weekAdapter!!.views
    val currentWeekView: WeekView
        get() = weekViews[currentItem]

    init {
        initAttrs(context, attrs)
        addOnPageChangeListener(mOnPageChangeListener)
    }
}