package com.dgut.todo.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.os.Bundle
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
import android.widget.Toast
import com.dgut.todo.R
import com.dgut.todo.adapter.TaskAdapter
import com.dgut.common.data.DBManagerTask
import com.dgut.common.model.TaskModel
import com.dgut.todo.activity.UpdateTaskActivity
import com.dgut.todo.utils.DASHBOARD_RECYCLEVIEW_REFRESH
import com.dgut.todo.utils.TASK_IS_FINISH
import com.dgut.todo.utils.getFormatDate
import com.dgut.todo.utils.getFormatTime
import com.dgut.todo.utils.views.recyclerview.itemclick.RecyclerItemClickListener
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import java.util.*


class SearchFragment : Fragment(), View.OnClickListener {

    val TAG: String = SearchFragment::class.java.simpleName

    private lateinit var txtNoHistory: TextView
    private lateinit var recyclerViewSearch: RecyclerView
    private var searchContent = ""

    var mArrayList: ArrayList<TaskModel> = ArrayList()
    private lateinit var dbManager: DBManagerTask
    lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater!!.inflate(R.layout.fragment_search, container, false)

        initialize(view)

        return view
    }

    override fun onResume() {
        super.onResume()
        isTaskListEmpty()
    }

    private fun initialize(view: View) {

        txtNoHistory = view.txtNoTask
        recyclerViewSearch = view.recyclerViewSearch

        recyclerViewSearch.setHasFixedSize(true)
        recyclerViewSearch.layoutManager =
            LinearLayoutManager(activity!!) as RecyclerView.LayoutManager

        dbManager = DBManagerTask(activity)
        taskAdapter = TaskAdapter(activity, mArrayList)

        view.imgSearch.setOnClickListener(this)
    }

    private fun clickForDetails(holder: TaskAdapter.ViewHolder, position: Int) {

        val taskList = taskAdapter.getList()

        if (holder.textTitle.visibility == View.GONE && holder.textTask.visibility == View.GONE) {

            holder.textTitle.visibility = View.VISIBLE
            holder.textTask.visibility = View.VISIBLE
            holder.txtShowTitle.maxLines = Integer.MAX_VALUE
            holder.txtShowTask.maxLines = Integer.MAX_VALUE

//            if (taskList[position].date != "") {
            if (taskList[position].year != "") {
//                holder.txtShowDate.text = getFormatDate(taskList[position].date!!)
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

    private fun initSwipe() {

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
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
                    val taskList = taskAdapter.getList()
                    if(taskList[position].finish!!.toInt() == TASK_IS_FINISH) {
                        taskAdapter.unFinishTask(position)
                    }else {
                        taskAdapter.finishTask(position)
                    }
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

                if (actionState === ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Get RecyclerView item from the ViewHolder
                    val itemView = viewHolder.itemView

                    val p = Paint()
                    val icon: Bitmap

                    if (dX > 0) {
                        /* Note, ApplicationManager is a helper class I created
                            myself to get a context outside an Activity class -
                            feel free to use your own method */

                        val position = viewHolder.adapterPosition
                        val taskList = taskAdapter.getList()
                        if(position != -1 && taskList[position].finish!!.toInt() == TASK_IS_FINISH) {
                            icon = BitmapFactory.decodeResource(
                                    resources, R.mipmap.ic_unfinish
                            )
                        }else {
                            icon = BitmapFactory.decodeResource(
                                    resources, R.mipmap.ic_check_white_png
                            )
                        }

                        /* Set your color for positive displacement */
                        p.color = Color.parseColor(getString(R.color.green))

                        // Draw Rect with varying right side, equal to displacement dX
                        canvas.drawRect(
                            itemView.left.toFloat(), itemView.top.toFloat(),
                            itemView.left.toFloat() + dX, itemView.bottom.toFloat(), p
                        )

                        // Set the image icon for Right swipe
                        canvas.drawBitmap(
                            icon,
                            itemView.left.toFloat() + convertDpToPx(16),
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            p
                        )
                    } else {
                        icon = BitmapFactory.decodeResource(
                            resources, R.mipmap.ic_delete_white_png
                        )

                        /* Set your color for negative displacement */
                        p.color = Color.parseColor(getString(R.color.red))


                        // Draw Rect with varying left side, equal to the item's right side
                        // plus negative displacement dX
                        canvas.drawRect(
                            itemView.right.toFloat() + dX, itemView.top.toFloat(),
                            itemView.right.toFloat(), itemView.bottom.toFloat(), p
                        )

                        //Set the image icon for Left swipe
                        canvas.drawBitmap(
                            icon,
                            itemView.right.toFloat() - convertDpToPx(16) - icon.width,
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            p
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
        itemTouchHelper.attachToRecyclerView(recyclerViewSearch)
    }

    private fun convertDpToPx(dp: Int): Int {
        return Math.round(dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun isTaskListEmpty() {
        if (taskAdapter.itemCount == 0) {
            txtNoHistory.visibility = View.VISIBLE
        } else {
            txtNoHistory.visibility = View.GONE
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.imgSearch -> {
                searchContent = search.text.toString().trim()
                if (searchContent == "") {
                    Toast.makeText(context, "Please input something", Toast.LENGTH_SHORT).show()
                    return
                }
                mArrayList = dbManager.searchTaskList(searchContent)
                if(mArrayList.size == 0){
                    Toast.makeText(context,"Nothing",Toast.LENGTH_SHORT).show()
                }
                taskAdapter = TaskAdapter(activity, mArrayList)
                isTaskListEmpty()
                recyclerViewSearch.adapter = taskAdapter

                initSwipe()

                recyclerViewSearch.addOnItemTouchListener(
                    RecyclerItemClickListener(
                        context,
                        recyclerViewSearch,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View, position: Int) {
                                Log.e(TAG, "item click Position : $position")

                                val holder: TaskAdapter.ViewHolder = TaskAdapter.ViewHolder(view)

                                clickForDetails(holder, position)
                            }

                            override fun onLongItemClick(view: View, position: Int) {
                                Log.e(TAG, "item long click Position : $position")
                                val taskList = taskAdapter.getList()
                                if(taskList[position].finish!!.toInt() == TASK_IS_FINISH){
                                    Toast.makeText(context,"history can't be updated",Toast.LENGTH_SHORT).show()
                                }else {
                                    longClickForUpdate(position)
                                }
                            }
                        })
                )
            }

        }
    }

    private fun longClickForUpdate(position: Int) {
        val taskList = taskAdapter.getList()
        val intent = Intent(activity, UpdateTaskActivity::class.java)
        intent.putExtra("id", taskList[position].id)
        startActivityForResult(
                intent,
                DASHBOARD_RECYCLEVIEW_REFRESH
        )
    }
}