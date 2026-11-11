package com.elvan.udukkai.data.model

import com.elvan.udukkai.core.utils.MiniJson
import com.elvan.udukkai.data.settings.LanguageJsonConverter

data class ItemNameRevision(val names: Map<String, String>, val changedAt: Long? = null)

object ItemNames {
    fun snapshot(names: Map<String, String>, legacyName: String, legacyEnglish: String): Map<String, String> =
        names.ifEmpty { LanguageJsonConverter.legacyItemNames(legacyName, legacyEnglish) }

    fun history(previous: ItemModel?, updated: ItemModel, changedAt: Long = updated.updatedAt): List<ItemNameRevision> {
        val known = (previous?.previousNames.orEmpty() + updated.previousNames).distinct()
        val oldName = previous?.name?.filterValues { it.isNotBlank() }.orEmpty()
        if (oldName.isEmpty() || oldName == updated.name.filterValues { it.isNotBlank() }) return known
        val revision = ItemNameRevision(oldName, changedAt)
        return (known + revision).distinct()
    }

    fun encodeHistory(history: List<ItemNameRevision>): String = MiniJson.stringify(history.map {
        it.names + listOfNotNull(it.changedAt?.let { date -> "changedAt" to date })
    })

    fun decodeHistory(json: String?): List<ItemNameRevision> {
        val revisions = runCatching { MiniJson.parse(json.orEmpty()) as? List<*> }.getOrNull().orEmpty()
        return revisions.mapNotNull { revision ->
            val fields = revision as? Map<*, *> ?: return@mapNotNull null
            val names = fields.entries.mapNotNull { (key, value) ->
                if (key is String && value is String) key to value else null
            }.toMap()
            names.takeIf { it.isNotEmpty() }?.let { ItemNameRevision(it, (fields["changedAt"] as? Number)?.toLong()) }
        }
    }
}

/** An explicit item ID always wins. Legacy names can link only to a unique item within the invoice's company. */
class InvoiceItemCatalog(items: List<ItemModel>, private val companyId: Long?) {
    private val byId = items.associateBy { it.id }
    private val byName = items.filter { it.isAvailableForCompany(companyId) }
        .flatMap { item ->
            (listOf(item.name) + item.previousNames.map { it.names }).flatMap { names ->
                names.values.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            }.distinct().map { it to item.id }
        }.groupBy({ it.first }, { it.second })

    fun find(id: String?, snapshot: Map<String, String>): ItemModel? {
        if (!id.isNullOrBlank()) return id.toLongOrNull()?.let(byId::get)
        val matches = snapshot.values.flatMap { byName[it.trim().lowercase()].orEmpty() }.toSet()
        return matches.singleOrNull()?.let(byId::get)
    }

}
