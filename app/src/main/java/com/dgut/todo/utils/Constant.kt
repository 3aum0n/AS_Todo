package com.dgut.todo.utils

/**
 * @Constants
 * All constant Variable are declare here
 * and all are companion object so it can
 * be access directly without reference
 */

/******************* Play *****************/

const val APP_PACKAGE_NAME = "com.todo.android"

/****************** Intro ********************/

const val KEY_PAGE_NUMBER = "pageNum"
const val TOTAL_INTRO_PAGES = 4


/***************  Database  ***************/

const val DB_NAME = "Todo.db"
const val DB_VERSION = 1


/***************  Category  ****************/

const val TABLE_CATEGORY = "CATEGORY"
const val ID = "id"
const val CATEGORY_NAME = "category_name"


/*****************   Task  *****************/

const val TABLE_TASK = "TASK"
const val TASK_TITLE = "title"
const val TASK_TASK = "task"
const val TASK_CATEGORY = "category"
const val TASK_DATE = "date"
const val TASK_TIME = "time"
const val TASK_FINISH = "finish"

const val TASK_IS_FINISH = 1
const val TASK_IS_NOT_FINISH = 0


/************* onActivityResult *****************/

const val DASHBOARD_RECYCLEVIEW_REFRESH = 101

