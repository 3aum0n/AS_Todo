package com.dgut.calendar.month

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.dgut.calendar.CalendarUtils
import com.dgut.calendar.LunarCalendarUtils
import com.dgut.common.data.DBManagerTask
import com.dgut.todo.R
import java.util.*


class MonthView(
    context: Context?,
    array: TypedArray?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    year: Int,
    month: Int
) : View(context, attrs, defStyleAttr) {
    private var mPaint: Paint? = null
    private var mLunarPaint: Paint? = null
    private var mNormalDayColor = 0
    private var mSelectDayColor = 0
    private var mSelectBGColor = 0
    private var mSelectBGTodayColor = 0
    private var mCurrentDayColor = 0
    private var mHintCircleColor = 0
    private var mLunarTextColor = 0
    private var mHolidayTextColor = 0
    private var mLastOrNextMonthTextColor = 0
    private var mCurrYear = 0
    private var mCurrMonth = 0
    private var mCurrDay = 0

    /**
     * 获取当前选择年
     *
     * @return
     */
    var selectYear = 0
        private set

    /**
     * 获取当前选择月
     *
     * @return
     */
    var selectMonth = 0
        private set

    /**
     * 获取当前选择日
     *
     * @return
     */
    var selectDay = 0
        private set
    private var mColumnSize = 0
    var rowSize = 0
        private set
    private var mSelectCircleSize = 0
    private var mDaySize = 0
    private var mLunarTextSize = 0
    var weekRow // 当前月份第几周
            = 0
        private set
    private val mCircleRadius = 6
    private lateinit var mDaysText: Array<IntArray>
    private lateinit var mHolidays: IntArray
    private lateinit var mHolidayOrLunarText: Array<Array<String?>>
    private var mIsShowLunar = false
    private var mIsShowHint = false
    private var mIsShowHolidayHint = false
    private var mDisplayMetrics: DisplayMetrics? = null
    private var mDateClickListener: OnMonthClickListener? = null
    private var mGestureDetector: GestureDetector? = null
    private var mRestBitmap: Bitmap? = null
    private var mWorkBitmap: Bitmap? = null

    constructor(context: Context?, year: Int, month: Int) : this(context, null, year, month) {}
    constructor(context: Context?, array: TypedArray?, year: Int, month: Int) : this(
        context,
        array,
        null,
        year,
        month
    ) {
    }

    constructor(
        context: Context?,
        array: TypedArray?,
        attrs: AttributeSet?,
        year: Int,
        month: Int
    ) : this(context, array, attrs, 0, year, month) {
    }

    private fun initTaskHint() {
        if (mIsShowHint) {
            // 从数据库中获取圆点提示数据
            val dao = DBManagerTask(context)
            CalendarUtils.getInstance(context)!!.addTaskHints(
                selectYear, selectMonth, dao.getTaskHintByMonth(
                    selectYear, selectMonth
                )
            )
        }
    }

    private fun initGestureDetector() {
        mGestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    doClickAction(e.getX().toInt(), e.getY().toInt())
                    return true
                }
            })
    }

    private fun initAttrs(array: TypedArray?, year: Int, month: Int) {
        if (array != null) {
            mSelectDayColor = array.getColor(
                R.styleable.MonthCalendarView_month_selected_text_color,
                Color.parseColor("#FFFFFF")
            )
            mSelectBGColor = array.getColor(
                R.styleable.MonthCalendarView_month_selected_circle_color,
                Color.parseColor("#E8E8E8")
            )
            mSelectBGTodayColor = array.getColor(
                R.styleable.MonthCalendarView_month_selected_circle_today_color,
                Color.parseColor("#FF8594")
            )
            mNormalDayColor = array.getColor(
                R.styleable.MonthCalendarView_month_normal_text_color,
                Color.parseColor("#575471")
            )
            mCurrentDayColor = array.getColor(
                R.styleable.MonthCalendarView_month_today_text_color,
                Color.parseColor("#FF8594")
            )
            mHintCircleColor = array.getColor(
                R.styleable.MonthCalendarView_month_hint_circle_color,
                Color.parseColor("#FE8595")
            )
            mLastOrNextMonthTextColor = array.getColor(
                R.styleable.MonthCalendarView_month_last_or_next_month_text_color,
                Color.parseColor("#ACA9BC")
            )
            mLunarTextColor = array.getColor(
                R.styleable.MonthCalendarView_month_lunar_text_color,
                Color.parseColor("#ACA9BC")
            )
            mHolidayTextColor = array.getColor(
                R.styleable.MonthCalendarView_month_holiday_color,
                Color.parseColor("#A68BFF")
            )
            mDaySize = array.getInteger(R.styleable.MonthCalendarView_month_day_text_size, 13)
            mLunarTextSize =
                array.getInteger(R.styleable.MonthCalendarView_month_day_lunar_text_size, 8)
            mIsShowHint = array.getBoolean(R.styleable.MonthCalendarView_month_show_task_hint, true)
            mIsShowLunar = array.getBoolean(R.styleable.MonthCalendarView_month_show_lunar, true)
            mIsShowHolidayHint =
                array.getBoolean(R.styleable.MonthCalendarView_month_show_holiday_hint, true)
        } else {
            mSelectDayColor = Color.parseColor("#FFFFFF")
            mSelectBGColor = Color.parseColor("#E8E8E8")
            mSelectBGTodayColor = Color.parseColor("#FF8594")
            mNormalDayColor = Color.parseColor("#575471")
            mCurrentDayColor = Color.parseColor("#FF8594")
            mHintCircleColor = Color.parseColor("#FE8595")
            mLastOrNextMonthTextColor = Color.parseColor("#ACA9BC")
            mHolidayTextColor = Color.parseColor("#A68BFF")
            mDaySize = 13
            mLunarTextSize = 8
            mIsShowHint = true
            mIsShowLunar = true
            mIsShowHolidayHint = true
        }
        selectYear = year
        selectMonth = month
        mRestBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_rest_day)
        mWorkBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_work_day)
        mHolidays = CalendarUtils.getInstance(context)!!.getHolidays(selectYear, selectMonth + 1)
    }

    private fun initPaint() {
        mDisplayMetrics = resources.displayMetrics
        val displayMetrics = mDisplayMetrics
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.textSize = mDaySize * displayMetrics!!.scaledDensity
        mLunarPaint = Paint()
        mLunarPaint!!.isAntiAlias = true
        mLunarPaint!!.textSize = mLunarTextSize * displayMetrics.scaledDensity
        mLunarPaint!!.color = mLunarTextColor
    }

    private fun initMonth() {
        val calendar = Calendar.getInstance()
        mCurrYear = calendar[Calendar.YEAR]
        mCurrMonth = calendar[Calendar.MONTH]
        mCurrDay = calendar[Calendar.DATE]
        if (selectYear == mCurrYear && selectMonth == mCurrMonth) {
            setSelectYearMonth(selectYear, selectMonth, mCurrDay)
        } else {
            setSelectYearMonth(selectYear, selectMonth, 1)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = mDisplayMetrics!!.densityDpi * 200
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = mDisplayMetrics!!.densityDpi * 300
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        initSize()
        clearData()
        drawLastMonth(canvas)
        val selected = drawThisMonth(canvas)
        drawNextMonth(canvas)
        drawHintCircle(canvas)
        drawLunarText(canvas, selected)
        drawHoliday(canvas)
    }

    private fun initSize() {
        mColumnSize = width / NUM_COLUMNS
        rowSize = height / NUM_ROWS
        mSelectCircleSize = (mColumnSize / 3.2).toInt()
        while (mSelectCircleSize > rowSize / 2) {
            mSelectCircleSize = (mSelectCircleSize / 1.3).toInt()
        }
    }

    private fun clearData() {
        mDaysText = Array(6) { IntArray(7) }
        mHolidayOrLunarText = Array(6) { arrayOfNulls<String>(7) }
    }

    private fun drawLastMonth(canvas: Canvas) {
        val lastYear: Int
        val lastMonth: Int
        if (selectMonth == 0) {
            lastYear = selectYear - 1
            lastMonth = 11
        } else {
            lastYear = selectYear
            lastMonth = selectMonth - 1
        }
        mPaint!!.color = mLastOrNextMonthTextColor
        val monthDays: Int = CalendarUtils.getMonthDays(lastYear, lastMonth)
        val weekNumber: Int = CalendarUtils.getFirstDayWeek(selectYear, selectMonth)
        for (day in 0 until weekNumber - 1) {
            mDaysText[0][day] = monthDays - weekNumber + day + 2
            val dayString = mDaysText[0][day].toString()
            val startX =
                (mColumnSize * day + (mColumnSize - mPaint!!.measureText(dayString)) / 2).toInt()
            val startY = (rowSize / 2 - (mPaint!!.ascent() + mPaint!!.descent()) / 2).toInt()
            canvas.drawText(dayString, startX.toFloat(), startY.toFloat(), mPaint)
            mHolidayOrLunarText[0][day] =
                CalendarUtils.getHolidayFromSolar(lastYear, lastMonth, mDaysText[0][day])
        }
    }

    private fun drawThisMonth(canvas: Canvas): IntArray {
        var dayString: String
        val selectedPoint = IntArray(2)
        val monthDays: Int = CalendarUtils.getMonthDays(selectYear, selectMonth)
        val weekNumber: Int = CalendarUtils.getFirstDayWeek(selectYear, selectMonth)
        for (day in 0 until monthDays) {
            dayString = (day + 1).toString()
            val col = (day + weekNumber - 1) % 7
            val row = (day + weekNumber - 1) / 7
            mDaysText[row][col] = day + 1
            val startX =
                (mColumnSize * col + (mColumnSize - mPaint!!.measureText(dayString)) / 2).toInt()
            val startY =
                (rowSize * row + rowSize / 2 - (mPaint!!.ascent() + mPaint!!.descent()) / 2).toInt()
            if (dayString == selectDay.toString()) {
                val startRecX = mColumnSize * col
                val startRecY = rowSize * row
                val endRecX = startRecX + mColumnSize
                val endRecY = startRecY + rowSize
                if (selectYear == mCurrYear && mCurrMonth == selectMonth && day + 1 == mCurrDay) {
                    mPaint!!.color = mSelectBGTodayColor
                } else {
                    mPaint!!.color = mSelectBGColor
                }
                canvas.drawCircle(
                    ((startRecX + endRecX) / 2).toFloat(),
                    ((startRecY + endRecY) / 2).toFloat(),
                    mSelectCircleSize.toFloat(),
                    mPaint
                )
                weekRow = row + 1
            }
            if (dayString == selectDay.toString()) {
                selectedPoint[0] = row
                selectedPoint[1] = col
                mPaint!!.color = mSelectDayColor
            } else if (dayString == mCurrDay.toString() && mCurrDay != selectDay && mCurrMonth == selectMonth && mCurrYear == selectYear) {
                mPaint!!.color = mCurrentDayColor
            } else {
                mPaint!!.color = mNormalDayColor
            }
            canvas.drawText(dayString, startX.toFloat(), startY.toFloat(), mPaint)
            mHolidayOrLunarText[row][col] =
                CalendarUtils.getHolidayFromSolar(selectYear, selectMonth, mDaysText[row][col])
        }
        return selectedPoint
    }

    private fun drawNextMonth(canvas: Canvas) {
        mPaint!!.color = mLastOrNextMonthTextColor
        val monthDays: Int = CalendarUtils.getMonthDays(selectYear, selectMonth)
        val weekNumber: Int = CalendarUtils.getFirstDayWeek(selectYear, selectMonth)
        val nextMonthDays = 42 - monthDays - weekNumber + 1
        var nextMonth = selectMonth + 1
        var nextYear = selectYear
        if (nextMonth == 12) {
            nextMonth = 0
            nextYear += 1
        }
        for (day in 0 until nextMonthDays) {
            val column = (monthDays + weekNumber - 1 + day) % 7
            val row = 5 - (nextMonthDays - day - 1) / 7
            try {
                mDaysText[row][column] = day + 1
                mHolidayOrLunarText[row][column] =
                    CalendarUtils.getHolidayFromSolar(nextYear, nextMonth, mDaysText[row][column])
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val dayString = mDaysText[row][column].toString()
            val startX =
                (mColumnSize * column + (mColumnSize - mPaint!!.measureText(dayString)) / 2).toInt()
            val startY =
                (rowSize * row + rowSize / 2 - (mPaint!!.ascent() + mPaint!!.descent()) / 2).toInt()
            canvas.drawText(dayString, startX.toFloat(), startY.toFloat(), mPaint)
        }
    }

    /**
     * 绘制农历
     *
     * @param canvas
     * @param selected
     */
    private fun drawLunarText(canvas: Canvas, selected: IntArray) {
        if (mIsShowLunar) {
            val firstYear: Int
            var firstMonth: Int
            var firstDay: Int
            val monthDays: Int
            val weekNumber: Int = CalendarUtils.getFirstDayWeek(selectYear, selectMonth)
            if (weekNumber == 1) {
                firstYear = selectYear
                firstMonth = selectMonth + 1
                firstDay = 1
                monthDays = CalendarUtils.getMonthDays(firstYear, firstMonth)
            } else {
                if (selectMonth == 0) {
                    firstYear = selectYear - 1
                    firstMonth = 11
                    monthDays = CalendarUtils.getMonthDays(firstYear, firstMonth)
                    firstMonth = 12
                } else {
                    firstYear = selectYear
                    firstMonth = selectMonth - 1
                    monthDays = CalendarUtils.getMonthDays(firstYear, firstMonth)
                    firstMonth = selectMonth
                }
                firstDay = monthDays - weekNumber + 2
            }
            val lunar: LunarCalendarUtils.Lunar =
                LunarCalendarUtils.solarToLunar(
                    LunarCalendarUtils.Solar(
                        firstYear,
                        firstMonth,
                        firstDay
                    )
                )
            var days: Int
            var day: Int = lunar.lunarDay
            val leapMonth: Int = LunarCalendarUtils.leapMonth(lunar.lunarYear)
            days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap)
            var isChangeMonth = false
            for (i in 0..41) {
                val column = i % 7
                val row = i / 7
                if (day > days) {
                    day = 1
                    var isAdd = true
                    if (lunar.lunarMonth === 12) {
                        lunar.lunarMonth = 1
                        lunar.lunarYear = lunar.lunarYear + 1
                        isAdd = false
                    }
                    if (lunar.lunarMonth === leapMonth) {
                        days = LunarCalendarUtils.daysInMonth(
                            lunar.lunarYear,
                            lunar.lunarMonth,
                            lunar.isLeap
                        )
                    } else {
                        if (isAdd) {
                            lunar.lunarMonth++
                            days = LunarCalendarUtils.daysInLunarMonth(
                                lunar.lunarYear,
                                lunar.lunarMonth
                            )
                        }
                    }
                }
                if (firstDay > monthDays) {
                    firstDay = 1
                    isChangeMonth = true
                }
                if (row == 0 && mDaysText[row][column] >= 23 || row >= 4 && mDaysText[row][column] <= 14) {
                    mLunarPaint!!.color = mLunarTextColor
                } else {
                    mLunarPaint!!.color = mHolidayTextColor
                }
                var dayString = mHolidayOrLunarText[row][column]
                if ("" == dayString) {
                    dayString =
                        LunarCalendarUtils.getLunarHoliday(lunar.lunarYear, lunar.lunarMonth, day)
                }
                if ("" == dayString) {
                    dayString = LunarCalendarUtils.getLunarDayString(day)
                    mLunarPaint!!.color = mLunarTextColor
                }
                if ("初一" == dayString) {
                    var curYear = firstYear
                    var curMonth = firstMonth
                    if (isChangeMonth) {
                        curMonth++
                        if (curMonth == 13) {
                            curMonth = 1
                            curYear++
                        }
                    }
                    val chuyi: LunarCalendarUtils.Lunar =
                        LunarCalendarUtils.solarToLunar(
                            LunarCalendarUtils.Solar(
                                curYear,
                                curMonth,
                                firstDay
                            )
                        )
                    dayString =
                        LunarCalendarUtils.getLunarFirstDayString(chuyi.lunarMonth, chuyi.isLeap)
                }
                if (selected[0] == row && selected[1] == column) {
                    mLunarPaint!!.color = mSelectDayColor
                }
                val startX =
                    (mColumnSize * column + (mColumnSize - mLunarPaint!!.measureText(dayString)) / 2).toInt()
                val startY =
                    (rowSize * row + rowSize * 0.72 - (mLunarPaint!!.ascent() + mLunarPaint!!.descent()) / 2).toInt()
                canvas.drawText(dayString, startX.toFloat(), startY.toFloat(), mLunarPaint)
                day++
                firstDay++
            }
        }
    }

    private fun drawHoliday(canvas: Canvas) {
        if (mIsShowHolidayHint) {
            val rect = Rect(0, 0, mRestBitmap!!.getWidth(), mRestBitmap!!.getHeight())
            val rectF = Rect()
            val distance = (mSelectCircleSize / 2.5).toInt()
            for (i in mHolidays.indices) {
                val column = i % 7
                val row = i / 7
                rectF[mColumnSize * (column + 1) - mRestBitmap!!.getWidth() - distance, rowSize * row + distance, mColumnSize * (column + 1) - distance] =
                    rowSize * row + mRestBitmap!!.getHeight() + distance
                if (mHolidays[i] == 1) {
                    canvas.drawBitmap(mRestBitmap, rect, rectF, null)
                } else if (mHolidays[i] == 2) {
                    canvas.drawBitmap(mWorkBitmap, rect, rectF, null)
                }
            }
        }
    }

    /**
     * 绘制圆点提示
     *
     * @param canvas
     */
    private fun drawHintCircle(canvas: Canvas) {
        if (mIsShowHint) {
            val hints: List<Int> = CalendarUtils.getInstance(context)!!.getTaskHints(
                selectYear, selectMonth
            )
            if (hints.size > 0) {
                mPaint!!.color = mHintCircleColor
                val monthDays: Int = CalendarUtils.getMonthDays(selectYear, selectMonth)
                val weekNumber: Int = CalendarUtils.getFirstDayWeek(selectYear, selectMonth)
                for (day in 0 until monthDays) {
                    val col = (day + weekNumber - 1) % 7
                    val row = (day + weekNumber - 1) / 7
                    if (!hints.contains(day + 1)) continue
                    val circleX = (mColumnSize * col + mColumnSize * 0.5).toFloat()
                    val circleY = (rowSize * row + rowSize * 0.75).toFloat()
                    canvas.drawCircle(circleX, circleY, mCircleRadius.toFloat(), mPaint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mGestureDetector!!.onTouchEvent(event)
    }

    fun setSelectYearMonth(year: Int, month: Int, day: Int) {
        selectYear = year
        selectMonth = month
        selectDay = day
    }

    private fun doClickAction(x: Int, y: Int) {
        if (y > height) return
        val row = y / rowSize
        var column = x / mColumnSize
        column = Math.min(column, 6)
        var clickYear = selectYear
        var clickMonth = selectMonth
        if (row == 0) {
            if (mDaysText[row][column] >= 23) {
                if (selectMonth == 0) {
                    clickYear = selectYear - 1
                    clickMonth = 11
                } else {
                    clickYear = selectYear
                    clickMonth = selectMonth - 1
                }
                if (mDateClickListener != null) {
                    mDateClickListener!!.onClickLastMonth(
                        clickYear,
                        clickMonth,
                        mDaysText[row][column]
                    )
                }
            } else {
                clickThisMonth(clickYear, clickMonth, mDaysText[row][column])
            }
        } else {
            val monthDays: Int = CalendarUtils.getMonthDays(selectYear, selectMonth)
            val weekNumber: Int = CalendarUtils.getFirstDayWeek(selectYear, selectMonth)
            val nextMonthDays = 42 - monthDays - weekNumber + 1
            if (mDaysText[row][column] <= nextMonthDays && row >= 4) {
                if (selectMonth == 11) {
                    clickYear = selectYear + 1
                    clickMonth = 0
                } else {
                    clickYear = selectYear
                    clickMonth = selectMonth + 1
                }
                if (mDateClickListener != null) {
                    mDateClickListener!!.onClickNextMonth(
                        clickYear,
                        clickMonth,
                        mDaysText[row][column]
                    )
                }
            } else {
                clickThisMonth(clickYear, clickMonth, mDaysText[row][column])
            }
        }
    }

    /**
     * 跳转到某日期
     *
     * @param year
     * @param month
     * @param day
     */
    fun clickThisMonth(year: Int, month: Int, day: Int) {
        if (mDateClickListener != null) {
            mDateClickListener!!.onClickThisMonth(year, month, day)
        }
        setSelectYearMonth(year, month, day)
        invalidate()
    }

    /**
     * 添加多个圆点提示
     *
     * @param hints
     */
    fun addTaskHints(hints: List<Int?>?) {
        if (mIsShowHint) {
            CalendarUtils.getInstance(context)!!.addTaskHints(selectYear, selectMonth, hints)
            invalidate()
        }
    }

    /**
     * 删除多个圆点提示
     *
     * @param hints
     */
    fun removeTaskHints(hints: List<Int?>?) {
        if (mIsShowHint) {
            CalendarUtils.getInstance(context)!!.removeTaskHints(selectYear, selectMonth, hints)
            invalidate()
        }
    }

    /**
     * 添加一个圆点提示
     *
     * @param day
     */
    fun addTaskHint(day: Int?): Boolean {
        if (mIsShowHint) {
            if (CalendarUtils.getInstance(context)!!.addTaskHint(selectYear, selectMonth, day!!)) {
                invalidate()
                return true
            }
        }
        return false
    }

    /**
     * 删除一个圆点提示
     *
     * @param day
     */
    fun removeTaskHint(day: Int?): Boolean {
        if (mIsShowHint) {
            if (CalendarUtils.getInstance(context)!!
                    .removeTaskHint(selectYear, selectMonth, day!!)
            ) {
                invalidate()
                return true
            }
        }
        return false
    }

    /**
     * 设置点击日期监听
     *
     * @param dateClickListener
     */
    fun setOnDateClickListener(dateClickListener: OnMonthClickListener?) {
        mDateClickListener = dateClickListener
    }

    companion object {
        private const val NUM_COLUMNS = 7
        private const val NUM_ROWS = 6
    }

    init {
        initAttrs(array, year, month)
        initPaint()
        initMonth()
        initGestureDetector()
        initTaskHint()
    }
}