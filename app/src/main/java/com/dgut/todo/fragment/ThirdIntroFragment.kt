package com.dgut.todo.fragment

/**
 * Created by dgut on 13/6/17.
 */
class ThirdIntroFragment : android.support.v4.app.Fragment() {

    companion object {
        fun newInstance(pageNum: Int): ThirdIntroFragment {

            val fragmentThird = ThirdIntroFragment()

            val bundle = android.os.Bundle()
            bundle.putInt(com.dgut.todo.utils.KEY_PAGE_NUMBER, pageNum)

            fragmentThird.arguments = bundle

            return fragmentThird
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater?,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): android.view.View? {
        val view =
            inflater!!.inflate(com.dgut.todo.R.layout.fragment_third_intro, container, false)

        initialize(view)
        return view
    }

    private fun initialize(view: android.view.View?) {

    }
}