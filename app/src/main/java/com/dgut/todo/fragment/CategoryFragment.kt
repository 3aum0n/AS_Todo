package com.dgut.todo.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dgut.common.data.CategoryAdd
import com.dgut.common.data.CategoryIsEmpty
import com.dgut.common.model.CategoryModel
import com.dgut.todo.R
import com.dgut.todo.adapter.CategoryAdapter
import com.dgut.common.data.DBManagerCategory
import com.dgut.todo.utils.dialogAddCategory
import kotlinx.android.synthetic.main.fragment_category.view.*
import java.util.*


class CategoryFragment : Fragment(), View.OnClickListener, CategoryAdd, CategoryIsEmpty {

    private val TAG: String = CategoryFragment::class.java.simpleName

    private lateinit var fabAddCategory: FloatingActionButton
    private lateinit var recyclerViewCategory: RecyclerView
    private lateinit var txtNoCategory: TextView

    var mArrayList: ArrayList<CategoryModel> = ArrayList()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater!!.inflate(R.layout.fragment_category, container, false)

        initialize(view)

        return view
    }

    /**
     * initializing views and data
     * */
    private fun initialize(view: View) {

        fabAddCategory = view.fabAddCategory
        recyclerViewCategory = view.recyclerViewCategory
        txtNoCategory = view.txtNoCategory

        recyclerViewCategory.setHasFixedSize(true)
        recyclerViewCategory.layoutManager =
            LinearLayoutManager(activity!!) as RecyclerView.LayoutManager

        fabAddCategory.setOnClickListener(this)

        val dbManageCategory = DBManagerCategory(activity)
        mArrayList = dbManageCategory.getCategoryList()

        categoryAdapter = CategoryAdapter(activity, mArrayList, this)
        recyclerViewCategory.adapter = categoryAdapter
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "Resume")
    }

    /**
     * Views clicks
     * */
    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.fabAddCategory -> {
                dialogAddCategory(activity, this)
            }
        }
    }

    /**
     * If new category is added
     * then RecycleView will update
     *
     * @Boolean is category added or not
     * */
    override fun isCategoryAdded(isAdded: Boolean) {
        if (isAdded) {

            Log.e(TAG, "true : $isAdded")

            val dbManageCategory = DBManagerCategory(activity)
            mArrayList = dbManageCategory.getCategoryList()

            categoryAdapter.clearAdapter()
            categoryAdapter.setList(mArrayList)
        }
    }

    override fun categoryIsEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            txtNoCategory.visibility = View.VISIBLE
        } else {
            txtNoCategory.visibility = View.GONE
        }
    }
}
