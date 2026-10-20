package com.elvan.udukkai.ui.navigation

import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru

sealed class ActiveSubpage {
    data object Settings : ActiveSubpage()
    data object RecycleBin : ActiveSubpage()
    data class ItemEditor(val item: PorulTharavuru? = null) : ActiveSubpage()
    data class MerchantEditor(val merchant: VaangunarTharavuru? = null) : ActiveSubpage()
    data class InvoiceEditor(val invoice: PattiyalTharavuru? = null) : ActiveSubpage()
    data class ReceiptEditor(val receipt: PatrugalTharavuru? = null) : ActiveSubpage()
    data class CustomerView(val customer: VaangunarTharavuru) : ActiveSubpage()
    data class ProductView(val product: PorulTharavuru) : ActiveSubpage()
    data class InvoiceView(val invoice: PattiyalTharavuru) : ActiveSubpage()
    data class ReceiptView(val receipt: PatrugalTharavuru) : ActiveSubpage()
}
