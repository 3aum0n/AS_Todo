package com.dgut.common

import android.view.View
import android.widget.AdapterView


class onItemSelectedListener(private val categoryName: CategoryName) :
    AdapterView.OnItemSelectedListener {


    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val catName = parent!!.getItemAtPosition(pos).toString()
        categoryName.spinnerCatName(catName)
    }

    interface CategoryName {
        fun spinnerCatName(categoryName: String)
        fun isCategoryAdded(isAdded: Boolean)
    }

}