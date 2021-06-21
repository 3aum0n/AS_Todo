package com.dgut.calendar

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class CalendarUtils {
    private var sAllHolidays: Map<String, IntArray>? = HashMap()
    private val sMonthTaskHint: MutableMap<String, MutableList<Int>> = HashMap()
    private fun initAllHolidays(context: Context) {
        try {
            val `is` = context.assets.open("holiday.json")
            val baos = ByteArrayOutputStream()
            var i = 0
            while (`is`.read().also { i = it } != -1) {
                baos.write(i)
            }
            sAllHolidays = Gson().fromJson<Map<String, IntArray>>(
                baos.toString(),
                object : TypeToken<Map<String?, Any>?>() {}.type
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun addTaskHints(year: Int, month: Int, days: List<Int?>?): List<Int> {
        val key = hashKey(year, month)
        var hints = sUtils!!.sMonthTaskHint[key]
        if (hints == null) {
            hints = ArrayList()
            hints.removeAll(days!!) // 避免重复
            hints.addAll(days)
            sUtils!!.sMonthTaskHint[key] = hints
        } else {
            hints.addAll(days!!)
        }
        return hints
    }

    fun removeTaskHints(year: Int, month: Int, days: List<Int?>?): List<Int> {
        val key = hashKey(year, month)
        var hints = sUtils!!.sMonthTaskHint[key]
        if (hints == null) {
            hints = ArrayList()
            sUtils!!.sMonthTaskHint[key] = hints
        } else {
            hints.removeAll(days!!)
        }
        return hints
    }

    fun addTaskHint(year: Int, month: Int, day: Int): Boolean {
        val key = hashKey(year, month)
        var hints = sUtils!!.sMonthTaskHint[key]
        return if (hints == null) {
            hints = ArrayList()
            hints.add(day)
            sUtils!!.sMonthTaskHint[key] = hints
            true
        } else {
            if (!hints.contains(day)) {
                hints.add(day)
                true
            } else {
                false
            }
        }
    }

    fun removeTaskHint(year: Int, month: Int, day: Int): Boolean {
        val key = hashKey(year, month)
        var hints = sUtils!!.sMonthTaskHint[key]
        if (hints == null) {
            hints = ArrayList()
            sUtils!!.sMonthTaskHint[key] = hints
        } else {
            return if (hints.contains(day)) {
                val i = hints.iterator()
                while (i.hasNext()) {
                    val next = i.next()
                    if (next == day) {
                        i.remove()
                        break
                    }
                }
                true
            } else {
                false
            }
        }
        return false
    }

    fun getTaskHints(year: Int, month: Int): List<Int> {
        val key = hashKey(year, month)
        var hints = sUtils!!.sMonthTaskHint[key]
        if (hints == null) {
            hints = ArrayList()
            sUtils!!.sMonthTaskHint[key] = hints
        }
        return hints
    }

    fun getHolidays(year: Int, month: Int): IntArray {
        var holidays: IntArray?
        if (sUtils!!.sAllHolidays != null) {
            // TODO 假期角标以及上班角标的绘制
//            holidays = sUtils!!.sAllHolidays!!.get(year.toString() + "" + month)
//            if (holidays == null) {
//                holidays = IntArray(42)
//            }
            holidays = IntArray(42)
        } else {
            holidays = IntArray(42)
        }
        return holidays
    }

    companion object {
        private var sUtils: CalendarUtils? = null

        @Synchronized
        fun getInstance(context: Context): CalendarUtils? {
            if (sUtils == null) {
                sUtils = CalendarUtils()
                sUtils!!.initAllHolidays(context)
            }
            return sUtils
        }

        private fun hashKey(year: Int, month: Int): String {
            return String.format("%s:%s", year, month)
        }

        /**
         * 通过年份和月份 得到当月的日子
         *
         * @param year
         * @param month
         * @return
         */
        fun getMonthDays(year: Int, month: Int): Int {
            var month = month
            month++
            return when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                    29
                } else {
                    28
                }
                else -> -1
            }
        }

        /**
         * 返回当前月份1号位于周几
         *
         * @param year  年份
         * @param month 月份，传入系统获取的，不需要正常的
         * @return 日：1		一：2		二：3		三：4		四：5		五：6		六：7
         */
        fun getFirstDayWeek(year: Int, month: Int): Int {
            val calendar = Calendar.getInstance()
            calendar[year, month] = 1
            return calendar[Calendar.DAY_OF_WEEK]
        }

        /**
         * 获得两个日期距离几周
         *
         * @return
         */
        fun getWeeksAgo(
            lastYear: Int,
            lastMonth: Int,
            lastDay: Int,
            year: Int,
            month: Int,
            day: Int
        ): Int {
            val start = Calendar.getInstance()
            val end = Calendar.getInstance()
            start[lastYear, lastMonth] = lastDay
            end[year, month] = day
            var week = start[Calendar.DAY_OF_WEEK]
            start.add(Calendar.DATE, -week)
            week = end[Calendar.DAY_OF_WEEK]
            end.add(Calendar.DATE, 7 - week)
            val v = (end.timeInMillis - start.timeInMillis) / (3600 * 1000 * 24 * 7 * 1.0f)
            return (v - 1).toInt()
        }

        /**
         * 获得两个日期距离几个月
         *
         * @return
         */
        fun getMonthsAgo(lastYear: Int, lastMonth: Int, year: Int, month: Int): Int {
            return (year - lastYear) * 12 + (month - lastMonth)
        }

        fun getWeekRow(year: Int, month: Int, day: Int): Int {
            var day = day
            val week = getFirstDayWeek(year, month)
            val calendar = Calendar.getInstance()
            calendar[year, month] = day
            val lastWeek = calendar[Calendar.DAY_OF_WEEK]
            if (lastWeek == 7) day--
            return (day + week - 1) / 7
        }

        /**
         * 根据国历获取假期
         *
         * @return
         */
        fun getHolidayFromSolar(year: Int, month: Int, day: Int): String {
            var message = ""
            if (month == 0 && day == 1) {
                message = "元旦"
            } else if (month == 1 && day == 14) {
                message = "情人节"
            } else if (month == 2 && day == 8) {
                message = "妇女节"
            } else if (month == 2 && day == 12) {
                message = "植树节"
            } else if (month == 3) {
                if (day == 1) {
                    message = "愚人节"
                } else if (day >= 4 && day <= 6) {
                    if (year <= 1999) {
                        val compare = ((year - 1900) * 0.2422 + 5.59 - (year - 1900) / 4).toInt()
                        if (compare == day) {
                            message = "清明节"
                        }
                    } else {
                        val compare = ((year - 2000) * 0.2422 + 4.81 - (year - 2000) / 4).toInt()
                        if (compare == day) {
                            message = "清明节"
                        }
                    }
                }
            } else if (month == 4 && day == 1) {
                message = "劳动节"
            } else if (month == 4 && day == 4) {
                message = "青年节"
            } else if (month == 4 && day == 12) {
                message = "护士节"
            } else if (month == 5 && day == 1) {
                message = "儿童节"
            } else if (month == 6 && day == 1) {
                message = "建党节"
            } else if (month == 7 && day == 1) {
                message = "建军节"
            } else if (month == 8 && day == 10) {
                message = "教师节"
            } else if (month == 9 && day == 1) {
                message = "国庆节"
            } else if (month == 10 && day == 11) {
                message = "光棍节"
            } else if (month == 11 && day == 25) {
                message = "圣诞节"
            }
            return message
        }

        fun getMonthRows(year: Int, month: Int): Int {
            val size = getFirstDayWeek(year, month) + getMonthDays(year, month) - 1
            return if (size % 7 == 0) size / 7 else size / 7 + 1
        }
    }

    private fun <E> List<E>?.addAll(elements: List<E?>) {

    }
}


