package com.github.guiorgy.demo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controls.*

class MainActivity : AppCompatActivity() {

    private fun updateLabels() {
        twoway_seek_bar_label.text =
            resources.getString(R.string.twoway_seek_bar_label, twoway_seek_bar.value)
        zerot_seek_bar_label.text =
            resources.getString(R.string.start_seek_bar_label, twoway_seek_bar.zero)
        min_seek_bar_label.text =
            resources.getString(R.string.min_seek_bar_label, twoway_seek_bar.min)
        max_seek_bar_label.text =
            resources.getString(R.string.max_seek_bar_label, twoway_seek_bar.max)

        if (zero_seek_bar.progress != twoway_seek_bar.zero.toInt())
            zero_seek_bar.progress = twoway_seek_bar.zero.toInt()

        if (min_seek_bar.progress != twoway_seek_bar.min.toInt())
            min_seek_bar.progress = twoway_seek_bar.min.toInt()

        if (max_seek_bar.progress != twoway_seek_bar.max.toInt())
            max_seek_bar.progress = twoway_seek_bar.max.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        twoway_seek_bar.onValueChange { _, progress, _ ->
            Log.d(TAG, "twowayseekbar progress=$progress")
            updateLabels()
        }
        twoway_seek_bar.notifyWhileDragging = drag_checkbox.isChecked

        zero_seek_bar.setOnSeekBarChangeListener(object : ProgressListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                twoway_seek_bar.zero = progress.toDouble()
                updateLabels()
                Log.d(
                    TAG,
                    "twowayseekbar start value=$progress (progress=${twoway_seek_bar.value})"
                )
            }
        })

        min_seek_bar.setOnSeekBarChangeListener(object : ProgressListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                twoway_seek_bar.min = progress.toDouble()
                updateLabels()
                Log.d(TAG, "twowayseekbar min value=$progress (progress=${twoway_seek_bar.value})")
            }
        })

        max_seek_bar.setOnSeekBarChangeListener(object : ProgressListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                twoway_seek_bar.max = progress.toDouble()
                updateLabels()
                Log.d(TAG, "twowayseekbar max value=$progress (progress=${twoway_seek_bar.value})")
            }
        })

        updateLabels()

        reverse_checkbox.setOnCheckedChangeListener { _, checked: Boolean ->
            twoway_seek_bar.layoutDirection =
                if (checked)
                    View.LAYOUT_DIRECTION_RTL
                else
                    View.LAYOUT_DIRECTION_LTR
        }

        drag_checkbox.setOnCheckedChangeListener { _, checked: Boolean ->
            twoway_seek_bar.notifyWhileDragging = checked
        }

        version_label.text = BuildConfig.VERSION_NAME
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