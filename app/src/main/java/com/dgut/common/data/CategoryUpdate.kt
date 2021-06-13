package com.dgut.common.data

import com.dgut.common.model.CategoryModel


interface CategoryUpdate {
    fun isCategoryUpdated(isUpdated: Boolean, mArrayList: ArrayList<CategoryModel>)
}