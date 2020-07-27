package com.azyoot.relearn.ui.animation

import android.view.animation.Interpolator
import kotlin.math.pow

data class CustomBounceKeyFrame(
    val startTime: Float, /* start of the bounce-back, when the interpolated value should be 1*/
    val bounceHeight: Float
)

class CustomBouncesInterpolator(
    private val bounces: List<CustomBounceKeyFrame> = listOf(
        CustomBounceKeyFrame(0.6F, 0.2F)
    )
) : Interpolator {

    init {
        if (bounces.isEmpty()) throw IllegalStateException("There must be at least one bounce")
    }

    private fun interpolateFirst(time: Float, nextFrameStart: Float) =
        (time / nextFrameStart).pow(2)

    private fun interpolateInBounce(
        time: Float,
        start: Float,
        height: Float,
        nextFrameStart: Float
    ): Float {
        val halfWidth = (nextFrameStart - start) / 2
        return 1 - height +
                ((time - (start + halfWidth)) / halfWidth).pow(2) * height
    }

    override fun getInterpolation(time: Float): Float {

        val nextFrame = bounces.firstOrNull { it.startTime > time }
            ?: // last bounce
            return interpolateInBounce(
                time,
                bounces.last().startTime,
                bounces.last().bounceHeight,
                1F
            )

        val nextFrameIndex = bounces.indexOf(nextFrame)
        val nextFrameStart = nextFrame.startTime
        if (nextFrameIndex == 0) {
            //next frame is first: initial period
            return interpolateFirst(time, nextFrameStart)
        } else {
            //get current frame
            val currentFrame = bounces[nextFrameIndex - 1]
            return interpolateInBounce(
                time,
                currentFrame.startTime,
                currentFrame.bounceHeight,
                nextFrameStart
            )
        }
    }
}