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
        fun start(context: Context) {
            context.startActivity(Intent(context, RecycleBinActivity::class.java))
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

class FinancialYearsActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        val year = intent.getIntExtra(EXTRA_YEAR, 0).takeIf { it != 0 }
        com.elvan.udukkai.ui.screens.create.FinancialYearsScreen(
            year = year,
            isFactory = isFactory,
            onBack = { finish() },
            onOpenYear = { start(this, it) },
            onOpenInvoice = { InvoiceViewActivity.start(this, it) },
            onOpenReceipt = { ReceiptViewActivity.start(this, it) }
        )
    }

    companion object {
        private const val EXTRA_YEAR = "financial_year"

        fun start(context: Context, year: Int? = null) {
            context.startActivity(Intent(context, FinancialYearsActivity::class.java).apply {
                year?.let { putExtra(EXTRA_YEAR, it) }
            })
        }
    }
}
