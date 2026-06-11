package com.leonvelez.eventospi.data.model

data class ReactionSummaryResponse(
    val like: Int = 0,
    val love: Int = 0,
    val laugh: Int = 0,
    val wow: Int = 0,
    val sad: Int = 0
)
