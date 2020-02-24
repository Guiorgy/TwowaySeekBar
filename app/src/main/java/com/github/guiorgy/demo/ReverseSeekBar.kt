package com.github.guiorgy.demo

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar


class ReverseSeekBar : SeekBar {
        constructor(context: Context) : super(context) {
            init()
        }
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }
        constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
            init()
        }

        private var first = true

        override fun onTouchEvent(event: MotionEvent): Boolean {
            event.setLocation(this.width - event.x, event.y)
            return super.onTouchEvent(event)
        }

        override fun getProgress(): Int {
            return max - super.getProgress() + min
        }

        override fun setProgress(progress: Int) {
            super.setProgress(max - progress + min)
        }

        override fun onDraw(canvas: Canvas) {
            if (first) {
                first = false
                val old = progress
                progress = min + max - progress
                super.onDraw(canvas)
                progress = old
            } else
                super.onDraw(canvas)
        }

        private fun init() {
            rotation = 180f
        }

    }