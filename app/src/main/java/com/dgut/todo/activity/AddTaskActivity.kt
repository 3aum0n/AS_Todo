package com.dgut.todo.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import com.dgut.common.data.CategoryAdd
import com.dgut.common.data.DBManagerCategory
import com.dgut.common.data.DBManagerTask
import com.dgut.common.onItemSelectedListener
import com.dgut.todo.R
import com.dgut.todo.utils.dialogAddCategory
import com.dgut.todo.utils.toastMessage
import com.dgut.todo.widget.AlarmReceiver
import kotlinx.android.synthetic.main.activity_add_task.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddTaskActivity : AppCompatActivity(), View.OnClickListener, CategoryAdd,
    onItemSelectedListener.CategoryName {

    val TAG: String = MainActivity::class.java.simpleName

    val mActivity: Activity = this@AddTaskActivity

    var myCalendar = Calendar.getInstance()

    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener

    //Final variable to save in database
    private var finalYear = ""
    private var finalMonth = ""
    private var finalDay = ""
    private var finalTime = ""
    private var finalTitle = ""
    private var finalTask = ""
    private var finalCategoryName = ""

    private var SelectYear: Int = 0
    private var SelectMonth: Int = 0
    private var SelectDay: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val variables = intent
        SelectYear = variables.getIntExtra("year", myCalendar[Calendar.YEAR])
        SelectMonth = variables.getIntExtra("month", myCalendar[Calendar.MONTH])
        SelectDay = variables.getIntExtra("day", myCalendar[Calendar.DAY_OF_MONTH])

        initialize()
    }


    /**
     * initializing views and data
     * */
    private fun initialize() {

        setSupportActionBar(toolbarAddTask)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        /**
         * click listener
         * */
        edtSetDate.setOnClickListener(this)
        edtSetTime.setOnClickListener(this)
        imgCancelDate.setOnClickListener(this)
        imgCancelTime.setOnClickListener(this)
        imgAddCategory.setOnClickListener(this)

        myCalendar.set(Calendar.YEAR, SelectYear)
        myCalendar.set(Calendar.MONTH, SelectMonth)
        myCalendar.set(Calendar.DAY_OF_MONTH, SelectDay)
        updateLabelDate()

        /**
         * load category in spinner
         * */
        loadDataInSpinner()
    }

    /**
     * action bar back button click
     * */
    override fun onSupportNavigateUp(): Boolean {
        checkTask()
        return super.onSupportNavigateUp()
    }

    /**
     * inflating actionbar menu
     * */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_task, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * actionbar clicks
     * */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.action_done -> {
                addTask()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        checkTask()
    }

    /**
     * on leaving this screen this method will check
     * weather user have enter any task or not If YES
     * it will show dialog else finish()
     * */
    private fun checkTask() {

        finalTitle = edtTitle.text.toString().trim()
        finalTask = edtTask.text.toString().trim()

        if (finalTitle != "" && finalTask != "") {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(mActivity)

            alertDialog.setTitle(getString(R.string.save_task))
            alertDialog.setMessage(getString(R.string.do_you_want_to_save_this_task))

            alertDialog.setPositiveButton(R.string.save) { _, _ ->
                addTask()
            }

            alertDialog.setNegativeButton(R.string.cancel) { _, _ ->
                finish()
            }

            val alert: AlertDialog = alertDialog.create()
            alert.show()
        } else {
            finish()
        }

    }

    /**
     * Add Task in database
     * */
    private fun addTask() {

        finalTitle = edtTitle.text.toString().trim()
        finalTask = edtTask.text.toString().trim()

        val dbManager = DBManagerTask(mActivity)

        if (finalTitle != "") {
            if (finalTask != "") {
                if (finalYear != "" && finalMonth != "" && finalDay != "") {
                    if (finalTime != "") {

                        //if time enter
                        Log.e(
                            TAG, "Title : " + finalTitle +
                                    "\nTask : " + finalTask +
                                    "\nYear: " + finalYear +
                                    "\nMonth : " + finalMonth +
                                    "\nDay : " + finalDay +
                                    "\nTime : " + finalTime +
                                    "\nCategory : " + finalCategoryName
                        )
                        dbManager.insert(
                            finalTitle,
                            finalTask,
                            finalCategoryName,
                            finalYear,
                            finalMonth,
                            finalDay,
                            finalTime
                        )

//                        myCalendar.set(Calendar.SECOND, 0)
//                        setNotification(myCalendar) // Set notification

                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {

                        //if only date enter
                        Log.e(
                            TAG, "Title : " + finalTitle +
                                    "\nTask : " + finalTask +
                                    "\nYear: " + finalYear +
                                    "\nMonth : " + finalMonth +
                                    "\nDay : " + finalDay +
                                    "\nCategory : " + finalCategoryName
                        )

                        dbManager.insert(
                            finalTitle,
                            finalTask,
                            finalCategoryName,
                            finalYear,
                            finalMonth,
                            finalDay
                        )

//                        myCalendar.set(Calendar.SECOND, 0)
//                        setNotification(myCalendar) // Set notification

                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                } else {

                    //if date not enter
                    Log.e(
                        TAG, "Title : " + finalTitle +
                                "Task : " + finalTask +
                                "Category : " + finalCategoryName
                    )

                    dbManager.insert(finalTitle, finalTask, finalCategoryName)
                    setResult(Activity.RESULT_OK)
                    finish()

                }

            } else {
                toastMessage(mActivity, getString(R.string.please_add_task))
            }
        } else {
            toastMessage(mActivity, getString(R.string.please_add_title))
        }
    }

    private fun setNotification(myCalendar: Calendar) {

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

//        val taskRowId = Integer.parseInt(id)
        val intent = Intent(this, AlarmReceiver::class.java)
//        intent.putExtra(Constant.TASK_ID, taskRowId)
//        intent.putExtra(Constant.TASK_TITLE, title)
//        intent.putExtra(Constant.TASK_TASK, task)

        val pendingIntent =
            PendingIntent.getBroadcast(this, 0/*taskRowId*/, intent, PendingIntent.FLAG_ONE_SHOT)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            myCalendar.timeInMillis/*timeInMilis*/,
            pendingIntent
        )

    }

    /**
     * load category in spinner
     * */
    private fun loadDataInSpinner() {

        val dbManager = DBManagerCategory(mActivity)
        var labels = dbManager.getListOfCategory()

        if (labels.isEmpty()) {
            val arrayList: ArrayList<String> = ArrayList()
            arrayList.add("No category added")
            labels = arrayList
        }

        val dataAdapter =
            ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, labels)

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        Collections.sort(labels)

        spinnerCategory.adapter = dataAdapter

        spinnerCategory.onItemSelectedListener = onItemSelectedListener(this)
    }

    override fun spinnerCatName(categoryName: String) {
        if (categoryName != "No category added") {
            if (categoryName != "") {
                finalCategoryName = categoryName
            }
        }

    }

    /**
     * Views clicks
     * */
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.edtSetDate -> {
                dateAndTime()
                setDate()
            }
            R.id.edtSetTime -> {
                dateAndTime()
                setTime()
            }
            R.id.imgCancelDate -> {
                edtSetDate.setText("")
                finalYear = ""
                finalMonth = ""
                finalDay = ""
                imgCancelDate.visibility = View.GONE
                if (relativeLayoutTime.visibility == View.VISIBLE) {
                    relativeLayoutTime.visibility = View.GONE
                    edtSetTime.setText("")
                    finalTime = ""
                    imgCancelTime.visibility = View.GONE
                }

            }
            R.id.imgCancelTime -> {
                edtSetTime.setText("")
                finalTime = ""
                imgCancelTime.visibility = View.GONE
            }
            R.id.imgAddCategory -> {
                dialogAddCategory(mActivity, this)
            }
        }
    }

    /**
     * current Date and Time initialize
     * */
    private fun dateAndTime() {

        myCalendar = Calendar.getInstance()

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabelDate()
        }

        timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            myCalendar.set(Calendar.MINUTE, minute)
            updateLabelTime()
        }

    }

    /**
     * @DatePickerDialog for selecting date
     * */
    private fun setDate() {

        val datePickerDialog = DatePickerDialog(
            this, dateSetListener, myCalendar.get(Calendar.YEAR),
            myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)
        )
        // Limit date selection range
