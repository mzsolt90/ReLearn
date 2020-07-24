package com.azyoot.relearn.ui.relearn

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import androidx.core.content.ContextCompat
import com.azyoot.relearn.R
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import javax.inject.Inject

class ReLearnTranslationFormatter @Inject constructor(private val applicationContext: Context){

    private val bulletSpan: BulletSpan
        get() = BulletSpan(
            applicationContext.resources.getDimensionPixelSize(R.dimen.notification_bullet_gap_width),
            ContextCompat.getColor(applicationContext, R.color.notification_bullet_color)
        )

    fun formatTranslationTextForNotification(reLearnTranslation: ReLearnTranslation) =
        SpannableStringBuilder().apply {
            reLearnTranslation.translations.forEach { translation ->
                append(translation, bulletSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                append("\n")
            }
        }.trim()
}