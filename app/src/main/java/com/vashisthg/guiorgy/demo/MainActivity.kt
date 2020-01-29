package com.vashisthg.guiorgy.demo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vashisthg.guiorgy.TwowaySeekBar
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var twowayLabel: TextView
    private lateinit var twowaySeekBar: TwowaySeekBar

    private lateinit var startLabel: TextView
    private lateinit var startSeekBar: SeekBar

    private lateinit var minLabel: TextView
    private lateinit var minSeekBar: SeekBar

    private lateinit var maxLabel: TextView
    private lateinit var maxSeekBar: ReverseSeekBar

    private fun updateLabels() {
        twowayLabel.text = resources.getString(R.string.twoway_seek_bar_label, twowaySeekBar.startValue, twowaySeekBar.progress)
        startLabel.text = resources.getString(R.string.start_seek_bar_label, twowaySeekBar.startValue)
        minLabel.text = resources.getString(R.string.min_seek_bar_label, twowaySeekBar.minValue)
        maxLabel.text = resources.getString(R.string.max_seek_bar_label, twowaySeekBar.maxValue)

        if (startSeekBar.progress != twowaySeekBar.startValue.toInt())
            startSeekBar.progress = twowaySeekBar.startValue.toInt()

        if (minSeekBar.progress != twowaySeekBar.minValue.toInt())
            minSeekBar.progress = twowaySeekBar.minValue.toInt()

        if (maxSeekBar.progress != twowaySeekBar.maxValue.toInt())
            maxSeekBar.progress = twowaySeekBar.maxValue.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        twowayLabel = findViewById(R.id.twoway_seek_bar_label)
        startLabel = findViewById(R.id.start_seek_bar_label)
        minLabel = findViewById(R.id.min_seek_bar_label)
        maxLabel = findViewById(R.id.max_seek_bar_label)

        twowaySeekBar = findViewById(R.id.twoway_seek_bar)
        twowaySeekBar.onProgressChange { _, progress, _ ->
            Log.d(TAG, "twowayseekbar progress=$progress")
            updateLabels()
        }
        twowaySeekBar.notifyWhileDragging = true

        startSeekBar = findViewById(R.id.start_seek_bar)
        startSeekBar.setOnSeekBarChangeListener(object: ProgressListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                twowaySeekBar.startValue = progress.toDouble()
                updateLabels()
                Log.d(TAG, "twowayseekbar start value=$progress (progress=${twowaySeekBar.progress})")
            }
        })

        minSeekBar = findViewById(R.id.min_seek_bar)
        minSeekBar.setOnSeekBarChangeListener(object: ProgressListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                twowaySeekBar.minValue = progress.toDouble()
                updateLabels()
                Log.d(TAG, "twowayseekbar min value=$progress (progress=${twowaySeekBar.progress})")
            }
        })

        maxSeekBar = findViewById(R.id.max_seek_bar)
        maxSeekBar.setOnSeekBarChangeListener(object: ProgressListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                twowaySeekBar.maxValue = progress.toDouble()
                updateLabels()
                Log.d(TAG, "twowayseekbar max value=$progress (progress=${twowaySeekBar.progress})")
            }
        })

        val btn = findViewById<Button>(R.id.set_random_btn)
        btn.setOnClickListener {
            val progress = Random.nextDouble(twowaySeekBar.minValue, twowaySeekBar.maxValue)
            twowaySeekBar.progress = progress
            updateLabels()
            Log.d(TAG, "twowayseekbar progress=$progress")
        }

        updateLabels()

    }

    companion object {
        private const val TAG = "MainActivityDemo"
    }

    private interface ProgressListener : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }
}