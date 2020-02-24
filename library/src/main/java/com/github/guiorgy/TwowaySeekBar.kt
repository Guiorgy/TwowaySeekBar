package com.github.guiorgy

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.*
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import android.view.KeyEvent.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.*
import android.view.accessibility.AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_INT
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.ViewUtils.isLayoutRtl
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.util.Preconditions
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD as ACCESSIBILITY_ACTION_SCROLL_BACKWARD
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD as ACCESSIBILITY_ACTION_SCROLL_FORWARD


/**
 * First prototype created by vashisthg on 01/04/14 (https://github.com/vashisthg/StartPointSeekBar)
 * TwowaySeekBar created by Guiorgy (https://github.com/Guiorgy/TwowaySeekBar)
 *
 *
 * <p><strong><b>XML attributes</b></strong></p>
 *
 * <p>
 * See [R.styleable.TwowaySeekBar] TwowaySeekBar Attributes},
 * [R.styleable.View] View Attributes}
 * </p>
 *
 * @attr ref [R.styleable.TwowaySeekBar_track]
 * @attr ref [R.styleable.TwowaySeekBar_trackTint]
 * @attr ref [R.styleable.TwowaySeekBar_trackTintMode]
 * @attr ref [R.styleable.TwowaySeekBar_progress]
 * @attr ref [R.styleable.TwowaySeekBar_progressTint]
 * @attr ref [R.styleable.TwowaySeekBar_progressTintMode]
 * @attr ref [R.styleable.TwowaySeekBar_trackCornerRadius]
 * @attr ref [R.styleable.TwowaySeekBar_tickMark]
 * @attr ref [R.styleable.TwowaySeekBar_tickMarkTint]
 * @attr ref [R.styleable.TwowaySeekBar_tickMarkTintMode]
 * @attr ref [R.styleable.TwowaySeekBar_enableTickMarks]
 * @attr ref [R.styleable.TwowaySeekBar_android_thumb]
 * @attr ref [R.styleable.TwowaySeekBar_thumb]
 * @attr ref [R.styleable.TwowaySeekBar_thumbTint]
 * @attr ref [R.styleable.TwowaySeekBar_thumbTintMode]
 * @attr ref [R.styleable.TwowaySeekBar_min]
 * @attr ref [R.styleable.TwowaySeekBar_zero]
 * @attr ref [R.styleable.TwowaySeekBar_max]
 * @attr ref [R.styleable.TwowaySeekBar_value]
 * @attr ref [R.styleable.TwowaySeekBar_normalizedValue]
 * @attr ref [R.styleable.TwowaySeekBar_splitTrack]
 * @attr ref [R.styleable.TwowaySeekBar_android_thumbOffset]
 * @attr ref [R.styleable.TwowaySeekBar_thumbOffset]
 * @attr ref [R.styleable.TwowaySeekBar_keyValueIncrement]
 * @attr ref [R.styleable.TwowaySeekBar_android_mirrorForRtl]
 * @attr ref [R.styleable.TwowaySeekBar_mirrorForRtl]
 * @attr ref [R.styleable.TwowaySeekBar_android_minWidth]
 * @attr ref [R.styleable.TwowaySeekBar_minWidth]
 * @attr ref [R.styleable.TwowaySeekBar_android_maxWidth]
 * @attr ref [R.styleable.TwowaySeekBar_maxWidth]
 * @attr ref [R.styleable.TwowaySeekBar_android_minHeight]
 * @attr ref [R.styleable.TwowaySeekBar_minHeight]
 * @attr ref [R.styleable.TwowaySeekBar_android_maxHeight]
 * @attr ref [R.styleable.TwowaySeekBar_maxHeight]
 * @attr ref [R.styleable.TwowaySeekBar_useDisabledAlpha]
 */
@Suppress("MemberVisibilityCanBePrivate")
open class TwowaySeekBar : View {

    companion object {
        // todo surround statements that use reflection with try catch (also those with @hide)
        // Beginning with P reflection was restricted. To bypass this, we can use double reflection
        protected val getDeclaredMethod: Method = Class::class.java.getDeclaredMethod(
            "getDeclaredMethod",
            String::class.java, arrayOf<Class<*>>()::class.java
        )

        protected val getStateCount: Method =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                StateListDrawable::class.java.getMethod("getStateCount")
            else
                getDeclaredMethod.invoke(StateListDrawable::class.java, "getStateCount") as Method
        protected val getStateSet: Method =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                StateListDrawable::class.java.getMethod("getStateSet", Int::class.javaPrimitiveType)
            else
                getDeclaredMethod.invoke(
                    StateListDrawable::class.java,
                    "getStateSet",
                    Int::class.javaPrimitiveType
                ) as Method
        protected val getStateDrawable: Method =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                StateListDrawable::class.java.getMethod(
                    "getStateDrawable",
                    Int::class.javaPrimitiveType
                )
            else
                getDeclaredMethod.invoke(
                    StateListDrawable::class.java,
                    "getStateDrawable",
                    Int::class.javaPrimitiveType
                ) as Method

        protected val getInstance: Method =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                AccessibilityManager::class.java.getMethod(
                    "getInstance",
                    Context::class.javaObjectType
                )
            else
                getDeclaredMethod.invoke(
                    AccessibilityManager::class.java,
                    "getInstance",
                    Context::class.javaObjectType
                ) as Method

        protected val DEFAULT_STYLE_ATTRIBUTES = R.attr.twowaySeekBarStyle
        protected val DEFAULT_STYLE_RESOURCE = R.style.TwowaySeekBarStyle

        protected val DEFAULT_PORTER_DUFF_TINT_MODE = PorterDuff.Mode.SRC_IN
        protected const val DEFAULT_TINT_MODE = -1

        protected val DEFAULT_TRACK_TINT_MODE = PorterDuff.Mode.SRC_ATOP
        protected val DEFAULT_PROGRESS_TINT_MODE = DEFAULT_PORTER_DUFF_TINT_MODE
        protected val DEFAULT_TICK_MARK_TINT_MODE = DEFAULT_PORTER_DUFF_TINT_MODE
        protected val DEFAULT_THUMB_TINT_MODE = DEFAULT_PORTER_DUFF_TINT_MODE

        protected const val DEFAULT_MIN = -100f
        protected const val DEFAULT_ZERO = 0f
        protected const val DEFAULT_MAX = +100f

        protected const val DEFAULT_Value = 0f
        protected const val DEFAULT_NORMALIZED_VALUE = 0f

        protected const val DEFAULT_ENABLE_TICK_MARKS = false
        protected const val DEFAULT_SPLIT_TRACK = false

        protected const val DEFAULT_KEY_VALUE_INCREMENT = 1.0
        protected const val DEFAULT_KEY_Value_MAX_STEPS = 20

        protected const val DEFAULT_MIRROR_FOR_RTL = false

        protected const val DEFAULT_MIN_WIDTH = 24
        protected const val DEFAULT_MAX_WIDTH = 48
        protected const val DEFAULT_MIN_HEIGHT = 24
        protected const val DEFAULT_MAX_HEIGHT = 48

        const val HORIZONTAL = 0
        const val VERTICAL = 1
        protected const val DEFAULT_ORIENTATION = HORIZONTAL

        protected const val NO_ALPHA = 0xFF
        protected const val DEFAULT_USE_DISABLED_ALPHA = true
        protected const val DEFAULT_DISABLED_ALPHA = 0.5f

        protected const val TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200L

        /**
         * Parses a [android.graphics.PorterDuff.Mode] from a tintMode
         * attribute's enum value.
         */
        protected fun parseTintMode(
            value: Int,
            defaultTintMode: PorterDuff.Mode = DEFAULT_PORTER_DUFF_TINT_MODE
        ): PorterDuff.Mode {
            return when (value) {
                DEFAULT_TINT_MODE -> defaultTintMode
                3 -> PorterDuff.Mode.SRC_OVER
                5 -> PorterDuff.Mode.SRC_IN
                9 -> PorterDuff.Mode.SRC_ATOP
                14 -> PorterDuff.Mode.MULTIPLY
                15 -> PorterDuff.Mode.SCREEN
                16 -> PorterDuff.Mode.ADD
                else -> DEFAULT_PORTER_DUFF_TINT_MODE
            }
        }

        /**
         * Returns  `true` if the target drawable needs to be tileified.
         *
         * Doesn't work with API 28 (Pie)
         *
         * @param   drawable the drawable to check
         * @return  `true` if the target drawable needs to be tileified,
         *          `false` otherwise
         */
        protected fun needsTileify(drawable: Drawable?): Boolean {
            if (drawable is LayerDrawable) {
                val n = drawable.numberOfLayers
                for (i in 0 until n)
                    if (needsTileify(drawable.getDrawable(i)))
                        return true
                return false
            }

            if (drawable is StateListDrawable) {
                return when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val n = drawable.stateCount
                        for (i in 0 until n)
                            if (needsTileify(drawable.getStateDrawable(i)))
                                return true
                        false
                    }
                    // Methods getStateCount and getStateDrawable were protected before Q
                    else -> {
                        val n = getStateCount.invoke(drawable) as Int
                        for (i in 0 until n)
                            if (needsTileify(getStateDrawable.invoke(drawable, i) as Drawable))
                                return true
                        false
                    }
                }
            }

