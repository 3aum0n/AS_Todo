package com.dgut.common.data

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.dgut.common.model.TaskModel
import com.dgut.todo.utils.*


class DBManagerTask(val context: Context) {

    private lateinit var dbHelper: DatabaseHelper
    lateinit var database: SQLiteDatabase

    @Throws(SQLException::class)
    fun open(): DBManagerTask {
        dbHelper = DatabaseHelper(context)
        database = dbHelper.writableDatabase
        return this
    }

    private fun close() {
        dbHelper.close()
    }

    /**
     * insert value in task table
     */
    fun insert(
            title: String,
            task: String,
            category: String,
            year: String = "",
            month: String = "",
            day: String = "",
            time: String = ""
    ) {
        open()

        val contentValues = ContentValues()
        contentValues.put(TASK_TITLE, title)
        contentValues.put(TASK_TASK, task)
        contentValues.put(TASK_CATEGORY, category)
        contentValues.put(TASK_YEAR, year)
        contentValues.put(TASK_MONTH, month)
        contentValues.put(TASK_DAY, day)
        contentValues.put(TASK_TIME, time)
        contentValues.put(TASK_FINISH, TASK_IS_NOT_FINISH)


        database.insert(TABLE_TASK, null, contentValues)
        close()
    }

    /**
     * update value in task table
     */
    fun update(
            id: Int,
            title: String,
            task: String,
            category: String,
            year: String = "",
            month: String = "",
            day: String = "",
            time: String = ""
    ) {
        open()

        val contentValues = ContentValues()

        contentValues.put(TASK_TITLE, title)
        contentValues.put(TASK_TASK, task)
        contentValues.put(TASK_CATEGORY, category)
        contentValues.put(TASK_YEAR, year)
        contentValues.put(TASK_MONTH, month)
        contentValues.put(TASK_DAY, day)
        contentValues.put(TASK_TIME, time)

        database.update(TABLE_TASK, contentValues, "$ID = $id", null)
        close()
    }

    /**
     * delete row in task table
     */
    fun delete(id: Int) {
        open()
        database.delete(TABLE_TASK, "$ID=$id", null)
        close()
    }

