package com.leonvelez.eventospi.utils

import com.leonvelez.eventospi.ui.model.EventCategory
import com.leonvelez.eventospi.ui.model.MAP_CATEGORY_OPTIONS

fun mapCategoryLabel(category: Int): String {
    return MAP_CATEGORY_OPTIONS.firstOrNull { it.first == category }?.second ?: "Categoría $category"
}

fun eventCategoryDisplayLabel(category: Int): String {
    return EventCategory.fromBackendValue(category)?.label ?: "Categoría $category"
}
