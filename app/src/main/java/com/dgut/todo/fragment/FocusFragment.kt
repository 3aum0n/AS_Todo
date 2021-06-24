package com.dgut.todo.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dgut.todo.R
import kotlinx.android.synthetic.main.fragment_focus.*
import kotlinx.android.synthetic.main.fragment_focus.view.*


class FocusFragment : Fragment(), View.OnClickListener {

    private var maxMinute: Long = 0

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater!!.inflate(R.layout.fragment_focus, container, false)

        initialize(view)

        return view
    }

    private fun initialize(view: View) {

        view.StartFocus.setOnClickListener(this)
        view.time.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.StartFocus -> {
                var textContent = time.text.toString().trim()
                if (textContent == "") {
                    Toast.makeText(context, "Please input the duration", Toast.LENGTH_SHORT).show()
                    return
                }
                Toast.makeText(context, "The countdown starts...", Toast.LENGTH_SHORT).show()
                maxMinute = textContent.toLong()
                val intent = Intent(context, CountdownFragment::class.java)
                intent.putExtra("maxMinute", maxMinute)
                startActivityForResult(intent, 1)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            alertFinish()
        }
    }


    private fun alertFinish() {
        val li = LayoutInflater.from(context)
        val promptsView = li.inflate(R.layout.alert_focus_finish, null)
        val alert = AlertDialog.Builder(context)
        alert.setView(promptsView)
        val alertDialog = alert.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.show()

    }
}