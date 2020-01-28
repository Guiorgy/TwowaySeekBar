package com.vashisthg.guiorgy.demo

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar


class ReversedSeekBar : SeekBar {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onDraw(canvas: Canvas) {
        val px = this.width / 2.0f
        val py = this.height / 2.0f
        canvas.scale(-1f, 1f, px, py)
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        event.setLocation(this.width - event.x, event.y)
        return super.onTouchEvent(event)
    }
}