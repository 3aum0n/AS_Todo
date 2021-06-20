package com.dgut.todo.fragment

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.dgut.todo.R
import kotlinx.android.synthetic.main.fragment_countdown.*
import java.util.*

class CountdownFragment : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_countdown)
        initialize()
    }

    private lateinit var CountDown: CountDownTimer
    private var minute: Long = 0
    private var maxSecond: Long = 0
    private var count: Int = 0
    private var flag: Int = 0

    private fun initialize() {

        CancelFocus.setOnClickListener(this)
        minute = getIntent().getLongExtra("maxMinute", 0)
        var zero: Long = 0
        var list = listOf<String>("In order to meet a better self, work hard !",
                "A good beginning is half done !",
                "Don't give up and don't give in !",
                "Do what you say,say what you do !",
                "All things come to those who wait !")
        if (minute == zero) {
            maxSecond = 0
        } else {
            maxSecond = minute * 60 * 1000 + 1000
        }
        CountDown = object : CountDownTimer(maxSecond, 1000) {
            override fun onTick(p0: Long) {
                if (minute * 60 + count <= maxSecond / 4000 && flag == 0) {
                    val drawable: Drawable = getResources().getDrawable(R.drawable.night)
                    countdownLayout.setBackground(drawable)
                    flag = 1
                }
                if (count == 30) {
                    val randoms = Random()
                    var nextInt = Math.abs(randoms.nextInt()) % 5
                    sentence.text = list.get(nextInt)
                }
                if (count >= 10 && minute >= 10) {
                    countdownText.text = "$minute:$count"
                } else if (count < 10 && minute >= 10) {
                    countdownText.text = "$minute:0$count"
                } else if (count >= 10 && minute < 10) {
                    countdownText.text = "0$minute:$count"
                } else if (count < 10 && minute < 10) {
                    countdownText.text = "0$minute:0$count"
                }
                if (count == 0) {
                    minute--
                    count = 60
                }
                count--
            }

            override fun onFinish() {
                finish()
            }

        }
        CountDown.start()

    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.CancelFocus -> {
                setResult(Activity.RESULT_OK, intent);
                CountDown.cancel()
                CountDown.onFinish()
            }

        }
    }
}