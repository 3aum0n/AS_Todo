package com.dgut.todo.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dgut.calendar.OnCalendarClickListener
import com.dgut.calendar.schedule.ScheduleLayout
import com.dgut.calendar.schedule.ScheduleRecyclerView
import com.dgut.common.data.DBManagerTask
import com.dgut.common.model.TaskModel
import com.dgut.todo.R
import com.dgut.todo.activity.AddTaskActivity
import com.dgut.todo.activity.MainActivity
import com.dgut.todo.activity.UpdateTaskActivity
import com.dgut.todo.adapter.TaskAdapter
import com.dgut.todo.utils.DASHBOARD_RECYCLEVIEW_REFRESH
import com.dgut.todo.utils.getFormatDate
import com.dgut.todo.utils.getFormatTime
import com.dgut.todo.utils.views.recyclerview.itemclick.RecyclerItemClickListener
import com.dgut.todo.utils.views.recyclerview.itemdrag.OnStartDragListener
import kotlinx.android.synthetic.main.fragment_dashboard.view.fabAddTask
import kotlinx.android.synthetic.main.fragment_schedule.view.*
import java.util.*
import kotlin.collections.ArrayList


class ScheduleFragment : Fragment(), View.OnClickListener, OnStartDragListener,
        OnCalendarClickListener {

    val TAG: String = ScheduleFragment::class.java.simpleName

    private lateinit var slSchedule: ScheduleLayout
    private var rvScheduleList: ScheduleRecyclerView? = null
    private lateinit var recyclerViewTask: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var rlNoTask: TextView

    var mArrayList: ArrayList<TaskModel> = ArrayList()
    private lateinit var dbManager: DBManagerTask
    lateinit var taskAdapter: TaskAdapter

    private lateinit var mItemTouchHelper: ItemTouchHelper

    var calendar: Calendar = Calendar.getInstance()
    private var mCurrentSelectYear = calendar[Calendar.YEAR]
    private var mCurrentSelectMonth = calendar[Calendar.MONTH]
    private var mCurrentSelectDay = calendar[Calendar.DAY_OF_MONTH]

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        var view = inflater!!.inflate(R.layout.fragment_schedule, container, false)

        initialize(view)

        return view
    }

    private fun initialize(view: View) {

        fabAddTask = view.fabAddTask
        rlNoTask = view.rlNoTask
        slSchedule = view.slSchedule
        recyclerViewTask = view.rvScheduleList

        recyclerViewTask.setHasFixedSize(true)
        recyclerViewTask.layoutManager =
                LinearLayoutManager(activity!!)

        fabAddTask.setOnClickListener(this)
        slSchedule.setOnCalendarClickListener(this)

        dbManager = DBManagerTask(activity)
        mArrayList =
                dbManager.getScheduleByDate(
                        calendar[Calendar.YEAR].toString(),
                        String.format("%02d", (calendar[Calendar.MONTH] + 1)),
                        String.format("%02d", calendar[Calendar.DAY_OF_MONTH])
                )

        taskAdapter = TaskAdapter(activity, mArrayList)
        recyclerViewTask.adapter = taskAdapter

        initSwipe()

        recyclerViewTask.addOnItemTouchListener(
                RecyclerItemClickListener(
                        context,
                        recyclerViewTask,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View, position: Int) {
                                Log.e(TAG, "item click Position : $position")

                                val holder: TaskAdapter.ViewHolder = TaskAdapter.ViewHolder(view)

                                clickForDetails(holder, position)
                            }

                            override fun onLongItemClick(view: View, position: Int) {
                                longClickForUpdate(position)
                            }
                        })
        )

    }

    override fun onClickDate(year: Int, month: Int, day: Int) {
        setCurrentSelectDate(year, month, day)
        isTaskListEmpty()
    }

    override fun onPageChange(year: Int, month: Int, day: Int) {}

    private fun setCurrentSelectDate(year: Int, month: Int, day: Int) {
        mCurrentSelectYear = year
        mCurrentSelectMonth = month
        mCurrentSelectDay = day

        mArrayList =
                dbManager.getScheduleByDate(
                        mCurrentSelectYear.toString(),
                        String.format("%02d", mCurrentSelectMonth + 1),
                        String.format("%02d", mCurrentSelectDay)
                )

        taskAdapter.clearAdapter()
        taskAdapter.setList(mArrayList)

        (activity as MainActivity).resetMainTitleDate(year, month, day)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper.startDrag(viewHolder)
    }

    private fun clickForDetails(holder: TaskAdapter.ViewHolder, position: Int) {

        val taskList = taskAdapter.getList()

        if (holder.textTitle.visibility == View.GONE && holder.textTask.visibility == View.GONE) {

            holder.textTitle.visibility = View.VISIBLE
            holder.textTask.visibility = View.VISIBLE
            holder.txtShowTitle.maxLines = Integer.MAX_VALUE
            holder.txtShowTask.maxLines = Integer.MAX_VALUE

            if (taskList[position].year != "") {
                holder.txtShowDate.text =
                        getFormatDate(taskList[position].year!! + "-" + taskList[position].month!! + "-" + taskList[position].day!!)
                holder.textDate.visibility = View.VISIBLE
                holder.txtShowDate.visibility = View.VISIBLE
            }

            if (taskList[position].time != "") {
                holder.txtShowTime.text = getFormatTime(taskList[position].time!!)
                holder.textTime.visibility = View.VISIBLE
                holder.txtShowTime.visibility = View.VISIBLE
            }

        } else {

            holder.textTitle.visibility = View.GONE
            holder.textTask.visibility = View.GONE
            holder.txtShowTask.maxLines = 1
            holder.txtShowTitle.maxLines = 1

            if (taskList[position].year != "") {
                holder.textDate.visibility = View.GONE
                holder.txtShowDate.visibility = View.GONE
            }

            if (taskList[position].time != "") {
                holder.textTime.visibility = View.GONE
                holder.txtShowTime.visibility = View.GONE
            }

        }
    }

    private fun longClickForUpdate(position: Int) {
        Log.e(TAG, "item long click Position : $position")
        val taskList = taskAdapter.getList()
        val intent = Intent(activity, UpdateTaskActivity::class.java)
        intent.putExtra("id", taskList[position].id)
        startActivityForResult(
                intent,
                DASHBOARD_RECYCLEVIEW_REFRESH
        )
    }

    override fun onResume() {
        super.onResume()
        isTaskListEmpty()
    }

    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.fabAddTask -> {
                val intent = Intent(activity, AddTaskActivity::class.java)
                intent.putExtra("year", mCurrentSelectYear)
                intent.putExtra("month", mCurrentSelectMonth)
                intent.putExtra("day", mCurrentSelectDay)
                startActivityForResult(
                        intent,
                        DASHBOARD_RECYCLEVIEW_REFRESH
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                DASHBOARD_RECYCLEVIEW_REFRESH -> {
                    mArrayList =
                            dbManager.getScheduleByDate(
                                    mCurrentSelectYear.toString(),
                                    String.format("%02d", mCurrentSelectMonth + 1),
                                    String.format("%02d", mCurrentSelectDay)
                            )
                    taskAdapter.clearAdapter()
                    taskAdapter.setList(mArrayList)
                }
            }
        }
    }

    private fun initSwipe() {

        val simpleItemTouchCallback = object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (direction == ItemTouchHelper.LEFT) {
                    taskAdapter.deleteTask(position)
                    isTaskListEmpty()
                } else {
                    taskAdapter.finishTask(position)
                    isTaskListEmpty()
                }
            }

            @SuppressLint("ResourceType")
            override fun onChildDraw(
                    canvas: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
            ) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    val itemView = viewHolder.itemView

                    val paint = Paint()
                    val iconBitmap: Bitmap

                    if (dX > 0) {

                        iconBitmap =
                                BitmapFactory.decodeResource(resources, R.mipmap.ic_check_white_png)

                        paint.color = Color.parseColor(getString(R.color.green))

                        canvas.drawRect(
                                itemView.left.toFloat(), itemView.top.toFloat(),
                                itemView.left.toFloat() + dX, itemView.bottom.toFloat(), paint
                        )

                        // Set the image icon for Right side swipe
                        canvas.drawBitmap(
                                iconBitmap,
                                itemView.left.toFloat() + convertDpToPx(16),
                                itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - iconBitmap.height.toFloat()) / 2,
                                paint
                        )
                    } else {

                        iconBitmap =
                                BitmapFactory.decodeResource(resources, R.mipmap.ic_delete_white_png)

                        paint.color = Color.parseColor(getString(R.color.red))

                        canvas.drawRect(
                                itemView.right.toFloat() + dX, itemView.top.toFloat(),
                                itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                        )

                        //Set the image icon for Left side swipe
                        canvas.drawBitmap(
                                iconBitmap,
                                itemView.right.toFloat() - convertDpToPx(16) - iconBitmap.width,
                                itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - iconBitmap.height.toFloat()) / 2,
                                paint
                        )
                    }

                    val ALPHA_FULL: Float = 1.0f

                    // Fade out the view as it is swiped out of the parent's bounds
                    val alpha: Float =
                            ALPHA_FULL - Math.abs(dX) / viewHolder.itemView.width.toFloat()
                    viewHolder.itemView.alpha = alpha
                    viewHolder.itemView.translationX = dX

                } else {
                    super.onChildDraw(
                            canvas,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                    )
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerViewTask)
    }

    private fun convertDpToPx(dp: Int): Int {
        return Math.round(dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun isTaskListEmpty() {
        if (taskAdapter.itemCount == 0) {
            rlNoTask.visibility = View.VISIBLE
        } else {
            rlNoTask.visibility = View.GONE
        }
    }

}
