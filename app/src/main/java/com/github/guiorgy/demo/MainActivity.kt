package com.github.guiorgy.demo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import com.github.guiorgy.TwowaySeekBar

class MainActivity : AppCompatActivity() {

    private lateinit var twowayLabel: TextView
    private lateinit var twowaySeekBar: TwowaySeekBar

    private lateinit var startLabel: TextView
    private lateinit var startSeekBar: SeekBar

    private lateinit var minLabel: TextView
    private lateinit var minSeekBar: SeekBar

    private lateinit var maxLabel: TextView
    private lateinit var maxSeekBar: ReverseSeekBar

    private lateinit var rtlCheckBox: AppCompatCheckBox
    private lateinit var dragCheckBox: AppCompatCheckBox

    private fun updateLabels() {
        twowayLabel.text =
            resources.getString(R.string.twoway_seek_bar_label, twowaySeekBar.value)
        startLabel.text =
            resources.getString(R.string.start_seek_bar_label, twowaySeekBar.zero)
        minLabel.text =
            resources.getString(R.string.min_seek_bar_label, twowaySeekBar.min)
        maxLabel.text =
            resources.getString(R.string.max_seek_bar_label, twowaySeekBar.max)

        if (startSeekBar.progress != twowaySeekBar.zero.toInt())
            startSeekBar.progress = twowaySeekBar.zero.toInt()

        if (minSeekBar.progress != twowaySeekBar.min.toInt())
            minSeekBar.progress = twowaySeekBar.min.toInt()

        if (maxSeekBar.progress != twowaySeekBar.max.toInt())
            maxSeekBar.progress = twowaySeekBar.max.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val l = LinearLayout(this)
        l.orientation = LinearLayout.VERTICAL

        rtlCheckBox = findViewById(R.id.rtl_checkbox)
        dragCheckBox = findViewById(R.id.drag_checkbox)

        twowayLabel = findViewById(R.id.twoway_seek_bar_label)
        startLabel = findViewById(R.id.start_seek_bar_label)
        minLabel = findViewById(R.id.min_seek_bar_label)
        maxLabel = findViewById(R.id.max_seek_bar_label)

        twowaySeekBar = findViewById(R.id.twoway_seek_bar)
        twowaySeekBar.onValueChange { _, progress, _ ->
            Log.d(TAG, "twowayseekbar progress=$progress")
            updateLabels()
        }
        twowaySeekBar.notifyWhileDragging = dragCheckBox.isChecked

        startSeekBar = findViewById(R.id.start_seek_bar)
        startSeekBar.setOnSeekBarChangeListener(object : ProgressListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                twowaySeekBar.zero = progress.toDouble()
                updateLabels()
                Log.d(
                    TAG,
                    "twowayseekbar start value=$progress (progress=${twowaySeekBar.value})"
                )
            }
        })

        minSeekBar = findViewById(R.id.min_seek_bar)
        minSeekBar.setOnSeekBarChangeListener(object : ProgressListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                twowaySeekBar.min = progress.toDouble()
                updateLabels()
                Log.d(TAG, "twowayseekbar min value=$progress (progress=${twowaySeekBar.value})")
            }
        })

        maxSeekBar = findViewById(R.id.max_seek_bar)
        maxSeekBar.setOnSeekBarChangeListener(object : ProgressListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                twowaySeekBar.max = progress.toDouble()
                updateLabels()
                Log.d(TAG, "twowayseekbar max value=$progress (progress=${twowaySeekBar.value})")
            }
        })

        updateLabels()

        rtlCheckBox.setOnCheckedChangeListener { _, checked: Boolean ->
            twowaySeekBar.layoutDirection =
                if (checked)
                    View.LAYOUT_DIRECTION_RTL
                else
                    View.LAYOUT_DIRECTION_LTR
        }

        dragCheckBox.setOnCheckedChangeListener { _, checked: Boolean ->
            twowaySeekBar.notifyWhileDragging = checked
        }

        findViewById<TextView>(R.id.version_label).text = BuildConfig.VERSION_NAME
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