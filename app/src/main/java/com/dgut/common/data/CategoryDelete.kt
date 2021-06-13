package com.dgut.common.data

import com.dgut.common.model.CategoryModel


interface CategoryDelete {
    fun isCategoryDeleted(isDeleted: Boolean, mArrayList: ArrayList<CategoryModel>)
}