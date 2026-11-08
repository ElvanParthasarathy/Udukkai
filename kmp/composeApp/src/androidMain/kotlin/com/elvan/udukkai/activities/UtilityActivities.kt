package com.elvan.udukkai.activities

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.elvan.udukkai.factory.ui.screens.FactoryRecycleBinScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesRecycleBinScreen
import com.elvan.udukkai.factory.ui.screens.FactoryReportsScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesReportsScreen

class RecycleBinActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        if (isFactory) {
            FactoryRecycleBinScreen(onBack = { finish() })
        } else {
            TextilesRecycleBinScreen(onBack = { finish() })
        }
    }

    companion object {
        fun start(context: Context, menuReveal: android.os.Bundle? = null) {
            context.startActivity(Intent(context, RecycleBinActivity::class.java).putExtra(EXTRA_MENU_REVEAL, menuReveal != null), menuReveal)
        }
    }
}

class ReportsActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        if (isFactory) {
            FactoryReportsScreen(onBack = { finish() })
        } else {
            TextilesReportsScreen(onBack = { finish() })
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ReportsActivity::class.java))
        }
    }
}