    /**
     * get task list from Task table
     */
    fun getTaskList(): ArrayList<TaskModel> {

        open()

        val arrayList = ArrayList<TaskModel>()

        val query = "SELECT * FROM $TABLE_TASK"
        val cursor = database.rawQuery(query, null)
        if (cursor != null && cursor.moveToFirst()) {
            do {

                val isFinish =
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(TASK_FINISH)))

                if (isFinish == TASK_IS_NOT_FINISH) {

                    val taskModel = TaskModel()

                    taskModel.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ID)))
                    taskModel.title = cursor.getString(cursor.getColumnIndex(TASK_TITLE))
                    taskModel.task = cursor.getString(cursor.getColumnIndex(TASK_TASK))
                    taskModel.category = cursor.getString(cursor.getColumnIndex(TASK_CATEGORY))
                    taskModel.year = cursor.getString(cursor.getColumnIndex(TASK_YEAR))
                    taskModel.month = cursor.getString(cursor.getColumnIndex(TASK_MONTH))
                    taskModel.day = cursor.getString(cursor.getColumnIndex(TASK_DAY))
                    taskModel.time = cursor.getString(cursor.getColumnIndex(TASK_TIME))

                    arrayList.add(taskModel)

                }

            } while (cursor.moveToNext())
        }
        cursor.close()
        close()
        return arrayList
    }

    /**
     * get task list from Task table by id
     */
    fun getTaskListById(id: Int): TaskModel {

        open()

        val taskModel = TaskModel()

        val query = "SELECT * FROM $TABLE_TASK WHERE ID = $id"
        val cursor = database.rawQuery(query, null)
        if (cursor != null && cursor.moveToFirst()) {
            do {

                val isFinish =
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(TASK_FINISH)))

                if (isFinish == TASK_IS_NOT_FINISH) {

                    taskModel.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ID)))
                    taskModel.title = cursor.getString(cursor.getColumnIndex(TASK_TITLE))
                    taskModel.task = cursor.getString(cursor.getColumnIndex(TASK_TASK))
                    taskModel.category = cursor.getString(cursor.getColumnIndex(TASK_CATEGORY))
                    taskModel.year = cursor.getString(cursor.getColumnIndex(TASK_YEAR))
                    taskModel.month = cursor.getString(cursor.getColumnIndex(TASK_MONTH))
                    taskModel.day = cursor.getString(cursor.getColumnIndex(TASK_DAY))
                    taskModel.time = cursor.getString(cursor.getColumnIndex(TASK_TIME))

                }

            } while (cursor.moveToNext())
        }
        cursor.close()
        close()
        return taskModel
    }

    /**
     * Finish task
     * */
    fun finishTask(id: Int) {
        open()
        val contentValues = ContentValues()

        contentValues.put(TASK_FINISH, TASK_IS_FINISH)

        database.update(TABLE_TASK, contentValues, "$ID = $id", null)
        close()
    }

    fun getHistoryTaskList(): ArrayList<TaskModel> {
        open()

        val arrayList = ArrayList<TaskModel>()

        val query = "SELECT * FROM $TABLE_TASK"
        val cursor = database.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {

                val isFinish =
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(TASK_FINISH)))

                if (isFinish == TASK_IS_FINISH) {

                    val taskModel = TaskModel()

                    taskModel.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ID)))
                    taskModel.title = cursor.getString(cursor.getColumnIndex(TASK_TITLE))
                    taskModel.task = cursor.getString(cursor.getColumnIndex(TASK_TASK))
                    taskModel.category = cursor.getString(cursor.getColumnIndex(TASK_CATEGORY))
                    taskModel.year = cursor.getString(cursor.getColumnIndex(TASK_YEAR))
                    taskModel.month = cursor.getString(cursor.getColumnIndex(TASK_MONTH))
                    taskModel.day = cursor.getString(cursor.getColumnIndex(TASK_DAY))
                    taskModel.time = cursor.getString(cursor.getColumnIndex(TASK_TIME))

                    arrayList.add(taskModel)

                }

            } while (cursor.moveToNext())
        }
        cursor.close()
        close()
        return arrayList
    }

    fun unFinishTask(id: Int) {
        open()
        val contentValues = ContentValues()

        contentValues.put(TASK_FINISH, TASK_IS_NOT_FINISH)

        database.update(TABLE_TASK, contentValues, "$ID = $id", null)
        close()
    }

    fun searchTaskList(content: String): ArrayList<TaskModel> {
        open()

        val arrayList = ArrayList<TaskModel>()

        val query =
                "SELECT * FROM $TABLE_TASK WHERE TITLE LIKE '%$content%' OR task LIKE '%$content%' OR category LIKE '%$content%'"
        val cursor = database.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {

                val taskModel = TaskModel()

                taskModel.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ID)))
                taskModel.title = cursor.getString(cursor.getColumnIndex(TASK_TITLE))
                taskModel.task = cursor.getString(cursor.getColumnIndex(TASK_TASK))
                taskModel.category = cursor.getString(cursor.getColumnIndex(TASK_CATEGORY))
                taskModel.year = cursor.getString(cursor.getColumnIndex(TASK_YEAR))
                taskModel.month = cursor.getString(cursor.getColumnIndex(TASK_MONTH))
                taskModel.day = cursor.getString(cursor.getColumnIndex(TASK_DAY))
                taskModel.time = cursor.getString(cursor.getColumnIndex(TASK_TIME))

                arrayList.add(taskModel)

            } while (cursor.moveToNext())
        }
        cursor.close()
        close()
        return arrayList
    }

    fun getTaskHintByMonth(year: Int, month: Int): List<Int> {
        open()

        val taskHint: MutableList<Int> = java.util.ArrayList()

        val cursor = database.query(
                TABLE_TASK, arrayOf(TASK_DAY),
                java.lang.String.format(
                        "%s=? and %s=?", TASK_YEAR,
                        TASK_MONTH
                ), arrayOf(year.toString(), month.toString()), null, null, null
        )
        while (cursor.moveToNext()) {
            taskHint.add(cursor.getInt(0))
        }
        close()
        cursor.close()
        return taskHint
    }

    fun getTaskHintByWeek(
            firstYear: Int,
            firstMonth: Int,
            firstDay: Int,
            endYear: Int,
            endMonth: Int,
            endDay: Int
    ): List<Int>? {
        open()

        val taskHint: MutableList<Int> = ArrayList()
        val cursor1 = database.query(
                TABLE_TASK,
                arrayOf(TASK_DAY),
                java.lang.String.format(
                        "%s=? and %s=? and %s>=?",
                        TASK_YEAR,
                        TASK_MONTH,
                        TASK_DAY
                ),
                arrayOf(firstYear.toString(), firstMonth.toString(), firstDay.toString()),
                null,
                null,
                null
        )
        while (cursor1.moveToNext()) {
            taskHint.add(cursor1.getInt(0))
        }
        cursor1.close()
        val cursor2 = database.query(
                TABLE_TASK, arrayOf(TASK_DAY),
                java.lang.String.format(
                        "%s=? and %s=? and %s<=?",
                        TASK_YEAR,
                        TASK_MONTH,
                        TASK_DAY
                ), arrayOf(endYear.toString(), endMonth.toString(), endDay.toString()), null, null, null
        )
        while (cursor2.moveToNext()) {
            taskHint.add(cursor2.getInt(0))
        }
        cursor2.close()
        close()
        return taskHint
    }

    fun getScheduleByDate(year: String, month: String, day: String): ArrayList<TaskModel> {
        open()

        val arrayList = ArrayList<TaskModel>()

        val query =
                "SELECT * FROM $TABLE_TASK WHERE $TASK_YEAR = '$year' AND $TASK_MONTH = '$month' AND $TASK_DAY = '$day'"
        val cursor = database.rawQuery(query, null)
        if (cursor != null && cursor.moveToFirst()) {
            do {

                val isFinish =
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(TASK_FINISH)))

                if (isFinish == TASK_IS_NOT_FINISH) {

                    val taskModel = TaskModel()

                    taskModel.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ID)))
                    taskModel.title = cursor.getString(cursor.getColumnIndex(TASK_TITLE))
                    taskModel.task = cursor.getString(cursor.getColumnIndex(TASK_TASK))
                    taskModel.category = cursor.getString(cursor.getColumnIndex(TASK_CATEGORY))
                    taskModel.year = cursor.getString(cursor.getColumnIndex(TASK_YEAR))
                    taskModel.month = cursor.getString(cursor.getColumnIndex(TASK_MONTH))
                    taskModel.day = cursor.getString(cursor.getColumnIndex(TASK_DAY))
                    taskModel.time = cursor.getString(cursor.getColumnIndex(TASK_TIME))

                    arrayList.add(taskModel)

                }

            } while (cursor.moveToNext())
        }
        cursor.close()
        close()
        return arrayList
    }
}