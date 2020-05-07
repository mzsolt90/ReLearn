package com.azyoot.relearn.ui.relearn

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.azyoot.relearn.util.ensureStartsWithHttpsScheme
import timber.log.Timber

class ReLearnLaunchUrlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val url = intent.getStringExtra(EXTRA_URL)
            if (!url.isNullOrEmpty()) {
                launchUrl(url)
            }
        }
    }

    private fun launchUrl(url: String) {
        Timber.d("Launching url for relearn $url")

        val chromeCustomTabBuilder = CustomTabsIntent.Builder()
        val customTabsIntent = chromeCustomTabBuilder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url.ensureStartsWithHttpsScheme()))

        finish()
    }

    companion object {
        const val EXTRA_URL = "url"
    }
}