package com.azyoot.relearn.ui.animation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.util.TypedValue
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.azyoot.relearn.R


class AnimatedTumbleweed(val context: Context) : Drawable(), Animatable {

    private val tumbleweedDrawable =
        ContextCompat.getDrawable(context, R.drawable.tumbleweed) as VectorDrawable
    private val groundDrawable =
        ContextCompat.getDrawable(context, R.drawable.half_circle) as VectorDrawable
    private val backgroundDrawable =
        ContextCompat.getDrawable(context, R.drawable.tumbleweed_background) as VectorDrawable

    private val animatorSet = AnimatorSet()

    private val bounceInterpolator = CustomBouncesInterpolator(
        listOf(
            CustomBounceKeyFrame(0.3F, 0.3F),
            CustomBounceKeyFrame(0.6F, 0.4F)
        )
    )

    private val translateXAnimator =
        ObjectAnimator.ofFloat(TRANSLATE_X_RATIO, -TRANSLATE_X_RATIO).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 1700
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

    private val rotateAnimator = ObjectAnimator.ofFloat(0F, 360F).apply {
        interpolator = LinearInterpolator()
        duration = 3000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
    }

    private val bounceAnimator = ObjectAnimator.ofFloat(-BOUNCE_HEIGHT_FACTOR, -0F).apply {
        interpolator = bounceInterpolator
        duration = 1500
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
    }

    private val backgroundAnimator = ObjectAnimator.ofFloat(0F, 100F).apply {
        interpolator = LinearInterpolator()
        duration = 6000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
    }

    private val currentTranslationX
        get() = paddedBounds.left + (translateXAnimator.animatedValue as Float) * tumbleweedDrawable.bounds.width()

    private val currentTranslationY
        get() = paddedBounds.top + (bounceAnimator.animatedValue as Float) * tumbleweedDrawable.bounds.height()

    private val currentRotation
        get() = (rotateAnimator.animatedValue as Float)

    private val currentGroundScale
        get() = bounceAnimator.animatedFraction * (1 - GROUND_SCALE_MIN) + GROUND_SCALE_MIN

    private val paddingHorizontal
        get() = 0

    private val paddingVertical
        get() = (BOUNCE_HEIGHT_FACTOR * tumbleweedDrawable.bounds.height() / 2).toInt()

