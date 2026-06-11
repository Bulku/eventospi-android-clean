package com.leonvelez.eventospi.ui.model

enum class EventCategory(val backendValue: Int, val label: String) {
    Deportes(0, "Deportes"),
    Musica(1, "Música"),
    Fiestas(2, "Fiestas"),
    Educacion(3, "Educación"),
    Tecnologia(4, "Tecnología"),
    Gastronomia(5, "Gastronomía"),
    Videojuegos(6, "Videojuegos"),
    Arte(7, "Arte"),
    Bienestar(8, "Bienestar"),
    Viajes(9, "Viajes");

    companion object {
        fun fromBackendValue(value: Int): EventCategory? {
            return entries.firstOrNull { it.backendValue == value }
        }
    }
}

enum class EventReactionType(val emoji: String, val label: String) {
    Like("👍", "Me gusta"),
    Love("❤️", "Me encanta"),
    Haha("😂", "Me divierte"),
    Wow("😮", "Me sorprende"),
    Sad("😢", "Me entristece")
}

enum class MapVisualFilter(val label: String) {
    Todos("Todos"),
    MisInscripciones("Mis inscripciones"),
    PorCategoria("Por categoría")
}

val MAP_CATEGORY_OPTIONS = listOf(
    0 to "Deportes",
    1 to "Música",
    2 to "Fiestas",
    3 to "Educación",
    4 to "Tecnología",
    5 to "Gastronomía",
    6 to "Videojuegos",
    7 to "Arte",
    8 to "Bienestar",
    9 to "Viajes"
)

data class DashboardStats(
    val totalEvents: Int = 0,
    val totalRegistrations: Int = 0,
    val uniqueRegisteredUsersInEvents: Int = 0,
    val eventsByCategory: List<Pair<String, Int>> = emptyList(),
    val topCreatorName: String = "Sin datos",
    val topCreatorCount: Int = 0,
    val topRegisteredUserName: String = "Sin datos",
    val topRegisteredUserCount: Int = 0
)
