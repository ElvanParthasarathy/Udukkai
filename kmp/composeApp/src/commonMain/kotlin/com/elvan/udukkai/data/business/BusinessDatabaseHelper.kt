package com.elvan.udukkai.data.business

import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru

interface BusinessDatabaseHelper {
    fun loadAllMerchants(mode: AppMode): List<VaangunarTharavuru>
    fun saveMerchant(mode: AppMode, merchant: VaangunarTharavuru): Long
    fun deleteMerchant(mode: AppMode, id: Long): Boolean
    fun loadDeletedMerchants(mode: AppMode): List<VaangunarTharavuru>
    fun restoreMerchant(mode: AppMode, id: Long): Boolean
    fun permanentDeleteMerchant(mode: AppMode, id: Long): Boolean
    fun purgeExpiredMerchants(mode: AppMode, days: Int = 30): Int

    fun loadAllItems(mode: AppMode): List<PorulTharavuru>
    fun saveItem(mode: AppMode, item: PorulTharavuru): Long
    fun deleteItem(mode: AppMode, id: Long): Boolean
    fun loadDeletedItems(mode: AppMode): List<PorulTharavuru>
    fun restoreItem(mode: AppMode, id: Long): Boolean
    fun permanentDeleteItem(mode: AppMode, id: Long): Boolean
    fun purgeExpiredItems(mode: AppMode, days: Int = 30): Int

    fun loadAllInvoices(mode: AppMode): List<PattiyalTharavuru>
    fun saveInvoice(mode: AppMode, invoice: PattiyalTharavuru): Long
    fun deleteInvoice(mode: AppMode, id: Long): Boolean
    fun loadDeletedInvoices(mode: AppMode): List<PattiyalTharavuru>
    fun restoreInvoice(mode: AppMode, id: Long): Boolean
    fun permanentDeleteInvoice(mode: AppMode, id: Long): Boolean
    fun purgeExpiredInvoices(mode: AppMode, days: Int = 30): Int

    fun loadAllReceipts(mode: AppMode): List<PatrugalTharavuru>
    fun saveReceipt(mode: AppMode, receipt: PatrugalTharavuru): Long
    fun deleteReceipt(mode: AppMode, id: Long): Boolean
    fun loadDeletedReceipts(mode: AppMode): List<PatrugalTharavuru>
    fun restoreReceipt(mode: AppMode, id: Long): Boolean
    fun permanentDeleteReceipt(mode: AppMode, id: Long): Boolean
    fun purgeExpiredReceipts(mode: AppMode, days: Int = 30): Int

    // Receipt ↔ Invoice Junction Links
    fun getLinksForPatru(mode: AppMode, patruId: Long): List<PatruPattiyalInaippuTharavuru>
    fun saveReceiptWithLinks(mode: AppMode, receipt: PatrugalTharavuru, links: List<PatruPattiyalInaippuTharavuru>): Long
    fun getPaidAmountForInvoice(mode: AppMode, invoiceId: Long): Double
    fun getPaidAmountsForInvoices(mode: AppMode, invoiceIds: List<Long>): Map<Long, Double>

    fun clearAllData(mode: AppMode): Boolean
}

expect fun getBusinessDatabaseHelper(): BusinessDatabaseHelper