//        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()

    }


    /**
     * @TimePickerDialog for selecting time
     * */
    private fun setTime() {
        val timePickerDialog = TimePickerDialog(
            this, timeSetListener, myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE), false
        )
        timePickerDialog.show()
    }

    /**
     * UI Update of time
     * */
    private fun updateLabelTime() {

        val myFormat = "HH:mm"  // HH:mm:ss
        val sdf = SimpleDateFormat(myFormat, Locale.CHINA)

        finalTime = sdf.format(myCalendar.time)


        val myFormat2 = "h:mm a"
        val sdf2 = SimpleDateFormat(myFormat2, Locale.CHINA)
        edtSetTime.setText(sdf2.format(myCalendar.time))

        imgCancelTime.visibility = View.VISIBLE
    }


    /**
     * UI Update of time
     * */
    private fun updateLabelDate() {

        val myFormat = "yyyy-MM-dd"
        val sdf =
            SimpleDateFormat(myFormat, Locale.CHINA).format(myCalendar.time).toString().split("-")

        finalYear = sdf[0]
        finalMonth = sdf[1]
        finalDay = sdf[2]

        val myFormat2 = "EEE, d MMM yyyy"
        val sdf2 = SimpleDateFormat(myFormat2, Locale.CHINA)
        edtSetDate.setText(sdf2.format(myCalendar.time))

        relativeLayoutTime.visibility = View.VISIBLE
        imgCancelDate.visibility = View.VISIBLE
    }

    override fun isCategoryAdded(isAdded: Boolean) {
        if (isAdded) {
            loadDataInSpinner()
        }
    }

}


