package com.vashisthg.guiorgy

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.RequiresApi
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by vashisthg on 01/04/14.
 * https://github.com/vashisthg/StartPointSeekBar
 * Modified by Guiorgy
 */
@Suppress("MemberVisibilityCanBePrivate")
open class TwowaySeekBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : View(context, attrs, defStyleAttr) {

    // region protected properties
    protected val noInvalidate = true
    protected var hasThumbTint = false
    protected var hasThumbTintMode = false
    protected val thumbImage: Bitmap
    protected val thumbPressedImage: Bitmap
    protected val defaultRangeColor: Int
    protected val defaultBackgroundColor: Int
    protected val thumbHalfWidth: Float
    protected val thumbHalfHeight: Float
    protected val lineHeight: Float
    protected val padding: Float
    protected val scaledTouchSlop: Int
    protected var isDragging = false
    protected var isThumbPressed = false
    protected var mDownMotionX = 0f
    protected var mActivePointerId = INVALID_POINTER_ID
    protected var listener: OnProgressChangeListener? = null
    // endregion

    // region public properties
    /**
     * Whether to call the OnProgressChangeListener while dragging, or
     * only when user lets go of the thumb.
     *
     * @see TwowaySeekBar.OnProgressChangeListener
     */
    var notifyWhileDragging = false

    var minValue: Double = -100.0
    set(value) {
        if (maxValue < value)
            return

        field = value
        if (startValue < value)
            startValue = value
        else
            invalidate()
    }

    var startValue: Double = 0.0
    set(value) {
        field =
            when {
                value < minValue -> minValue
                maxValue < value -> maxValue
                else -> value
            }
        invalidate()
    }

    var maxValue: Double = 100.0
    set(value) {
        if (value < minValue)
            return

        field = value
        if (value < startValue)
            startValue = value
        else
            invalidate()
    }

    /**
     * Progress in the range of minValue..maxValue
     */
    var progress: Double
    get() = recoverValue(normalizedProgress)
    /**
     * Sets the current progress to the specified value.
     */
    set(value) {
        normalizedProgress = normalizeValue(value)
    }

    /**
     * Progress in tge range of 0..1
     */
    var normalizedProgress = 0.0
    /**
     * Sets the current progress to the specified value.
     */
    set(value) {
        field = value
        if (field > 1) field = 1.0
        if (field < 0) field = 0.0
        invalidate()
    }

    var thumbOffset: Int = 0
    /**
     * Sets the thumb offset that allows the thumb to extend out of the range of
     * the track.
     *
     * @param value The offset amount in pixels.
     */
    set(value) {
        field = value
        invalidate()
    }

    var thumb: Drawable? = null
    /**
     * Sets the thumb that will be drawn at the end of the progress meter within the SeekBar.
     *
     * If the thumb is a valid drawable (i.e. not null), half its width will be
     * used as the new thumb offset (@see #thumbOffset).
     *
     * @param value Drawable representing the thumb
     */
    set(value) {
        // This way, calling setThumb again with the same bitmap will result in
        // it recalculating thumbOffset (if for example if the bounds of the
        // drawable changed)
        val needUpdate =
            if (field != null && field !== value) {
                field!!.callback = null
                true
            } else false

        if (value != null) {
            value.callback = this
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&canResolveLayoutDirection())
                value.layoutDirection = layoutDirection

            // Assuming the thumb drawable is symmetric, set the thumb offset
            // such that the thumb will hang halfway off either edge of the
            // progress bar.
            thumbOffset = value.intrinsicWidth / 2

            // If we're updating get the new states
            if (needUpdate &&
                (value.intrinsicWidth != field!!.intrinsicWidth ||
                        value.intrinsicHeight != field!!.intrinsicHeight))
                requestLayout()
        }

        field = value
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            applyThumbTint()
        invalidate()

