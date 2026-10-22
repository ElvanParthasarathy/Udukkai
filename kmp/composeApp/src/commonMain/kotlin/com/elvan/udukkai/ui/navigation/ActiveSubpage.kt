package com.elvan.udukkai.ui.navigation

import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.CustomerModel

sealed class ActiveSubpage {
    data object Settings : ActiveSubpage()
    data object RecycleBin : ActiveSubpage()
    data class ItemEditor(val item: ItemModel? = null) : ActiveSubpage()
    data class MerchantEditor(val merchant: CustomerModel? = null) : ActiveSubpage()
    data class InvoiceEditor(val invoice: InvoiceModel? = null) : ActiveSubpage()
    data class ReceiptEditor(val receipt: ReceiptModel? = null) : ActiveSubpage()
    data class CustomerView(val customer: CustomerModel) : ActiveSubpage()
    data class ItemView(val item: ItemModel) : ActiveSubpage()
    data class InvoiceView(val invoice: InvoiceModel) : ActiveSubpage()
    data class ReceiptView(val receipt: ReceiptModel) : ActiveSubpage()
}
