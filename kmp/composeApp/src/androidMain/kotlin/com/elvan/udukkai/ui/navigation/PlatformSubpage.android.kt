package com.elvan.udukkai.ui.navigation

import com.elvan.udukkai.SettingsActivity
import com.elvan.udukkai.activities.*
import com.elvan.udukkai.core.platform.CurrentActivity

actual fun openPlatformSubpage(subpage: ActiveSubpage): Boolean {
    val activity = CurrentActivity.activity ?: return false
    when (subpage) {
        is ActiveSubpage.Settings -> SettingsActivity.start(activity)
        is ActiveSubpage.RecycleBin -> RecycleBinActivity.start(activity)
        is ActiveSubpage.Reports -> ReportsActivity.start(activity)
        is ActiveSubpage.InvoiceEditor -> InvoiceEditorActivity.start(activity, subpage.invoice)
        is ActiveSubpage.ReceiptEditor -> ReceiptEditorActivity.start(activity, subpage.receipt, subpage.forInvoice)
        is ActiveSubpage.ItemEditor -> ItemEditorActivity.start(activity, subpage.item)
        is ActiveSubpage.MerchantEditor -> CustomerEditorActivity.start(activity, subpage.merchant)
        is ActiveSubpage.InvoiceView -> InvoiceViewActivity.start(activity, subpage.invoice)
        is ActiveSubpage.ReceiptView -> ReceiptViewActivity.start(activity, subpage.receipt)
        is ActiveSubpage.ItemView -> ItemViewActivity.start(activity, subpage.item)
        is ActiveSubpage.CustomerView -> CustomerViewActivity.start(activity, subpage.customer)
    }
    return true
}