    private fun getColorFromTheme(@AttrRes id: Int, @ColorRes default: Int): Int {
        val typedValue = TypedValue()
        val a: TypedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(id))
        val color = a.getColor(0, ContextCompat.getColor(context, default))
        a.recycle()
        return color
    }

    private val primaryColor = getColorFromTheme(R.attr.colorOnSurface, R.color.primaryColor)
    private val secondaryColor =
        getColorFromTheme(R.attr.colorSecondary, R.color.secondaryDarkColor)

    private val paddedBounds
        get() = Rect(
            tumbleweedDrawable.bounds.left - paddingHorizontal,
            tumbleweedDrawable.bounds.top - paddingHorizontal,
            tumbleweedDrawable.bounds.right + paddingHorizontal,
            tumbleweedDrawable.bounds.bottom + paddingVertical
        )

    init {
        animatorSet.playTogether(
            rotateAnimator,
            bounceAnimator,
            translateXAnimator,
            backgroundAnimator
        )
        tumbleweedDrawable.bounds =
            Rect(0, 0, tumbleweedDrawable.intrinsicWidth, tumbleweedDrawable.intrinsicHeight)
        groundDrawable.bounds =
            Rect(0, 0, tumbleweedDrawable.intrinsicWidth, tumbleweedDrawable.intrinsicHeight)
        backgroundDrawable.bounds = getBackgroundBoundsFromTumbleweed()

        groundDrawable.alpha = (GROUND_ALPHA * 255).toInt()
        backgroundDrawable.alpha = (BACKGROUND_ALPHA * 255).toInt()
        bounds = paddedBounds

        tumbleweedDrawable.setTint(primaryColor)
        groundDrawable.setTint(secondaryColor)
        backgroundDrawable.setTint(secondaryColor)
    }

    override fun isRunning() = animatorSet.isRunning

    override fun start() {
        animatorSet.start()
        invalidateSelf()
    }

    override fun stop() {
        animatorSet.pause()
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.save()

        val animatedTranslationX =
            (backgroundAnimator.animatedValue as Float) * backgroundDrawable.bounds.width() / 100F
        val heightCorrection = tumbleweedDrawable.bounds.height() * BACKGROUND_CORRECTION

        canvas.translate(
            paddedBounds.left - animatedTranslationX - backgroundDrawable.bounds.width().toFloat(),
            paddedBounds.top.toFloat() - heightCorrection
        )
        backgroundDrawable.draw(canvas)
        canvas.translate(backgroundDrawable.bounds.width().toFloat(), 0F)
        backgroundDrawable.draw(canvas)
        canvas.translate(backgroundDrawable.bounds.width().toFloat(), 0F)
        backgroundDrawable.draw(canvas)

        canvas.restore()
    }

    private fun drawGround(canvas: Canvas) {
        canvas.save()
        canvas.translate(
            currentTranslationX,
            tumbleweedDrawable.bounds.bottom -
                    groundDrawable.bounds.height().toFloat() / 2 -
                    groundDrawable.bounds.height() * GROUND_CORRECTION
        )
        canvas.scale(
            currentGroundScale,
            GROUND_SCALE_FACTOR * currentGroundScale,
            groundDrawable.bounds.exactCenterX(),
            groundDrawable.bounds.exactCenterY()
        )
        groundDrawable.draw(canvas)
        canvas.restore()
    }

    private fun drawTumbleweed(canvas: Canvas) {
        canvas.translate(currentTranslationX, currentTranslationY)
        canvas.rotate(
            currentRotation,
            tumbleweedDrawable.bounds.exactCenterX(),
            tumbleweedDrawable.bounds.exactCenterY()
        )
        tumbleweedDrawable.draw(canvas)
    }

    override fun draw(canvas: Canvas) {
        canvas.save()

        drawBackground(canvas)
        drawGround(canvas)
        drawTumbleweed(canvas)

        canvas.restore()

        if (isRunning) {
            invalidateSelf()
        }
    }

    override fun getAlpha() = tumbleweedDrawable.alpha

    override fun setAlpha(alpha: Int) {
        tumbleweedDrawable.alpha = alpha
    }

    override fun getOpacity() = tumbleweedDrawable.opacity

    override fun setColorFilter(colorFilter: ColorFilter?) {
        tumbleweedDrawable.colorFilter = colorFilter
    }

    private fun getUnpaddedBounds(rect: Rect) = Rect(
        rect.left + paddingHorizontal,
        rect.top + paddingVertical,
        rect.right - paddingHorizontal,
        rect.bottom - paddingVertical
    )

    private fun getBackgroundBoundsFromTumbleweed(): Rect {
        val xyRatio =
            backgroundDrawable.intrinsicWidth.toFloat() / backgroundDrawable.intrinsicHeight.toFloat()
        val height = tumbleweedDrawable.bounds.height() * BACKGROUND_HEIGHT_RATIO
        val width = height * xyRatio
        return Rect(
            tumbleweedDrawable.bounds.left,
            (tumbleweedDrawable.bounds.bottom - height).toInt(),
            (tumbleweedDrawable.bounds.left + width).toInt(),
            tumbleweedDrawable.bounds.bottom
        )
    }

    override fun onBoundsChange(bounds: Rect?) {
        bounds ?: return
        tumbleweedDrawable.bounds = getUnpaddedBounds(bounds)
        groundDrawable.bounds = getUnpaddedBounds(bounds)
        backgroundDrawable.bounds = getBackgroundBoundsFromTumbleweed()
        invalidateSelf()
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        tumbleweedDrawable.bounds = getUnpaddedBounds(Rect(left, top, right, bottom))
        groundDrawable.bounds = getUnpaddedBounds(Rect(left, top, right, bottom))
        backgroundDrawable.bounds = getBackgroundBoundsFromTumbleweed()
        invalidateSelf()
    }

    override fun setBounds(bounds: Rect) {
        tumbleweedDrawable.bounds = getUnpaddedBounds(bounds)
        groundDrawable.bounds = getUnpaddedBounds(bounds)
        backgroundDrawable.bounds = getBackgroundBoundsFromTumbleweed()
        invalidateSelf()
    }

    override fun getMinimumWidth(): Int {
        return paddedBounds.width()
    }

    override fun getMinimumHeight(): Int {
        return paddedBounds.height()
    }

    override fun getIntrinsicWidth() = tumbleweedDrawable.intrinsicWidth + 2 * paddingHorizontal

    override fun getIntrinsicHeight() = tumbleweedDrawable.intrinsicHeight + 2 * paddingVertical

    override fun getHotspotBounds(outRect: Rect) {
        outRect.set(paddedBounds)
    }

    override fun setHotspotBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setHotspotBounds(left, top, right, bottom)
        tumbleweedDrawable.setHotspotBounds(left, top, right, bottom)
        groundDrawable.setHotspotBounds(left, top, right, bottom)
        backgroundDrawable.setHotspotBounds(left, top, right, bottom)
    }

    override fun getDirtyBounds(): Rect {
        return paddedBounds
    }

    companion object {
        private const val BOUNCE_HEIGHT_FACTOR = 0.6F

        private const val GROUND_SCALE_FACTOR = 0.3F
        private const val GROUND_SCALE_MIN = 0.2F
        private const val GROUND_CORRECTION = 0.1F
        private const val GROUND_ALPHA = 0.7F

        private const val BACKGROUND_ALPHA = 0.5F
        private const val BACKGROUND_HEIGHT_RATIO = 0.3F
        private const val BACKGROUND_CORRECTION = 0.4F

        private const val TRANSLATE_X_RATIO = 0.3F
    }
}