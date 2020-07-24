package com.azyoot.relearn.ui.common

import android.graphics.Rect
import android.view.View
import androidx.constraintlayout.widget.Group
import kotlin.math.max
import kotlin.math.min

fun View.rect() = Rect(left, top, right, bottom)

fun Group.getBoundingRect() =
    referencedIds.fold((parent as View).findViewById<View>(referencedIds[0]).rect(),
        { rect: Rect, id: Int ->
            rect.let {
                val view = (parent as View).findViewById<View>(id)
                Rect(
                    min(view.left, it.left),
                    min(view.top, it.top),
                    max(view.right, it.right),
                    max(view.bottom, it.bottom)
                )
            }
        })

fun Group.setAlphaForViews(alpha: Float){
    referencedIds.forEach { id ->
        (parent as View).findViewById<View>(id).alpha = alpha
    }
}

fun Group.setEnabledForViews(isEnabled: Boolean){
    referencedIds.forEach { id ->
        (parent as View).findViewById<View>(id).isEnabled = isEnabled
    }
}

fun Group.setVisibilityProper(visibility: Int){
    referencedIds.forEach { id ->
        (parent as View).findViewById<View>(id).visibility = visibility
    }
}