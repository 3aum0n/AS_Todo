package com.dgut.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import com.dgut.todo.R


class WeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    private var mWeekTextColor = 0
    private var mWeekSize = 0
    private var mPaint: Paint? = null
    private var mDisplayMetrics: DisplayMetrics? = null
    private lateinit var mWeekString: Array<String>
    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.WeekBarView)
        mWeekTextColor =
            array.getColor(R.styleable.WeekBarView_week_text_color, Color.parseColor("#4588E3"))
        mWeekSize = array.getInteger(R.styleable.WeekBarView_week_text_size, 13)
        mWeekString = context.resources.getStringArray(R.array.calendar_week)
        array.recycle()
    }

    private fun initPaint() {
        mDisplayMetrics = resources.displayMetrics
        val displayMetrics = mDisplayMetrics
        mPaint = Paint()
        mPaint!!.color = mWeekTextColor
        mPaint!!.isAntiAlias = true
        mPaint!!.textSize = mWeekSize * displayMetrics!!.scaledDensity
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = mDisplayMetrics!!.densityDpi * 30
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = mDisplayMetrics!!.densityDpi * 300
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        val width = width
        val height = height
        val columnWidth = width / 7
        for (i in mWeekString.indices) {

            val text = mWeekString[i]
            val fontWidth = mPaint!!.measureText(text).toInt()
            val startX = columnWidth * i + (columnWidth - fontWidth) / 2
            val startY = (height / 2 - (mPaint!!.ascent() + mPaint!!.descent()) / 2).toInt()
            canvas.drawText(text, startX.toFloat(), startY.toFloat(), mPaint)
        }
    }

    init {
        initAttrs(context, attrs)
        initPaint()
    }
}