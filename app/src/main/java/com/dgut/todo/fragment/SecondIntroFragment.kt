package com.dgut.todo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dgut.todo.R
import com.dgut.todo.utils.KEY_PAGE_NUMBER


class SecondIntroFragment : Fragment() {

    companion object {
        fun newInstance(pageNum: Int): SecondIntroFragment {

            val fragmentSecond = SecondIntroFragment()

            val bundle = Bundle()
            bundle.putInt(KEY_PAGE_NUMBER, pageNum)

            fragmentSecond.arguments = bundle

            return fragmentSecond
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater!!.inflate(R.layout.fragment_second_intro, container, false)

        initialize(view)
        return view
    }

    private fun initialize(view: View?) {

    }
}