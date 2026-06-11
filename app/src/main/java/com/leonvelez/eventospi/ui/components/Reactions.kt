package com.leonvelez.eventospi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leonvelez.eventospi.data.model.ReactionSummaryResponse
import com.leonvelez.eventospi.ui.model.EventReactionType
import com.leonvelez.eventospi.utils.countFor

@Composable
fun ReactionBubble(
    reaction: EventReactionType,
    isSelected: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = if (isSelected) Color(0xFFDCE6FF) else Color(0xFFF4F6FB),
            shape = RoundedCornerShape(999.dp),
            tonalElevation = if (isSelected) 2.dp else 0.dp,
            modifier = Modifier
                .size(36.dp)
                .clickable { onClick() }
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reaction.emoji,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        )
    }
}

@Composable
fun EventReactionPanel(
    selectedReaction: EventReactionType?,
    reactionSummary: ReactionSummaryResponse?,
    onReactionSelected: (EventReactionType) -> Unit
) {
    val topRow = listOf(
        EventReactionType.Like,
        EventReactionType.Love,
        EventReactionType.Haha
    )

    val bottomRow = listOf(
        EventReactionType.Wow,
        EventReactionType.Sad
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reacciones",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            topRow.forEach { reaction ->
                ReactionBubble(
                    reaction = reaction,
                    isSelected = selectedReaction == reaction,
                    count = reactionSummary?.countFor(reaction) ?: 0,
                    onClick = { onReactionSelected(reaction) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomRow.forEach { reaction ->
                ReactionBubble(
                    reaction = reaction,
                    isSelected = selectedReaction == reaction,
                    count = reactionSummary?.countFor(reaction) ?: 0,
                    onClick = { onReactionSelected(reaction) }
                )
            }
        }
    }
}
