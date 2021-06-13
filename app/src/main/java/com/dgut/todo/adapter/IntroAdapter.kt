package com.dgut.todo.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.dgut.todo.fragment.FirstIntroFragment
import com.dgut.todo.fragment.FourthIntroFragment
import com.dgut.todo.fragment.SecondIntroFragment
import com.dgut.todo.fragment.ThirdIntroFragment
import com.dgut.todo.utils.TOTAL_INTRO_PAGES


class IntroAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {


    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FirstIntroFragment.newInstance(0)
            1 -> SecondIntroFragment.newInstance(1)
            2 -> ThirdIntroFragment.newInstance(2)
            3 -> FourthIntroFragment.newInstance(3)
            else -> ThirdIntroFragment.newInstance(2)
        }
    }

    override fun getCount(): Int {
        return TOTAL_INTRO_PAGES
    }
}