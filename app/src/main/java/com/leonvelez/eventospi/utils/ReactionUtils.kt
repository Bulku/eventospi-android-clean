package com.leonvelez.eventospi.utils

import com.leonvelez.eventospi.data.model.ReactionSummaryResponse
import com.leonvelez.eventospi.ui.model.EventReactionType

fun EventReactionType.toBackendId(): Int {
    return when (this) {
        EventReactionType.Like -> 1
        EventReactionType.Love -> 2
        EventReactionType.Haha -> 3
        EventReactionType.Wow -> 4
        EventReactionType.Sad -> 5
    }
}

fun ReactionSummaryResponse.countFor(reaction: EventReactionType): Int {
    return when (reaction) {
        EventReactionType.Like -> like
        EventReactionType.Love -> love
        EventReactionType.Haha -> laugh
        EventReactionType.Wow -> wow
        EventReactionType.Sad -> sad
    }
}
