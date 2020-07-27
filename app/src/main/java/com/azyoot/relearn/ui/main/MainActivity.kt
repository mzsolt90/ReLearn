package com.azyoot.relearn.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azyoot.relearn.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setIcon(R.drawable.ic_logo)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment.newInstance())
            .commitNow()
    }

}
