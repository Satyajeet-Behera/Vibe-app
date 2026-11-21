package com.vibe.social.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeTextPrimary
import com.vibe.social.theme.VibeZinc500

@Composable
fun ExpandableCaption(
    username: String,
    caption: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 2,
    onUsernameClick: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    val annotatedCaption = remember(username, caption, isExpanded) {
        buildAnnotatedString {
            // Bold Username
            pushStringAnnotation(tag = "USERNAME", annotation = username)
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            ) {
                append(username)
            }
            pop()
            append(" ")

            // Parse caption for hashtags and mentions
            val words = caption.split(" ")
            words.forEachIndexed { index, word ->
                if (word.startsWith("#") || word.startsWith("@")) {
                    pushStringAnnotation(
                        tag = if (word.startsWith("#")) "HASHTAG" else "MENTION",
                        annotation = word
                    )
                    withStyle(
                        style = SpanStyle(
                            color = VibeAccentBlue,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    ) {
                        append(word)
                    }
                    pop()
                } else {
                    withStyle(
                        style = SpanStyle(
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    ) {
                        append(word)
                    }
                }
                if (index < words.size - 1) append(" ")
            }
        }
    }

    Box(modifier = modifier) {
        if (!isExpanded) {
            Text(
                text = annotatedCaption,
                maxLines = collapsedMaxLines,
                lineHeight = 17.sp,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) {
                        hasOverflow = true
                    }
                },
                modifier = Modifier.clickable(
                    enabled = hasOverflow,
                    onClick = { isExpanded = true }
                )
            )
            if (hasOverflow) {
                // Secondary visual hint in zinc-500
                Text(
                    text = "  ... more",
                    color = VibeZinc500,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.clickable { isExpanded = true }
                )
            }
        } else {
            Text(
                text = annotatedCaption,
                lineHeight = 17.sp,
                modifier = Modifier.clickable { isExpanded = false }
            )
        }
    }
}
