package com.elvan.udukkai.ui.navigation

import com.elvan.udukkai.SettingsActivity
import com.elvan.udukkai.activities.*
import com.elvan.udukkai.core.platform.CurrentActivity
import com.elvan.udukkai.ui.components.shell.ElvanMenuState

/** Picked from a menu: the new window grows out of the menu panel (the system runs it, so it holds across the window change). */
private fun takeMenuReveal(activity: android.app.Activity): android.os.Bundle? {
    val panel = ElvanMenuState.clickedPanel ?: return null
    ElvanMenuState.clickedPanel = null
    return android.app.ActivityOptions.makeClipRevealAnimation(
        activity.window.decorView, panel.left.toInt(), panel.top.toInt(), panel.width.toInt(), panel.height.toInt()
    ).toBundle()
}

actual fun openPlatformSubpage(subpage: ActiveSubpage): Boolean {
    val activity = CurrentActivity.activity ?: return false
    when (subpage) {
        is ActiveSubpage.Settings -> SettingsActivity.start(activity, menuReveal = takeMenuReveal(activity))
        is ActiveSubpage.RecycleBin -> RecycleBinActivity.start(activity, takeMenuReveal(activity))
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
