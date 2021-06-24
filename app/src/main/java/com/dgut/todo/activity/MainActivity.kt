package com.dgut.todo.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.dgut.todo.R
import com.dgut.todo.fragment.*
import com.dgut.todo.utils.APP_PACKAGE_NAME
import com.dgut.todo.utils.toastMessage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private val TAG: String = MainActivity::class.java.simpleName

    private val mActivity: Activity = this@MainActivity

    private lateinit var handler: Handler
    private var doubleBackToExitPressedOnce = false

    private lateinit var mMonthText: Array<String>
    private var tvTitleMonth: String? = null
    private var tvTitleDay: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMonthText = resources.getStringArray(R.array.calendar_month)

        initialize()

        // loading dashboard fragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.framLayout, DashboardFragment())
        ft.commit()
    }

    /**
     * initializing views and data
     * */
    fun initialize() {

        setSupportActionBar(toolbarMain)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbarMain,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.setDrawerListener(toggle)
        toggle.syncState()

        handler = Handler()

        nav_view.setNavigationItemSelectedListener(this)
        bottom_navigation.setOnNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }

            this.doubleBackToExitPressedOnce = true
            toastMessage(mActivity, getString(R.string.please_click_again_to_exit))

            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    /**
     * inflating actionbar menu
     * */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /**
     * actionbar click
     * */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

//        when (id) {
//            R.id.action_settings -> {
//                startActivity(Intent(mActivity, SettingActivity::class.java))
//                return true
//            }
//        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        /**
         * @Handler
         * load fragment after delay of drawer close
         * */
        handler.postDelayed({ navigate(item.itemId) }, 280)
        return true
    }

    /**
     * Navigation Drawer item clicks
     * */
    fun navigate(id: Int) {
        var fragment: Fragment? = null
        var fragmentClass: Class<*>? = null
        tvTitleMonth = mMonthText[Calendar.getInstance()[Calendar.MONTH]]
        tvTitleDay = getString(R.string.calendar_today)

        when (id) {
            R.id.nav_dashboard, R.id.btm_nav_dashboard -> {
                fragmentClass = DashboardFragment::class.java
                toolbarMain.title = getString(R.string.dashboard)
            }
            R.id.nav_category -> {
                fragmentClass = CategoryFragment::class.java
                toolbarMain.title = getString(R.string.category)
            }
            R.id.nav_history -> {
                fragmentClass = HistoryFragment::class.java
                toolbarMain.title = getString(R.string.history)
            }
            R.id.btm_nav_schedule -> {
                fragmentClass = ScheduleFragment::class.java
                toolbarMain.title = "$tvTitleMonth $tvTitleDay"
            }
            R.id.btm_nav_focus -> {
                fragmentClass = FocusFragment::class.java
                toolbarMain.title = getString(R.string.focus)
            }
            R.id.btm_nav_search -> {
                fragmentClass = SearchFragment::class.java
                toolbarMain.title = getString(R.string.search)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)

        try {
            fragment = fragmentClass!!.newInstance() as Fragment

            val fragmentManager = supportFragmentManager

            fragmentManager
                .beginTransaction()
                .replace(R.id.framLayout, fragment).commit()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetMainTitleDate(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        if (year == calendar[Calendar.YEAR] && month == calendar[Calendar.MONTH] && day == calendar[Calendar.DAY_OF_MONTH]) {
            tvTitleMonth = mMonthText.get(month)
            tvTitleDay = getString(R.string.calendar_today)
        } else {
            if (year == calendar[Calendar.YEAR]) {
                tvTitleMonth = mMonthText.get(month)
            } else {
                tvTitleMonth = String.format(
                    "%s%s", java.lang.String.format(getString(R.string.calendar_year), year),
                    mMonthText.get(month)
                )
            }
            tvTitleDay = java.lang.String.format(getString(R.string.calendar_day), day)
        }
        toolbarMain.title = "$tvTitleMonth $tvTitleDay"
    }
}