            // If there's a bitmap that's not wrapped with a ClipDrawable or
            // ScaleDrawable, we'll need to wrap it and apply tiling.
            return drawable is BitmapDrawable
        }
    }

    // region constructor and attribute initialization
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = DEFAULT_STYLE_ATTRIBUTES
    ) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, DEFAULT_STYLE_RESOURCE)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs, defStyleAttr, defStyleRes)
    }

    @SuppressLint("ResourceType", "Recycle")
    private fun initView(attrs: AttributeSet) {
        val attributes = intArrayOf(
            android.R.attr.background,
            android.R.attr.padding,
            android.R.attr.paddingLeft,
            android.R.attr.paddingTop,
            android.R.attr.paddingBottom,
            android.R.attr.paddingRight,
            android.R.attr.paddingStart,
            android.R.attr.paddingEnd,
            android.R.attr.focusable
        )

        val a = context.obtainStyledAttributes(
            attrs,
            attributes,
            DEFAULT_STYLE_ATTRIBUTES,
            DEFAULT_STYLE_RESOURCE
        )
        a.apply {
            try {
                background = getDrawable(0)

                val p = getDimensionPixelSize(1, -1)
                val pl = getDimensionPixelSize(2, p)
                val pt = getDimensionPixelSize(3, p)
                val pr = getDimensionPixelSize(4, p)
                val pb = getDimensionPixelSize(5, p)
                val ps = getDimensionPixelSize(6, -1)
                val pe = getDimensionPixelSize(7, -1)
                if (ps != -1 || pe != -1)
                    setPaddingRelative(ps, pt, pe, pb)
                else
                    setPadding(pl, pt, pr, pb)

                isFocusable = getBoolean(8, true)
            } finally {
                recycle()
            }
        }
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        noInvalidate = true

        // Parse some of View attributes when style is not explicitly specified
        if (attrs != null && defStyleRes == DEFAULT_STYLE_RESOURCE)
            initView(attrs)

        // Attribute initialization
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TwowaySeekBar,
            defStyleAttr,
            defStyleRes
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                saveAttributeDataForStyleable(
                    context,
                    R.styleable.TwowaySeekBar,
                    attrs,
                    this,
                    defStyleAttr,
                    defStyleRes
                )

            try {
                val trackDrawable: Drawable? = getDrawable(R.styleable.TwowaySeekBar_track)
                if (trackDrawable != null) {
                    // Calling track.set can set maxHeight, so make sure the corresponding
                    // XML attribute for maxHeight is read after this.
                    track =
                        if (needsTileify(trackDrawable))
                            tileify(trackDrawable)
                        else
                            trackDrawable
                }

                val progressDrawable: Drawable? = getDrawable(R.styleable.TwowaySeekBar_progress)
                if (progressDrawable != null) {
                    // Calling progress.set can set maxHeight, so make sure the corresponding
                    // XML attribute for maxHeight is read after this.
                    progress =
                        if (needsTileify(progressDrawable))
                            tileify(progressDrawable)
                        else
                            progressDrawable
                }

                minWidth = getDimensionPixelSize(
                    R.styleable.TwowaySeekBar_minWidth,
                    getDimensionPixelSize(
                        R.styleable.TwowaySeekBar_android_minWidth,
                        DEFAULT_MIN_WIDTH
                    )
                )
                maxWidth = getDimensionPixelSize(
                    R.styleable.TwowaySeekBar_maxWidth,
                    getDimensionPixelSize(
                        R.styleable.TwowaySeekBar_android_maxWidth,
                        DEFAULT_MAX_WIDTH
                    )
                )
                minHeight = getDimensionPixelSize(
                    R.styleable.TwowaySeekBar_minHeight,
                    getDimensionPixelSize(
                        R.styleable.TwowaySeekBar_android_minHeight,
                        DEFAULT_MIN_HEIGHT
                    )
                )
                maxHeight = getDimensionPixelSize(
                    R.styleable.TwowaySeekBar_maxHeight,
                    getDimensionPixelSize(
                        R.styleable.TwowaySeekBar_android_maxHeight,
                        DEFAULT_MAX_HEIGHT
                    )
                )

                mirrorForRtl = getBoolean(
                    R.styleable.TwowaySeekBar_mirrorForRtl,
                    getBoolean(
                        R.styleable.TwowaySeekBar_android_mirrorForRtl,
                        DEFAULT_MIRROR_FOR_RTL
                    )
                )

                if (hasValue(R.styleable.TwowaySeekBar_trackTintMode)) {
                    trackTintMode = parseTintMode(
                        getInt(
                            R.styleable.TwowaySeekBar_trackTintMode,
                            DEFAULT_TINT_MODE
                        ),
                        DEFAULT_TRACK_TINT_MODE
                    )
                    hasTrackTintMode = true
                }

                if (hasValue(R.styleable.TwowaySeekBar_trackTint)) {
                    trackTintList = getColorStateList(R.styleable.TwowaySeekBar_trackTint)
                    hasTrackTint = true
                }

                if (hasValue(R.styleable.TwowaySeekBar_progressTintMode)) {
                    progressTintMode = parseTintMode(
                        getInt(
                            R.styleable.TwowaySeekBar_progressTintMode,
                            DEFAULT_TINT_MODE
                        ),
                        DEFAULT_PROGRESS_TINT_MODE
                    )
                    hasProgressTintMode = true
                }

                if (hasValue(R.styleable.TwowaySeekBar_progressTint)) {
                    progressTintList =
                        getColorStateList(R.styleable.TwowaySeekBar_progressTint)
                    hasProgressTint = true
                }

                tickMark = getDrawable(R.styleable.TwowaySeekBar_tickMark)

                if (hasValue(R.styleable.TwowaySeekBar_tickMarkTintMode)) {
                    tickMarkTintMode = parseTintMode(
                        getInt(
                            R.styleable.TwowaySeekBar_tickMarkTintMode,
                            DEFAULT_TINT_MODE
                        ),
                        DEFAULT_TICK_MARK_TINT_MODE
                    )
                    hasTickMarkTintMode = true
                }

                if (hasValue(R.styleable.TwowaySeekBar_tickMarkTint)) {
                    tickMarkTintList =
                        getColorStateList(R.styleable.TwowaySeekBar_tickMarkTint)
                    hasTickMarkTint = true
                }

                enableTickMarks =
                    getBoolean(R.styleable.TwowaySeekBar_enableTickMarks, DEFAULT_ENABLE_TICK_MARKS)

                thumb = getDrawable(R.styleable.TwowaySeekBar_thumb)
                    ?: getDrawable(R.styleable.TwowaySeekBar_android_thumb)

                if (hasValue(R.styleable.TwowaySeekBar_thumbTintMode)) {
                    thumbTintMode = parseTintMode(
                        getInt(
                            R.styleable.TwowaySeekBar_thumbTintMode,
                            DEFAULT_TINT_MODE
                        ),
                        DEFAULT_THUMB_TINT_MODE
                    )
                    hasThumbTintMode = true
                }

                if (hasValue(R.styleable.TwowaySeekBar_thumbTint)) {
                    thumbTintList = getColorStateList(R.styleable.TwowaySeekBar_thumbTint)
                    hasThumbTint = true
                }

                splitTrack =
                    getBoolean(R.styleable.TwowaySeekBar_splitTrack, DEFAULT_SPLIT_TRACK)

                // Guess thumb offset if thumb != null, but allow layout to override.
                this@TwowaySeekBar.thumbOffset = getDimensionPixelOffset(
                    R.styleable.TwowaySeekBar_thumbOffset,
                    getDimensionPixelOffset(
                        R.styleable.TwowaySeekBar_android_thumbOffset,
                        this@TwowaySeekBar.thumbOffset
                    )
                )

                min = getFloat(R.styleable.TwowaySeekBar_min, DEFAULT_MIN).toDouble()
                zero = getFloat(R.styleable.TwowaySeekBar_zero, DEFAULT_ZERO).toDouble()
                max = getFloat(R.styleable.TwowaySeekBar_max, DEFAULT_MAX).toDouble()

                if (hasValue(R.styleable.TwowaySeekBar_keyValueIncrement))
                    keyValueIncrement = getFloat(
                        R.styleable.TwowaySeekBar_keyValueIncrement,
                        DEFAULT_KEY_VALUE_INCREMENT.toFloat()
                    ).toDouble()

                value = getFloat(R.styleable.TwowaySeekBar_value, DEFAULT_Value)
                    .toDouble()

                if (hasValue(R.styleable.TwowaySeekBar_normalizedValue))
                    normalizedValue = getFloat(
                        R.styleable.TwowaySeekBar_normalizedValue,
                        DEFAULT_NORMALIZED_VALUE
                    ).toDouble()
                oldNormalizedValue = normalizedValue
            } finally {
                val useDisabledAlpha: Boolean = getBoolean(
                    R.styleable.TwowaySeekBar_useDisabledAlpha,
                    DEFAULT_USE_DISABLED_ALPHA
                )
                recycle()

                if (useDisabledAlpha)
                    context.obtainStyledAttributes(
                        attrs,
                        intArrayOf(android.R.attr.disabledAlpha),
                        defStyleAttr,
                        defStyleRes
                    ).apply {
                        disabledAlpha = getFloat(0, DEFAULT_DISABLED_ALPHA)
                        recycle()
                    }
                else
                    disabledAlpha = 1f
            }
        }

        noInvalidate = false

        applyProgressTint()
        applyTrackTint()
        applyThumbTint()
        applyTickMarkTint()

        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        // If not explicitly specified this view is important for accessibility.
        if (importantForAccessibility == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        }
    }
    // endregion

    // region protected properties
    @JvmField
    protected var hasTrackTint = false
    @JvmField
    protected var hasTrackTintMode = false

    @JvmField
    protected var hasProgressTint = false
    @JvmField
    protected var hasProgressTintMode = false

    @JvmField
    protected var hasTickMarkTint = false
    @JvmField
    protected var hasTickMarkTintMode = false

    @JvmField
    protected var hasThumbTint = false
    @JvmField
    protected var hasThumbTintMode = false

    @JvmField
    protected var disabledAlpha = DEFAULT_DISABLED_ALPHA

    @JvmField
    protected var listener: OnValueChangeListener? = null

    @JvmField
    protected var noInvalidate = true
    @JvmField
    protected var inDrawing = false

    @JvmField
    protected var touchDownX = 0f
    @JvmField
    protected var scaledTouchSlop = 0
    @JvmField
    protected var isDragging = false
    /**
     * On touch, this offset plus the normalized value from the
     * position of the touch will form the value. Usually 0.
     */
    @JvmField
    protected var touchValueOffset = 0.0

    @JvmField
    protected val thumbRect = Rect()
    @JvmField
    protected val gestureExclusionRects = ArrayList<Rect>()

    @JvmField
    protected var oldNormalizedValue: Double = DEFAULT_NORMALIZED_VALUE.toDouble()
    @JvmField
    protected var sampleWidth = 0
    @JvmField
    protected var rtlResolve = true
    // endregion

    // region public properties
    var track: Drawable? = null
        /**
         * Sets the track that will be drawn for the [thumb] to slide on
         *
         * @param value Drawable representing the track
         */
        @Throws(NullPointerException::class)
        set(value) {
            if (field === value)
                return

            if (field != null) {
                field!!.callback = null
                unscheduleDrawable(field)
            }

            field = value

            if (value != null) {
                value.callback = this

                DrawableCompat.setLayoutDirection(value, layoutDirection)

                if (value.isStateful)
                    value.state = drawableState

                // Make sure the track is always tall enough
                val drawableHeight: Int = value.minimumHeight
                if (maxHeight < drawableHeight) {
                    maxHeight = drawableHeight
                    requestLayout()
                }

                applyTrackTint()
            }

            updateTrackBounds(width, height)
            invalidate()
        }

    var trackTintList: ColorStateList? = null
        /**
         * Applies a tint to the track, if one exists. Does not
         * modify the current tint mode, which is [PorterDuff.Mode.SRC_ATOP]
         * by default.
         * <p>
         * Subsequent calls to [TwowaySeekBar.track] will automatically
         * mutate the drawable and apply the specified tint and tint mode using
         * [Drawable.setTintList].
         *
         * @param value the tint to apply, may be `null` to clear tint
         *
         * @attr ref [R.styleable.TwowaySeekBar_trackTint]
         * @see TwowaySeekBar.trackTintList
         * @see Drawable.setTintList
         */
        set(value) {
            field = value
            hasTrackTint = true
            applyTrackTint()
        }

    var trackTintMode: PorterDuff.Mode = DEFAULT_TRACK_TINT_MODE
        /**
         * Specifies the blending mode used to apply the tint specified by
         * [TwowaySeekBar.trackTintList] to the track.
         * The default mode is [PorterDuff.Mode.SRC_ATOP].
         *
         * @param value the blending mode used to apply the tint, may be
         *        `null` to clear tint
         *
         * @attr ref [R.styleable.TwowaySeekBar_trackTintMode]
         * @see TwowaySeekBar.trackTintMode
         * @see Drawable.setTintMode
         */
        set(value) {
            field = value
            hasTrackTintMode = true
            applyTrackTint()
        }

    var progress: Drawable? = null
        /**
         * Sets the progress that will be drawn on top of [track]
         *
         * @param value Drawable representing the track
         */
        @Throws(NullPointerException::class)
        set(value) {
            if (field === value)
                return

            if (field != null) {
                field!!.callback = null
                unscheduleDrawable(field)
            }

            field = value

            if (value != null) {
                value.callback = this

                DrawableCompat.setLayoutDirection(value, layoutDirection)

                if (value.isStateful)
                    value.state = drawableState

                // Make sure the track is always tall enough
                val drawableHeight: Int = value.minimumHeight
                if (maxHeight < drawableHeight) {
                    maxHeight = drawableHeight
                    requestLayout()
                }

                applyProgressTint()
            }

            updateTrackBounds(width, height)
            invalidate()
        }

    var progressTintList: ColorStateList? = null
        /**
         * Applies a tint to the [progress] drawable. Does not modify
         * the current tint mode, which is [PorterDuff.Mode.SRC_IN] by default.
         * <p>
         * Subsequent calls to [TwowaySeekBar.progress] will automatically
         * mutate the drawable and apply the specified tint and tint mode using
         * [Drawable.setTintList].
         *
         * @param value the tint to apply, may be `null` to clear tint
         *
         * @attr ref [R.styleable.TwowaySeekBar_trackTint]
         * @see TwowaySeekBar.progressTintList
         * @see Drawable.setTintList
         */
        set(value) {
            field = value
            hasProgressTint = true
            applyProgressTint()
        }

    var progressTintMode: PorterDuff.Mode = DEFAULT_PROGRESS_TINT_MODE
        /**
         * Specifies the blending mode used to apply the tint specified by
         * [TwowaySeekBar.progressTintList] to the [progress] drawable.
         * The default mode is [PorterDuff.Mode.SRC_IN].
         *
         * @param value the blending mode used to apply the tint, may be
         *        `null` to clear tint
         *
         * @attr ref [R.styleable.TwowaySeekBar_trackTintMode]
         * @see TwowaySeekBar.progressTintMode
         * @see Drawable.setTintMode
         */
        set(value) {
            field = value
            hasProgressTintMode = true
            applyProgressTint()
        }

    var tickMark: Drawable? = null
        /**
         * Sets the drawable displayed at each track position, e.g. at each
         * possible thumb position.
         *
         * @param value the drawable to display at each track position
         */
        set(value) {
            field?.callback = null

            field = value

            if (value != null) {
                value.callback = this

                DrawableCompat.setLayoutDirection(value, layoutDirection)

                if (value.isStateful)
                    value.state = drawableState

                applyTickMarkTint()
            }

            invalidate()
        }

    var tickMarkTintList: ColorStateList? = null
        /**
         * Applies a tint to the tick mark drawable. Does not modify the current tint
         * mode, which is [PorterDuff.Mode.SRC_IN] by default.
         * <p>
         * Subsequent calls to [TwowaySeekBar.tickMark] will automatically
         * mutate the drawable and apply the specified tint and tint mode using
         * [Drawable.setTintList].
         *
         * @param value the tint to apply, may be `null` to clear tint
         *
         * @attr ref [R.styleable.TwowaySeekBar_tickMarkTint]
         * @see TwowaySeekBar.tickMarkTintList
         * @see Drawable.setTintList
         */
        set(value) {
            field = value
            hasTickMarkTint = true
            applyTickMarkTint()
        }

    var tickMarkTintMode: PorterDuff.Mode = DEFAULT_TICK_MARK_TINT_MODE
        /**
         * Specifies the blending mode used to apply the tint specified by
         * [TwowaySeekBar.tickMarkTintList] to the tick mark drawable. The
         * default mode is [PorterDuff.Mode.SRC_IN].
         *
         * @param value the blending mode used to apply the tint, may be
         *        `null` to clear tint
         *
         * @attr ref [R.styleable.TwowaySeekBar_tickMarkTintMode]
         * @see TwowaySeekBar.tickMarkTintMode
         * @see Drawable.setTintMode
         */
        set(value) {
            field = value
            hasTickMarkTintMode = true
            applyTickMarkTint()
        }

    var thumb: Drawable? = null
        /**
         * Sets the thumb that will be drawn on the [track] within the TwowaySeekBar.
         *
         * If the thumb is a valid drawable (i.e. not null), half its width will be
         * used as the new thumb offset [thumbOffset].
         *
         * @param value Drawable representing the thumb
         */
        @Throws(NullPointerException::class)
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

                DrawableCompat.setLayoutDirection(value, layoutDirection)

                // Assuming the thumb drawable is symmetric, set the thumb offset
                // such that the thumb will hang halfway off either edge of the track.
                thumbOffset = value.intrinsicWidth / 2

                // If we're updating get the new states
                if (needUpdate &&
                    (value.intrinsicWidth != field!!.intrinsicWidth ||
                            value.intrinsicHeight != field!!.intrinsicHeight)
                )
                    requestLayout()
            }

            field = value
            applyThumbTint()
            invalidate()

            if (needUpdate) {
                updateBounds(width, height)
                if (value != null && value.isStateful) {
                    // Note that if the states are different this won't work.
                    // For now, let's consider that an app bug.
                    value.state = drawableState
                }
            }
        }

    var thumbTintList: ColorStateList? = null
        /**
         * Applies a tint to the thumb drawable. Does not modify the current tint
         * mode, which is [PorterDuff.Mode.SRC_IN] by default.
         * <p>
         * Subsequent calls to [TwowaySeekBar.thumb] will automatically
         * mutate the drawable and apply the specified tint and tint mode using
         * [Drawable.setTintList].
         *
         * @param value the tint to apply, may be `null` to clear tint
         *
         * @attr ref [R.styleable.TwowaySeekBar_thumbTint]
         * @see TwowaySeekBar.thumbTintList
         * @see Drawable.setTintList
         */
        set(value) {
            field = value
            hasThumbTint = true
            applyThumbTint()
        }

    var thumbTintMode: PorterDuff.Mode = DEFAULT_THUMB_TINT_MODE
        /**
         * Specifies the blending mode used to apply the tint specified by
         * [TwowaySeekBar.thumbTintList] to the thumb drawable. The
         * default mode is [PorterDuff.Mode.SRC_IN].
         *
         * @param value the blending mode used to apply the tint, may be
         *        `null` to clear tint
         *
         * @attr ref [R.styleable.TwowaySeekBar_thumbTintMode]
         * @see TwowaySeekBar.thumbTintMode
         * @see Drawable.setTintMode
         */
        set(value) {
            field = value
            hasThumbTintMode = true
            applyThumbTint()
        }

    var enableTickMarks = DEFAULT_ENABLE_TICK_MARKS
        set(value) {
            field = value
            invalidate()
        }

    var min: Double = DEFAULT_MIN.toDouble()
        set(value) {
            val progress = this.value

            field = min(value, max)

            if (zero < field)
                zero = field

            // This already calls .invalidate()
            this.value = progress

            val range = max - field
            if (keyValueIncrement == 0.0 || range / keyValueIncrement > DEFAULT_KEY_Value_MAX_STEPS) {
                // It will take the user too long to change this via keys, change it
                // to something more reasonable
                keyValueIncrement =
                    max(DEFAULT_KEY_VALUE_INCREMENT, range / DEFAULT_KEY_Value_MAX_STEPS)
            }
        }

    var zero: Double = DEFAULT_ZERO.toDouble()
        set(value) {
            field = min(max(min, value), max)
            if (progress != null && thumb != null)
                updateProgressBounds()
            invalidate()
        }

    var max: Double = DEFAULT_MAX.toDouble()
        set(value) {
            val progress = this.value

            field = max(min, value)

            if (field < zero)
                zero = field

            // This already calls .invalidate()
            this.value = progress

            val range = max - field
            if (keyValueIncrement == 0.0 || range / keyValueIncrement > DEFAULT_KEY_Value_MAX_STEPS) {
                // It will take the user too long to change this via keys, change it
                // to something more reasonable
                keyValueIncrement =
                    max(DEFAULT_KEY_VALUE_INCREMENT, range / DEFAULT_KEY_Value_MAX_STEPS)
            }
        }

    /**
     * Value in the range of min..max
     */
    var value: Double
        get() = recoverValue(normalizedValue)
        /**
         * Set the current value.
         */
        set(value) {
            normalizedValue = normalizeValue(value)
        }

    /**
     * Value in tge range of 0..1
     */
    var normalizedValue: Double = DEFAULT_NORMALIZED_VALUE.toDouble()
        /**
         * Set the current value.
         */
        set(value) {
            field = min(max(value, 0.0), 1.1)
            if (progress != null && thumb != null)
                updateThumbAndProgressBounds()
            invalidate()
        }

    /**
     * Whether to call the [OnValueChangeListener] while dragging, or
     * only when user lets go of the [thumb].
     *
     * @see TwowaySeekBar.OnValueChangeListener
     */
    var notifyWhileDragging = false

    /**
     * Specifies whether the [track] should be split by the [thumb]. When true,
     * the thumb's optical bounds will be clipped out of the [track] drawable,
     * then the thumb will be drawn into the resulting gap.
     */
    var splitTrack: Boolean = DEFAULT_SPLIT_TRACK
        set(value) {
            field = value
            invalidate()
        }

    var thumbOffset: Int = 0
        /**
         * Sets the thumb offset that allows the thumb to extend out of the range of
         * the [track].
         *
         * @param value The offset amount in pixels.
         */
        set(value) {
            field = value
            invalidate()
        }

    /**
     * On key presses (right or left), the amount to increment/decrement the value.
     */
    var keyValueIncrement: Double = DEFAULT_KEY_VALUE_INCREMENT
        set(value) {
            field = abs(value)
            if (enableTickMarks)
                invalidate()
        }

    var mirrorForRtl: Boolean = DEFAULT_MIRROR_FOR_RTL
        protected set

    /**
     * Defines the orientation of the seekbar to be drawn.
     */
    var orientation: Int = DEFAULT_ORIENTATION
        /**
         * @param value Pass [HORIZONTAL] or [VERTICAL]. Default
         * value is [HORIZONTAL].
         *
         * @attr ref [R.styleable.TwowaySeekBar_orientation]
         */
        set(value) {
            if (value != field) {
                field = value
                invalidate()
            }
        }

    var minWidth: Int = DEFAULT_MIN_WIDTH
        set(value) {
            field = value
            requestLayout()
        }

    var maxWidth: Int = DEFAULT_MAX_WIDTH
        set(value) {
            field = value
            requestLayout()
        }

    var minHeight: Int = DEFAULT_MIN_HEIGHT
        set(value) {
            field = value
            requestLayout()
        }

    var maxHeight: Int = DEFAULT_MAX_HEIGHT
        set(value) {
            field = value
            requestLayout()
        }
    // endregion

    // region drawable
    @Throws(NullPointerException::class)
    protected fun applyTrackTint() {
        if (track != null && (hasTrackTint || hasTrackTintMode)) {
            track = track!!.mutate()

            if (hasTrackTint)
                DrawableCompat.setTintList(track!!, trackTintList)

            if (hasTrackTintMode)
                DrawableCompat.setTintMode(track!!, trackTintMode)

            // The drawable (or one of its children) may not have been
            // stateful before applying the tint, so let's try again.
            if (track!!.isStateful)
                track!!.state = drawableState
        }
    }

    @Throws(NullPointerException::class)
    protected fun applyProgressTint() {
        if (progress != null && (hasProgressTint || hasProgressTintMode)) {
            progress = progress!!.mutate()

            if (hasProgressTint)
                DrawableCompat.setTintList(progress!!, progressTintList)

            if (hasProgressTintMode)
                DrawableCompat.setTintMode(progress!!, progressTintMode)

            // The drawable (or one of its children) may not have been
            // stateful before applying the tint, so let's try again.
            if (progress!!.isStateful)
                progress!!.state = drawableState
        }
    }

    @Throws(NullPointerException::class)
    protected fun applyTickMarkTint() {
        if (tickMark != null && (hasTickMarkTint || hasTickMarkTintMode)) {
            tickMark = tickMark!!.mutate()

            if (hasTickMarkTint)
                DrawableCompat.setTintList(tickMark!!, tickMarkTintList)

            if (hasTickMarkTintMode)
                DrawableCompat.setTintMode(tickMark!!, tickMarkTintMode)

            // The drawable (or one of its children) may not have been
            // stateful before applying the tint, so let's try again.
            if (tickMark!!.isStateful)
                tickMark!!.state = drawableState
        }
    }

    @Throws(NullPointerException::class)
    protected fun applyThumbTint() {
        if (thumb != null && (hasThumbTint || hasThumbTintMode)) {
            thumb = thumb!!.mutate()

            if (hasThumbTint)
                DrawableCompat.setTintList(thumb!!, thumbTintList)

            if (hasThumbTintMode)
                DrawableCompat.setTintMode(thumb!!, thumbTintMode)

            // The drawable (or one of its children) may not have been
            // stateful before applying the tint, so let's try again.
            if (thumb!!.isStateful)
                thumb!!.state = drawableState
        }
    }

    /**
     * Converts a drawable to a tiled version of itself. It will recursively
     * traverse layer and state list drawables.
     */
    protected fun tileify(drawable: Drawable?, clip: Boolean = false): Drawable? {
        // TODO: This is a terrible idea that potentially destroys any drawable
        // that extends any of these classes. We *really* need to remove this.

        if (drawable is LayerDrawable) {
            val size = drawable.numberOfLayers
            val outDrawables = arrayOfNulls<Drawable>(size)

            for (i in 0 until size)
                outDrawables[i] = tileify(drawable.getDrawable(i), drawable === progress)

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val clone = LayerDrawable(outDrawables)
                for (i in 0 until size) {
                    clone.setId(i, drawable.getId(i))
                    clone.setLayerGravity(i, drawable.getLayerGravity(i))
                    clone.setLayerWidth(i, drawable.getLayerWidth(i))
                    clone.setLayerHeight(i, drawable.getLayerHeight(i))
                    clone.setLayerInsetLeft(i, drawable.getLayerInsetLeft(i))
                    clone.setLayerInsetRight(i, drawable.getLayerInsetRight(i))
                    clone.setLayerInsetTop(i, drawable.getLayerInsetTop(i))
                    clone.setLayerInsetBottom(i, drawable.getLayerInsetBottom(i))
                    clone.setLayerInsetStart(i, drawable.getLayerInsetStart(i))
                    clone.setLayerInsetEnd(i, drawable.getLayerInsetEnd(i))
                }
                clone
            } else
                drawable
        }

        if (drawable is StateListDrawable) {
            val out = StateListDrawable()

            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val n = drawable.stateCount
                    for (i in 0 until n)
                        out.addState(
                            drawable.getStateSet(i),
                            tileify(drawable.getStateDrawable(i), clip)
                        )
                    out
                }
                // Methods getStateCount and getStateDrawable were protected before Q
                else -> {
                    val n = getStateCount.invoke(drawable) as Int
                    for (i in 0 until n)
                        out.addState(
                            getStateSet.invoke(drawable, i) as IntArray,
                            tileify(getStateDrawable.invoke(drawable, i) as Drawable, clip)
                        )
                    out
                }
            }
        }

        if (drawable is BitmapDrawable) {
            val cs = drawable.getConstantState()
            val clone = cs?.newDrawable(super.getResources()) as BitmapDrawable
            clone.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)

            if (sampleWidth <= 0)
                sampleWidth = clone.intrinsicWidth

            return if (clip)
                ClipDrawable(clone, Gravity.START, ClipDrawable.HORIZONTAL)
            else
                clone
        }
        return drawable
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who === track
                || who === progress
                || who === thumb
                || who === tickMark
                || super.verifyDrawable(who)
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        track?.jumpToCurrentState()
        progress?.jumpToCurrentState()
        thumb?.jumpToCurrentState()
        tickMark?.jumpToCurrentState()
    }

    @Throws(NullPointerException::class)
    override fun drawableStateChanged() {
        super.drawableStateChanged()

        if (track != null && track!!.isStateful && track!!.setState(drawableState))
            invalidateDrawable(track!!)

        if (track != null && disabledAlpha < 1f)
            track!!.alpha =
                if (isEnabled) NO_ALPHA else (NO_ALPHA * disabledAlpha).toInt()

        if (progress != null && progress!!.isStateful && progress!!.setState(drawableState))
            invalidateDrawable(progress!!)

        if (thumb != null && thumb!!.isStateful && thumb!!.setState(drawableState))
            invalidateDrawable(thumb!!)

        if (tickMark != null && tickMark!!.isStateful && tickMark!!.setState(drawableState))
            invalidateDrawable(tickMark!!)
    }

    override fun drawableHotspotChanged(x: Float, y: Float) {
        super.drawableHotspotChanged(x, y)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            track?.setHotspot(x, y)
            progress?.setHotspot(x, y)
            thumb?.setHotspot(x, y)
            // tickMark?.setHotspot(x, y)
        }
    }

    /**
     * The [View.onResolveDrawables] method is marked with `@hide`, which
     * forces the android framework to treat it as nonexistent at compile
     * time.
     *
     * If you want to compile code using this (or similar) method, then
     * do either of the two:
     *
     * 1. Modify the source code and recompile the android framework, then
     *    replace \AppData\Local\Android\Sdk\platforms\android-xx\android.jar
     *    with the new compiled framework.
     *
     * 2. Go to \AppData\Local\Android\Sdk\platforms\android-xx and extract
     *    the xyz.class file you want to modify from android.jar. Modify if
     *    using any Java bytecode editor, save and replace the file in android.jar
     *
     * NOTE: Methods marked with `@hide` are not guaranteed to behave
     * similarly across platforms, or even exist, or be accessible, not
     * even in future versions of android!
     */
    @Throws(NullPointerException::class)
    override fun onResolveDrawables(layoutDirection: Int) {
        if (track != null)
            DrawableCompat.setLayoutDirection(track!!, layoutDirection)
        if (progress != null)
            DrawableCompat.setLayoutDirection(progress!!, layoutDirection)
        if (thumb != null)
            DrawableCompat.setLayoutDirection(thumb!!, layoutDirection)
        if (tickMark != null)
            DrawableCompat.setLayoutDirection(tickMark!!, layoutDirection)
    }

    override fun invalidateDrawable(drawable: Drawable) {
        if (!inDrawing) {
            if (verifyDrawable(drawable)) {
                @Suppress("DEPRECATION")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    invalidate()
                else {
                    val dirty = drawable.bounds
                    val scrollX: Int = scrollX + paddingLeft
                    val scrollY: Int = scrollY + paddingTop
                    invalidate(
                        dirty.left + scrollX, dirty.top + scrollY,
                        dirty.right + scrollX, dirty.bottom + scrollY
                    )
                }
            } else {
                super.invalidateDrawable(drawable)
            }
        }
    }

    override fun postInvalidate() {
        if (!noInvalidate)
            super.postInvalidate()
    }

    override fun invalidate() {
        if (!noInvalidate)
            super.invalidate()
    }

    @Suppress("DEPRECATION")
    override fun invalidate(dirty: Rect?) {
        if (!noInvalidate)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                invalidate()
            else
                super.invalidate(dirty)
    }

    @Suppress("DEPRECATION")
    override fun invalidate(l: Int, t: Int, r: Int, b: Int) {
        if (!noInvalidate)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                invalidate()
            else
                super.invalidate(l, t, r, b)
    }
    // endregion

    // region value
    protected fun incrementValueBy(delta: Double): Boolean {
        val value = this.value
        this.value += delta
        return if (value != this.value) {
            callOnValueChange()
            true
        } else false
    }

    /**
     * Converts a normalized value between 0 and 1 to a
     * value between [min] and [max].
     *
     * @param normalized The value to recover
     * @return The recover value
     */
    protected fun recoverValue(normalized: Double): Double {
        return min + normalized * (max - min)
    }

    /**
     * Converts the given number between [min] and [max] to
     * a normalized value between 0 and 1.
     *
     * @param value The value to normalize.
     * @return The normalized double.
     */
    protected fun normalizeValue(value: Double): Double {
        val result = (value - min) / (max - min)
        return if (result.isFinite()) result else 0.0
    }
    // endregion

    // region draw and bounds
    @Synchronized
    @Throws(NullPointerException::class)
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = max(
            minWidth,
            minOf(maxWidth, track?.intrinsicWidth ?: maxWidth, progress?.intrinsicWidth ?: maxWidth)
        )
        var height = maxOf(
            minHeight,
            minOf(
                maxHeight,
                track?.intrinsicHeight ?: maxHeight,
                progress?.intrinsicHeight ?: maxHeight
            ),
            thumb?.intrinsicHeight ?: 0
        )

        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom

        setMeasuredDimension(
            resolveSizeAndState(width, widthMeasureSpec, 0),
            resolveSizeAndState(height, heightMeasureSpec, 0)
        )
    }

    // region draw
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        inDrawing = true

        drawBoundsDebug(canvas)

        drawTrackAndProgress(canvas)
        if (enableTickMarks)
            drawTickMarks(canvas)
        drawThumb(canvas)
        inDrawing = false
    }

    /**
     * Draw drawable bound for debug purposes
     * TODO: Remove this!
     */
    private fun drawBoundsDebug(canvas: Canvas) {
        val saveCount = canvas.save()
        val p = Paint()
        p.color = Color.RED
        canvas.drawRect(track!!.bounds, p)
        p.color = Color.BLUE
        canvas.drawRect(thumb!!.bounds, p)
        canvas.restoreToCount(saveCount)
    }

    /**
     * Draws the [track] and [progress]
     *
     * @param canvas The canvas to draw upon.
     */
    @Suppress("DEPRECATION", "NewApi")
    @Throws(NullPointerException::class)
    protected fun drawTrackAndProgress(canvas: Canvas) {
        val saveCount =
            if (thumb != null && splitTrack) {
                val insets: Insets = thumb!!.opticalInsets
                thumb!!.copyBounds(thumbRect)
                thumbRect.offset(paddingLeft - thumbOffset, paddingTop)
                thumbRect.left += insets.left
                thumbRect.right -= insets.right
                val save = canvas.save()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    canvas.clipOutRect(thumbRect)
                else
                    canvas.clipRect(thumbRect, Region.Op.DIFFERENCE)
                save
            } else
                canvas.save()

        // Translate canvas
        if (isLayoutRtl(this) && mirrorForRtl) {
            canvas.translate(width - paddingRight.toFloat(), paddingTop.toFloat())
            canvas.scale(-1.0f, 1.0f)
        } else
            canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())

        track?.draw(canvas)
        progress?.draw(canvas)

        canvas.restoreToCount(saveCount)
    }

    /**
     * Draws the [tickMark]
     *
     * @param canvas The canvas to draw upon.
     */
    @Throws(NullPointerException::class)
    protected fun drawTickMarks(canvas: Canvas) {
        if (tickMark != null) {
            val count = ((max - min) / keyValueIncrement).toInt()
            if (count > 1) {
                val width: Int = tickMark!!.intrinsicWidth
                val height: Int = tickMark!!.intrinsicHeight
                val halfWidth = if (width >= 0) width / 2 else 1
                val halfHeight = if (height >= 0) height / 2 else 1
                tickMark!!.setBounds(-halfWidth, -halfHeight, halfWidth, halfHeight)

                val saveCount = canvas.save()
                // Translate canvas
                if (isLayoutRtl(this) && mirrorForRtl) {
                    canvas.translate(width - paddingRight.toFloat(), paddingTop.toFloat())
                    canvas.scale(-1.0f, 1.0f)
                } else
                    canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())

                val spacing = ((this.width - paddingLeft - paddingRight) / count).toFloat()
                canvas.translate(0f, this.height / 2f)
                for (i in 0..count) {
                    tickMark!!.draw(canvas)
                    canvas.translate(spacing, 0f)
                }
                canvas.restoreToCount(saveCount)
            }
        }
    }

    /**
     * Draws the [thumb]
     *
     * @param canvas The canvas to draw upon.
     */
    @Throws(NullPointerException::class)
    protected fun drawThumb(canvas: Canvas) {
        if (thumb != null) {
            val saveCount = canvas.save()
            // Translate the padding. For the x, we need to allow the thumb to
            // draw in its extra space
            canvas.translate(paddingLeft - thumbOffset.toFloat(), paddingTop.toFloat())

            thumb!!.draw(canvas)
            canvas.restoreToCount(saveCount)
        }
    }
    // endregion

    // region bounds
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun setHotspot(x: Float, y: Float) =
        background?.setHotspot(x, y)

    protected fun updateTrackBounds(
        width: Int,
        height: Int
    ) {
        // onDraw will translate the canvas so we draw starting at 0,0.
        // Subtract out padding for the purposes of the calculations below.
        val right = width - paddingRight + paddingLeft
        val top = 0
        val bottom = height - paddingTop + paddingBottom
        val left = 0

        track?.setBounds(left, top, right, bottom)
    }

    @Throws(NullPointerException::class)
    protected fun updateBounds(width: Int, height: Int) {
        val paddedHeight: Int = height - paddingTop - paddingBottom

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

        val trackWidth: Int = width - paddingRight - paddingLeft

        if (track != null)
            track?.setBounds(0, trackOffset, trackWidth, trackOffset + trackHeight)

        if (thumb != null)
            updateThumbAndProgressBounds(trackWidth, trackOffset, thumbOffset)
    }

    /**
     * Updates the [thumb] and [progress] drawable bounds.
     *
     * @param trackWidth Width of the view, excluding padding
     * @param thumbOffset Vertical offset for centering the [thumb].
     * If set to [Int.MIN_VALUE], the current offset will be used.
     * @param trackOffset Vertical offset for centering the [progress].
     * If set to [Int.MIN_VALUE], the current offset will be used.
     */
    @Throws(NullPointerException::class)
    protected fun updateThumbAndProgressBounds(
        trackWidth: Int = width - paddingRight - paddingLeft,
        trackOffset: Int = Int.MIN_VALUE,
        thumbOffset: Int = Int.MIN_VALUE
    ) {
        val thumbWidth = thumb!!.intrinsicWidth
        val thumbHeight = thumb!!.intrinsicHeight

        // The extra space for the thumb to move on the track
        val available = trackWidth - thumbWidth + this.thumbOffset * 2

        val thumbPos = (normalizedValue * available + 0.5f).toInt()

        val top: Int
        val bottom: Int
        if (thumbOffset == Int.MIN_VALUE) {
            val oldBounds = thumb!!.bounds
            top = oldBounds.top
            bottom = oldBounds.bottom
        } else {
            top = thumbOffset
            bottom = thumbOffset + thumbHeight
        }

        val left = if (isLayoutRtl(this) && mirrorForRtl) available - thumbPos else thumbPos

        val right = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val right = left + thumbWidth
            if (background != null) {
                val offsetX: Int = paddingLeft - this.thumbOffset
                val offsetY: Int = paddingTop
                background.setHotspotBounds(
                    left + offsetX, top + offsetY,
                    right + offsetX, bottom + offsetY
                )
            }
            right
        } else {
            left + thumbWidth
        }

        // Canvas will be translated, so 0,0 is where we start drawing
        thumb!!.setBounds(left, top, right, bottom)

        updateProgressBounds(trackWidth, left, trackOffset)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            updateGestureExclusionRects()
    }

    protected fun updateProgressBounds(
        trackWidth: Int = width - paddingRight - paddingLeft,
        thumbPos: Int = thumb!!.bounds.left,
        trackOffset: Int = Int.MIN_VALUE,
        callBackToApp: Boolean = false
    ) {
        val paddedHeight: Int = height - paddingTop - paddingBottom
        val trackHeight = min(maxHeight, paddedHeight)

        val top: Int
        val bottom: Int
        if (trackOffset == Int.MIN_VALUE) {
            val oldBounds = progress!!.bounds
            top = oldBounds.top
            bottom = oldBounds.bottom
        } else {
            top = trackOffset
            bottom = trackOffset + trackHeight
        }

        val startPos: Int = (normalizeValue(zero) * trackWidth).toInt()

        if (progress != null)
            if (zero < value)
                progress?.setBounds(startPos, top, thumbPos, bottom)
            else
                progress?.setBounds(thumbPos, top, startPos, bottom)

        if (callBackToApp && (getInstance.invoke(context) as AccessibilityManager).isEnabled)
            scheduleAccessibilityEventSender()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        updateTrackBounds(width, height)
        updateBounds(width, height)
    }
    // endregion
    // endregion

    // region events
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                if (isInScrollingContainer)
                    touchDownX = event.x
                else
                    startDrag(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging)
                    trackTouchEvent(event)
                else
                    startDrag(event)

                if (notifyWhileDragging)
                    callOnValueChange()
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    trackTouchEvent(event)
                    isDragging = false
                    isPressed = false
                } else {
                    // Touch up when we never crossed the touch slop threshold should
                    // be interpreted as a tap-seek to that location.
                    isDragging = true
                    trackTouchEvent(event)
                    isDragging = false
                }

                // SeekBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate()

                callOnValueChange()
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

    protected fun startDrag(event: MotionEvent) {
        isPressed = true

        @Suppress("DEPRECATION")
        if (thumb != null) // This may be within the padding region.
            invalidate(thumb!!.bounds)

        isDragging = true
        trackTouchEvent(event)
        attemptClaimDrag()
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    protected fun attemptClaimDrag() {
        parent?.requestDisallowInterceptTouchEvent(true)
    }

    protected fun trackTouchEvent(event: MotionEvent) {
        val x = event.x.roundToInt()
        val y = event.y.roundToInt()
        val width = width
        val availableWidth: Int = width - paddingLeft - paddingRight
        val scales =
            if (isLayoutRtl(this) && mirrorForRtl)
                doubleArrayOf(
                    1.0,
                    0.0,
                    (availableWidth - x + paddingLeft) / availableWidth.toDouble()
                )
            else
                doubleArrayOf(0.0, 1.0, (x - paddingLeft) / availableWidth.toDouble())

        var value = 0.0
        val scale = when {
            x < paddingLeft ->
                scales[0]
            x > width - paddingRight ->
                scales[1]
            else -> {
                value = touchValueOffset
                scales[2]
            }
        }

        value += scale * (max - min) + min

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setHotspot(x.toFloat(), y.toFloat())
        incrementValueBy(value - this.value)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (isEnabled) {
            val increment = if (isLayoutRtl(this)) -keyValueIncrement else keyValueIncrement
            when (keyCode) {
                KEYCODE_DPAD_LEFT, KEYCODE_MINUS ->
                    incrementValueBy(-increment)
                KEYCODE_DPAD_RIGHT, KEYCODE_PLUS, KEYCODE_EQUALS ->
                    incrementValueBy(increment)
                else ->
                    false
            } || super.onKeyDown(keyCode, event)
        } else super.onKeyDown(keyCode, event)
    }
    // endregion

    // region instance state
    protected class SavedState : BaseSavedState {
        @JvmField
        var value = 0.0

        /**
         * Constructor called from [TwowaySeekBar.onSaveInstanceState]
         */
        constructor(superState: Parcelable?, seekBar: TwowaySeekBar) : super(superState) {
            this.value = seekBar.value
        }

        /**
         * Called from [TwowaySeekBar.onRestoreInstanceState] to restore state
         */
        fun restore(seekBar: TwowaySeekBar) {
            seekBar.value = this.value
        }

        /**
         * Constructor called from [.CREATOR]
         */
        private constructor(parcel: Parcel) : super(parcel) {
            value = parcel.readDouble()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeDouble(value)
        }

        override fun describeContents(): Int {
            return value.hashCode()
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(super.onSaveInstanceState(), this)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        savedState.restore(this)
    }
    // endregion

    // region listener
    /**
     * Callback listener interface to notify about changed range values.
     */
    @FunctionalInterface
    interface OnValueChangeListener {
        /**
         * Notification that the seekbar value has changed.
         *
         * @param seekBar The SeekBar whose value has changed
         * @param value The current value. This will be in the range of min..max
         * @param normalizedValue The current value. This will be in the range of 0..1
         */
        fun onValueChanged(seekBar: TwowaySeekBar?, value: Double, normalizedValue: Double)
    }

    /**
     * Sets a listener to receive notifications of changes to the SeekBar's value.
     *
     * @param listener The seek bar notification listener
     *
     * @see TwowaySeekBar.OnValueChangeListener
     */
    fun setOnValueChangeListener(listener: OnValueChangeListener?) {
        this.listener = listener
    }

    /**
     * Sets a listener to receive notifications of changes to the SeekBar's value.
     *
     * @param listener The seek bar notification listener
     *
     * @see TwowaySeekBar.OnValueChangeListener
     */
    inline fun onValueChange(crossinline listener: (seekBar: TwowaySeekBar?, value: Double, normalizedValue: Double) -> Unit) {
        setOnValueChangeListener(object : OnValueChangeListener {
            override fun onValueChanged(
                seekBar: TwowaySeekBar?,
                value: Double,
                normalizedValue: Double
            ) {
                listener(seekBar, value, normalizedValue)
            }
        })
    }

    protected fun callOnValueChange() {
        if (oldNormalizedValue == normalizedValue) return
        listener?.onValueChanged(this, value, normalizedValue)
        oldNormalizedValue = normalizedValue
    }
    // endregion

    // region other
    // region gesture
    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun updateGestureExclusionRects(rects: List<Rect> = Collections.emptyList()) {
        if (thumb == null) {
            super.setSystemGestureExclusionRects(rects)
            return
        }
        gestureExclusionRects.clear()
        thumb?.copyBounds(thumbRect)
        gestureExclusionRects.add(thumbRect)
        gestureExclusionRects.addAll(rects)
        super.setSystemGestureExclusionRects(gestureExclusionRects)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun setSystemGestureExclusionRects(rects: List<Rect>) {
        Preconditions.checkNotNull(rects, "rects must not be null")
        updateGestureExclusionRects(rects)
    }
    // endregion

    // region rtl
    override fun getPaddingLeft(): Int {
        rtlResolve = false
        val result = super.getPaddingLeft()
        rtlResolve = true
        return result
    }

    override fun getPaddingTop(): Int {
        rtlResolve = false
        val result = super.getPaddingTop()
        rtlResolve = true
        return result
    }

    override fun getPaddingRight(): Int {
        rtlResolve = false
        val result = super.getPaddingRight()
        rtlResolve = true
        return result
    }

    override fun getPaddingBottom(): Int {
        rtlResolve = false
        val result = super.getPaddingBottom()
        rtlResolve = true
        return result
    }

    override fun getPaddingStart(): Int {
        rtlResolve = false
        val result = super.getPaddingStart()
        rtlResolve = true
        return result
    }

    override fun getPaddingEnd(): Int {
        rtlResolve = false
        val result = super.getPaddingEnd()
        rtlResolve = true
        return result
    }

    @Throws(NullPointerException::class)
    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        if (thumb != null && rtlResolve) {
            updateThumbAndProgressBounds()
            // Since we draw translated, the drawable's bounds that it signals
            // for invalidation won't be the actual bounds we want invalidated,
            // so just invalidate this whole view.
            invalidate()
        }
    }
    // endregion

    // region accessibility
    /**
     * Command for sending an accessibility event.
     */
    protected class AccessibilityEventSender(val twowaySeekBar: TwowaySeekBar) : Runnable {
        override fun run() =
            twowaySeekBar.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
    }

    private var accessibilityEventSender: AccessibilityEventSender? = null

    override fun onDetachedFromWindow() {
        if (accessibilityEventSender != null)
            removeCallbacks(accessibilityEventSender)
        accessibilityEventSender = null
        super.onDetachedFromWindow()
    }

    /**
     * Schedule a command for sending an accessibility event.
     *
     * Note: A command is used to ensure that accessibility events
     * are sent at most one in a given time frame to save
     * system resources while the progress changes quickly.
     */
    protected fun scheduleAccessibilityEventSender() {
        accessibilityEventSender = AccessibilityEventSender(this)
        removeCallbacks(accessibilityEventSender)
        postDelayed(accessibilityEventSender, TIMEOUT_SEND_ACCESSIBILITY_EVENT)
        accessibilityEventSender = null
    }

    override fun getAccessibilityClassName(): CharSequence? = TwowaySeekBar::class.qualifiedName

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = accessibilityClassName
        event.itemCount = (max - min).toInt()
        event.currentItemIndex = value.toInt()
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.rangeInfo =
            RangeInfo.obtain(
                RANGE_TYPE_INT,
                min.toFloat(),
                max.toFloat(),
                value.toFloat()
            )

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            info.className = accessibilityClassName
            if (isEnabled) {
                if (value > min)
                    info.addAction(ACTION_SCROLL_BACKWARD)

                if (value < max)
                    info.addAction(ACTION_SCROLL_FORWARD)
            }
        } else {
            if (isEnabled) {
                if (value > min)
                    info.addAction(ACCESSIBILITY_ACTION_SCROLL_BACKWARD)

                if (value < max)
                    info.addAction(ACCESSIBILITY_ACTION_SCROLL_FORWARD)
            }
        }
    }

    override fun performAccessibilityAction(action: Int, arguments: Bundle?): Boolean {
        return when {
            super.performAccessibilityAction(action, arguments) -> true
            !isEnabled -> false
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    && action == android.R.id.accessibilityActionSetProgress ->
                if (arguments == null || !arguments.containsKey(ACTION_ARGUMENT_PROGRESS_VALUE))
                    false
                else
                    incrementValueBy(arguments.getFloat(ACTION_ARGUMENT_PROGRESS_VALUE).toDouble() - value)
            action == ACTION_SCROLL_FORWARD || action == ACTION_SCROLL_BACKWARD ->
                incrementValueBy(if (action == ACTION_SCROLL_FORWARD) keyValueIncrement else -keyValueIncrement)
            else -> false
        }
    }
    // endregion
    // endregion

}