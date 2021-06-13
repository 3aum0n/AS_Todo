package com.dgut.todo.utils.views.recyclerview.itemdrag

import android.support.v7.widget.RecyclerView


interface OnStartDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}