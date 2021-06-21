package com.dgut.calendar.schedule

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.dgut.calendar.CalendarUtils
import com.dgut.calendar.OnCalendarClickListener
import com.dgut.calendar.month.MonthCalendarView
import com.dgut.calendar.month.MonthView
import com.dgut.calendar.week.WeekCalendarView
import com.dgut.calendar.week.WeekView
import com.dgut.todo.R
import org.joda.time.DateTime
import java.util.*


class ScheduleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val DEFAULT_MONTH = 0
    private val DEFAULT_WEEK = 1
    private var mcvCalendar: MonthCalendarView? = null
    private var wcvCalendar: WeekCalendarView? = null
    private var rlMonthCalendar: RelativeLayout? = null
    private var rlScheduleList: RelativeLayout? = null
    var schedulerRecyclerView: ScheduleRecyclerView? = null
        private set
    var currentSelectYear = 0
        private set
    var currentSelectMonth = 0
        private set
    var currentSelectDay = 0
        private set
    private var mRowSize = 0
    private var mMinDistance = 0
    private var mAutoScrollDistance = 0
    private var mDefaultView = 0
    private val mDownPosition = FloatArray(2)
    private var mIsScrolling = false
    private var mIsAutoChangeMonthRow = false
    private var mCurrentRowsIsSix = true
    private var mState: ScheduleState? = null
    private var mOnCalendarClickListener: OnCalendarClickListener? = null
    private var mGestureDetector: GestureDetector? = null
    private fun initAttrs(array: TypedArray) {
        mDefaultView = array.getInt(R.styleable.ScheduleLayout_default_view, DEFAULT_MONTH)
        mIsAutoChangeMonthRow =
            array.getBoolean(R.styleable.ScheduleLayout_auto_change_month_row, false)
        array.recycle()
        mState = ScheduleState.OPEN
        mRowSize = getResources().getDimensionPixelSize(R.dimen.week_calendar_height)
        mMinDistance = getResources().getDimensionPixelSize(R.dimen.calendar_min_distance)
        mAutoScrollDistance = getResources().getDimensionPixelSize(R.dimen.auto_scroll_distance)
    }

    private fun initGestureDetector() {
        mGestureDetector = GestureDetector(getContext(), OnScheduleScrollListener(this))
    }

    private fun initDate() {
        val calendar = Calendar.getInstance()
        resetCurrentSelectDate(
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mcvCalendar = findViewById(R.id.mcvCalendar) as MonthCalendarView?
        wcvCalendar = findViewById(R.id.wcvCalendar) as WeekCalendarView?
        rlMonthCalendar = findViewById(R.id.rlMonthCalendar) as RelativeLayout?
        rlScheduleList = findViewById(R.id.rlScheduleList) as RelativeLayout?
        schedulerRecyclerView = findViewById(R.id.rvScheduleList) as ScheduleRecyclerView?
        bindingMonthAndWeekCalendar()
    }

    private fun bindingMonthAndWeekCalendar() {
        mcvCalendar!!.setOnCalendarClickListener(mMonthCalendarClickListener)
        wcvCalendar!!.setOnCalendarClickListener(mWeekCalendarClickListener)
        // 初始化视图
        val calendar = Calendar.getInstance()
        if (mIsAutoChangeMonthRow) {
            mCurrentRowsIsSix =
                CalendarUtils.getMonthRows(calendar[Calendar.YEAR], calendar[Calendar.MONTH]) === 6
        }
        if (mDefaultView == DEFAULT_MONTH) {
            wcvCalendar!!.setVisibility(View.INVISIBLE)
            mState = ScheduleState.OPEN
            if (!mCurrentRowsIsSix) {
                rlScheduleList!!.setY(rlScheduleList!!.getY() - mRowSize)
            }
        } else if (mDefaultView == DEFAULT_WEEK) {
            wcvCalendar!!.setVisibility(View.VISIBLE)
            mState = ScheduleState.CLOSE
            val row: Int = CalendarUtils.getWeekRow(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
            rlMonthCalendar!!.setY((-row * mRowSize).toFloat())
            rlScheduleList!!.setY(rlScheduleList!!.getY() - 5 * mRowSize)
        }
    }

    private fun resetCurrentSelectDate(year: Int, month: Int, day: Int) {
        currentSelectYear = year
        currentSelectMonth = month
        currentSelectDay = day
    }

    private val mMonthCalendarClickListener: OnCalendarClickListener =
        object : OnCalendarClickListener {
            override fun onClickDate(year: Int, month: Int, day: Int) {
                wcvCalendar!!.setOnCalendarClickListener(null)
                val weeks: Int = CalendarUtils.getWeeksAgo(
                    currentSelectYear,
                    currentSelectMonth,
                    currentSelectDay,
                    year,
                    month,
                    day
                )
                resetCurrentSelectDate(year, month, day)
                val position: Int = wcvCalendar!!.getCurrentItem() + weeks
                if (weeks != 0) {
                    wcvCalendar!!.setCurrentItem(position, false)
                }
                resetWeekView(position)
                wcvCalendar!!.setOnCalendarClickListener(mWeekCalendarClickListener)
            }

            override fun onPageChange(year: Int, month: Int, day: Int) {
                computeCurrentRowsIsSix(year, month)
            }
        }

    private fun computeCurrentRowsIsSix(year: Int, month: Int) {
        if (mIsAutoChangeMonthRow) {
            val isSixRow = CalendarUtils.getMonthRows(year, month) === 6
            if (mCurrentRowsIsSix != isSixRow) {
                mCurrentRowsIsSix = isSixRow
                if (mState === ScheduleState.OPEN) {
                    if (mCurrentRowsIsSix) {
                        val animation = AutoMoveAnimation(rlScheduleList, mRowSize)
                        rlScheduleList!!.startAnimation(animation)
                    } else {
                        val animation = AutoMoveAnimation(rlScheduleList, -mRowSize)
                        rlScheduleList!!.startAnimation(animation)
                    }
                }
            }
        }
    }

    private fun resetWeekView(position: Int) {
        val weekView: WeekView = wcvCalendar!!.currentWeekView
        weekView.setSelectYearMonth(currentSelectYear, currentSelectMonth, currentSelectDay)
        weekView.invalidate()
        mOnCalendarClickListener?.onClickDate(
            currentSelectYear,
            currentSelectMonth,
            currentSelectDay
        )
    }

    private val mWeekCalendarClickListener: OnCalendarClickListener =
        object : OnCalendarClickListener {
            override fun onClickDate(year: Int, month: Int, day: Int) {
                mcvCalendar!!.setOnCalendarClickListener(null)
                val months: Int =
                    CalendarUtils.getMonthsAgo(currentSelectYear, currentSelectMonth, year, month)
                resetCurrentSelectDate(year, month, day)
                if (months != 0) {
                    val position: Int = mcvCalendar!!.getCurrentItem() + months
                    mcvCalendar!!.setCurrentItem(position, false)
                }
                resetMonthView()
                mcvCalendar!!.setOnCalendarClickListener(mMonthCalendarClickListener)
                if (mIsAutoChangeMonthRow) {
                    mCurrentRowsIsSix = CalendarUtils.getMonthRows(year, month) === 6
                }
            }

            override fun onPageChange(year: Int, month: Int, day: Int) {
                if (mIsAutoChangeMonthRow) {
                    if (currentSelectMonth != month) {
                        mCurrentRowsIsSix = CalendarUtils.getMonthRows(year, month) === 6
                    }
                }
            }
        }

    private fun resetMonthView() {
        val monthView: MonthView = mcvCalendar!!.currentMonthView
        monthView.setSelectYearMonth(currentSelectYear, currentSelectMonth, currentSelectDay)
        monthView.invalidate()
        mOnCalendarClickListener?.onClickDate(
            currentSelectYear,
            currentSelectMonth,
            currentSelectDay
        )
        resetCalendarPosition()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height: Int = MeasureSpec.getSize(heightMeasureSpec)
        resetViewHeight(rlScheduleList, height - mRowSize)
        resetViewHeight(this, height)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun resetViewHeight(view: View?, height: Int) {
        val layoutParams: ViewGroup.LayoutParams = view!!.layoutParams
        if (layoutParams.height != height) {
            layoutParams.height = height
            view.layoutParams = layoutParams
        }
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.getActionMasked()) {
            MotionEvent.ACTION_DOWN -> {
                mDownPosition[0] = ev.getRawX()
                mDownPosition[1] = ev.getRawY()
                mGestureDetector!!.onTouchEvent(ev)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (mIsScrolling) {
            return true
        }
        when (ev.getActionMasked()) {
            MotionEvent.ACTION_MOVE -> {
                val x: Float = ev.getRawX()
                val y: Float = ev.getRawY()
                val distanceX = Math.abs(x - mDownPosition[0])
                val distanceY = Math.abs(y - mDownPosition[1])
                if (distanceY > mMinDistance && distanceY > distanceX * 2.0f) {
                    return y > mDownPosition[1] && isRecyclerViewTouch || y < mDownPosition[1] && mState === ScheduleState.OPEN
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private val isRecyclerViewTouch: Boolean
        private get() = mState === ScheduleState.CLOSE && (schedulerRecyclerView!!.childCount == 0 || schedulerRecyclerView!!.isScrollTop)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.getActionMasked()) {
            MotionEvent.ACTION_DOWN -> {
                mDownPosition[0] = event.getRawX()
                mDownPosition[1] = event.getRawY()
                resetCalendarPosition()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                transferEvent(event)
                mIsScrolling = true
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                transferEvent(event)
                changeCalendarState()
                resetScrollingState()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun transferEvent(event: MotionEvent) {
        if (mState === ScheduleState.CLOSE) {
            mcvCalendar!!.setVisibility(View.VISIBLE)
            wcvCalendar!!.setVisibility(View.INVISIBLE)
            mGestureDetector!!.onTouchEvent(event)
        } else {
            mGestureDetector!!.onTouchEvent(event)
        }
    }

    private fun changeCalendarState() {
        if (rlScheduleList!!.getY() > mRowSize * 2 &&
            rlScheduleList!!.getY() < mcvCalendar!!.getHeight() - mRowSize
        ) { // 位于中间
            val animation = ScheduleAnimation(this, mState, mAutoScrollDistance.toFloat())
            animation.duration = 300
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    changeState()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            rlScheduleList!!.startAnimation(animation)
        } else if (rlScheduleList!!.getY() <= mRowSize * 2) { // 位于顶部
            val animation =
                ScheduleAnimation(this, ScheduleState.OPEN, mAutoScrollDistance.toFloat())
            animation.duration = 50
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    if (mState === ScheduleState.OPEN) {
                        changeState()
                    } else {
                        resetCalendar()
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            rlScheduleList!!.startAnimation(animation)
        } else {
            val animation =
                ScheduleAnimation(this, ScheduleState.CLOSE, mAutoScrollDistance.toFloat())
            animation.duration = 50
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    if (mState === ScheduleState.CLOSE) {
                        mState = ScheduleState.OPEN
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            rlScheduleList!!.startAnimation(animation)
        }
    }

    private fun resetCalendarPosition() {
        if (mState === ScheduleState.OPEN) {
            rlMonthCalendar!!.setY(0f)
            if (mCurrentRowsIsSix) {
                rlScheduleList!!.setY(mcvCalendar!!.getHeight().toFloat())
            } else {
                rlScheduleList!!.setY((mcvCalendar!!.getHeight() - mRowSize).toFloat())
            }
        } else {
            rlMonthCalendar!!.setY(
                (-CalendarUtils.getWeekRow(
                    currentSelectYear,
                    currentSelectMonth,
                    currentSelectDay
                ) * mRowSize).toFloat()
            )
            rlScheduleList!!.setY(mRowSize.toFloat())
        }
    }

    private fun resetCalendar() {
        if (mState === ScheduleState.OPEN) {
            mcvCalendar!!.setVisibility(View.VISIBLE)
            wcvCalendar!!.setVisibility(View.INVISIBLE)
        } else {
            mcvCalendar!!.setVisibility(View.INVISIBLE)
            wcvCalendar!!.setVisibility(View.VISIBLE)
        }
    }

    private fun changeState() {
        if (mState === ScheduleState.OPEN) {
            mState = ScheduleState.CLOSE
            mcvCalendar!!.setVisibility(View.INVISIBLE)
            wcvCalendar!!.setVisibility(View.VISIBLE)
            rlMonthCalendar!!.setY(((1 - mcvCalendar!!.currentMonthView.weekRow) * mRowSize).toFloat())
            checkWeekCalendar()
        } else {
            mState = ScheduleState.OPEN
            mcvCalendar!!.setVisibility(View.VISIBLE)
            wcvCalendar!!.setVisibility(View.INVISIBLE)
            rlMonthCalendar!!.setY(0f)
        }
    }

    private fun checkWeekCalendar() {
        val weekView: WeekView = wcvCalendar!!.currentWeekView
        var start: DateTime = weekView.startDate!!
        var end: DateTime = weekView.endDate
        var current =
            DateTime(currentSelectYear, currentSelectMonth + 1, currentSelectDay, 23, 59, 59)
        var week = 0
        while (current.millis < start.millis) {
            week--
            start = start.plusDays(-7)
        }
        current = DateTime(currentSelectYear, currentSelectMonth + 1, currentSelectDay, 0, 0, 0)
        if (week == 0) {
            while (current.millis > end.millis) {
                week++
                end = end.plusDays(7)
            }
        }
        if (week != 0) {
            val position: Int = wcvCalendar!!.getCurrentItem() + week
            if (wcvCalendar!!.weekViews.get(position) != null) {
                wcvCalendar!!.weekViews.get(position)
                    .setSelectYearMonth(currentSelectYear, currentSelectMonth, currentSelectDay)
                wcvCalendar!!.weekViews.get(position).invalidate()
            } else {
                val newWeekView: WeekView = wcvCalendar!!.weekAdapter!!.instanceWeekView(position)
                newWeekView.setSelectYearMonth(
                    currentSelectYear,
                    currentSelectMonth,
                    currentSelectDay
                )
                newWeekView.invalidate()
            }
            wcvCalendar!!.setCurrentItem(position, false)
        }
    }

    private fun resetScrollingState() {
        mDownPosition[0] = 0f
        mDownPosition[1] = 0f
        mIsScrolling = false
    }

    fun onCalendarScroll(distanceY: Float) {
        var distanceY = distanceY
        val monthView: MonthView = mcvCalendar!!.currentMonthView
        distanceY = Math.min(distanceY, mAutoScrollDistance.toFloat())
        val calendarDistanceY = distanceY / if (mCurrentRowsIsSix) 5.0f else 4.0f
        val row: Int = monthView.weekRow - 1
        val calendarTop = -row * mRowSize
        val scheduleTop = mRowSize
        var calendarY: Float = rlMonthCalendar!!.getY() - calendarDistanceY * row
        calendarY = Math.min(calendarY, 0f)
        calendarY = Math.max(calendarY, calendarTop.toFloat())
        rlMonthCalendar!!.setY(calendarY)
        var scheduleY: Float = rlScheduleList!!.getY() - distanceY
        scheduleY = if (mCurrentRowsIsSix) {
            Math.min(scheduleY, (mcvCalendar!!.getHeight()).toFloat())
        } else {
            Math.min(scheduleY, (mcvCalendar!!.getHeight() - mRowSize).toFloat())
        }
        scheduleY = Math.max(scheduleY, scheduleTop.toFloat())
        rlScheduleList!!.setY(scheduleY)
    }

    fun setOnCalendarClickListener(onCalendarClickListener: OnCalendarClickListener?) {
        mOnCalendarClickListener = onCalendarClickListener
    }

    private fun resetMonthViewDate(year: Int, month: Int, day: Int, position: Int) {
        if (mcvCalendar!!.monthViews.get(position) == null) {
            postDelayed(Runnable { resetMonthViewDate(year, month, day, position) }, 50)
        } else {
            mcvCalendar!!.monthViews.get(position).clickThisMonth(year, month, day)
        }
    }

    /**
     * 初始化年月日
     *
     * @param year
     * @param month (0-11)
     * @param day   (1-31)
     */
    fun initData(year: Int, month: Int, day: Int) {
        val monthDis: Int =
            CalendarUtils.getMonthsAgo(currentSelectYear, currentSelectMonth, year, month)
        val position: Int = mcvCalendar!!.getCurrentItem() + monthDis
        mcvCalendar!!.setCurrentItem(position)
        resetMonthViewDate(year, month, day, position)
    }

    /**
     * 添加多个圆点提示
     *
     * @param hints
     */
    fun addTaskHints(hints: List<Int?>?) {
        CalendarUtils.getInstance(getContext())
            ?.addTaskHints(currentSelectYear, currentSelectMonth, hints)
        mcvCalendar!!.currentMonthView.invalidate()
        wcvCalendar!!.currentWeekView.invalidate()
    }

    /**
     * 删除多个圆点提示
     *
     * @param hints
     */
    fun removeTaskHints(hints: List<Int?>?) {
        CalendarUtils.getInstance(getContext())
            ?.removeTaskHints(currentSelectYear, currentSelectMonth, hints)
        mcvCalendar!!.currentMonthView.invalidate()
        wcvCalendar!!.currentWeekView.invalidate()
    }

    /**
     * 添加一个圆点提示
     *
     * @param day
     */
    fun addTaskHint(day: Int?) {
        if (mcvCalendar!!.currentMonthView.addTaskHint(day)) {
            wcvCalendar!!.currentWeekView.invalidate()
        }
    }

    /**
     * 删除一个圆点提示
     *
     * @param day
     */
    fun removeTaskHint(day: Int?) {
        if (mcvCalendar!!.currentMonthView.removeTaskHint(day)) {
            wcvCalendar!!.currentWeekView.invalidate()
        }
    }

    val monthCalendar: MonthCalendarView?
        get() = mcvCalendar
    val weekCalendar: WeekCalendarView?
        get() = wcvCalendar

    init {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.ScheduleLayout))
        initDate()
        initGestureDetector()
    }
}