        if (needUpdate) {
            updateThumbAndTrackPos(width, height)
            if (value != null && value.isStateful) {
                // Note that if the states are different this won't work.
                // For now, let's consider that an app bug.
                value.state = drawableState
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    var thumbTintList: ColorStateList? = null
    /**
     * Applies a tint to the thumb drawable. Does not modify the current tint
     * mode, which is {@link PorterDuff.Mode#SRC_IN} by default.
     * <p>
     * Subsequent calls to {@link #setThumb(Drawable)} will automatically
     * mutate the drawable and apply the specified tint and tint mode using
     * {@link Drawable#setTintList(ColorStateList)}.
     *
     * @param value the tint to apply, may be {@code null} to clear tint
     *
     * @attr ref android.R.styleable#SeekBar_thumbTint
     * @see #getThumbTintList()
     * @see Drawable#setTintList(ColorStateList)
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    set(value) {
        field = value
        hasThumbTint = true
        applyThumbTint()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    var thumbTintMode: PorterDuff.Mode? = PorterDuff.Mode.SRC_IN
    /**
     * Specifies the blending mode used to apply the tint specified by
     * {@link #setThumbTintList(ColorStateList)}} to the thumb drawable. The
     * default mode is {@link PorterDuff.Mode#SRC_IN}.
     *
     * @param value the blending mode used to apply the tint, may be
     *                 {@code null} to clear tint
     *
     * @attr ref android.R.styleable#SeekBar_thumbTintMode
     * @see #getThumbTintMode()
     * @see Drawable#setTintMode(PorterDuff.Mode)
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    set(value) {
        field = value
        hasThumbTintMode = true
        applyThumbTint()
    }
    // endregion

    // region listeners
    /**
     * Callback listener interface to notify about changed range values.
     */
    @FunctionalInterface
    interface OnProgressChangeListener {
        /**
         * Notification that the progress level has changed.
         *
         * @param seekBar The SeekBar whose progress has changed
         * @param progress The current progress level. This will be in the range of minValue..maxValue
         * @param normalizedProgress The current progress level. This will be in the range of 0..1
         */
        fun onProgressChanged(seekBar: TwowaySeekBar?, progress: Double, normalizedProgress: Double)
    }

    /**
     * Sets a listener to receive notifications of changes to the SeekBar's progress level.
     *
     * @param listener The seek bar notification listener
     *
     * @see TwowaySeekBar.OnProgressChangeListener
     */
    fun setOnProgressChangeListener(listener: OnProgressChangeListener?) {
        this.listener = listener
    }

    /**
     * Sets a listener to receive notifications of changes to the SeekBar's progress level.
     *
     * @param listener The seek bar notification listener
     *
     * @see TwowaySeekBar.OnProgressChangeListener
     */
    inline fun onProgressChange(crossinline listener: (seekBar: TwowaySeekBar?, progress: Double, normalizedProgress: Double) -> Unit) {
        setOnProgressChangeListener(object : OnProgressChangeListener {
            override fun onProgressChanged(seekBar: TwowaySeekBar?, progress: Double, normalizedProgress: Double) {
                listener(seekBar, progress, normalizedProgress)
            }
        })
    }

    protected var oldNormalizedProgress: Double = 0.0
    protected fun callOnProgressChange() {
        if (oldNormalizedProgress == normalizedProgress) return
        listener?.onProgressChanged(this, progress, normalizedProgress)
        oldNormalizedProgress = normalizedProgress
    }
    // endregion

    // region protected methods
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun applyThumbTint() {
        if (thumb != null && (hasThumbTint || hasThumbTintMode)) {
            thumb = thumb!!.mutate()

            if (hasThumbTint)
                thumb!!.setTintList(thumbTintList)

            if (hasThumbTintMode)
                thumb!!.setTintMode(thumbTintMode)

            // The drawable (or one of its children) may not have been
            // stateful before applying the tint, so let's try again.
            if (thumb!!.isStateful) {
                thumb!!.state = drawableState
            }
        }
    }

    protected fun updateThumbAndTrackPos(width: Int, height: Int) {
        /*val paddedHeight: Int = height - mPaddingTop - mPaddingBottom
        val track: Drawable? = getCurrentDrawable()
        val thumb: Drawable? = thumb

        // The max height does not incorporate padding, whereas the height
        // parameter does.
        val trackHeight = min(maxHeight, paddedHeight)
        val thumbHeight = thumb?.intrinsicHeight ?: 0

        // Apply offset to whichever item is taller.
        val trackOffset: Int
        val thumbOffset: Int
        if (thumbHeight > trackHeight) {
            val offsetHeight = (paddedHeight - thumbHeight) / 2
            trackOffset = offsetHeight + (thumbHeight - trackHeight) / 2
            thumbOffset = offsetHeight
        } else {
            val offsetHeight = (paddedHeight - trackHeight) / 2
            trackOffset = offsetHeight
            thumbOffset = offsetHeight + (trackHeight - thumbHeight) / 2
        }

        if (track != null) {
            val trackWidth: Int = width - mPaddingRight - mPaddingLeft
            track.setBounds(0, trackOffset, trackWidth, trackOffset + trackHeight)
        }

        if (thumb != null) {
            setThumbPos(width, thumb, normalizedProgress, thumbOffset)
        }*/
    }

    /**
     * Converts a normalized value to a Number object in the value space between
     * absolute minimum and maximum.
     *
     * @param normalized
     * @return
     */
    protected fun recoverValue(normalized: Double): Double {
        return minValue + normalized * (maxValue - minValue)
    }

    /**
     * Converts the given Number value to a normalized double.
     *
     * @param value The Number value to normalize.
     * @return The normalized double.
     */
    protected fun normalizeValue(value: Double): Double {
        val result = (value - minValue) / (maxValue - minValue)
        return if (result.isFinite()) result else 0.0
    }
    // endregion

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 200
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec)
        }
        var height = thumbImage.height
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = min(height, MeasureSpec.getSize(heightMeasureSpec))
        }
        setMeasuredDimension(width, height)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        val pointerIndex: Int
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.pointerCount - 1)
                pointerIndex = event.findPointerIndex(mActivePointerId)
                mDownMotionX = event.getX(pointerIndex)
                isThumbPressed = evalPressedThumb(mDownMotionX)
                // Only handle thumb presses.
                if (!isThumbPressed)
                    return true
                isPressed = true
                invalidate()
                isDragging = true
                trackTouchEvent(event)
                attemptClaimDrag()
            }
            MotionEvent.ACTION_MOVE -> if (isThumbPressed) {
                if (isDragging) {
                    trackTouchEvent(event)
                } else { // Scroll to follow the motion event
                    pointerIndex = event.findPointerIndex(mActivePointerId)
                    val x = event.getX(pointerIndex)
                    if (Math.abs(x - mDownMotionX) > scaledTouchSlop) {
                        isPressed = true
                        invalidate()
                        isDragging = true
                        trackTouchEvent(event)
                        attemptClaimDrag()
                    }
                }
                if (notifyWhileDragging)
                    callOnProgressChange()
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    trackTouchEvent(event)
                    isDragging = false
                    isPressed = false
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    isDragging = true
                    trackTouchEvent(event)
                    isDragging = false
                }
                isThumbPressed = false
                invalidate()
                callOnProgressChange()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.pointerCount - 1
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index)
                mActivePointerId = event.getPointerId(index)
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    isPressed = false
                }
                invalidate() // see above explanation
            }
        }
        return true
    }

    protected fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and ACTION_POINTER_INDEX_MASK shr ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) { // This was our active pointer going up. Choose
// a new active pointer and adjust accordingly.
// TODO: Make this decision more intelligent.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mDownMotionX = ev.getX(newPointerIndex)
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    protected fun attemptClaimDrag() {
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(true)
    }

    protected fun trackTouchEvent(event: MotionEvent) {
        val pointerIndex = event.findPointerIndex(mActivePointerId)
        val x = event.getX(pointerIndex)
        setNormalizedValue(screenToNormalized(x))
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoordinate The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    protected fun screenToNormalized(screenCoordinate: Float): Double {
        val width = width
        return if (width <= 2 * padding) { // prevent division by zero, simply return 0.
            0.0
        } else {
            val result =
                (screenCoordinate - padding) / (width - 2 * padding).toDouble()
            min(1.0, max(0.0, result))
        }
    }

    /**
     * Sets normalized max value to value so that 0 <= normalized min value <=
     * value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized max value to set.
     */
    protected fun setNormalizedValue(value: Double) {
        normalizedProgress = max(0.0, value)
        invalidate()
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    protected fun evalPressedThumb(touchX: Float): Boolean {
        return isInThumbRange(touchX, normalizedProgress)
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as
     * "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    protected fun isInThumbRange(
        touchX: Float,
        normalizedThumbValue: Double
    ): Boolean {
        return abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoordinate The normalized value to convert.
     * @return The converted value in screen space.
     */
    protected fun normalizedToScreen(normalizedCoordinate: Double): Float {
        return (padding + normalizedCoordinate * (width - 2 * padding)).toFloat()
    }

    protected val rect = RectF()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        // draw seek bar background line
        rect.set(
            padding,
            0.5f * (height - lineHeight), width - padding,
            0.5f * (height + lineHeight)
        )
        paint.color = defaultBackgroundColor
        canvas.drawRect(rect, paint)
        // draw seek bar active range line
        if (startValue != progress) {
            val startValue = normalizedToScreen(normalizeValue(startValue))
            val progress = normalizedToScreen(normalizedProgress)
            if (startValue < progress) {
                rect.left = startValue
                rect.right = progress
            } else {
                rect.right = startValue
                rect.left = progress
            }
            paint.color = defaultRangeColor
            canvas.drawRect(rect, paint)
        }
        drawThumb(normalizedToScreen(normalizedProgress), isThumbPressed, canvas)
    }

    /**
     * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
     *
     * @param screenCoordinate  The x-coordinate in screen space where to draw the image.
     * @param pressed           Is the thumb currently in "pressed" state?
     * @param canvas            The canvas to draw upon.
     */
    protected fun drawThumb(screenCoordinate: Float, pressed: Boolean, canvas: Canvas) {
        canvas.drawBitmap(
            if (pressed) thumbPressedImage else thumbImage
            , screenCoordinate - thumbHalfWidth
            , 0.5f * height - thumbHalfHeight, paint)
    }

    companion object {
        protected val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        protected val DEFAULT_RANGE_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5)
        protected const val DEFAULT_BACKGROUND_COLOR = Color.GRAY
        protected const val DEFAULT_MIN_VALUE = -100f
        protected const val DEFAULT_MAX_VALUE = +100f
        /**
         * An invalid pointer id.
         */
        const val INVALID_POINTER_ID = 255
        // Localized constants from MotionEvent for compatibility
        // with API < 8 "Froyo".
        const val ACTION_POINTER_UP = 0x6
        const val ACTION_POINTER_INDEX_MASK = 0x0000ff00
        const val ACTION_POINTER_INDEX_SHIFT = 8

        /**
         * Returns  `true` if the target drawable needs to be tileified.
         *
         * @param   dr the drawable to check
         * @return  `true` if the target drawable needs to be tileified,
         *          `false` otherwise
         */
        protected fun needsTileify(dr: Drawable?): Boolean {
            if (dr is LayerDrawable) {
                val n = dr.numberOfLayers
                for (i in 0 until n)
                    if (needsTileify(dr.getDrawable(i)))
                        return true
                return false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && dr is StateListDrawable) {
                val n = dr.stateCount
                for (i in 0 until n)
                    if (needsTileify(dr.getStateDrawable(i)))
                        return true
                return false
            }
            // If there's a bitmap that's not wrapped with a ClipDrawable or
            // ScaleDrawable, we'll need to wrap it and apply tiling.
            return dr is BitmapDrawable
        }
    }

    init {
        // Attribute initialization
        val attr = context.obtainStyledAttributes(attrs, R.styleable.TwowaySeekBar, defStyleAttr, defStyleRes)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            saveAttributeDataForStyleable(context, R.styleable.TwowaySeekBar, attrs, attr, defStyleAttr, defStyleRes)

        try {
            var thumbDrawable = attr.getDrawable(R.styleable.TwowaySeekBar_thumbDrawable)
            if (thumbDrawable == null) {
                @Suppress("DEPRECATION")
                thumbDrawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    context.getDrawable(R.drawable.seek_thumb_normal)
                else resources.getDrawable(R.drawable.seek_thumb_normal)
            }
            thumbImage = (thumbDrawable as BitmapDrawable?)!!.bitmap
            var thumbPressedDrawable =
                attr.getDrawable(R.styleable.TwowaySeekBar_thumbPressedDrawable)
            if (thumbPressedDrawable == null) {
                @Suppress("DEPRECATION")
                thumbPressedDrawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    context.getDrawable(R.drawable.seek_thumb_pressed)
                else resources.getDrawable(R.drawable.seek_thumb_pressed)
            }
            thumbPressedImage = (thumbPressedDrawable as BitmapDrawable?)!!.bitmap
            minValue = attr.getFloat(
                R.styleable.TwowaySeekBar_minValue,
                DEFAULT_MIN_VALUE
            ).toDouble()
            maxValue = attr.getFloat(
                R.styleable.TwowaySeekBar_maxValue,
                DEFAULT_MAX_VALUE
            ).toDouble()
            defaultBackgroundColor = attr.getColor(
                R.styleable.TwowaySeekBar_defaultBackgroundColor,
                DEFAULT_BACKGROUND_COLOR
            )
            defaultRangeColor = attr.getColor(
                R.styleable.TwowaySeekBar_defaultBackgroundRangeColor,
                DEFAULT_RANGE_COLOR
            )
        } finally {
            attr.recycle()
        }
        val thumbWidth = thumbImage.width.toFloat()
        thumbHalfWidth = 0.5f * thumbWidth
        thumbHalfHeight = 0.5f * thumbImage.height
        lineHeight = 0.3f * thumbHalfHeight
        padding = thumbHalfWidth
        isFocusable = true
        isFocusableInTouchMode = true
        scaledTouchSlop = ViewConfiguration.get(context)
            .scaledTouchSlop
    }

    override fun postInvalidate() {
        //if (!noInvalidate)
            super.postInvalidate()
    }

    override fun invalidate() {
        //if (!noInvalidate)
            super.invalidate()
    }

    override fun getAccessibilityClassName(): CharSequence? = TwowaySeekBar::class.qualifiedName
}