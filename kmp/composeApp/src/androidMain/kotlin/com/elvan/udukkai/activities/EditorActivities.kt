package com.elvan.udukkai.activities

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.factory.ui.screens.editor.FactoryCustomerEditorScreen
import com.elvan.udukkai.factory.ui.screens.editor.FactoryInvoiceEditorScreen
import com.elvan.udukkai.factory.ui.screens.editor.FactoryItemEditorScreen
import com.elvan.udukkai.factory.ui.screens.editor.FactoryReceiptEditorScreen
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCustomerEditorScreen
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesInvoiceEditorScreen
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesItemEditorScreen
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesReceiptEditorScreen

class InvoiceEditorActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        val invoice = PlatformNavigationState.consumePendingInvoice()
        if (isFactory) {
            FactoryInvoiceEditorScreen(invoice = invoice, onBack = { finish() })
        } else {
            TextilesInvoiceEditorScreen(invoice = invoice, onBack = { finish() })
        }
    }

    companion object {
        fun start(context: Context, invoice: InvoiceModel? = null) {
            PlatformNavigationState.pendingInvoice = invoice
            context.startActivity(Intent(context, InvoiceEditorActivity::class.java))
        }
    }
}

class ReceiptEditorActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        val receipt = PlatformNavigationState.consumePendingReceipt()
        val forInvoice = PlatformNavigationState.consumePendingInvoice()
        if (isFactory) {
            FactoryReceiptEditorScreen(receipt = receipt, forInvoice = forInvoice, onBack = { finish() })
        } else {
            TextilesReceiptEditorScreen(receipt = receipt, forInvoice = forInvoice, onBack = { finish() })
        }
    }

    companion object {
        /** [forInvoice]: a new receipt for this bill, its customer and amount filled in. */
        fun start(context: Context, receipt: ReceiptModel? = null, forInvoice: InvoiceModel? = null) {
            PlatformNavigationState.pendingReceipt = receipt
            PlatformNavigationState.pendingInvoice = forInvoice
            context.startActivity(Intent(context, ReceiptEditorActivity::class.java))
        }
    }
}

class ItemEditorActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        val item = PlatformNavigationState.consumePendingItem()
        if (isFactory) {
            FactoryItemEditorScreen(item = item, onBack = { finish() })
        } else {
            TextilesItemEditorScreen(item = item, onBack = { finish() })
        }
    }

    companion object {
        fun start(context: Context, item: ItemModel? = null) {
            PlatformNavigationState.pendingItem = item
            context.startActivity(Intent(context, ItemEditorActivity::class.java))
        }
    }
}

class CustomerEditorActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        val customer = PlatformNavigationState.consumePendingCustomer()
        if (isFactory) {
            FactoryCustomerEditorScreen(merchant = customer, onBack = { finish() })
        } else {
            TextilesCustomerEditorScreen(merchant = customer, onBack = { finish() })
        }
    }

    companion object {
        fun start(context: Context, customer: CustomerModel? = null) {
            PlatformNavigationState.pendingCustomer = customer
            context.startActivity(Intent(context, CustomerEditorActivity::class.java))
        }
    }
}
