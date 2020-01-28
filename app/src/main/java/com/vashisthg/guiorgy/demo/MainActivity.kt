package com.vashisthg.guiorgy.demo

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.vashisthg.guiorgy.TwowaySeekBar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val twowaySeekBar = findViewById<TwowaySeekBar>(R.id.twoway_seek_bar)
        twowaySeekBar.onProgressChange { _, value ->
            Log.d(LOGTAG, "twowayseekbar value:$value")
        }
        twowaySeekBar.notifyWhileDragging = true

        val minSeekBar = findViewById<SeekBar>(R.id.min_seek_bar)
        minSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d(LOGTAG, "twowayseekbar min value:$progress")
                twowaySeekBar.minValue = progress.toDouble()
                twowaySeekBar.startValue = (twowaySeekBar.maxValue + twowaySeekBar.minValue) / 2.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        val maxSeekBar = findViewById<ReversedSeekBar>(R.id.max_seek_bar)
        maxSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar == null) return
                @Suppress("NAME_SHADOWING") val progress = seekBar.max - progress + seekBar.min
                Log.d(LOGTAG, "twowayseekbar max value:$progress")
                twowaySeekBar.maxValue = progress.toDouble()
                twowaySeekBar.startValue = (twowaySeekBar.maxValue + twowaySeekBar.minValue) / 2.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    companion object {
        private const val LOGTAG = "MainActivityDemo"
    }
}