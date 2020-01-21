package com.vashisthg.guiorgy.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.vashisthg.guiorgy.TwowaySeekBar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val seekBar = findViewById<TwowaySeekBar>(R.id.seek_bar)
        seekBar.onSeekBarChangedListener { _, value ->
            Log.d(LOGTAG, "seekbar value:$value")
        }
        seekBar.notifyWhileDragging = true
        // setting progress on your own
        seekBar.setProgress(+20.0)
    }

    companion object {
        private const val LOGTAG = "MainActivity"
    }
}