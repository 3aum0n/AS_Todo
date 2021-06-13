package com.dgut.todo.fragment


class FirstIntroFragment : android.support.v4.app.Fragment() {

    companion object {
        fun newInstance(pageNum: Int): FirstIntroFragment {

            val fragmentFirst = FirstIntroFragment()

            val bundle = android.os.Bundle()
            bundle.putInt(com.dgut.todo.utils.KEY_PAGE_NUMBER, pageNum)

            fragmentFirst.arguments = bundle

            return fragmentFirst
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater?,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): android.view.View? {
        val view =
            inflater!!.inflate(com.dgut.todo.R.layout.fragment_first_intro, container, false)

        initialize(view)
        return view
    }

    private fun initialize(view: android.view.View?) {

    }
}