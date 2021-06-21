package com.dgut.calendar.week

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
import org.joda.time.DateTime
import java.util.*


class WeekView(
    context: Context?,
    array: TypedArray?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    dateTime: DateTime?
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
    private var mRowSize = 0
    private var mSelectCircleSize = 0
    private var mDaySize = 0
    private var mLunarTextSize = 0
    private val mCircleRadius = 6
    private lateinit var mHolidays: IntArray
    private lateinit var mHolidayOrLunarText: Array<String?>
    private var mIsShowLunar = false
    private var mIsShowHint = false
    private var mIsShowHolidayHint = false
    var startDate: DateTime? = null
        private set
    private var mDisplayMetrics: DisplayMetrics? = null
    private var mOnWeekClickListener: OnWeekClickListener? = null
    private var mGestureDetector: GestureDetector? = null
    private var mRestBitmap: Bitmap? = null
    private var mWorkBitmap: Bitmap? = null

    constructor(context: Context?, dateTime: DateTime?) : this(context, null, dateTime) {}
    constructor(context: Context?, array: TypedArray?, dateTime: DateTime?) : this(
        context,
        array,
        null,
        dateTime
    )

    constructor(
        context: Context?,
        array: TypedArray?,
        attrs: AttributeSet?,
        dateTime: DateTime?
    ) : this(context, array, attrs, 0, dateTime)

    private fun initTaskHint(date: DateTime?) {
        if (mIsShowHint) {
            // 从数据库中获取圆点提示数据
            val dao = DBManagerTask(context)
            if (CalendarUtils.getInstance(context)!!.getTaskHints(date!!.year, date.monthOfYear - 1)
                    .size === 0
            ) CalendarUtils.getInstance(
                context
            )!!.addTaskHints(
                date!!.year, date.monthOfYear - 1, dao.getTaskHintByMonth(
                    selectYear, selectMonth
                )
            )
        }
    }

    private fun initAttrs(array: TypedArray?, dateTime: DateTime?) {
        if (array != null) {
            mSelectDayColor = array.getColor(
                R.styleable.WeekCalendarView_week_selected_text_color,
                Color.parseColor("#FFFFFF")
            )
            mSelectBGColor = array.getColor(
                R.styleable.WeekCalendarView_week_selected_circle_color,
                Color.parseColor("#E8E8E8")
            )
            mSelectBGTodayColor = array.getColor(
                R.styleable.WeekCalendarView_week_selected_circle_today_color,
                Color.parseColor("#FF8594")
            )
            mNormalDayColor = array.getColor(
                R.styleable.WeekCalendarView_week_normal_text_color,
                Color.parseColor("#575471")
            )
            mCurrentDayColor = array.getColor(
                R.styleable.WeekCalendarView_week_today_text_color,
                Color.parseColor("#FF8594")
            )
            mHintCircleColor = array.getColor(
                R.styleable.WeekCalendarView_week_hint_circle_color,
                Color.parseColor("#FE8595")
            )
            mLunarTextColor = array.getColor(
                R.styleable.WeekCalendarView_week_lunar_text_color,
                Color.parseColor("#ACA9BC")
            )
            mHolidayTextColor = array.getColor(
                R.styleable.WeekCalendarView_week_holiday_color,
                Color.parseColor("#A68BFF")
            )
            mDaySize = array.getInteger(R.styleable.WeekCalendarView_week_day_text_size, 13)
            mLunarTextSize =
                array.getInteger(R.styleable.WeekCalendarView_week_day_lunar_text_size, 8)
            mIsShowHint = array.getBoolean(R.styleable.WeekCalendarView_week_show_task_hint, true)
            mIsShowLunar = array.getBoolean(R.styleable.WeekCalendarView_week_show_lunar, true)
            mIsShowHolidayHint =
                array.getBoolean(R.styleable.WeekCalendarView_week_show_holiday_hint, true)
        } else {
            mSelectDayColor = Color.parseColor("#FFFFFF")
            mSelectBGColor = Color.parseColor("#E8E8E8")
            mSelectBGTodayColor = Color.parseColor("#FF8594")
            mNormalDayColor = Color.parseColor("#575471")
            mCurrentDayColor = Color.parseColor("#FF8594")
            mHintCircleColor = Color.parseColor("#FE8595")
            mLunarTextColor = Color.parseColor("#ACA9BC")
            mHolidayTextColor = Color.parseColor("#A68BFF")
            mDaySize = 13
            mDaySize = 8
            mIsShowHint = true
            mIsShowLunar = true
            mIsShowHolidayHint = true
        }
        startDate = dateTime
        mRestBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_rest_day)
        mWorkBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_work_day)
        val holidays: IntArray = CalendarUtils.getInstance(context)!!.getHolidays(
            startDate!!.year, startDate!!.monthOfYear
        )
        val row: Int = CalendarUtils.getWeekRow(
            startDate!!.year,
            startDate!!.monthOfYear - 1,
            startDate!!.dayOfMonth
        )
        mHolidays = IntArray(7)
        System.arraycopy(holidays, row * 7, mHolidays, 0, mHolidays.size)
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

    private fun initWeek() {
        val calendar = Calendar.getInstance()
        mCurrYear = calendar[Calendar.YEAR]
        mCurrMonth = calendar[Calendar.MONTH]
        mCurrDay = calendar[Calendar.DATE]
        val endDate = startDate!!.plusDays(7)
        if (startDate!!.millis <= System.currentTimeMillis() && endDate.millis > System.currentTimeMillis()) {
            if (startDate!!.monthOfYear != endDate.monthOfYear) {
                if (mCurrDay < startDate!!.dayOfMonth) {
                    setSelectYearMonth(startDate!!.year, endDate.monthOfYear - 1, mCurrDay)
                } else {
                    setSelectYearMonth(startDate!!.year, startDate!!.monthOfYear - 1, mCurrDay)
                }
            } else {
                setSelectYearMonth(startDate!!.year, startDate!!.monthOfYear - 1, mCurrDay)
            }
        } else {
            setSelectYearMonth(
                startDate!!.year,
                startDate!!.monthOfYear - 1,
                startDate!!.dayOfMonth
            )
        }
        initTaskHint(startDate)
        initTaskHint(endDate)
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

    fun setSelectYearMonth(year: Int, month: Int, day: Int) {
        selectYear = year
        selectMonth = month
        selectDay = day
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
        val selected = drawThisWeek(canvas)
        drawLunarText(canvas, selected)
        drawHintCircle(canvas)
        drawHoliday(canvas)
    }

    private fun clearData() {
        mHolidayOrLunarText = arrayOfNulls(7)
    }

    private fun initSize() {
        mColumnSize = width / NUM_COLUMNS
        mRowSize = height
        mSelectCircleSize = (mColumnSize / 3.2).toInt()
        while (mSelectCircleSize > mRowSize / 2) {
            mSelectCircleSize = (mSelectCircleSize / 1.3).toInt()
        }
    }

    private fun drawThisWeek(canvas: Canvas): Int {
        var selected = 0
        for (i in 0..6) {
            val date = startDate!!.plusDays(i)
            val day = date.dayOfMonth
            val dayString = day.toString()
            val startX =
                (mColumnSize * i + (mColumnSize - mPaint!!.measureText(dayString)) / 2).toInt()
            val startY = (mRowSize / 2 - (mPaint!!.ascent() + mPaint!!.descent()) / 2).toInt()
            if (day == selectDay) {
                val startRecX = mColumnSize * i
                val endRecX = startRecX + mColumnSize
                if (date.year == mCurrYear && date.monthOfYear - 1 == mCurrMonth && day == mCurrDay) {
                    mPaint!!.color = mSelectBGTodayColor
                } else {
                    mPaint!!.color = mSelectBGColor
                }
                canvas.drawCircle(
                    ((startRecX + endRecX) / 2).toFloat(),
                    (mRowSize / 2).toFloat(),
                    mSelectCircleSize.toFloat(),
                    mPaint
                )
            }
            if (day == selectDay) {
                selected = i
                mPaint!!.color = mSelectDayColor
            } else if (date.year == mCurrYear && date.monthOfYear - 1 == mCurrMonth && day == mCurrDay && day != selectDay && mCurrYear == selectYear) {
                mPaint!!.color = mCurrentDayColor
            } else {
                mPaint!!.color = mNormalDayColor
            }
            canvas.drawText(dayString, startX.toFloat(), startY.toFloat(), mPaint)
            mHolidayOrLunarText[i] =
                CalendarUtils.getHolidayFromSolar(date.year, date.monthOfYear - 1, day)
        }
        return selected
    }

    /**
     * 绘制农历
     *
     * @param canvas
     * @param selected
     */
    private fun drawLunarText(canvas: Canvas, selected: Int) {
        if (mIsShowLunar) {
            val lunar: LunarCalendarUtils.Lunar = LunarCalendarUtils.solarToLunar(
                LunarCalendarUtils.Solar(
                    startDate!!.year,
                    startDate!!.monthOfYear,
                    startDate!!.dayOfMonth
                )
            )
            val leapMonth: Int = LunarCalendarUtils.leapMonth(lunar.lunarYear)
            var days: Int =
                LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap)
            var day: Int = lunar.lunarDay
            for (i in 0..6) {
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
                mLunarPaint!!.color = mHolidayTextColor
                var dayString = mHolidayOrLunarText[i]
                if ("" == dayString) {
                    dayString =
                        LunarCalendarUtils.getLunarHoliday(lunar.lunarYear, lunar.lunarMonth, day)
                }
                if ("" == dayString) {
                    dayString = LunarCalendarUtils.getLunarDayString(day)
                    mLunarPaint!!.color = mLunarTextColor
                }
                if ("初一" == dayString) {
                    val curDay = startDate!!.plusDays(i)
                    val chuyi: LunarCalendarUtils.Lunar = LunarCalendarUtils.solarToLunar(
                        LunarCalendarUtils.Solar(
                            curDay.year,
                            curDay.monthOfYear,
                            curDay.dayOfMonth
                        )
                    )
                    dayString =
                        LunarCalendarUtils.getLunarFirstDayString(chuyi.lunarMonth, chuyi.isLeap)
                }
                if (i == selected) {
                    mLunarPaint!!.color = mSelectDayColor
                }
                val startX =
                    (mColumnSize * i + (mColumnSize - mLunarPaint!!.measureText(dayString)) / 2).toInt()
                val startY =
                    (mRowSize * 0.72 - (mLunarPaint!!.ascent() + mLunarPaint!!.descent()) / 2).toInt()
                canvas.drawText(dayString, startX.toFloat(), startY.toFloat(), mLunarPaint)
                day++
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
                rectF[mColumnSize * (column + 1) - mRestBitmap!!.getWidth() - distance, distance, mColumnSize * (column + 1) - distance] =
                    mRestBitmap!!.getHeight() + distance
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
            mPaint!!.color = mHintCircleColor
            val startMonth = startDate!!.monthOfYear
            val endMonth = startDate!!.plusDays(7).monthOfYear
            val startDay = startDate!!.dayOfMonth
            if (startMonth == endMonth) {
                val hints: List<Int> = CalendarUtils.getInstance(context)!!.getTaskHints(
                    startDate!!.year, startDate!!.monthOfYear - 1
                )
                for (i in 0..6) {
                    drawHintCircle(hints, startDay + i, i, canvas)
                }
            } else {
                for (i in 0..6) {
                    val hints: List<Int> = CalendarUtils.getInstance(context)!!.getTaskHints(
                        startDate!!.year, startDate!!.monthOfYear - 1
                    )
                    val nextHints: List<Int> = CalendarUtils.getInstance(
                        context
                    )!!.getTaskHints(
                        startDate!!.year, startDate!!.monthOfYear
                    )
                    val date = startDate!!.plusDays(i)
                    val month = date.monthOfYear
                    if (month == startMonth) {
                        drawHintCircle(hints, date.dayOfMonth, i, canvas)
                    } else {
                        drawHintCircle(nextHints, date.dayOfMonth, i, canvas)
                    }
                }
            }
        }
    }

    private fun drawHintCircle(hints: List<Int>, day: Int, col: Int, canvas: Canvas) {
        if (!hints.contains(day)) return
        val circleX = (mColumnSize * col + mColumnSize * 0.5).toFloat()
        val circleY = (mRowSize * 0.75).toFloat()
        canvas.drawCircle(circleX, circleY, mCircleRadius.toFloat(), mPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mGestureDetector!!.onTouchEvent(event)
    }

    private fun doClickAction(x: Int, y: Int) {
        if (y > height) return
        var column = x / mColumnSize
        column = Math.min(column, 6)
        val date = startDate!!.plusDays(column)
        clickThisWeek(date.year, date.monthOfYear - 1, date.dayOfMonth)
    }

    fun clickThisWeek(year: Int, month: Int, day: Int) {
        if (mOnWeekClickListener != null) {
            mOnWeekClickListener!!.onClickDate(year, month, day)
        }
        setSelectYearMonth(year, month, day)
        invalidate()
    }

    fun setOnWeekClickListener(onWeekClickListener: OnWeekClickListener?) {
        mOnWeekClickListener = onWeekClickListener
    }

    val endDate: DateTime
        get() = startDate!!.plusDays(6)

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
    fun addTaskHint(day: Int?) {
        if (mIsShowHint) {
            if (CalendarUtils.getInstance(context)!!.addTaskHint(selectYear, selectMonth, day!!)) {
                invalidate()
            }
        }
    }

    /**
     * 删除一个圆点提示
     *
     * @param day
     */
    fun removeTaskHint(day: Int?) {
        if (mIsShowHint) {
            if (CalendarUtils.getInstance(context)!!
                    .removeTaskHint(selectYear, selectMonth, day!!)
            ) {
                invalidate()
            }
        }
    }

    companion object {
        private const val NUM_COLUMNS = 7
    }

    init {
        initAttrs(array, dateTime)
        initPaint()
        initWeek()
        initGestureDetector()
    }
}