package com.vashisthg.guiorgy.demo

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar


class ReverseSeekBar : SeekBar {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

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

}