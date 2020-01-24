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

        val seekBar = findViewById<SeekBar>(R.id.seek_bar)
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d(LOGTAG, "seekbar value:$progress")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
            }
        })
    }

    companion object {
        private const val LOGTAG = "MainActivityDemo"
    }
}