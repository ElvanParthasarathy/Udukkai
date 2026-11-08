package com.elvan.udukkai.activities

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.elvan.udukkai.core.print.PrintPayload
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.factory.data.FactoryInvoiceRepository
import com.elvan.udukkai.factory.data.FactoryItemRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.factory.data.FactoryReceiptRepository
import com.elvan.udukkai.factory.ui.screens.factoryCustomerActivity
import com.elvan.udukkai.factory.ui.screens.factoryItemActivity
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.textiles.data.TextilesItemRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.textiles.data.TextilesReceiptRepository
import com.elvan.udukkai.textiles.ui.screens.textilesCustomerActivity
import com.elvan.udukkai.textiles.ui.screens.textilesItemActivity
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.screens.view.CustomerViewScreen
import com.elvan.udukkai.ui.screens.view.InvoiceViewScreen
import com.elvan.udukkai.ui.screens.view.ItemViewScreen
import com.elvan.udukkai.ui.screens.view.ReceiptViewScreen

class InvoiceViewActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        val invoiceId = intent.getLongExtra(EXTRA_ID, -1L)
        val initialInvoice = PlatformNavigationState.consumePendingInvoice()

        if (isFactory) {
            val currentInvoice = FactoryInvoiceRepository.invoices.find { it.id == invoiceId }
                ?: initialInvoice ?: return
            val invoiceLockedMsg = K.invoiceLockedDueToReceipt.tr()
            InvoiceViewScreen(
                invoice = currentInvoice,
                profile = FactoryProfileRepository.profile,
                allProfiles = FactoryProfileRepository.profiles,
                isPaid = FactoryInvoiceRepository.hasLinkedReceipts(currentInvoice.id),
                onBack = { finish() },
                receipts = FactoryReceiptRepository.receiptsForInvoice(currentInvoice.id),
                onOpenReceipt = { ReceiptViewActivity.start(this, it) },
                onEdit = {
                    if (FactoryInvoiceRepository.hasLinkedReceipts(currentInvoice.id)) {
                        ElvanSnackbar.show(invoiceLockedMsg)
                    } else {
                        InvoiceEditorActivity.start(this, currentInvoice)
                    }
                },
                onCopy = {
                    val copied = currentInvoice.copy(
                        id = 0L,
                        invoiceNumber = "",
                        invoiceDate = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    InvoiceEditorActivity.start(this, copied)
                },
                onConvertToTaxInvoice = {
                    val converted = currentInvoice.copy(
                        id = 0L,
                        invoiceNumber = "",
                        invoiceType = "tax-invoice",
                        invoiceDate = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    InvoiceEditorActivity.start(this, converted)
                },
                printPayload = { activeProfile ->
                    PrintPayload.forFactoryInvoice(
                        invoice = currentInvoice,
                        profile = activeProfile,
                        customer = currentInvoice.customerId?.let { FactoryCustomerRepository.getById(it) },
                        appLanguage = if (LanguageManager.activeLanguageCode.startsWith("ta")) "ta" else "en"
                    )
                }
            )
        } else {
            val currentInvoice = TextilesInvoiceRepository.invoices.find { it.id == invoiceId }
                ?: initialInvoice ?: return
            val invoiceLockedMsg = K.invoiceLockedDueToReceipt.tr()
            InvoiceViewScreen(
                invoice = currentInvoice,
                profile = TextilesProfileRepository.profile,
                allProfiles = TextilesProfileRepository.profiles,
                isPaid = TextilesInvoiceRepository.hasLinkedReceipts(currentInvoice.id),
                onBack = { finish() },
                receipts = TextilesReceiptRepository.receiptsForInvoice(currentInvoice.id),
                onOpenReceipt = { ReceiptViewActivity.start(this, it) },
                onEdit = {
                    if (TextilesInvoiceRepository.hasLinkedReceipts(currentInvoice.id)) {
                        ElvanSnackbar.show(invoiceLockedMsg)
                    } else {
                        InvoiceEditorActivity.start(this, currentInvoice)
                    }
                },
                onCopy = {
                    val copied = currentInvoice.copy(
                        id = 0L,
                        invoiceNumber = "",
                        invoiceDate = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    InvoiceEditorActivity.start(this, copied)
                },
                onConvertToTaxInvoice = {
                    val converted = currentInvoice.copy(
                        id = 0L,
                        invoiceNumber = "",
                        invoiceType = "tax-invoice",
                        invoiceDate = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    InvoiceEditorActivity.start(this, converted)
                },
                printPayload = { activeProfile ->
                    PrintPayload.forTextilesInvoice(
                        invoice = currentInvoice,
                        profile = activeProfile,
                        customer = currentInvoice.customerId?.let { TextilesCustomerRepository.getById(it) },
                        appLanguage = if (LanguageManager.activeLanguageCode.startsWith("ta")) "ta" else "en"
                    )
                }
            )
        }
    }

    companion object {
        const val EXTRA_ID = "extra_invoice_id"
        fun start(context: Context, invoice: InvoiceModel) {
            PlatformNavigationState.pendingInvoice = invoice
            val intent = Intent(context, InvoiceViewActivity::class.java).apply {
                putExtra(EXTRA_ID, invoice.id)
            }
            context.startActivity(intent)
        }
    }
}

class ReceiptViewActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        val receiptId = intent.getLongExtra(EXTRA_ID, -1L)
        val initialReceipt = PlatformNavigationState.consumePendingReceipt()

        if (isFactory) {
            val currentReceipt = FactoryReceiptRepository.receipts.find { it.id == receiptId }
                ?: initialReceipt ?: return
            ReceiptViewScreen(
                receipt = currentReceipt,
                profile = FactoryProfileRepository.profile,
                allProfiles = FactoryProfileRepository.profiles,
                onBack = { finish() },
                onEdit = { ReceiptEditorActivity.start(this, currentReceipt) },
                printPayload = { activeProfile ->
                    PrintPayload.forReceipt(
                        receipt = currentReceipt,
                        profile = activeProfile,
                        invoiceNumbers = FactoryReceiptRepository.getLinksForReceipt(currentReceipt.id).mapNotNull { link ->
                            FactoryInvoiceRepository.invoices.find { it.id == link.invoiceId }?.invoiceNumber
                        },
                        isCoolie = true,
                        appLanguage = if (LanguageManager.activeLanguageCode.startsWith("ta")) "ta" else "en"
                    )
                }
            )
        } else {
            val currentReceipt = TextilesReceiptRepository.receipts.find { it.id == receiptId }
                ?: initialReceipt ?: return
            ReceiptViewScreen(
                receipt = currentReceipt,
                profile = TextilesProfileRepository.profile,
                allProfiles = TextilesProfileRepository.profiles,
                onBack = { finish() },
                onEdit = { ReceiptEditorActivity.start(this, currentReceipt) },
                printPayload = { activeProfile ->
                    PrintPayload.forReceipt(
                        receipt = currentReceipt,
                        profile = activeProfile,
                        invoiceNumbers = TextilesReceiptRepository.getLinksForReceipt(currentReceipt.id).mapNotNull { link ->
                            TextilesInvoiceRepository.invoices.find { it.id == link.invoiceId }?.invoiceNumber
                        },
                        isCoolie = false,
                        appLanguage = if (LanguageManager.activeLanguageCode.startsWith("ta")) "ta" else "en"
                    )
                }
            )
        }
    }

    companion object {
        const val EXTRA_ID = "extra_receipt_id"
        fun start(context: Context, receipt: ReceiptModel) {
            PlatformNavigationState.pendingReceipt = receipt
            val intent = Intent(context, ReceiptViewActivity::class.java).apply {
                putExtra(EXTRA_ID, receipt.id)
            }
            context.startActivity(intent)
        }
    }
}

class ItemViewActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        val itemId = intent.getLongExtra(EXTRA_ID, -1L)
        val initialItem = PlatformNavigationState.consumePendingItem()
        val scrollState = rememberLazyListState()

        if (isFactory) {
            val currentItem = FactoryItemRepository.items.find { it.id == itemId }
                ?: initialItem ?: return
            ItemViewScreen(
                item = currentItem,
                isCoolie = true,
                activity = factoryItemActivity(currentItem),
                onBillClick = { id ->
                    FactoryInvoiceRepository.invoices.find { it.id == id }?.let {
                        InvoiceViewActivity.start(this, it)
                    }
                },
                profile = FactoryProfileRepository.profile,
                allProfiles = FactoryProfileRepository.profiles,
                scrollState = scrollState,
                onBack = { finish() },
                onEdit = { ItemEditorActivity.start(this, currentItem) },
                onCopy = {
                    val copied = currentItem.copy(
                        id = 0L,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    ItemEditorActivity.start(this, copied)
                }
            )
        } else {
            val currentItem = TextilesItemRepository.items.find { it.id == itemId }
                ?: initialItem ?: return
            ItemViewScreen(
                item = currentItem,
                activity = textilesItemActivity(currentItem),
                onBillClick = { id ->
                    TextilesInvoiceRepository.invoices.find { it.id == id }?.let {
                        InvoiceViewActivity.start(this, it)
                    }
                },
                profile = TextilesProfileRepository.profile,
                allProfiles = TextilesProfileRepository.profiles,
                scrollState = scrollState,
                onBack = { finish() },
                onEdit = { ItemEditorActivity.start(this, currentItem) },
                onCopy = {
                    val copied = currentItem.copy(
                        id = 0L,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    ItemEditorActivity.start(this, copied)
                }
            )
        }
    }

    companion object {
        const val EXTRA_ID = "extra_item_id"
        fun start(context: Context, item: ItemModel) {
            PlatformNavigationState.pendingItem = item
            val intent = Intent(context, ItemViewActivity::class.java).apply {
                putExtra(EXTRA_ID, item.id)
            }
            context.startActivity(intent)
        }
    }
}

class CustomerViewActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        val customerId = intent.getLongExtra(EXTRA_ID, -1L)
        val initialCustomer = PlatformNavigationState.consumePendingCustomer()
        val scrollState = rememberLazyListState()

        if (isFactory) {
            val currentCustomer = FactoryCustomerRepository.customers.find { it.id == customerId }
                ?: initialCustomer ?: return
            CustomerViewScreen(
                merchant = currentCustomer,
                activity = factoryCustomerActivity(currentCustomer),
                onBillClick = { id ->
                    FactoryInvoiceRepository.invoices.find { it.id == id }?.let {
                        InvoiceViewActivity.start(this, it)
                    }
                },
                profile = FactoryProfileRepository.profile,
                allProfiles = FactoryProfileRepository.profiles,
                scrollState = scrollState,
                onBack = { finish() },
                onEdit = { CustomerEditorActivity.start(this, currentCustomer) },
                onCopy = {
                    val copied = currentCustomer.copy(
                        id = 0L,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    CustomerEditorActivity.start(this, copied)
                }
            )
        } else {
            val currentCustomer = TextilesCustomerRepository.customers.find { it.id == customerId }
                ?: initialCustomer ?: return
            CustomerViewScreen(
                merchant = currentCustomer,
                activity = textilesCustomerActivity(currentCustomer),
                onBillClick = { id ->
                    TextilesInvoiceRepository.invoices.find { it.id == id }?.let {
                        InvoiceViewActivity.start(this, it)
                    }
                },
                profile = TextilesProfileRepository.profile,
                allProfiles = TextilesProfileRepository.profiles,
                scrollState = scrollState,
                onBack = { finish() },
                onEdit = { CustomerEditorActivity.start(this, currentCustomer) },
                onCopy = {
                    val copied = currentCustomer.copy(
                        id = 0L,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    CustomerEditorActivity.start(this, copied)
                }
            )
        }
    }

    companion object {
        const val EXTRA_ID = "extra_customer_id"
        fun start(context: Context, customer: CustomerModel) {
            PlatformNavigationState.pendingCustomer = customer
            val intent = Intent(context, CustomerViewActivity::class.java).apply {
                putExtra(EXTRA_ID, customer.id)
            }
            context.startActivity(intent)
        }
    }
}
