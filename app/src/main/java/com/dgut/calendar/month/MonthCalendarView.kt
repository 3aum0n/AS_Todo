package com.dgut.calendar.month

import android.content.Context
import android.content.res.TypedArray
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.SparseArray
import com.dgut.calendar.OnCalendarClickListener
import com.dgut.todo.R
import java.util.*

class MonthCalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ViewPager(context, attrs), OnMonthClickListener {
    private var mMonthAdapter: MonthAdapter? = null
    private var mOnCalendarClickListener: OnCalendarClickListener? = null
    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        initMonthAdapter(
            context,
            context.obtainStyledAttributes(attrs, R.styleable.MonthCalendarView)
        )
    }

    private fun initMonthAdapter(context: Context, array: TypedArray) {
        mMonthAdapter = MonthAdapter(context, array, this)
        adapter = mMonthAdapter
        setCurrentItem(mMonthAdapter!!.monthCount / 2, false)
    }

    override fun onClickThisMonth(year: Int, month: Int, day: Int) {
        mOnCalendarClickListener?.onClickDate(year, month, day)
    }

    override fun onClickLastMonth(year: Int, month: Int, day: Int) {
        val monthDateView: MonthView = mMonthAdapter!!.views.get(currentItem - 1)
        if (monthDateView != null) {
            monthDateView.setSelectYearMonth(year, month, day)
        }
        setCurrentItem(currentItem - 1, true)
    }

    override fun onClickNextMonth(year: Int, month: Int, day: Int) {
        val monthDateView: MonthView = mMonthAdapter!!.views.get(currentItem + 1)
        if (monthDateView != null) {
            monthDateView.setSelectYearMonth(year, month, day)
            monthDateView.invalidate()
        }
        onClickThisMonth(year, month, day)
        setCurrentItem(currentItem + 1, true)
    }

    private val mOnPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            val monthView: MonthView = mMonthAdapter!!.views.get(currentItem)
            if (monthView != null) {
                monthView.clickThisMonth(
                    monthView.selectYear,
                    monthView.selectMonth,
                    monthView.selectDay
                )
                mOnCalendarClickListener?.onPageChange(
                    monthView.selectYear,
                    monthView.selectMonth,
                    monthView.selectDay
                )
            } else {
                postDelayed({ onPageSelected(position) }, 50)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    /**
     * 跳转到今天
     */
    fun setTodayToView() {
        setCurrentItem(mMonthAdapter!!.monthCount / 2, true)
        val monthView: MonthView = mMonthAdapter!!.views.get(mMonthAdapter!!.monthCount / 2)
        if (monthView != null) {
            val calendar = Calendar.getInstance()
            monthView.clickThisMonth(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH], calendar[Calendar.DATE]
            )
        }
    }

    /**
     * 设置点击日期监听
     *
     * @param onCalendarClickListener
     */
    fun setOnCalendarClickListener(onCalendarClickListener: OnCalendarClickListener?) {
        mOnCalendarClickListener = onCalendarClickListener
    }

    val monthViews: SparseArray<MonthView>
        get() = mMonthAdapter!!.views
    val currentMonthView: MonthView
        get() = monthViews[currentItem]

    init {
        initAttrs(context, attrs)
        addOnPageChangeListener(mOnPageChangeListener)
    }
}